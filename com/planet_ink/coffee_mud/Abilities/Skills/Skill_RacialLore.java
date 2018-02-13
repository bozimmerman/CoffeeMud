package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2017-2018 Bo Zimmerman

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

public class Skill_RacialLore extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_RacialLore";
	}

	private final static String	localizedName	= CMLib.lang().L("Racial Lore");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
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

	private static final String[]	triggerStrings	= I(new String[] { "RACIALLORE", "RLORE" });

	protected long lastFail = 0;
	
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_EDUCATIONLORE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	@Override
	public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
	{
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		final Area A=R.getArea();
		if(A==null)
			return false;
		if((commands.size()==0)
		||((commands.size()==1)&&(commands.get(0).equalsIgnoreCase("LIST"))))
		{
			List<String> names=new ArrayList<String>();
			for(Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
				names.add(CMStrings.capitalizeAllFirstLettersAndLower(r.nextElement().name()));
			mob.tell(L("Recall information about which race?  These include: @x1",CMLib.english().toEnglishStringList(names)));
			return false;
		}
		boolean report=false;
		if((commands.size()>1)&&(commands.get(commands.size()-1).equalsIgnoreCase("REPORT")))
		{
			commands.remove(commands.size()-1);
			report=true;
		}
		
		if((System.currentTimeMillis() - lastFail) < 10000)
		{
			mob.tell(L("You still can't recall.  Give yourself some more time to think first."));
			return false;
		}
		
		Race targetR=CMClass.findRace(CMParms.combine(commands));
		if(targetR == null)
		{
			List<String> names=new ArrayList<String>();
			for(Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
				names.add(CMStrings.capitalizeAllFirstLettersAndLower(r.nextElement().name()));
			mob.tell(L("You have never heard of a race called @x1.  You might try one of these: @x2",
					CMParms.combine(commands),CMLib.english().toEnglishStringList(names)));
			return false;
		}
		final String raceName = targetR.name();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		
		final boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
		{
			lastFail = System.currentTimeMillis();
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to recall something about @x1, but can't.",raceName));
			return false;
		}
		final Room room=mob.location();
		final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_THINK,L("<S-NAME> <S-IS-ARE> recalling something about the @x1 race.",raceName));
		if(room.okMessage(mob,msg))
		{
			room.send(mob,msg);
			List<String> tidbits = new ArrayList<String>();
			final int expertise = super.getXLEVELLevel(mob);
			tidbits.add(L("they are part of the genus @x1",targetR.racialCategory()));
			String s=targetR.getStatAdjDesc();
			for(String str : CMParms.parseCommas(s,true))
			{
				if(str.indexOf('-')>0)
					tidbits.add(L("they suffer from @x1",str));
				else
					tidbits.add(L("they benefit from @x1",str));
			}
			s=targetR.getSensesChgDesc();
			for(String str : CMParms.parseCommas(s,true))
				tidbits.add(L("they have @x1",str));
			s=targetR.getDispositionChgDesc();
			for(String str : CMParms.parseCommas(s,true))
				tidbits.add(L("they are always @x1",str));

			Set<String> aDone = new HashSet<String>();
			for(Ability A1 : targetR.racialAbilities(null))
			{
				if(A1 instanceof Language)
					tidbits.add(L("they speak @x1",A1.name()));
				else
					tidbits.add(L("they are born with the ability '@x1'",A1.name()));
				aDone.add(A1.ID());
			}
			for(Quad<String,Integer,Integer,Boolean> q : targetR.culturalAbilities())
			{
				final Ability A1=CMClass.getAbilityPrototype(q.first);
				if(A1 instanceof Language)
					tidbits.add(L("they grow up speaking @x1",A1.name()));
				else
					tidbits.add(L("they are often skilled at '@x1'",A1.name()));
				aDone.add(A1.ID());
			}
			for(Ability A1 : targetR.racialEffects(null))
			{
				if(!aDone.contains(A1.ID()))
				{
					tidbits.add(L("they are always affected by @x1",A1.name()));
				}
			}
			s=targetR.getAbilitiesDesc();
			tidbits.add(L("their life expectancy is @x1 years",targetR.getAgingChart()[Race.AGE_ANCIENT]+""));
			for(String ableID : targetR.abilityImmunities())
			{
				final Ability A1=CMClass.getAbilityPrototype(ableID);
				if(A1!=null)
					tidbits.add(L("they are immune to @x1",A1.name()));
			}
			for(int i=0;i<Race.BODYPARTSTR.length;i++)
			{
				if(targetR.bodyMask()[i] == 1)
					tidbits.add(L("they have one @x1",Race.BODYPARTSTR[i].toLowerCase()));
				else
				if(targetR.bodyMask()[i] == 2)
					tidbits.add(L("they have two @x1",CMLib.english().makePlural(Race.BODYPARTSTR[i].toLowerCase())));
				else
				if(targetR.bodyMask()[i] > 1)
					tidbits.add(L("they have @x1 @x2",""+targetR.bodyMask()[i],CMLib.english().makePlural(Race.BODYPARTSTR[i].toLowerCase())));
			}
			tidbits.add(L("they require @x1 years to reach maturity",""+targetR.getAgingChart()[Race.AGE_MATURE]));
			for(long loc : Wearable.CODES.ALL())
			{
				if(CMath.bset(targetR.forbiddenWornBits(),loc))
					tidbits.add(L("due to their anatomy, they can't wear normal gear on their @x1",Wearable.CODES.NAME(loc).toLowerCase()));
			}
			for(int rsc : targetR.getBreathables())
			{
				if(rsc != RawMaterial.RESOURCE_AIR)
					tidbits.add(L("they can breathe @x1",RawMaterial.CODES.NAME(rsc).toLowerCase()));
			}
			int shortest = (targetR.shortestFemale()+targetR.shortestMale())/2;
			tidbits.add(L("at maturity they are between @x1 and @x2 inches tall",
					""+shortest,""+(shortest+targetR.heightVariance())));
			int lightest = targetR.lightestWeight();
			tidbits.add(L("at maturity they are between @x1 and @x2 pounds",
					""+lightest,""+(lightest+targetR.weightVariance())));
			tidbits.add(L("they like to fight with @x1",targetR.myNaturalWeapon().name()));
			for(RawMaterial M : targetR.myResources())
			{
				final String str=L("their bodies can be butchered for @x1",M.name().endsWith("s")?M.name():CMLib.english().makePlural(M.name()));
				if(!tidbits.contains(str))
					tidbits.add(str);
			}
			if(targetR.useRideClass())
				tidbits.add(L("they can be ridden by humanoids"));
			if(tidbits.size()==0)
			{
				if(report)
					CMLib.commands().postSay(mob, L("I know almost nothing about that race.  I guess it's not my area of Expertise. "));
				else
					mob.tell(L("You know almost nothing about that race.  I guess it's not your area of Expertise. "));
			}
			else
			{
				for(int i=0;i<expertise+1 && tidbits.size()>0;i++)
				{
					final String str=tidbits.remove(CMLib.dice().roll(1, tidbits.size(), -1));
					if(report)
						CMLib.commands().postSay(mob, L("I recall that @x1.",Character.toLowerCase(str.charAt(0))+str.substring(1)));
					else
						mob.tell(L("You recall that @x1.",Character.toLowerCase(str.charAt(0))+str.substring(1)));
				}
			}
		}
		else
			mob.location().show(mob,null,this,CMMsg.MSG_THINK,L("<S-NAME> get(s) frustrated over having forgotten something."));
		return success;
	}

}
