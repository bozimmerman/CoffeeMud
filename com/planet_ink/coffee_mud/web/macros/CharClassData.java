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
					StringBuffer s=ExternalPlay.getHelpText(C.ID());
					if(s==null)
						s=ExternalPlay.getHelpText(C.name());
					if(s!=null)
						str.append(helpHelp(s));
				}
				if(parms.containsKey("PLAYABLE"))
					str.append(C.playerSelectable()+", ");

				if(parms.containsKey("BASECLASS"))
					str.append(C.baseClass()+", ");

				if(parms.containsKey("MAXSTATS"))
					for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
						str.append(CharStats.TRAITS[i]+"("+C.maxStat()[i]+"), ");
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
					MOB mob=CMClass.getMOB("StdMOB");
					C.outfit(mob);
					for(int i=0;i<mob.inventorySize();i++)
					{
						Item I=mob.fetchInventory(i);
						if(I!=null)
							str.append(I.name()+", ");
					}
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return strstr;
			}
		}
		return "";
	}
}
