package com.planet_ink.coffee_mud.Abilities.Prayers;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2011-2018 Bo Zimmerman

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

public class Prayer_Prophecy extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Prophecy";
	}

	private final static String localizedName = CMLib.lang().L("Prophecy");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(In a Prophetic Trance)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
		{
			super.unInvoke();
			return;
		}
		final MOB mob=(MOB)affected;
		if((mob.amDead())||(this.tickDown>0)||(mob.isInCombat()))
		{
			if(mob.location()!=null)
				mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL,L("<S-NAME> end(s) <S-HIS-HER> trance."));
			super.unInvoke();
			return;
		}

		int numProphesies=super.getXLEVELLevel(mob) + 2;
		if(CMLib.ableMapper().qualifyingLevel(mob, this)>1)
			numProphesies += (super.adjustedLevel(mob, 0) / CMLib.ableMapper().qualifyingLevel(mob, this));
		final List<Pair<Integer,Quest>> prophesies=new Vector<Pair<Integer,Quest>>();
		for(final Enumeration<Quest> q = CMLib.quests().enumQuests(); q.hasMoreElements();)
		{
			final Quest Q = q.nextElement();
			if( Q.isCopy() || (Q.duration()==0) ||(Q.name().equalsIgnoreCase("holidays")))
			{
				continue;
			}
			int ticksRemaining=Integer.MAX_VALUE;
			if(Q.waiting())
			{
				ticksRemaining = Q.waitRemaining();
				if(ticksRemaining<=0)
					ticksRemaining=Integer.MAX_VALUE;
			}
			else
			if(Q.running())
			{
				ticksRemaining = Q.ticksRemaining();
				if(ticksRemaining<=0)
					ticksRemaining=Integer.MAX_VALUE;
			}
			if(ticksRemaining != Integer.MAX_VALUE)
			{
				if(prophesies.size()<numProphesies)
					prophesies.add(new Pair<Integer,Quest>(Integer.valueOf(ticksRemaining),Q));
				else
				{
					Pair<Integer,Quest> highP=null;
					for(final Pair<Integer,Quest> P : prophesies)
					{
						if((highP==null)||(P.first.intValue()>highP.first.intValue()))
							highP=P;
					}
					if((highP==null)||(highP.first.intValue() > ticksRemaining))
					{
						prophesies.remove(highP);
						prophesies.add(new Pair<Integer,Quest>(Integer.valueOf(ticksRemaining),Q));
					}
				}
			}
		}
		if(prophesies.size()==0)
			mob.tell(L("You receive no prophetic visions."));
		else
		{
			final TimeClock clock =CMLib.time().localClock(mob);
			String starting;
			switch(CMLib.dice().roll(1, 10, 0))
			{
			case 1:
				starting = "The visions say that ";
				break;
			case 2:
				starting = "You see that";
				break;
			case 3:
				starting = "You feel that";
				break;
			case 4:
				starting = "A voice tells you that";
				break;
			case 5:
				starting = "Someone whispers that";
				break;
			case 6:
				starting = "It is revealed to you that";
				break;
			case 7:
				starting = "In your visions, you see that";
				break;
			case 8:
				starting = "In your mind you hear that";
				break;
			case 9:
				starting = "Your spirit tells you that";
				break;
			default:
				starting = "You prophesy that";
				break;
			}
			final StringBuilder message=new StringBuilder(starting);
			for(int p=0;p<prophesies.size();p++)
			{
				final Pair<Integer,Quest> P=prophesies.get(p);
				final Quest Q=P.second;
				String name=Q.name();
				final long timeTil= P.first.longValue() * CMProps.getTickMillis();
				final String timeTilDesc = clock.deriveEllapsedTimeString(timeTil);
				final String possibleBetterName=Q.getStat("DISPLAY");
				if(possibleBetterName.length()>0)
					name=possibleBetterName;
				name=name.replace('_',' ');
				final List<String> V=CMParms.parseSpaces(name,true);
				for(int v=V.size()-1;v>=0;v--)
				{
					if(CMath.isNumber(V.get(v)))
						V.remove(v);
				}
				name=CMParms.combineQuoted(V, 0);
				final String ending;
				if(Q.running())
					ending=" will end in ";
				else
					ending=" will begin in ";
				if((p==prophesies.size()-1)&&(p>0))
					message.append(", and");
				else
				if(p>0)
					message.append(",");
				message.append(" \"").append(name).append("\"").append(ending).append(timeTilDesc);
			}
			message.append(".");
			mob.tell(message.toString());
		}
		super.unInvoke();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return;

		final MOB mob=(MOB)affected;

		if(msg.amISource(mob))
		{
			if(((msg.sourceMinor()==CMMsg.TYP_ENTER)
				||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
				||(msg.sourceMinor()==CMMsg.TYP_FLEE)
				||(msg.sourceMinor()==CMMsg.TYP_RECALL))
			&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(msg.sourceMajor()>0))
			{
				unInvoke();
				mob.recoverPhyStats();
			}
			else
			if((abilityCode()==0)
			&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(msg.othersMinor()!=CMMsg.TYP_LOOK)
			&&(msg.othersMinor()!=CMMsg.TYP_EXAMINE)
			&&(msg.othersMajor()>0))
			{
				if(msg.othersMajor(CMMsg.MASK_SOUND))
				{
					unInvoke();
					mob.recoverPhyStats();
				}
				else
				switch(msg.othersMinor())
				{
				case CMMsg.TYP_SPEAK:
				case CMMsg.TYP_CAST_SPELL:
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_CLOSE:
				case CMMsg.TYP_LOCK:
				case CMMsg.TYP_UNLOCK:
				case CMMsg.TYP_PUSH:
				case CMMsg.TYP_PULL:
					{
						unInvoke();
						mob.recoverPhyStats();
					}
					break;
				}
			}
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.isInCombat())&&(!auto))
		{
			mob.tell(L("Not while you're fighting!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Physical target=mob;
		if((auto)&&(givenTarget!=null))
			target=givenTarget;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,null,auto),auto?"":L("^S<T-NAME> @x1, entering a divine trance.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,3);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<T-NAME> @x1, but nothing happens.",prayWord(mob)));

		return success;
	}
}
