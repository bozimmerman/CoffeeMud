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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
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
		"ATTACKNAME",
		"ARMORNAME",
		"DAMAGENAME",
		"HOURS",
		"PRACTICES",
		"EXPERIENCE",
		"EXPERIENCELEVEL",
		"TRAINS",
		"MONEY",
		"DEITYNAME",
		"LIEGE",
		"CLANNAME",
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
		"GENDERNAME",
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
        "LEVELS",
        "ATTACK",
        "DAMAGE",
        "ARMOR",
        "SPEEDNAME",
        "SPEED",
        "EXPERTISE",
        "TATTOOS",
        "SECURITY",
        "TITLES",
        "FACTIONNAMES",
        "ACCTEXPUSED",
        "ACCTEXP",
        "FOLLOWERNAMES",
        "ACCOUNT"
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
		case 9: 
		{
				for(int c=M.charStats().numClasses()-1;c>=0;c--)
				{
					CharClass C=M.charStats().getMyClass(c);
					str.append(C.name(M.baseCharStats().getCurrentClassLevel())+" ("+M.charStats().getClassLevel(C)+") ");
				}
				str.append(", ");
				break;
		}
		case 10: if(M.maxCarry()>(Integer.MAX_VALUE/3)) str.append("NA, "); else str.append(M.maxCarry()+", "); break;
		case 11: str.append(CMStrings.capitalizeAndLower(CMLib.combat().fightingProwessStr(M))+", "); break;
		case 12: str.append(CMStrings.capitalizeAndLower(CMLib.combat().armorStr(M))+", "); break;
		case 13: str.append(CMLib.combat().adjustedDamage(M,null,null)+", "); break;
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
		{
				for(int inv=0;inv<M.inventorySize();inv++)
				{
					Item I=M.fetchInventory(inv);
					if((I!=null)&&(I.container()==null))
						  str.append(I.name()+", ");
				}
				break;
		}
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
                 String[] paths=CMProps.mxpImagePath(M.image());
                 if(paths[0].length()>0)
                     str.append(paths[0]+paths[1]+", ");
                 break;
        }
        case 49: if(CMProps.mxpImagePath(M.image())[0].length()>0)
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
		case 52: str.append(M.baseEnvStats().attackAdjustment()+", "); break;
		case 53: str.append(M.baseEnvStats().damage()+", "); break;
		case 54: str.append(M.baseEnvStats().armor()+", "); break;
		case 55: str.append(M.envStats().speed()+", "); break;
		case 56: str.append(M.baseEnvStats().speed()+", "); break;
		case 57: 
		{
			for(int e=0;e<M.numExpertises();e++)
			{
				String E=M.fetchExpertise(e);
				ExpertiseLibrary.ExpertiseDefinition X=CMLib.expertises().getDefinition(E);
				if(X==null)
					str.append(E+", ");
				else
					str.append(X.name+", ");
			}
			break;
		}
		case 58: 
		{
			for(int t=0;t<M.numTattoos();t++)
			{
				String E=M.fetchTattoo(t);
				  str.append(E+", ");
			}
			break;
		}
		case 59: 
		{
			if(M.playerStats()!=null)
				for(int b=0;b<M.playerStats().getSecurityGroups().size();b++)
				{
					String B=(String)M.playerStats().getSecurityGroups().elementAt(b);
					if(B!=null)	str.append(B+", ");
				}
				break;
		}
		case 60: 
		{
			if(M.playerStats()!=null)
				for(int b=0;b<M.playerStats().getTitles().size();b++)
				{
					String B=(String)M.playerStats().getTitles().elementAt(b);
					if(B!=null)	str.append(B+", ");
				}
				break;
		}
		case 61: 
		{
			for(Enumeration e=M.fetchFactions();e.hasMoreElements();)
			{
				String FID=(String)e.nextElement();
				Faction F=CMLib.factions().getFaction(FID);
				int value=M.fetchFaction(FID);
				if(F!=null)	str.append(F.name()+" ("+value+"), ");
			}
			break;
		}
		case 62: str.append(CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION)?"true":"false"); break;
		case 63: if(M.playerStats()!=null)str.append(CMLib.time().date2String(M.playerStats().getAccountExpiration()));
					break;
		case 64: {
		    for(int f=0;f<M.numFollowers();f++)
	            str.append(M.fetchFollower(f).name()).append(", ");
		    //Vector V=CMLib.database().DBScanFollowers(M);
		    //for(int v=0;v<V.size();v++)
		    //    str.append(((MOB)V.elementAt(v)).name()).append(", ");
		    break;
		}
		case 65:
			if((M.playerStats()!=null)&&(M.playerStats().getAccount()!=null))
				str.append(M.playerStats().getAccount().accountName());
			break;
		}
		return str.toString();
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
			MOB M=CMLib.players().getLoadPlayer(last);
			if(M==null)
			{
				MOB authM=Authenticate.getAuthenticatedMob(httpReq);
				if((authM!=null)&&(authM.Name().equalsIgnoreCase(last)))
					M=authM;
				else
					return " @break@";
			}

			boolean firstTime=(!httpReq.isRequestParameter("ACTION"))
							||(httpReq.getRequestParameter("ACTION")).equals("FIRSTTIME");
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
			for(int i : CharStats.CODES.ALL())
			{
				String stat=CharStats.CODES.NAME(i);
				if(!stat.equalsIgnoreCase("GENDER"))
				{
					CharStats C=M.charStats();
					if(parms.containsKey(stat))
					{
						String old=httpReq.getRequestParameter(stat);
						if((firstTime)||(old.length()==0)) 
						{
							if((!CharStats.CODES.isBASE(i))&&(i!=CharStats.STAT_GENDER))
								old=""+C.getSave(i);
							else
								old=""+C.getStat(i);
						}
						str.append(old+", ");
					}
				}
			}
			for(int i : CharStats.CODES.ALL())
			{
				String stat=CharStats.CODES.NAME(i);
				if(!stat.equalsIgnoreCase("GENDER"))
				{
					CharStats C=M.baseCharStats();
					if(parms.containsKey("BASE"+stat))
					{
						String old=httpReq.getRequestParameter("BASE"+stat);
						if((firstTime)||(old.length()==0)) 
							old=""+C.getStat(i);
						str.append(old+", ");
					}
				}
			}
			for(int i=0;i<BASICS.length;i++)
			{
				if(parms.containsKey(BASICS[i]))
				{
					if(httpReq.isRequestParameter(BASICS[i]))
						str.append(httpReq.getRequestParameter(BASICS[i])+", ");
					else
						str.append(getBasic(M,i));
				}
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
			if(parms.containsKey("DEITY"))
			{
				String old=httpReq.getRequestParameter("DEITY");
				if(firstTime) old=M.getWorshipCharID();
				str.append("<OPTION "+((old.length()==0)?"SELECTED":"")+" VALUE=\"\">Godless");
				for(Enumeration e=CMLib.map().deities();e.hasMoreElements();)
				{
					Deity E=(Deity)e.nextElement();
					str.append("<OPTION VALUE=\""+E.Name()+"\"");
					if(E.Name().equalsIgnoreCase(old))
						str.append(" SELECTED");
					str.append(">"+E.Name());
				}
			}
			if(parms.containsKey("TITLELIST"))
			{
				if(M.playerStats()!=null)
				{
					int b=0;
					Vector titles=new Vector();
					if(firstTime) CMParms.addToVector(M.playerStats().getTitles(),titles);
					else
					while(httpReq.isRequestParameter("TITLE"+b))
					{
						String B=httpReq.getRequestParameter("TITLE"+b);
						if((B!=null)&&(B.trim().length()>0)) titles.addElement(B);
						b++;
					}
					for(b=0;b<titles.size();b++)
					{
						String B=(String)titles.elementAt(b);
						if(B!=null)	str.append("<INPUT TYPE=TEXT NAME=TITLE"+b+" SIZE="+B.length()+" VALUE=\""+CMStrings.replaceAll(B,"\"","&quot;")+"\"><BR>");
					}
					str.append("<INPUT TYPE=TEXT NAME=TITLE"+titles.size()+" SIZE=60 VALUE=\"\">");
				}
			}
			if(parms.containsKey("CLAN"))
			{
				String old=httpReq.getRequestParameter("CLAN");
				if(firstTime) old=M.getClanID();
				str.append("<OPTION "+((old.length()==0)?"SELECTED":"")+" VALUE=\"\">Clanless");
				for(Enumeration e=CMLib.clans().allClans();e.hasMoreElements();)
				{
					Clan C=(Clan)e.nextElement();
					str.append("<OPTION VALUE=\""+C.clanID()+"\"");
					if(C.clanID().equalsIgnoreCase(old))
						str.append(" SELECTED");
					str.append(">"+C.getName());
				}
			}
			if(parms.containsKey("ALIGNMENT"))
			{
				String old=httpReq.getRequestParameter("ALIGNMENT");
				if((firstTime)||(old.length()==0)) 
					old=""+M.fetchFaction(CMLib.factions().AlignID());
			    if(CMLib.factions().getFaction(CMLib.factions().AlignID())!=null)
			    {
					for(int v=1;v<Faction.ALIGN_NAMES.length;v++)
					{
					    str.append("<OPTION VALUE="+Faction.ALIGN_NAMES[v]);
					    if(old.equalsIgnoreCase(Faction.ALIGN_NAMES[v]))
					        str.append(" SELECTED");
					    str.append(">"+CMStrings.capitalizeAndLower(Faction.ALIGN_NAMES[v].toLowerCase()));
					}
			    }
			}
			if(parms.containsKey("BASEGENDER"))
			{
				String old=httpReq.getRequestParameter("BASEGENDER");
				if(firstTime) old=""+M.baseCharStats().getStat(CharStats.STAT_GENDER);
			    str.append("<OPTION VALUE=M "+((old.equalsIgnoreCase("M"))?"SELECTED":"")+">M");
			    str.append("<OPTION VALUE=F "+((old.equalsIgnoreCase("F"))?"SELECTED":"")+">F");
			    str.append("<OPTION VALUE=N "+((old.equalsIgnoreCase("N"))?"SELECTED":"")+">N");
			}
			str.append(MobData.expertiseList(M,httpReq,parms));
			str.append(MobData.classList(M,httpReq,parms));
			str.append(MobData.itemList(M,M,httpReq,parms,0));
			str.append(MobData.abilities(M,httpReq,parms,0));
			str.append(MobData.factions(M,httpReq,parms,0));
			str.append(AreaData.affectsNBehaves(M,httpReq,parms,0));
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
