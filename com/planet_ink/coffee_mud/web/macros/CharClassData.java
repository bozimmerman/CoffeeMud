package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


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
					StringBuffer s=MUDHelp.getHelpText(C.ID(),null);
					if(s==null)
						s=MUDHelp.getHelpText(C.name(),null);
					if(s!=null)
						str.append(helpHelp(s));
				}
				if(parms.containsKey("PLAYABLE"))
					str.append(C.playerSelectable()+", ");

				if(parms.containsKey("BASECLASS"))
					str.append(C.baseClass()+", ");

				if(parms.containsKey("MAXSTATS"))
					for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
						str.append(CharStats.TRAITS[i]+"("+(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)+C.maxStatAdjustments()[i])+"), ");
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
				if(parms.containsKey("HITPOINTS"))
					str.append("20 at first, plus (((Constitution/2)-4)+Random("+C.getMinHitPointsLevel()+" to "+C.getMaxHitPointsLevel()+")) per level thereafter, ");
				if(parms.containsKey("MANA"))
					str.append("100 plus ((Intelligence/18)*"+C.getBonusManaLevel()+") per level after first, ");
				if(parms.containsKey("MOVEMENT"))
					str.append("100 plus ((Strength/9)*"+C.getMovementMultiplier()+") per level after first, ");
				StringBuffer preReqName=new StringBuffer(CharStats.TRAITS[C.getAttackAttribute()].toLowerCase());
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
					if(C.outfit()!=null)
					for(int i=0;i<C.outfit().size();i++)
					{
						Item I=(Item)C.outfit().elementAt(i);
						if(I!=null)
							str.append(I.name()+", ");
					}
				}
				if(parms.containsKey("BALANCE"))
					str.append(balanceChart(C));
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return strstr;
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
			Vector set=CMAble.getLevelListings(C.ID(),true,l);
			for(int s=0;s<set.size();s++)
			{
				String able=(String)set.elementAt(s);
				if(able.equalsIgnoreCase("Skill_Recall")) continue;
				if(able.equalsIgnoreCase("Skill_Write")) continue;
				if(able.equalsIgnoreCase("Skill_Swim")) continue;
				if(CMAble.getQualifyingLevel("All",true,able)==l) continue;
				if(seenBefore.contains(able)) continue;
				seenBefore.add(able);
				int numOthers=0;
				int thisCrossClassLevelDiffs=0;
				for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
				{
					CharClass C2=(CharClass)c.nextElement();
					if(C2==C) continue;
					if(!C2.playerSelectable()) continue;
					if(C2.baseClass().equals(C.baseClass()))
					{
						int tlvl=CMAble.getQualifyingLevel(C2.ID(),true,able);
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
				if((A.quality()==Ability.BENEFICIAL_OTHERS)
				   ||(A.quality()==Ability.BENEFICIAL_SELF))
				{
					beneficialSkills++;
					if(gained) beneficialSkillsGained++;
				}
				if(A.quality()==Ability.MALICIOUS)
				{
					maliciousSkills++;
					if(gained) maliciousSkillsGained++;
				}
			}
			M.charStats().getCurrentClass().level(M);
		}
		StringBuffer str=new StringBuffer("");
		str.append("<BR>Rule#1: Avg gained skill/level: "+Util.div(Math.round(100.0*Util.div(totalgained,30)),(long)100));
		str.append("<BR>Rule#2: Avg qualified skill/level: "+Util.div(Math.round(100.0*Util.div(totalqualified,30)),(long)100));
		str.append("<BR>Rule#4: Unique class skills gained: "+uniqueClassSkillsGained+"/"+uniqueClassSkills);
		str.append("<BR>Rule#4: Uncommon class skills gained: "+uncommonClassSkillsGained+"/"+uncommonClassSkills);
		str.append("<BR>Rule#5: Combat skills gained: "+(maliciousSkillsGained+beneficialSkillsGained)+"/"+(maliciousSkills+beneficialSkills));
		str.append("<BR>Rule#6: Avg Unique class skill/level: "+Util.div(Math.round(100.0*Util.div(uniqueClassSkills,30)),(long)100));
		str.append("<BR>Rule#8: Avg Cross class skill/level: "+Util.div(Math.round(100.0*Util.div(totalCrossClassSkills,30)),(long)100));
		return str.toString();
	}
}
