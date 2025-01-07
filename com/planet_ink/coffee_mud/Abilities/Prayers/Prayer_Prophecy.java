package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.MPContext;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2011-2024 Bo Zimmerman

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

	protected int abilityCode = 0;

	@Override
	public void setAbilityCode(final int newCode)
	{
		abilityCode = newCode;
	}

	@Override
	public int abilityCode()
	{
		return abilityCode;
	}

	protected String getStarterString()
	{
		String starting;
		switch(CMLib.dice().roll(1, 10, 0))
		{
		case 1:
			starting = "The visions say that";
			break;
		case 2:
			starting = "I see that";
			break;
		case 3:
			starting = "I feel that";
			break;
		case 4:
			starting = "A voice tells me that";
			break;
		case 5:
			starting = "Someone whispers that";
			break;
		case 6:
			starting = "It is revealed to me that";
			break;
		case 7:
			starting = "In my visions, I see that";
			break;
		case 8:
			starting = "In my mind I hear that";
			break;
		case 9:
			starting = "The spirits tell me that";
			break;
		default:
			starting = "I prophesy that";
			break;
		}
		return starting;
	}

	public String generateEventPrediction(final MOB invokerM, final MOB mob)
	{
		try
		{
			@SuppressWarnings("unchecked")
			List<XMLLibrary.XMLTag> predictionRoot = (List<XMLLibrary.XMLTag>)Resources.getResource("SYSTEM_PREDICTION_SCRIPTS");
			if(predictionRoot == null)
			{
				final CMFile file = new CMFile(Resources.buildResourcePath("skills/predictions.xml"),null);
				if(!file.canRead())
					throw new CMException(L("Random data file '@x1' not found.  Aborting.",file.getCanonicalPath()));
				final StringBuffer xml = file.textUnformatted();
				predictionRoot = CMLib.xml().parseAllXML(xml);
				Resources.submitResource("SYSTEM_PREDICTION_SCRIPTS", predictionRoot);
			}
			String s=null;
			String summary=null;
			for(int i=0;i<10;i++)
			{
				final Hashtable<String,Object> definedIDs = new Hashtable<String,Object>();
				CMLib.percolator().buildDefinedIDSet(predictionRoot,definedIDs, new XTreeSet<String>(definedIDs.keys()));
				final XMLTag piece=(XMLTag)definedIDs.get("RANDOM_PREDICTION");
				if(piece == null)
					throw new CMException(L("Predictions not found.  Aborting."));
				CMLib.percolator().preDefineReward(piece, definedIDs);
				CMLib.percolator().defineReward(piece,definedIDs);
				s=CMLib.percolator().findString("STRING", piece, definedIDs);
				if((s==null)||(s.trim().length()==0))
					throw new CMException(L("Predictions not generated."));
				final ScriptingEngine testE = (ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
				testE.setScript(s);
				if(!testE.isFunc("Prediction"))
					throw new CMException(L("Prediction corrupt."));
				final MPContext ctx = new MPContext(mob, mob, mob, null, null, null, mob.Name(), null);
				summary = testE.callFunc("Prediction", s, ctx);
				if((summary != null)&&(summary.trim().length()>0))
					break;
				// try again!
			}
			final TimeClock predictionClock = (TimeClock)CMLib.time().homeClock(mob).copyOf();
			switch(CMLib.dice().roll(1, 5, 0))
			{
			case 1:
				break;
			case 2:
			case 3:
			case 4:
				predictionClock.bumpWeeks(CMLib.dice().roll(1, 10, 5));
				break;
			case 5:
				predictionClock.bumpMonths(CMLib.dice().roll(1, 10, 3));
				break;
			}
			predictionClock.bumpDays(CMLib.dice().roll(1, 10, 3));
			predictionClock.bumpHours(CMLib.dice().roll(1, 10, 3));
			final Ability effA = CMClass.getAbility("ScriptLater");
			effA.invoke(invokerM, new XVector<String>(mob.Name(), predictionClock.toTimePeriodCodeString(), s),mob,true,0);
			return summary;
		}
		catch(final CMException e)
		{
			Log.errOut(e.getMessage());
			return null;
		}
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
		try
		{
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
			if(((prophesies.size()==0)
				||(CMLib.dice().roll(1, 11, 0) < super.getXLEVELLevel(invoker())))
			&&(invoker()!=null)
			&&(invoker().location()!=null))
			{
				final List<MOB> players = new ArrayList<MOB>();
				for(final Enumeration<MOB> m=invoker().location().inhabitants();m.hasMoreElements();)
				{
					final MOB M = m.nextElement();
					if((M!=null)
					&&(M != invoker())
					&&(M.isPlayer())
					&&(M.fetchEffect("ScriptLater")==null))
						players.add(M);
				}
				if(players.size()>0)
				{
					final MOB M = players.get(CMLib.dice().roll(1, players.size(), -1));
					final String finalPrediction = generateEventPrediction(invoker(), M);
					if((finalPrediction != null)&&(finalPrediction.trim().length()>0))
					{
						final StringBuilder msg = new StringBuilder(finalPrediction);
						this.setAbilityCode(1);
						CMLib.commands().forceStandardCommand(mob, "Yell", new XVector<String>("YELLTO",M.name(),msg.toString()));
						return;
					}
				}
			}

			if(prophesies.size()> 0)
			{
				final TimeClock clock =CMLib.time().localClock(mob);
				final StringBuilder message=new StringBuilder(getStarterString());
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
				this.setAbilityCode(1);
				CMLib.commands().forceStandardCommand(mob, "Yell", new XVector<String>("YELL",message.toString()));
			}
			else
				mob.tell(L("You receive no prophetic visions."));
		}
		finally
		{
			super.unInvoke();
		}
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
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((mob.isInCombat())&&(!auto))
		{
			mob.tell(L("Not while you're fighting!"));
			return false;
		}

		Physical target=mob;
		if((auto)&&(givenTarget!=null))
			target=givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(mob,target,null,L("<T-NAME> <T-IS-ARE> already affected by @x1.",name()));
			return false;
		}

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
