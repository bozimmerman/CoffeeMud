package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class CharClassData extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	
	
	// parameters include help, playable, max stats, pracs, trains, hitpoints,
	// mana, movement, attack, weapons, armor, limits, bonuses,
	// prime, quals, startingeq
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("CLASS");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			CharClass C=CMClass.getCharClass(last);
			if(C!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("HELP"))
				{
					StringBuffer s=CMLib.help().getHelpText(C.ID(),null,false);
					if(s==null)
						s=CMLib.help().getHelpText(C.name(),null,false);
					if(s!=null)
					{
						int limit=70;
						if(parms.containsKey("LIMIT")) limit=CMath.s_int((String)parms.get("LIMIT"));
						str.append(helpHelp(s,limit));
					}
				}
				if(parms.containsKey("PLAYABLE"))
					str.append(Area.THEME_DESCS_EXT[C.availabilityCode()]+", ");

				if(parms.containsKey("BASECLASS"))
					str.append(C.baseClass()+", ");

				if(parms.containsKey("MAXSTATS"))
					for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
						if(C.maxStatAdjustments()[i]!=0)
							str.append(CMStrings.capitalizeAndLower(CharStats.STAT_DESCS[i])+" ("+(CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT)+C.maxStatAdjustments()[i])+"), ");
				if(parms.containsKey("PRACS"))
				{
					str.append(C.getPracsFirstLevel()+" plus (Wisdom/4)");
					if(C.getBonusPracLevel()>0)
						str.append("+"+C.getBonusPracLevel());
					else
					if(C.getBonusPracLevel()<0)
						str.append(""+C.getBonusPracLevel());
					str.append(" per level after first, ");
				}
				if(parms.containsKey("TRAINS"))
					str.append(C.getTrainsFirstLevel()+" plus 1 per level after first, ");
				if(parms.containsKey("DAMAGE"))
					str.append("An extra point of damage per "+C.getLevelsPerBonusDamage()+" level(s), ");
				if(parms.containsKey("QUALDOMAINLIST"))
				{
					Hashtable domains=new Hashtable();
					Ability A=null;
					String domain=null;
					for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
					{
						A=(Ability)e.nextElement();
						if(CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID())>0)
						{
							if((A.classificationCode()&Ability.ALL_DOMAINS)==0)
								domain=Ability.ACODE_DESCS[A.classificationCode()];
							else
								domain=Ability.DOMAIN_DESCS[(A.classificationCode()&Ability.ALL_DOMAINS)>>5];
							Integer I=(Integer)domains.get(domain);
							if(I==null)I=new Integer(0);
							I=new Integer(I.intValue()+1);
							domains.remove(domain);
							domains.put(domain,I);
						}
					}
					Integer I=null;
					String winner=null;
					Integer winnerI=null;
					while(domains.size()>0)
					{
						winner=null;
						winnerI=null;
						for(Enumeration e=domains.keys();e.hasMoreElements();)
						{
							domain=(String)e.nextElement();
							I=(Integer)domains.get(domain);
							if((winnerI==null)||(I.intValue()>winnerI.intValue()))
							{
								winner=domain;
								winnerI=I;
							}
						}
						str.append(winner+"("+winnerI.intValue()+"), ");
						domains.remove(winner);
					}
				}
				
				if(parms.containsKey("HITPOINTS"))
					str.append("20 at first, plus (Constitution/"+C.getHPDivisor()+")+"+C.getHPDice()+"d"+C.getHPDie()+" per level thereafter, ");
				if(parms.containsKey("MANA"))
					str.append("100 plus (Intelligence/"+C.getManaDivisor()+")+"+C.getManaDice()+"d"+C.getManaDie()+" per level after first, ");
				if(parms.containsKey("MOVEMENT"))
					str.append("100 plus ((Strength/18)*"+C.getMovementMultiplier()+") per level after first, ");
				
				if(parms.containsKey("AVGHITPOINTS"))
				{
					int maxCon=18+C.maxStatAdjustments()[CharStats.STAT_CONSTITUTION];
					str.append("("+avgMath2(10,20,10,C.getHPDivisor(),C.getHPDice())+"/"+avgMath2(10,20,18,C.getHPDivisor(),C.getHPDice())+"/"+avgMath2(10,20,maxCon,C.getHPDivisor(),C.getHPDice())+") ");
					str.append("("+avgMath2(50,20,10,C.getHPDivisor(),C.getHPDice())+"/"+avgMath2(50,20,18,C.getHPDivisor(),C.getHPDice())+"/"+avgMath2(50,20,maxCon,C.getHPDivisor(),C.getHPDice())+") ");
					str.append("("+avgMath2(90,20,10,C.getHPDivisor(),C.getHPDice())+"/"+avgMath2(90,20,18,C.getHPDivisor(),C.getHPDice())+"/"+avgMath2(90,20,maxCon,C.getHPDivisor(),C.getHPDice())+") ");
				}
					
				if(parms.containsKey("AVGMANA"))
				{
					int maxInt=18+C.maxStatAdjustments()[CharStats.STAT_INTELLIGENCE];
					str.append("("+avgMath2(10,100,10,C.getManaDivisor(),C.getManaDice())+"/"+avgMath2(10,100,18,C.getManaDivisor(),C.getManaDice())+"/"+avgMath2(10,100,maxInt,C.getManaDivisor(),C.getManaDice())+") ");
					str.append("("+avgMath2(50,100,10,C.getManaDivisor(),C.getManaDice())+"/"+avgMath2(50,100,18,C.getManaDivisor(),C.getManaDice())+"/"+avgMath2(50,100,maxInt,C.getManaDivisor(),C.getManaDice())+") ");
					str.append("("+avgMath2(90,100,10,C.getManaDivisor(),C.getManaDice())+"/"+avgMath2(90,100,18,C.getManaDivisor(),C.getManaDice())+"/"+avgMath2(90,100,maxInt,C.getManaDivisor(),C.getManaDice())+") ");
				}
				if(parms.containsKey("AVGMOVEMENT"))
				{
					int ah=C.getMovementMultiplier();
					int maxStrength=18+C.maxStatAdjustments()[CharStats.STAT_STRENGTH];
					str.append("("+avgMath(10,ah,10,100)+"/"+avgMath(18,ah,10,100)+"/"+avgMath(maxStrength,ah,10,100)+") ");
					str.append("("+avgMath(10,ah,50,100)+"/"+avgMath(18,ah,50,100)+"/"+avgMath(maxStrength,ah,50,100)+") ");
					str.append("("+avgMath(10,ah,90,100)+"/"+avgMath(18,ah,90,100)+"/"+avgMath(maxStrength,ah,90,100)+") ");
				}
				
				StringBuffer preReqName=new StringBuffer(CharStats.STAT_DESCS[C.getAttackAttribute()].toLowerCase());
				preReqName.setCharAt(0,Character.toUpperCase(preReqName.charAt(0)));
				if(parms.containsKey("PRIME"))
					str.append(preReqName+", ");
				if(parms.containsKey("ATTACK"))
				{
					str.append("("+preReqName+"/6)");
					if(C.getBonusAttackLevel()>0)
						str.append("+"+C.getBonusAttackLevel());
					else
					if(C.getBonusAttackLevel()<0)
						str.append(""+C.getBonusAttackLevel());
					str.append(" per level after first, ");
				}
				if(parms.containsKey("WEAPONS"))
					if(C.weaponLimitations().length()>0)
						str.append(C.weaponLimitations()+", ");
					else
						str.append("Any, ");
				if(parms.containsKey("ARMOR"))
					if(C.armorLimitations().length()>0)
						str.append(C.armorLimitations()+", ");
					else
						str.append("Any, ");
				if(parms.containsKey("LIMITS"))
					if(C.otherLimitations().length()>0)
						str.append(C.otherLimitations()+", ");
					else
						str.append("None, ");
				if(parms.containsKey("BONUSES"))
					if(C.otherBonuses().length()>0)
						str.append(C.otherBonuses()+", ");
					else
						str.append("None, ");
				if(parms.containsKey("QUALS"))
					if(C.statQualifications().length()>0)
						str.append(C.statQualifications()+", ");
				if(parms.containsKey("STARTINGEQ"))
				{
					if(C.outfit(null)!=null)
					for(int i=0;i<C.outfit(null).size();i++)
					{
						Item I=(Item)C.outfit(null).elementAt(i);
						if(I!=null)
							str.append(I.name()+", ");
					}
				}
				if(parms.containsKey("BALANCE"))
					str.append(balanceChart(C));
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
                return clearWebMacros(strstr);
			}
		}
		return "";
	}
	
	public String balanceChart(CharClass C)
	{
		MOB M=CMClass.getMOB("StdMOB");
		M.baseEnvStats().setLevel(1);
		M.baseCharStats().setCurrentClass(C);
		M.recoverCharStats();
		C.startCharacter(M,false,false);
		HashSet seenBefore=new HashSet();
		int totalgained=0;
		int totalqualified=0;
		int uniqueClassSkills=0;
		int uniqueClassSkillsGained=0;
		int uncommonClassSkills=0;
		int uncommonClassSkillsGained=0;
		int totalCrossClassSkills=0;
		int totalCrossClassLevelDiffs=0;
		int maliciousSkills=0;
		int maliciousSkillsGained=0;
		int beneficialSkills=0;
		int beneficialSkillsGained=0;
		for(int l=1;l<=30;l++)
		{
			Vector set=CMLib.ableMapper().getLevelListings(C.ID(),true,l);
			for(int s=0;s<set.size();s++)
			{
				String able=(String)set.elementAt(s);
				if(able.equalsIgnoreCase("Skill_Recall")) continue;
				if(able.equalsIgnoreCase("Skill_Write")) continue;
				if(able.equalsIgnoreCase("Skill_Swim")) continue;
				if(CMLib.ableMapper().getQualifyingLevel("All",true,able)==l) continue;
				if(seenBefore.contains(able)) continue;
				seenBefore.add(able);
				int numOthers=0;
				int thisCrossClassLevelDiffs=0;
				for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
				{
					CharClass C2=(CharClass)c.nextElement();
					if(C2==C) continue;
					if(!CMProps.isTheme(C2.availabilityCode())) continue;
					if(C2.baseClass().equals(C.baseClass()))
					{
						int tlvl=CMLib.ableMapper().getQualifyingLevel(C2.ID(),true,able);
						if(tlvl>0)
						{
							if(tlvl>l)
								thisCrossClassLevelDiffs+=(tlvl-l);
							else
								thisCrossClassLevelDiffs+=(l-tlvl);
							numOthers++;
						}
					}
				}
				if(numOthers==0)
				{ 
					uniqueClassSkills++; 
					uncommonClassSkills++;
				}
				else
				{
					totalCrossClassLevelDiffs+=(thisCrossClassLevelDiffs/numOthers);
					totalCrossClassSkills++;
				}
				if(numOthers==1) 
					uncommonClassSkills++;
				boolean gained=(M.fetchAbility(able)!=null);
				if(gained)
				{
					totalgained++;
					if(numOthers==0){ uniqueClassSkillsGained++; uncommonClassSkillsGained++;}
					if(numOthers==1) uncommonClassSkillsGained++;
				}
				else
					totalqualified++;
				Ability A=CMClass.getAbility(able);
				if(A==null) continue;
				if((A.abstractQuality()==Ability.QUALITY_BENEFICIAL_OTHERS)
				   ||(A.abstractQuality()==Ability.QUALITY_BENEFICIAL_SELF))
				{
					beneficialSkills++;
					if(gained) beneficialSkillsGained++;
				}
				if(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
				{
					maliciousSkills++;
					if(gained) maliciousSkillsGained++;
				}
			}
			CMLib.leveler().level(M);
		}
		StringBuffer str=new StringBuffer("");
		str.append("<BR>Rule#1: Avg gained skill/level: "+CMath.div(Math.round(100.0*CMath.div(totalgained,30)),(long)100));
		str.append("<BR>Rule#2: Avg qualified skill/level: "+CMath.div(Math.round(100.0*CMath.div(totalqualified,30)),(long)100));
		str.append("<BR>Rule#4: Unique class skills gained: "+uniqueClassSkillsGained+"/"+uniqueClassSkills);
		str.append("<BR>Rule#4: Uncommon class skills gained: "+uncommonClassSkillsGained+"/"+uncommonClassSkills);
		str.append("<BR>Rule#5: Combat skills gained: "+(maliciousSkillsGained+beneficialSkillsGained)+"/"+(maliciousSkills+beneficialSkills));
		str.append("<BR>Rule#6: Avg Unique class skill/level: "+CMath.div(Math.round(100.0*CMath.div(uniqueClassSkills,30)),(long)100));
		str.append("<BR>Rule#8: Avg Cross class skill/level: "+CMath.div(Math.round(100.0*CMath.div(totalCrossClassSkills,30)),(long)100));
        M.destroy();
		return str.toString();
	}
	
	public int avgMath2(int level, int add, int stat, int divisor, int hpdice )
	{
		return add+(level*((int)Math.round(CMath.div(stat,divisor)))+(hpdice*(hpdice+1)/2));
	}
	public int avgMath(int stat, int avg, int lvl, int add)
	{
		return add+(int)Math.round(CMath.mul(CMath.div(stat,18),avg)*lvl);
	}
	
}
