package com.planet_ink.coffee_mud.Commands;
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
   Copyright 2004-2025 Bo Zimmerman

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
public class Report extends Skills
{
	public Report()
	{
	}

	private final String[] access=I(new String[]{"REPORT"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			final StringBuffer buf=new StringBuffer(
								L("say \"I have @x1/@x2 hit points, @x3/@x4 mana, @x5/@x6 move",
									""+mob.curState().getHitPoints(),
									""+mob.maxState().getHitPoints(),
									""+mob.curState().getMana(),
									""+mob.maxState().getMana(),
									""+mob.curState().getMovement(),
									""+mob.maxState().getMovement()));
			if((!CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.SHOWXP))
			&&!mob.charStats().getCurrentClass().expless()
			&&!mob.charStats().getMyRace().expless()
			&&(mob.getExpNeededLevel()<Integer.MAX_VALUE))
			   buf.append(L(", and need @x1 to level",""+mob.getExpNeededLevel()));
			buf.append(".\"");
			final Command C=CMClass.getCommand("Say");
			if(C!=null)
				C.execute(mob,CMParms.parse(buf.toString()),metaFlags);
		}
		else
		{
			final int level=parseOutLevel(commands);
			final String s=CMParms.combine(commands,1).toUpperCase();
			final StringBuffer say=new StringBuffer("");
			if("AFFECTS".startsWith(s)||(s.equalsIgnoreCase("ALL")))
			{

				final StringBuffer aff=new StringBuffer("\n\r^!I am affected by:^? ");
				final Command C=CMClass.getCommand("Affect");
				if(C!=null)
					aff.append(C.executeInternal(mob,metaFlags,mob).toString());
				say.append(aff.toString());
			}
			if("EXPERTISES".startsWith(s)||(s.equalsIgnoreCase("ALL")))
			{

				final StringBuffer aff=new StringBuffer("\n\r^!My expertises are:^?\n\r");
				final Command C=CMClass.getCommand("Expertises");
				if(C!=null)
					aff.append(C.executeInternal(mob,metaFlags,mob).toString());
				say.append(aff.toString());
			}
			if("STATS".startsWith(s)||(s.equalsIgnoreCase("ALL")))
			{
				final StringBuffer stats=new StringBuffer("");
				final int max=CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
				final CharStats CT=mob.charStats();
				for(final int i : CharStats.CODES.BASECODES())
				{
					stats.append("^c" + CMStrings.capitalizeAndLower(CMStrings.limit(CharStats.CODES.NAME(i),3))+": ^w"
							+CMStrings.padRight(Integer.toString(CT.getStat(i)),2)
							+"/"+(max+CT.getStat(CharStats.CODES.toMAXBASE(i)))+", ");
				}
				say.append("\n\r^NMy stats:^? "+stats.toString());
			}
			if(s.equalsIgnoreCase("ALL"))
			{

				final Vector<Integer> V=new Vector<Integer>();
				V.add(Integer.valueOf(Ability.ACODE_THIEF_SKILL));
				V.add(Integer.valueOf(Ability.ACODE_SKILL));
				V.add(Integer.valueOf(Ability.ACODE_COMMON_SKILL));
				V.add(Integer.valueOf(Ability.ACODE_SPELL));
				V.add(Integer.valueOf(Ability.ACODE_PRAYER));
				V.add(Integer.valueOf(Ability.ACODE_SUPERPOWER));
				V.add(Integer.valueOf(Ability.ACODE_LANGUAGE));
				V.add(Integer.valueOf(Ability.ACODE_TECH));
				V.add(Integer.valueOf(Ability.ACODE_CHANT));
				V.add(Integer.valueOf(Ability.ACODE_SONG));
				say.append("\n\r^NMy skills:^? "+getAbilities(null,mob,V,Ability.ALL_ACODES,false,level));
			}
			else
			{
				int acode=-1;
				int adom=-1;
				final String us = s.toUpperCase().replace(' ','_');
				for(int i=0;i<Ability.ACODE.DESCS.size();i++)
				{
					if(Ability.ACODE.DESCS.get(i).equals(us)
					||CMLib.english().makePlural(Ability.ACODE.DESCS.get(i)).equals(us))
					{
						acode=i;
						break;
					}
				}
				if(acode < 0)
				{
					if("POWER".equals(us)||"POWERS".equals(us))
						acode=Ability.ACODE_SUPERPOWER;
					else
					if("LANGS".equals(us))
						acode=Ability.ACODE_LANGUAGE;
					else
					for(int d=0;d<Ability.DOMAIN.DESCS.size();d++)
					{
						if(Ability.DOMAIN.DESCS.get(d).equals(us)
						||CMLib.english().makePlural(Ability.DOMAIN.DESCS.get(d)).equals(us))
						{
							adom=d;
							break;
						}
					}
				}
				if((acode < 0)&&(adom<0))
				{
					for(int i=0;i<Ability.ACODE.DESCS.size();i++)
					{
						if(CMLib.english().makePlural(Ability.ACODE.DESCS.get(i)).startsWith(us))
						{
							acode=i;
							break;
						}
					}
				}
				if((acode < 0)&&(adom<0))
				{
					if("POWERS".startsWith(us))
						acode=Ability.ACODE_SUPERPOWER;
					else
					if("LANGS".startsWith(us))
						acode=Ability.ACODE_LANGUAGE;
					else
					for(int d=0;d<Ability.DOMAIN.DESCS.size();d++)
					{
						if(CMLib.english().makePlural(Ability.DOMAIN.DESCS.get(d)).startsWith(us))
						{
							adom=d;
							break;
						}
					}
				}
				if(acode >= 0)
				{
					switch(acode)
					{
					case Ability.ACODE_SPELL:
						say.append("\n\r^NMy spells:^? "+getAbilities(null,mob,Ability.ACODE_SPELL,-1,false,level));
						break;
					case Ability.ACODE_SKILL:
					{
						final Vector<Integer> V=new Vector<Integer>();
						V.add(Integer.valueOf(Ability.ACODE_THIEF_SKILL));
						V.add(Integer.valueOf(Ability.ACODE_SKILL));
						V.add(Integer.valueOf(Ability.ACODE_TECH));
						V.add(Integer.valueOf(Ability.ACODE_COMMON_SKILL));
						say.append("\n\r^NMy skills:^? "+getAbilities(null,mob,V,Ability.ALL_ACODES,false,level));
						break;
					}
					case Ability.ACODE_PRAYER:
						say.append("\n\r^NMy prayers:^? "+getAbilities(null,mob,Ability.ACODE_PRAYER,-1,false,level));
						break;
					case Ability.ACODE_SUPERPOWER:
						say.append("\n\r^NMy super powers:^? "+getAbilities(null,mob,Ability.ACODE_SUPERPOWER,-1,false,level));
						break;
					case Ability.ACODE_CHANT:
						say.append("\n\r^NMy chants:^? "+getAbilities(null,mob,Ability.ACODE_CHANT,-1,false,level));
						break;
					case Ability.ACODE_SONG:
						say.append("\n\r^NMy songs:^? "+getAbilities(null,mob,Ability.ACODE_SONG,-1,false,level));
						break;
					case Ability.ACODE_LANGUAGE:
						say.append("\n\r^NMy languages:^? "+getAbilities(null,mob,Ability.ACODE_LANGUAGE,-1,false,level));
						break;
					case Ability.ACODE_TECH:
						say.append("\n\r^NMy tech skills:^? "+getAbilities(null,mob,Ability.ACODE_TECH,-1,false,level));
						break;
					case Ability.ACODE_COMMON_SKILL:
						say.append("\n\r^NMy common skills:^? "+getAbilities(null,mob,Ability.ACODE_COMMON_SKILL,-1,false,level));
						break;
					}
				}
				else
				if(adom >= 1)
				{
					final String domainName = Ability.DOMAIN.DESCS.get(adom).toLowerCase();
					say.append("\n\r^NMy "+domainName+" skills:^? "+getAbilities(null,mob,Ability.ALL_ACODES,adom<<5,false,level));
				}
			}

			if(say.length()==0)
			{
				mob.tell(L("'@x1' is unknown.  Try SPELLS, SKILLS, PRAYERS, CHANTS, SONGS, "
						+ "COMMON SKILLS, LANGUAGES, EXPERTISES, STATS, a skill Domain, or ALL.",s));
			}
			else
				CMLib.commands().postSay(mob,null,say.toString(),false,false);
		}
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID());
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
