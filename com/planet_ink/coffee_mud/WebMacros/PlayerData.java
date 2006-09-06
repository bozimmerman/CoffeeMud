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
public class PlayerData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public final static String[] BASICS={
		"NAME",
		"DESCRIPTION",
		"LASTDATETIME",
		"EMAIL",
		"RACENAME",
		"CHARCLASS",
		"LEVEL",
		"LEVELSTR",
		"CLASSLEVEL",
		"CLASSES",
		"MAXCARRY",
		"ATTACK",
		"ARMOR",
		"DAMAGE",
		"HOURS",
		"PRACTICES",
		"EXPERIENCE",
		"EXPERIENCELEVEL",
		"TRAINS",
		"MONEY",
		"DEITY",
		"LIEGE",
		"CLAN",
		"CLANROLE",
		"ALIGNMENTNAME",
		"ALIGNMENTSTRING",
		"WIMP",
		"STARTROOM",
		"LOCATION",
		"STARTROOMID",
		"LOCATIONID",
		"INVENTORY",
		"WEIGHT",
		"ENCUMBRANCE",
		"GENDER",
		"LASTDATETIMEMILLIS",
		"HITPOINTS",
		"MANA",
		"MOVEMENT",
		"RIDING",
		"HEIGHT",
		"LASTIP",
		"QUESTPOINTS",
		"BASEHITPOINTS",
		"BASEMANA",
		"BASEMOVEMENT",
		"IMAGE",
        "MAXITEMS",
        "IMGURL",
        "HASIMG",
        "NOTES",
        "LEVELS"
	};

	public static int getBasicCode(String val)
	{
		for(int i=0;i<BASICS.length;i++)
			if(val.equalsIgnoreCase(BASICS[i]))
				return i;
		return -1;
	}

	public static String getBasic(MOB M, int i)
	{
		StringBuffer str=new StringBuffer("");
		switch(i)
		{
		case 0: str.append(M.Name()+", "); break;
		case 1: str.append(M.description()+", "); break;
		case 2:  if(M.playerStats()!=null)
					str.append(CMLib.time().date2String(M.playerStats().lastDateTime())+", ");
				 break;
		case 3: if(M.playerStats()!=null)
					str.append(M.playerStats().getEmail()+", ");
				break;
		case 4: str.append(M.baseCharStats().getMyRace().name()+", "); break;
		case 5: str.append(M.baseCharStats().getCurrentClass().name(M.baseCharStats().getCurrentClassLevel())+", "); break;
		case 6: str.append(M.baseEnvStats().level()+", "); break;
		case 7: str.append(M.baseCharStats().displayClassLevel(M,true)+", "); break;
		case 8: str.append(M.baseCharStats().getClassLevel(M.baseCharStats().getCurrentClass())+", "); break;
		case 9: for(int c=M.charStats().numClasses()-1;c>=0;c--)
				{
					CharClass C=M.charStats().getMyClass(c);
					str.append(C.name(M.baseCharStats().getCurrentClassLevel())+" ("+M.charStats().getClassLevel(C)+") ");
				}
				str.append(", ");
				break;
		case 10: if(M.maxCarry()==Integer.MAX_VALUE) str.append("N/A, "); else str.append(M.maxCarry()+", "); break;
		case 11: str.append(CMStrings.capitalizeAndLower(CMLib.combat().fightingProwessStr(M.adjustedAttackBonus(null)))+", "); break;
		case 12: str.append(CMStrings.capitalizeAndLower(CMLib.combat().armorStr((-M.adjustedArmor())+50))+", "); break;
		case 13: str.append(M.adjustedDamage(null,null)+", "); break;
		case 14: str.append(Math.round(CMath.div(M.getAgeHours(),60.0))+", "); break;
		case 15: str.append(M.getPractices()+", "); break;
		case 16: str.append(M.getExperience()+", "); break;
		case 17: if(M.getExpNeededLevel()==Integer.MAX_VALUE)
					str.append("N/A, ");
				 else
					str.append(M.getExpNextLevel()+", ");
				 break;
		case 18: str.append(M.getTrains()+", "); break;
		case 19: str.append(CMLib.beanCounter().getMoney(M)+", "); break;
		case 20: str.append(M.getWorshipCharID()+", "); break;
		case 21: str.append(M.getLiegeID()+", "); break;
		case 22: str.append(M.getClanID()+", "); break;
		case 23: if(M.getClanID().length()>0)
				 {
					 Clan C=CMLib.clans().getClan(M.getClanID());
					 if(C!=null)
						str.append(CMLib.clans().getRoleName(C.getGovernment(),M.getClanRole(),true,false)+", ");
				 }
				 break;
		case 24: str.append(M.fetchFaction(CMLib.factions().AlignID())+", ");
				 break;
		case 25: {
		    		Faction.FactionRange FR=CMLib.factions().getRange(CMLib.factions().AlignID(),M.fetchFaction(CMLib.factions().AlignID()));
		    		if(FR!=null)
		    		    str.append(FR.name()+", ");
		    		else
		    		    str.append(M.fetchFaction(CMLib.factions().AlignID()));
				 break;
				}
		case 26: str.append(M.getWimpHitPoint()+", "); break;
		case 27: if(M.getStartRoom()!=null)
					 str.append(M.getStartRoom().displayText()+", ");
				 break;
		case 28: if(M.location()!=null)
					 str.append(M.location().displayText()+", ");
				 break;
		case 29: if(M.getStartRoom()!=null)
					 str.append(M.getStartRoom().roomID()+", ");
				 break;
		case 30: if(M.location()!=null)
					 str.append(M.location().roomID()+", ");
				 break;
		case 31:
				for(int inv=0;inv<M.inventorySize();inv++)
				{
					Item I=M.fetchInventory(inv);
					if((I!=null)&&(I.container()==null))
						  str.append(I.name()+", ");
				}
			break;
		case 32: str.append(M.baseEnvStats().weight()+", "); break;
		case 33: str.append(M.envStats().weight()+", "); break;
		case 34: str.append(CMStrings.capitalizeAndLower(M.baseCharStats().genderName())+", "); break;
		case 35: if(M.playerStats()!=null)
					 str.append(M.playerStats().lastDateTime()+", ");
				 break;
		case 36: str.append(M.curState().getHitPoints()+", "); break;
		case 37: str.append(M.curState().getMana()+", "); break;
		case 38: str.append(M.curState().getMovement()+", "); break;
		case 39: if(M.riding()!=null)
					 str.append(M.riding().name()+", ");
				 break;
		case 40: str.append(M.baseEnvStats().height()+", "); break;
		case 41: if(!M.isMonster())
					 str.append(M.session().getAddress()+", ");
				 else
				 if(M.playerStats()!=null)
					 str.append(M.playerStats().lastIP()+", ");
				 break;
		case 42:  str.append(M.getQuestPoint()+", "); break;
		case 43: str.append(M.maxState().getHitPoints()+", "); break;
		case 44: str.append(M.maxState().getMana()+", "); break;
		case 45: str.append(M.maxState().getMovement()+", "); break;
		case 46: str.append(M.rawImage()+", "); break;
        case 47: str.append(M.maxItems()+", "); break;
        case 48:
        {
                 String image=M.image();
                 if(image.length()>0)
                     str.append(CMProps.mxpImagePath(image)+image+", ");
                 break;
        }
        case 49: if(CMProps.mxpImagePath(M.image()).length()>0)
                    str.append("true, ");
                 else
                     str.append("false, ");
                 break;
		case 50: if(M.playerStats()!=null)
				 	str.append(M.playerStats().notes()+", ");
				 break;
		case 51: if(M.playerStats()!=null)
				 {
					long lastDateTime=-1;
					for(int level=0;level<=M.envStats().level();level++)
					{
						long dateTime=M.playerStats().leveledDateTime(level);
						if((dateTime>1529122205)&&(dateTime!=lastDateTime))
						{
							str.append("<TR>");
							if(level==0)
							 	str.append("<TD><FONT COLOR=WHITE>Created</FONT></TD>");
							else
							 	str.append("<TD><FONT COLOR=WHITE>"+level+"</FONT></TD>");
							str.append("<TD><FONT COLOR=WHITE>"+CMLib.time().date2String(dateTime)+"</FONT></TD></TR>");
						}
					}
					str.append(", ");
				 }
				 break;
		}
		return str.toString();
	}

	public static String getBasic(MOB M, String val)
	{
		for(int i=0;i<BASICS.length;i++)
		{
			if(val.equalsIgnoreCase(BASICS[i]))
				return getBasic(M,i);
		}
		return "";
	}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("PLAYER");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			MOB M=CMLib.map().getLoadPlayer(last);
			if(M==null) return " @break@";

			boolean firstTime=(!httpReq.isRequestParameter("ACTION"))
							||(!(httpReq.getRequestParameter("ACTION")).equals("MODIFYMOB"));
			StringBuffer str=new StringBuffer("");
			for(int i=0;i<MOB.AUTODESC.length;i++)
			{
				if(parms.containsKey(MOB.AUTODESC[i]))
				{
					boolean set=CMath.isSet(M.getBitmap(),i);
					if(MOB.AUTOREV[i]) set=!set;
					str.append((set?"ON":"OFF")+",");
				}
			}
			for(int i=0;i<CharStats.STAT_NAMES.length;i++)
			{
				String stat=CharStats.STAT_NAMES[i];
				if(!stat.equalsIgnoreCase("GENDER"))
				{
					CharStats C=M.charStats();
					if(parms.containsKey("BASE"+stat))
					{
						stat=stat.substring(4);
						C=M.baseCharStats();
					}
					if(parms.containsKey(stat))
					{
						if(i>CharStats.NUM_BASE_STATS)
							str.append(C.getSave(i)+", ");
						else
							str.append(C.getStat(i)+", ");
					}
				}
			}
			for(int i=0;i<BASICS.length;i++)
			{
				if(parms.containsKey(BASICS[i]))
					str.append(getBasic(M,i));
			}
			if(parms.containsKey("RACE"))
			{
				String old=httpReq.getRequestParameter("RACE");
				if((firstTime)||(old.length()==0)) 
					old=""+M.baseCharStats().getMyRace().ID();
				for(Enumeration r=CMClass.races();r.hasMoreElements();)
				{
					Race R2=(Race)r.nextElement();
					str.append("<OPTION VALUE=\""+R2.ID()+"\"");
					if(R2.ID().equals(old))
						str.append(" SELECTED");
					str.append(">"+R2.name());
				}
			}
			if(parms.containsKey("ALIGNMENT"))
			{
				String old=httpReq.getRequestParameter("ALIGNMENT");
			    if(CMLib.factions().getFaction(CMLib.factions().AlignID())!=null)
			    {
					if(firstTime) old=""+M.fetchFaction(CMLib.factions().AlignID());
					for(int v=1;v<Faction.ALIGN_NAMES.length;v++)
					{
					    str.append("<OPTION VALUE="+Faction.ALIGN_NAMES[v]);
					    if(old.equalsIgnoreCase(Faction.ALIGN_NAMES[v]))
					        str.append(" SELECTED");
					    str.append(">"+CMStrings.capitalizeAndLower(Faction.ALIGN_NAMES[v].toLowerCase()));
					}
			    }
			}
			str.append(MobData.itemList(M,httpReq,parms));
			str.append(MobData.abilities(M,httpReq,parms));
			str.append(MobData.factions(M,httpReq,parms));
			str.append(AreaData.affectsNBehaves(M,httpReq,parms));
			str.append(ExitData.dispositions(M,firstTime,httpReq,parms));
			str.append(MobData.senses(M,firstTime,httpReq,parms));
			String strstr=str.toString();
			if(strstr.endsWith(", "))
				strstr=strstr.substring(0,strstr.length()-2);
            return clearWebMacros(strstr);
		}
		return "";
	}
}
