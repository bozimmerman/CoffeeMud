package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2022-2025 Bo Zimmerman

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
public class Skill_SpreadHate extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_SpreadHate";
	}

	private final static String	localizedName	= CMLib.lang().L("Spread Hate");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "(Spreading Hate)";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SPREADHATE", "SHATE" });

	protected Ability mood = null;
	protected static final String[] moodTypes = new String[] { "ANGRY", "RUDE", "MEAN" };

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if ((msg.target() == affected)
		&& (msg.tool() instanceof Ability )
		&&(msg.sourceMinor()!=CMMsg.TYP_TEACH)
		&&(msg.tool().ID().equals("Disease_Hatred")))
			return false;
		else
		if(((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)))
		&&(msg.sourceMessage()!=null)
		&&((msg.tool()==null)||(msg.tool().ID().equals("Common"))))
		{
			if(mood == null)
				mood = CMClass.getAbility("Mood");
			if((mood != null)
			&&(!msg.source().phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_MOOD)))
			{
				mood.setAffectedOne(affected);
				final String newStr = moodTypes[CMLib.dice().roll(1, moodTypes.length, -1)];
				if(!mood.text().equals(newStr))
					mood.setMiscText(newStr);
				if(!mood.okMessage(msg.source(), msg))
					return false;
			}
		}
		return true;
	}

	protected static Map<String,String>[] types = null;

	protected void addNewHate(final List<Map<String, String>> maps, final String word, final String fword, final String mask)
	{
		final String lword = word.toLowerCase();
		final List<String> rcwords = CMLib.english().parseWords(lword);
		while(maps.size()<rcwords.size())
			maps.add(new Hashtable<String,String>());
		final String pluralKey = CMLib.english().makePlural(lword).replace(' ', '_').toUpperCase();
		if(fword == null)
			maps.get(rcwords.size()-1).put(lword, pluralKey+"=\""+mask+" \\\"+"+lword+"\\\" \"");
		else
			maps.get(rcwords.size()-1).put(lword, pluralKey+"=\""+mask+" \\\"+"+fword+"\\\" \"");
	}

	protected String findHateable(final String msg)
	{
		final List<String> words = CMLib.english().parseWords(msg.toLowerCase());
		if(types == null)
		{
			synchronized(this.getClass())
			{
				if(types == null)
				{
					final List<Map<String,String>> maps = new ArrayList<Map<String,String>>();
					maps.add(new Hashtable<String,String>());
					maps.get(0).put(L("male"), "MALES=\"-GENDER +MALE\"");
					maps.get(0).put(L("males"), "MALES=\"-GENDER +MALE\"");
					maps.get(0).put(L("female"), "FEMALES=\"-GENDER +FEMALE\"");
					maps.get(0).put(L("females"), "FEMALES=\"-GENDER +FEMALE\"");
					maps.get(0).put(L("neuter"), "NEUTERS=\"-GENDER +NEUTER\"");
					maps.get(0).put(L("neuters"), "NEUTERS=\"-GENDER +NEUTER\"");
					maps.get(0).put(L("boy"), "MALES=\"-GENDER +MALE\"");
					maps.get(0).put(L("boys"), "MALES=\"-GENDER +MALE\"");
					maps.get(0).put(L("girl"), "FEMALES=\"-GENDER +FEMALE\"");
					maps.get(0).put(L("girls"), "FEMALES=\"-GENDER +FEMALE\"");
					maps.get(0).put(L("dude"), "MALES=\"-GENDER +MALE\"");
					maps.get(0).put(L("dudes"), "MALES=\"-GENDER +MALE\"");
					maps.get(0).put(L("chick"), "FEMALES=\"-GENDER +FEMALE\"");
					maps.get(0).put(L("chicks"), "FEMALES=\"-GENDER +FEMALE\"");
					for(final Enumeration<Race> r = CMClass.races();r.hasMoreElements();)
					{
						final Race R = r.nextElement();
						if(R == null)
							continue;
						addNewHate(maps, R.name().toLowerCase(), R.ID(), "-RACE");
						addNewHate(maps, CMLib.english().makePlural(R.name().toLowerCase()), R.ID(), "-RACE");
						addNewHate(maps, R.racialCategory().toLowerCase(), null, "-RACECAT");
						addNewHate(maps, CMLib.english().makePlural(R.racialCategory().toLowerCase()), R.racialCategory().toLowerCase(), "-RACECAT");
					}
				}
			}
		}
		for(int i=types.length-1;i>=0;i--)
		{
			for(int x=0;x<=words.size()-(i+1);x++)
			{
				final StringBuilder wd = new StringBuilder();
				for(int y=x;y<x+i+1;y++)
					wd.append(words.get(y)).append(" ");
				final String word = wd.toString().trim();
				if(types[i].containsKey(word)) // we have a winner!
					return types[i].get(word);
			}
		}
		return null;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.source() == affected)
		&&(msg.target() instanceof MOB)
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage() != null)
		&&(CMLib.flags().canBeHeardSpeakingBy(msg.source(), (MOB)msg.target()))
		&&(msg.source().mayIFight((MOB)msg.target())))
		{
			final String sayMsg = CMStrings.getSayFromMessage(msg.sourceMessage());
			final String hatedMask = findHateable(sayMsg);
			if((hatedMask != null)
			&&(!CMLib.masking().maskCheck(hatedMask, msg.target(), true)))
			{
				final MOB targetMOB = (MOB)msg.target();
				final Ability apathyA = CMClass.getAbility("Disease_Hatred");
				if(apathyA != null)
				{
					if((CMLib.dice().rollPercentage()>targetMOB.charStats().getSave(CharStats.STAT_SAVE_DISEASE))
					&&(targetMOB.location()!=null))
					{
						final MOB following=targetMOB.amFollowing();
						apathyA.invoke(msg.source(),targetMOB,true,0);
						final Ability effectA = targetMOB.fetchEffect("Disease_Hatred");
						if(effectA != null)
							effectA.setMiscText("+"+hatedMask);
						if(targetMOB.amFollowing()!=following)
							targetMOB.setFollowing(following);
					}
					else
						((StdAbility)apathyA).spreadImmunity(targetMOB);
				}
			}
		}
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats stats)
	{
		super.affectPhyStats(affected,stats);
		stats.addAmbiance("^rhateful^?");
	}

	@Override
	public void unInvoke()
	{
		final MOB invoker=this.invoker();
		super.unInvoke();
		if((invoker!=null)&&(this.unInvoked))
			invoker.tell(L("You are no longer aggressively hateful."));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;

		final Ability oldA = mob.fetchEffect(ID());
		if(oldA != null)
		{
			oldA.unInvoke();
			return true;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final String str=auto?"":L("<S-NAME> become(s) aggressively hateful.");
			final CMMsg msg=CMClass.getMsg(mob,mob,this,CMMsg.MSG_QUIETMOVEMENT,str,CMMsg.MSG_QUIETMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),str,CMMsg.MSG_QUIETMOVEMENT,str);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final Ability A=super.beneficialAffect(mob, mob, asLevel, 0);
				if(A != null)
					A.makeLongLasting();
			}
		}
		else
			return beneficialWordsFizzle(mob,mob,L("<S-NAME> attempt(s) to go aggressively hateful, but fail(s)."));

		// return whether it worked
		return success;
	}
}
