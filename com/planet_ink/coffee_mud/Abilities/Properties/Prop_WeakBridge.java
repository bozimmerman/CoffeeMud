package com.planet_ink.coffee_mud.Abilities.Properties;
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

public class Prop_WeakBridge extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_WeakBridge";
	}

	@Override
	public String name()
	{
		return "Weak Rickity Bridge";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_EXITS;
	}

	protected boolean bridgeIsUp=true;
	protected int max=400;
	protected int chance=75;
	protected int ticksDown=100;
	protected List<MOB> mobsToKill=new Vector<MOB>();

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ENTER;
	}

	@Override
	public String accountForYourself()
	{
		return "Weak and Rickity";
	}

	@Override
	public void setMiscText(String newText)
	{
		mobsToKill=new Vector<MOB>();
		super.setMiscText(newText);
		max=CMParms.getParmInt(newText,"max",400);
		chance=CMParms.getParmInt(newText,"chance",75);
		ticksDown=CMParms.getParmInt(newText,"down",300);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&((msg.amITarget(affected))||(msg.tool()==affected)))
		{
			final MOB mob=msg.source();
			if(CMLib.flags().isInFlight(mob))
				return true;
			if(!bridgeIsUp)
			{
				mob.tell(L("The bridge appears to be out."));
				return false;
			}
		}
		return true;
	}

	public int weight(MOB mob)
	{
		int weight=0;
		if(affected instanceof Room)
		{
			final Room room=(Room)affected;
			for(int i=0;i<room.numInhabitants();i++)
			{
				final MOB M=room.fetchInhabitant(i);
				if((M!=null)&&(M!=mob)&&(!CMLib.flags().isInFlight(M)))
					weight+=M.phyStats().weight();
			}
		}
		return weight+mob.phyStats().weight();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&((msg.amITarget(affected))||(msg.tool()==affected))
		&&(!CMLib.flags().isFalling(msg.source())))
		{
			final MOB mob=msg.source();
			if(CMLib.flags().isInFlight(mob))
				return;
			if(bridgeIsUp)
			{
				if((weight(mob)>max)
				&&(CMLib.dice().rollPercentage()<chance))
				{
					synchronized(mobsToKill)
					{
						if(!mobsToKill.contains(mob))
						{
							mobsToKill.add(mob);
							if(!CMLib.flags().isFalling(mob))
							{
								final Ability falling=CMClass.getAbility("Falling");
								falling.setMiscText("NORMAL");
								falling.setAffectedOne(affected);
								falling.invoke(null,null,mob,true,0);
							}
							CMLib.threads().startTickDown(this,Tickable.TICKID_SPELL_AFFECT,1);
						}
					}
				}
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		// get rid of flying restrictions when bridge is up
		if((affected!=null)
		&&(affected instanceof Room)
		&&(bridgeIsUp))
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SLEEPING);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_SPELL_AFFECT)
		{
			if(bridgeIsUp)
			{
				synchronized(mobsToKill)
				{
					bridgeIsUp=false;
					final List<MOB> V=new XVector<MOB>(mobsToKill);
					mobsToKill.clear();
					if(affected instanceof Room)
					{
						final Room room=(Room)affected;
						for(int i=0;i<room.numInhabitants();i++)
						{
							final MOB M=room.fetchInhabitant(i);
							if((M!=null)
							&&(!CMLib.flags().isInFlight(M))
							&&(!V.contains(M)))
								V.add(M);
						}
					}
					for(int i=0;i<V.size();i++)
					{
						final MOB mob=V.get(i);
						if((mob.location()!=null)
						&&(!CMLib.flags().isInFlight(mob)))
						{
							if((affected instanceof Room)
							&&((((Room)affected).domainType()==Room.DOMAIN_INDOORS_AIR)
							   ||(((Room)affected).domainType()==Room.DOMAIN_OUTDOORS_AIR))
							&&(((Room)affected).getRoomInDir(Directions.DOWN)!=null)
							&&(((Room)affected).getExitInDir(Directions.DOWN)!=null)
							&&(((Room)affected).getExitInDir(Directions.DOWN).isOpen()))
							{
								mob.tell(L("The bridge breaks under your weight!"));
								if((!CMLib.flags().isFalling(mob))
								&&(mob.location()==affected))
								{
									final Ability falling=CMClass.getAbility("Falling");
									falling.setProficiency(0);
									falling.setAffectedOne(affected);
									falling.invoke(null,null,mob,true,0);
								}
							}
							else
							{
								mob.location().showSource(mob,null,CMMsg.MSG_OK_VISUAL,L("The bridge breaks under your weight!"));
								mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> fall(s) to <S-HIS-HER> death!!"));
								mob.location().show(mob,null,CMMsg.MSG_DEATH,null);
							}
						}
					}
					if(affected instanceof Room)
						((Room)affected).recoverPhyStats();
					CMLib.threads().deleteTick(this,Tickable.TICKID_SPELL_AFFECT);
					CMLib.threads().startTickDown(this,Tickable.TICKID_SPELL_AFFECT,ticksDown);
				}
			}
			else
			{
				bridgeIsUp=true;
				CMLib.threads().deleteTick(this,Tickable.TICKID_SPELL_AFFECT);
				if(affected instanceof Room)
					((Room)affected).recoverPhyStats();
			}
		}
		return true;
	}
}
