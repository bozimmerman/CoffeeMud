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
   Copyright 2003-2018 Bo Zimmerman

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
public class Trap_RoomPit extends StdTrap
{
	@Override
	public String ID()
	{
		return "Trap_RoomPit";
	}

	private final static String	localizedName	= CMLib.lang().L("pit trap");

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

	@Override
	protected int trapLevel()
	{
		return 1;
	}

	@Override
	public String requiresToSet()
	{
		return "";
	}

	protected List<Room> pit=null;

	@Override
	public void unInvoke()
	{
		if((pit!=null)
		&&(canBeUninvoked())
		&&(pit.size()>1))
		{
			final Room R1=pit.get(0);
			final Room R2=pit.get(pit.size()-1);
			while(R1.numInhabitants()>0)
			{
				final MOB M=R1.fetchInhabitant(0);
				if(M!=null)
				{
					M.killMeDead(false);
					R1.delInhabitant(M);
				}
			}
			while(R2.numInhabitants()>0)
			{
				final MOB M=R2.fetchInhabitant(0);
				if(M!=null)
				{
					M.killMeDead(false);
					R2.delInhabitant(M);
				}
			}
			final Room R=R2.getRoomInDir(Directions.UP);
			if((R!=null)&&(R.getRoomInDir(Directions.DOWN)==R2))
			{
				R.rawDoors()[Directions.DOWN]=null;
				R.setRawExit(Directions.DOWN,null);
			}
			R2.rawDoors()[Directions.UP]=null;
			R2.setRawExit(Directions.UP,null);
			R2.rawDoors()[Directions.DOWN]=null;
			R2.setRawExit(Directions.DOWN,null);
			R1.rawDoors()[Directions.UP]=null;
			R1.setRawExit(Directions.UP,null);
			pit=null;
			R1.destroy();
			R2.destroy();
			super.unInvoke();
		}
		else
		{
			pit=null;
			super.unInvoke();
		}
	}

	@Override
	public boolean canSetTrapOn(MOB mob, Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		if(P instanceof Room)
		{
			if(((Room)P).getRoomInDir(Directions.DOWN)!=null)
			{
				mob.tell(L("The flooring here won't support a pit."));
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return false;

		if((tickID==Tickable.TICKID_TRAP_DESTRUCTION)
		&&(canBeUninvoked())
		&&(pit!=null)
		&&(pit.size()>1)
		&&(((pit.get(0).numPCInhabitants()>0)
			||(pit.get(pit.size()-1).numPCInhabitants()>0))))
			return true;
		return super.tick(ticking,tickID);
	}

	protected synchronized void makePit(MOB target)
	{
		if((pit==null)||(pit.size()<2))
		{
			final List<Room> V=new Vector<Room>();
			final Room myPitUp=CMClass.getLocale("ClimbableSurface");
			myPitUp.setRoomID("");
			myPitUp.setSavable(false);
			myPitUp.setArea(target.location().getArea());
			myPitUp.basePhyStats().setDisposition(myPitUp.basePhyStats().disposition()|PhyStats.IS_DARK);
			myPitUp.setDisplayText(L("Inside a dark pit"));
			myPitUp.setDescription(L("The walls here are slick and tall.  The trap door is just above you."));
			myPitUp.recoverPhyStats();

			final Exit exit=CMClass.getExit("StdOpenDoorway");
			final Room myPit=CMClass.getLocale("StdRoom");
			myPit.setSavable(false);
			myPit.setRoomID("");
			myPit.setArea(target.location().getArea());
			myPit.basePhyStats().setDisposition(myPit.basePhyStats().disposition()|PhyStats.IS_DARK);
			myPit.setDisplayText(L("Inside a dark pit"));
			myPit.setDescription(L("The walls here are slick and tall.  You can barely see the trap door well above you."));
			myPit.setRawExit(Directions.UP,exit);
			myPit.rawDoors()[Directions.UP]=myPitUp;
			myPitUp.setRawExit(Directions.DOWN,exit);
			myPitUp.rawDoors()[Directions.DOWN]=myPit;
			myPitUp.recoverPhyStats();
			V.add(myPit);
			V.add(myPitUp);
			pit=V;
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		boolean unSpring=false;
		if((!sprung)
		&&(affected instanceof Room)
		&&(msg.amITarget(affected))
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(!msg.source().isMine(affected)))
		&&(msg.tool() instanceof Exit))
		{
			final Room room=(Room)affected;
			if((room.getExitInDir(Directions.DOWN)==msg.tool())
			||(room.getReverseExit(Directions.DOWN)==msg.tool()))
			{
				unSpring=true;
				sprung=true;
			}
		}
		super.executeMsg(myHost,msg);
		if(unSpring)
			sprung=false;
	}

	public void finishSpringing(MOB target)
	{
		if((!invoker().mayIFight(target))||(target.phyStats().weight()<5))
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> float(s) gently into the pit!"));
		else
		{
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> hit(s) the pit floor with a THUMP!"));
			final int damage=CMLib.dice().roll(trapLevel()+abilityCode(),6,1);
			CMLib.combat().postDamage(invoker(),target,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,-1,null);
		}
		CMLib.commands().postLook(target,true);
	}

	@Override
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null)&&(!CMLib.flags().isInFlight(target)))
		{
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target))
			||(target==invoker())
			||(doesSaveVsTraps(target)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,L("<S-NAME> avoid(s) falling into a pit!"));
			else
			if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,L("<S-NAME> fall(s) into a pit!")))
			{
				super.spring(target);
				makePit(target);
				final Exit door=CMClass.getExit("StdClosedDoorway");
				door.setSavable(false);
				door.setOpenDelayTicks(10);
				pit.get(pit.size()-1).setRawExit(Directions.UP,door);
				pit.get(pit.size()-1).rawDoors()[Directions.UP]=target.location();
				if((target.location().getRoomInDir(Directions.DOWN)==null)
				&&(target.location().getExitInDir(Directions.DOWN)==null))
				{
					target.location().setRawExit(Directions.DOWN,door);
					target.location().rawDoors()[Directions.DOWN]=(pit.get(pit.size()-1));
				}
				if((!door.isOpen())&&(affected instanceof Room))
					door.executeMsg(target, CMClass.getMsg(target,door,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_OPEN,null));
				pit.get(0).bringMobHere(target,false);
				finishSpringing(target);
			}
		}
	}
}
