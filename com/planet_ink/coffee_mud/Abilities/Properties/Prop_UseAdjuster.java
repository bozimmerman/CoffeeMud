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
   Copyright 2003-2020 Bo Zimmerman

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
public class Prop_UseAdjuster extends Prop_Adjuster implements ArchonOnly
{
	@Override
	public String ID()
	{
		return "Prop_UseAdjuster";
	}

	@Override
	public String name()
	{
		return "Adjusting stats when used";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public boolean bubbleAffect()
	{
		return false;
	}

	@Override
	public void affectPhyStats(final Physical host, final PhyStats affectableStats)
	{
		// super important that nothing is done here, to kill prop_haveadjuster
	}
	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectedStats)
	{
		// super important that nothing is done here, to kill prop_haveadjuster
	}

	@Override
	public void affectCharState(final MOB affectedMOB, final CharState affectedState)
	{
		// super important that nothing is done here, to kill prop_haveadjuster
	}


	@Override
	public int triggerMask()
	{
		if(affected instanceof Weapon)
			return TriggeredAffect.TRIGGER_HITTING_WITH;
		else
		if((affected instanceof Food)||(affected instanceof Drink))
			return TriggeredAffect.TRIGGER_USE;
		else
		if(affected instanceof Armor)
			return TriggeredAffect.TRIGGER_BEING_HIT;
		else
		if(affected instanceof Container)
			return TriggeredAffect.TRIGGER_DROP_PUTIN;
		else
			return TriggeredAffect.TRIGGER_USE;
	}

	protected volatile boolean processing = false;
	protected boolean reversed = false;

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		if((parameters != null)
		&&(parameters.length>0)
		&&(parameters[0].length()>0))
		{
			final List<String> chk=CMParms.parse(parameters[0].toUpperCase().trim());
			reversed = chk.contains("REVERSED") || chk.contains("REVERSE");
		}
		parameters=CMLib.masking().separateMaskStrs(text());
	}

	public void adjCharState(final MOB mob, final Object[] changes, final CharState charState)
	{
		if(changes==null)
			return;
		if(multiplyCharStates)
		{
			for(int c=0;c<changes.length;c+=2)
			{
				switch(((Integer)changes[c]).intValue())
				{
				case CharState.STAT_HITPOINTS:
					charState.adjHitPoints((int)Math.round(CMath.mul(charState.getHitPoints(), CMath.div(((Integer) changes[c + 1]).intValue(),100))-charState.getHitPoints()),mob.maxState());
					break;
				case CharState.STAT_HUNGER:
					charState.adjHunger(((Integer) changes[c + 1]).intValue(),mob.maxState().maxHunger(mob.baseWeight()));
					break;
				case CharState.STAT_THIRST:
					charState.adjThirst(((Integer) changes[c + 1]).intValue(),mob.maxState().maxThirst(mob.baseWeight()));
					break;
				case CharState.STAT_MANA:
					charState.adjMana((int)Math.round(CMath.mul(charState.getMana(), CMath.div(((Integer) changes[c + 1]).intValue(),100))-charState.getMana()),mob.maxState());
					break;
				case CharState.STAT_MOVE:
					charState.adjMovement((int)Math.round(CMath.mul(charState.getMovement(), CMath.div(((Integer) changes[c + 1]).intValue(),100)))-charState.getMovement(),mob.maxState());
					break;
				}
			}
		}
		else
		{
			for(int c=0;c<changes.length;c+=2)
			{
				switch(((Integer)changes[c]).intValue())
				{
				case CharState.STAT_HITPOINTS:
					charState.adjHitPoints( ((Integer) changes[c + 1]).intValue(), mob.maxState());
					break;
				case CharState.STAT_HUNGER:
					charState.adjHunger( ((Integer) changes[c + 1]).intValue(), mob.maxState().maxHunger(mob.baseWeight()));
					break;
				case CharState.STAT_THIRST:
					charState.adjThirst( ((Integer) changes[c + 1]).intValue(), mob.maxState().maxThirst(mob.baseWeight()));
					break;
				case CharState.STAT_MANA:
					charState.adjMana( ((Integer) changes[c + 1]).intValue(), mob.maxState());
					break;
				case CharState.STAT_MOVE:
					charState.adjMovement( ((Integer) changes[c + 1]).intValue(), mob.maxState());
					break;
				}
			}
		}
		if(firstTime)
		{
			firstTime=false;
			charState.copyInto(mob.curState());
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(processing)
			return;
		processing=true;
		try
		{
			if(affected==null)
				return;
			final Item myItem=(Item)affected;
			final ItemPossessor owner=myItem.owner();
			if(owner==null)
				return;
			if(!(owner instanceof MOB))
				return;
			if(msg.amISource((MOB)owner))
			{
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_EAT:
				case CMMsg.TYP_DRINK:
					if(msg.amITarget(myItem))
					{
						if(canApply(owner))
						{
							adjCharStats((MOB)owner, charStatsChanges,msg.source().baseCharStats());
							phyStuff(phyStatsChanges, msg.source().basePhyStats());
							adjCharState((MOB)owner, charStateChanges,msg.source().curState());
						}
					}
					break;
				case CMMsg.TYP_DAMAGE:
					if((myItem instanceof Weapon)
					&&(msg.tool()==myItem)
					&&(msg.value()>0)
					&&(msg.source()!=msg.target())
					&&(msg.target() instanceof MOB))
					{
						if(reversed)
						{
							if(canApply((MOB)msg.target()))
							{
								adjCharStats((MOB)msg.target(), charStatsChanges,((MOB)msg.target()).baseCharStats());
								phyStuff(phyStatsChanges, ((MOB)msg.target()).basePhyStats());
								adjCharState((MOB)msg.target(), charStateChanges,((MOB)msg.target()).curState());
							}
						}
						else
						if(canApply(owner))
						{
							adjCharStats(msg.source(), charStatsChanges,msg.source().baseCharStats());
							phyStuff(phyStatsChanges, msg.source().basePhyStats());
							adjCharState(msg.source(), charStateChanges,msg.source().curState());
						}
					}
					break;
				case CMMsg.TYP_PUT:
				case CMMsg.TYP_INSTALL:
					if((myItem instanceof Container)
					&&(!(myItem instanceof Drink))
					&&(msg.amITarget(myItem)))
					{
						if(canApply(owner))
						{
							adjCharStats((MOB)owner, charStatsChanges,msg.source().baseCharStats());
							phyStuff(phyStatsChanges, msg.source().basePhyStats());
							adjCharState((MOB)owner, charStateChanges,msg.source().curState());
						}
					}
					break;
				}
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.target() == owner)
			&&(myItem instanceof Armor)
			&&(msg.value()>0)
			&&(msg.source() != owner)
			&&(myItem.amBeingWornProperly()))
			{
				if(reversed)
				{
					if(canApply(msg.source()))
					{
						adjCharStats(msg.source(), charStatsChanges,msg.source().baseCharStats());
						phyStuff(phyStatsChanges, msg.source().basePhyStats());
						adjCharState(msg.source(), charStateChanges,msg.source().curState());
					}
				}
				else
				if(canApply(owner))
				{
					adjCharStats((MOB)owner, charStatsChanges,((MOB)owner).baseCharStats());
					phyStuff(phyStatsChanges, ((MOB)owner).basePhyStats());
					adjCharState((MOB)owner, charStateChanges, ((MOB)owner).curState());
				}
			}
		}
		finally
		{
			processing=false;
		}
	}
}
