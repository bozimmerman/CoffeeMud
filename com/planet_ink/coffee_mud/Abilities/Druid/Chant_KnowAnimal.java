package com.planet_ink.coffee_mud.Abilities.Druid;
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

import java.io.IOException;
import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Chant_KnowAnimal extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_KnowAnimal";
	}

	private final static String	localizedName	= CMLib.lang().L("Know Animal");

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
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_NATURELORE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	@Override
	public boolean preInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel, final int secondsElapsed, final double actionsRemaining)
	{
		return true;
	}

	protected void addSpace(final StringBuilder str)
	{
		if((str.length()>0)&&(!Character.isWhitespace(str.charAt(str.length()-1))))
			str.append(" ");
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room roomR=mob.location();
		if(roomR==null)
			return false;
		final Area areaA=roomR.getArea();
		if(areaA==null)
			return false;

		final MOB target = this.getTarget(mob, commands, givenTarget, false, false);
		if(target == null)
			return false;

		if(!CMLib.flags().isAnAnimal(target))
		{
			mob.tell(L("@x1 doesn't seem to be an animal.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final Room room=mob.location();
		final CMMsg msg=CMClass.getMsg(mob,target,this,super.verbalCastCode(mob, target, auto),auto?"":L("^S<S-NAME> <S-IS-ARE> chanting about <T-NAMESELF>.^?"));
		if(room.okMessage(mob,msg))
		{
			room.send(mob,msg);
			final String heShe = target.charStats().HeShe();
			final String hisHer = CMStrings.capitalizeAndLower(target.charStats().hisher());
			final StringBuilder rpt = new StringBuilder(L("@x1 is a @x2 @x3.",target.name(mob),target.charStats().genderName(),target.charStats().raceName()));
			if(target.findTattoo("SYSTEM_SUMMONED")!=null)
			{
				final Tattoo tattChk=target.findTattoo("SUMMONED_BY:*");
				if(tattChk != null)
				{
					final String name = tattChk.ID().substring(12);
					rpt.append(L(" @x1 was summoned by @x2.",heShe,name));
				}
			}
			final int expertise = super.getXLEVELLevel(mob);
			if(expertise > 0)
			{
				final Command sC = CMClass.getCommand("Score");
				try
				{
					final String stats = (String)sC.executeInternal(target, 0, ":STATS");
					rpt.append(L(" @x1 has the following stats: \n\r@x2.",heShe,stats));
				}
				catch (final IOException e) {}
			}
			if(expertise > 1)
			{
				addSpace(rpt);
				final int prowessCode = CMProps.getIntVar(CMProps.Int.COMBATPROWESS);
				if((prowessCode&CMProps.Int.ANY_ARMOR_PROWESS)!=0)
					rpt.append(L("@x1 armored defence is @x2.^N",hisHer, CMLib.combat().armorStr(target)));
				addSpace(rpt);
				if((prowessCode&CMProps.Int.ANY_COMBAT_PROWESS)!=0)
					rpt.append(L("@x1 combat prowess is ^H@x2.^N",hisHer, CMLib.combat().fightingProwessStr(target)));
				addSpace(rpt);
				if((prowessCode&CMProps.Int.ANY_DAMAGE_PROWESS)!=0)
					rpt.append(L("@x1 damage threat is @x2.^N",hisHer, CMLib.combat().damageProwessStr(target)));
			}
			if(expertise > 2)
			{
				for(int i=0;i<PhyStats.CAN_SEE_DESCS.length;i++)
				{
					if(CMath.isSet(target.phyStats().sensesMask(), i))
					{
						addSpace(rpt);
						rpt.append(L(heShe+" "+PhyStats.CAN_SEE_DESCS[i].toLowerCase()+"."));
					}
				}
			}
			if(expertise > 3)
			{
				for(final Enumeration<Behavior> b=target.behaviors();b.hasMoreElements();)
				{
					final Behavior B=b.nextElement();
					final String accounting=B.accountForYourself();
					if(accounting.length()==0)
						continue;
					addSpace(rpt);
					rpt.append(L(heShe+" is "+accounting+"."));
				}
				for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((!A.canBeUninvoked())&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY))
					{
						final String accounting=A.accountForYourself();
						if(accounting.length()==0)
							continue;
						addSpace(rpt);
						rpt.append(accounting);
					}
				}
			}
			final Set<String> allKnows = new TreeSet<String>();
			if(expertise > 7)
			{
				for(final Enumeration<Ability> a = target.allAbilities();a.hasMoreElements();)
				{
					final Ability A = a.nextElement();
						allKnows.add(A.name());
				}
			}
			if(expertise > 4)
			{
				final Set<String> knows=new TreeSet<String>();
				final Race R = target.charStats().getMyRace();
				final List<Ability> lst = R.racialAbilities(target);
				for(final Ability A : lst)
				{
					knows.add(A.name());
					allKnows.remove(A.name());
				}
				final List<Quint<String, Integer, Integer, Boolean, String>> qlst = R.culturalAbilities();
				for(final Quint<String, Integer, Integer, Boolean, String> q : qlst)
				{
					final Ability A = CMClass.getAbility(q.first);
					if((A!=null)&&(target.fetchAbility(A.ID())!=null))
					{
						knows.add(A.name());
						allKnows.remove(A.name());
					}
				}
				final List<Ability> elst = R.racialEffects(target);
				for(final Ability A : elst)
				{
					knows.add(A.name());
					allKnows.remove(A.name());
				}
				if(knows.size()>0)
				{
					addSpace(rpt);
					rpt.append(L("@x1 knows: @x2.",heShe,CMLib.english().toEnglishStringList(knows)));
				}
			}
			if(expertise > 5)
			{
				for(final String factionID : new String[] {CMLib.factions().getAlignmentID(), CMLib.factions().getInclinationID() } )
				{
					if(CMLib.factions().isFactionLoaded(factionID))
					{
						final Faction F=CMLib.factions().getFaction(factionID);
						if(F!=null)
						{
							final int factionAmt=target.fetchFaction(factionID);
							final Faction.FRange FR=CMLib.factions().getRange(factionID,factionAmt);
							addSpace(rpt);
							rpt.append(L("@x1 @x2 is @x3.",hisHer,F.name(), FR.name()));
						}
					}
				}
			}
			if(expertise > 6)
			{
				final Room R = target.getStartRoom();
				addSpace(rpt);
				if(R == null)
					rpt.append(L("@x1 is from the unknown.",heShe));
				else
					rpt.append(L("@x1 is from '@x2'.",heShe,R.displayText()));
			}
			if(expertise > 7)
			{
				if(allKnows.size()>0)
				{
					addSpace(rpt);
					rpt.append(L("@x1 also knows: @x2.",heShe,CMLib.english().toEnglishStringList(allKnows)));
				}
			}
			if(expertise > 8)
			{
				for(int i=0;i<PhyStats.IS_DESCS.length;i++)
				{
					if(CMath.isSet(target.phyStats().disposition(), i))
					{
						addSpace(rpt);
						rpt.append(L(heShe+" "+PhyStats.IS_DESCS[i].toLowerCase()+"."));
					}
				}
			}
			if(expertise > 9)
			{
				final Tattoo parent = target.findTattoo("PARENT:");
				if(parent != null)
				{
					addSpace(rpt);
					rpt.append(L("@x1 is the @x2 of @x3.",heShe,target.charStats().sondaughter(),parent.ID().substring(7)));
				}
			}
			mob.tell(rpt.toString());
		}
		else
			mob.location().show(mob,target,this,super.verbalCastCode(mob, target, auto),L("<S-NAME> chant(s) about <T-NAME> but get(s) frustrated."));
		return success;
	}

}
