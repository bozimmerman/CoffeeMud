package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2025 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Trap_FloodRoom extends StdTrap
{
	@Override
	public String ID()
	{
		return "Trap_FloodRoom";
	}

	private final static String	localizedName	= CMLib.lang().L("flood room");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	public Trap_FloodRoom()
	{
		super();
		trapLevel = 29;
	}

	@Override
	public String requiresToSet()
	{
		return "100 pounds of stone, 10 water containers";
	}

	@Override
	public int baseRejuvTime(final int level)
	{
		return 16;
	}

	protected int numWaterskins(final MOB mob)
	{
		if(mob==null)
			return 0;
		if(mob.location()==null)
			return 0;
		int num=0;
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I=mob.location().getItem(i);
			if((I instanceof Drink)&&(((Drink)I).containsLiquid()))
				num++;
		}
		return num;
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final List<Item> V=new Vector<Item>();
		for(int i=0;i<100;i++)
			V.add(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_STONE));
		for(int i=0;i<10;i++)
			V.add(CMClass.getBasicItem("Waterskin"));
		return V;
	}

	protected void killWaterskins(final MOB mob)
	{
		if(mob==null)
			return;
		if(mob.location()==null)
			return;
		int num=10;
		int i=0;
		while((num>0)&&(i<mob.location().numItems()))
		{
			final Item I=mob.location().getItem(i);
			if((I instanceof Drink)&&(((Drink)I).containsLiquid()))
			{
				if(I instanceof RawMaterial)
				{
					i--;
					I.destroy();
				}
				else
					((Drink)I).setLiquidRemaining(0);
				if((--num)<=0)
					break;
			}
			i++;
		}
	}

	@Override
	public Trap setTrap(final MOB mob, final Physical P, final int trapBonus, final int qualifyingClassLevel, final boolean perm)
	{
		if(P==null)
			return null;
		RawMaterial I=null;
		if(mob!=null)
		{
			I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_ROCK);
			if(I!=null)
				super.destroyResources(mob.location(),I.material(),I.getSubType(),100);
			killWaterskins(mob);
		}
		return super.setTrap(mob,P,trapBonus,qualifyingClassLevel,perm);
	}

	@Override
	public boolean canSetTrapOn(final MOB mob, final Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		if(mob!=null)
		{
			final RawMaterial I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_ROCK);
			if((I==null)
			||(super.findNumberOfResource(mob.location(),I)<100))
			{
				mob.tell(L("You'll need to set down at least 100 pounds of stone first."));
				return false;
			}
			if(numWaterskins(mob)<=10)
			{
				mob.tell(L("You'll need to set down at least 10 water containers first."));
				return false;
			}
		}
		if(P instanceof Room)
		{
			final Room R=(Room)P;
			if((R.domainType()&Room.INDOORS)==0)
			{
				if(mob!=null)
					mob.tell(L("You can only set this trap indoors."));
				return false;
			}
		}
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(sprung && (affected instanceof MOB))
		{
			if((!disabled)&&((tickDown>2)&&(tickDown<13)))
			{
				if((((MOB)affected).charStats().getBreathables().length>0)
				&&(Arrays.binarySearch(((MOB)affected).charStats().getBreathables(),RawMaterial.RESOURCE_FRESHWATER)<0))
					affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_BREATHE);
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SWIMMING);
			}
		}
		else
			disabled=false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((sprung)
		&&(affected!=null)
		&&(!disabled())
		&&(tickDown>=0))
		{
			if(((msg.targetMinor()==CMMsg.TYP_LEAVE)
				||(msg.targetMinor()==CMMsg.TYP_FLEE))
			&&(msg.amITarget(affected)))
			{
				msg.source().tell(L("The exits are blocked! You can't get out!"));
				return false;
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_ENTER)
			&&(msg.amITarget(affected)))
			{
				msg.source().tell(L("The entry to that room is blocked!"));
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((tickID==Tickable.TICKID_TRAP_RESET)&&(getReset()>0))
		{
			if((sprung)
			&&(affected!=null)
			&&(affected instanceof Room)
			&&(!disabled())
			&&(tickDown>=0))
			{
				final Room R=(Room)affected;
				if(tickDown>13)
				{
					R.showHappens(CMMsg.MSG_OK_VISUAL,L("Water is filling up the room!"));
					CMLib.utensils().extinguish(invoker(),R,true);
					R.recoverPhyStats();
					R.recoverRoomStats();
				}
				else
				if(tickDown>2)
				{
					CMLib.utensils().extinguish(invoker(),R,true);
					R.recoverPhyStats();
					R.recoverRoomStats();
				}
				else
				{
					R.recoverPhyStats();
					R.recoverRoomStats();
					R.showHappens(CMMsg.MSG_OK_VISUAL,L("The water is draining away..."));
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void disable()
	{
		super.disable();
		if((affected instanceof Room))
		{
			((Room)affected).recoverPhyStats();
			((Room)affected).recoverRoomStats();
		}
	}

	@Override
	public void spring(final MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((doesSaveVsTraps(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target)))
			{
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						getAvoidMsg(L("<S-NAME> avoid(s) setting off a trap!")));
			}
			else
			{
				if(target.location().show(target,target,this,
						CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,getTrigMsg(L("<S-NAME> trigger(s) a trap!"))))
				{
					super.spring(target);
					target.location().showHappens(CMMsg.MSG_OK_VISUAL,
							getDamMsg(L("The exits are blocked off! Water starts pouring in!")));
				}
			}
		}
	}
}
