package com.planet_ink.coffee_mud.WebMacros.grinder;
import com.planet_ink.coffee_mud.WebMacros.AreaData;
import com.planet_ink.coffee_mud.WebMacros.ExitData;
import com.planet_ink.coffee_mud.WebMacros.MobData;
import com.planet_ink.coffee_mud.WebMacros.RoomData;
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
public class GrinderPlayers extends GrinderMobs
{
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
        "ACCOUNT"
	};
	
	public static int getBasicCode(String val)
	{
		for(int i=0;i<BASICS.length;i++)
			if(val.equalsIgnoreCase(BASICS[i]))
				return i;
		return -1;
	}

	public static String titleList(MOB E, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		if(E.playerStats()==null) return "";
		E.playerStats().getTitles().clear();
		if(httpReq.isRequestParameter("TITLE0"))
		{
			int num=0;
			while(httpReq.isRequestParameter("TITLE"+num))
			{
				String aff=httpReq.getRequestParameter("TITLE"+num);
				if(aff.trim().length()>0) E.playerStats().getTitles().addElement(aff.trim());
				num++;
			}
		}
		return "";
	}
	public static String setBasics(ExternalHTTPRequests httpReq,MOB M)
	{
		for(int i=0;i<BASICS.length;i++)
		if(httpReq.isRequestParameter(BASICS[i]))
		{
			String old=httpReq.getRequestParameter(BASICS[i]);
			if(old==null) old="";
			switch(i)
			{
			case 0: break; // dont set name!
			case 1: M.setDescription(old); break;
			case 2: if(M.playerStats()!=null) M.playerStats().setLastDateTime(CMLib.time().string2Millis(old)); break;
			case 3: if(M.playerStats()!=null) M.playerStats().setEmail(old); break;
			case 4: M.baseCharStats().setMyRace(CMClass.getRace(old)); break;
			case 5: break; // dont set class/levels list through this.
			case 6: M.baseEnvStats().setLevel(CMath.s_int(old)); break;
			case 7: break; // dont set levelstr
			case 8: break; // dont set classlevelstr
			case 9: break; // dont set classlist through this
			case 10: break; // cant set maxcarry
			case 11: M.baseEnvStats().setAttackAdjustment(CMath.s_int(old)); break;
			case 12: M.baseEnvStats().setArmor(CMath.s_int(old)); break;
			case 13: M.baseEnvStats().setDamage(CMath.s_int(old)); break;
			case 14: M.setAgeHours(CMath.s_long(old)); break;
			case 15: M.setPractices(CMath.s_int(old)); break;
			case 16: M.setExperience(CMath.s_int(old)); break;
			case 17: break; // dont set exp/level
			case 18: M.setTrains(CMath.s_int(old)); break;
			case 19: CMLib.beanCounter().setMoney(M,CMath.s_int(old)); break;
			case 20: if(CMLib.map().getDeity(old)!=null) M.setWorshipCharID(old); break;
			case 21: if(CMLib.players().getPlayer(old)!=null) M.setLiegeID(old); break;
			case 22: if(CMLib.clans().getClan(old)!=null) M.setClanID(old); break;
			case 23: M.setClanRole(CMath.s_int(old)); break;
			case 24: CMLib.factions().setAlignment(M,CMath.s_int(old));break;
			case 25: CMLib.factions().setAlignment(M,CMath.s_int(old));break;
			case 26: M.setWimpHitPoint(CMath.s_int(old)); break;
			case 27: if(CMLib.map().getRoom(old)!=null) M.setStartRoom(CMLib.map().getRoom(old)); break;
			case 28: if(CMLib.map().getRoom(old)!=null) M.setLocation(CMLib.map().getRoom(old)); break;
			case 29: if(CMLib.map().getRoom(old)!=null) M.setStartRoom(CMLib.map().getRoom(old)); break;
			case 30: if(CMLib.map().getRoom(old)!=null) M.setLocation(CMLib.map().getRoom(old)); break;
			case 31: break; // dont set inv list here
			case 32: M.baseEnvStats().setWeight(CMath.s_int(old)); break;
			case 33: M.envStats().setWeight(CMath.s_int(old)); break;
			case 34: if(old.length()==1) M.baseCharStats().setStat(CharStats.STAT_GENDER,old.toUpperCase().charAt(0)); break;
			case 35: if(M.playerStats()!=null) M.playerStats().setLastDateTime(CMath.s_long(old)); break;
			case 36: M.curState().setHitPoints(CMath.s_int(old)); break;
			case 37: M.curState().setMana(CMath.s_int(old)); break;
			case 38: M.curState().setMovement(CMath.s_int(old)); break;
			case 39: break; // dont set riding here
			case 40: M.baseEnvStats().setHeight(CMath.s_int(old)); break;
			case 41: if(M.playerStats()!=null) M.playerStats().setLastIP(old); break;
			case 42: M.setQuestPoint(CMath.s_int(old)); break;
			case 43: M.baseState().setHitPoints(CMath.s_int(old)); break;
			case 44: M.baseState().setMana(CMath.s_int(old)); break;
			case 45: M.baseState().setMovement(CMath.s_int(old)); break;
			case 46: break; // dont set rawimage here
	        case 47: break; // dont set maxitems here?!
	        case 48: break; // dont set image here
	        case 49: break; // dont set imagepath here
			case 50: if(M.playerStats()!=null) 	M.playerStats().setNotes(old); break;
			case 51: break; // dont set level chart
			case 52: M.baseEnvStats().setAttackAdjustment(CMath.s_int(old)); break;
			case 53: M.baseEnvStats().setDamage(CMath.s_int(old)); break;
			case 54: M.baseEnvStats().setArmor(CMath.s_int(old)); break;
			case 55: M.envStats().setSpeed(CMath.s_double(old)); break;
			case 56: M.baseEnvStats().setSpeed(CMath.s_double(old)); break;
			case 57: 
			{
				Vector V=CMParms.parseCommas(old.toUpperCase(),true);
				while(M.numExpertises()>0) M.delExpertise(M.fetchExpertise(0));
				for(int v=0;v<V.size();v++)
					if(CMLib.expertises().getDefinition((String)V.elementAt(v))!=null)
						M.addExpertise((String)V.elementAt(v));
				break;
			}
			case 58: 
			{
				Vector V=CMParms.parseCommas(old.toUpperCase(),true);
				while(M.numTattoos()>0) M.delTattoo(M.fetchTattoo(0));
				for(int v=0;v<V.size();v++)
					M.addTattoo((String)V.elementAt(v));
				break;
			}
			case 59: 
			{
				if(M.playerStats()!=null)
				{
					Vector V=CMParms.parseCommas(old.toUpperCase(),true);
					M.playerStats().getSecurityGroups().clear();
					CMParms.addToVector(V,M.playerStats().getSecurityGroups());
				}
				break;
			}
			case 60: break; // CAN'T do titles here!!
			case 61: break; // dont do faction lists here
			case 62: break; // dont do accountexpiration flag here.
			case 63: if(M.playerStats()!=null)
						M.playerStats().setAccountExpiration(CMLib.time().string2Millis(old));
					 break;
			case 64: 
			{
				if(M.playerStats()!=null)
				{
					String oldAccountName =(M.playerStats().getAccount()!=null) ? M.playerStats().getAccount().accountName() : ""; 
		            if(!old.equals(oldAccountName))
		            {
		            	PlayerAccount newAccount = CMLib.players().getLoadAccount(old);
		            	if(newAccount != null)
		            	{
			            	M.playerStats().setAccount(newAccount);
			            	newAccount.addNewPlayer(M);
			            	PlayerAccount oldAccount = (oldAccountName.length()>0)?CMLib.players().getLoadAccount(oldAccountName):null;
			            	if(oldAccount!=null)
			            	{
			            		oldAccount.delPlayer(M);
			            		CMLib.database().DBUpdateAccount(oldAccount);
			            	}
		            	}
		            }
				}
				break;
			}
			}
		}
		if(M.playerStats()!=null)
		{
			int b=0;
			M.playerStats().getTitles().clear();
			while(httpReq.isRequestParameter("TITLE"+b))
			{
				String old=httpReq.getRequestParameter("TITLE"+b);
				if(old==null) old="";
				M.playerStats().getTitles().addElement(old);
				b++;
			}
		}
		return "";
	}

	public static String classList(MOB M, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		if(httpReq.isRequestParameter("CHARCLASS1"))
		{
			StringBuffer classList=new StringBuffer("");
			StringBuffer levelsList=new StringBuffer("");
			int num=1;
			String aff=httpReq.getRequestParameter("CHARCLASS"+num);
			int totalLevel=0;
			while(aff!=null)
			{
				if(aff.length()>0)
				{
					CharClass C=CMClass.getCharClass(aff);
					if(C==null) return "Unknown class '"+aff+"'.";
					classList.append(C.ID()+";");
					String lvl=httpReq.getRequestParameter("CHARCLASSLVL"+num);
					if(lvl==null)lvl="0";
					totalLevel+=CMath.s_int(lvl);
					levelsList.append(lvl+";");
				}
				num++;
				aff=httpReq.getRequestParameter("CHARCLASS"+num);
			}
			M.baseCharStats().setMyClasses(classList.toString());
			M.baseCharStats().setMyLevels(levelsList.toString());
			M.baseEnvStats().setLevel(totalLevel);
		}
		return "";
	}
	
	public static String editPlayer(MOB whom, ExternalHTTPRequests httpReq, Hashtable parms, MOB M)
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		Vector allitems=new Vector();
		while(M.inventorySize()>0)
        {
            Item I=M.fetchInventory(0);
			allitems.addElement(I);
            M.delInventory(I);
        }
		
		for(int i=0;i<MOB.AUTODESC.length;i++)
		{
			if(httpReq.isRequestParameter(MOB.AUTODESC[i]))
			{
				String old=httpReq.getRequestParameter(MOB.AUTODESC[i]);
				if(old==null) old="";
				if(old.equalsIgnoreCase("on"))
					M.setBitmap((int)(M.getBitmap()|CMath.pow(2,i)));
				else
					M.setBitmap((int)CMath.unsetb(M.getBitmap(),CMath.pow(2,i)));
			}
		}
		for(int i : CharStats.CODES.ALL())
		{
			CharStats C=M.charStats();
			String stat=CharStats.CODES.NAME(i);
			if(httpReq.isRequestParameter(stat))
			{
				String old=httpReq.getRequestParameter(stat);
				if(old==null) old="";
				if(!stat.equalsIgnoreCase("GENDER"))
					C.setStat(i,CMath.s_int(old));
				else
				if(old.length()>0)
					C.setStat(i,(int)old.charAt(0));
			}
		}
		for(int i : CharStats.CODES.ALL())
		{
			CharStats C=M.baseCharStats();
			String stat=CharStats.CODES.NAME(i);
			if(httpReq.isRequestParameter("BASE"+stat))
			{
				String old=httpReq.getRequestParameter("BASE"+stat);
				if(old==null) old="";
				if(!stat.equalsIgnoreCase("GENDER"))
					C.setStat(i,CMath.s_int(old));
				else
				if(old.length()>0)
					C.setStat(i,(int)old.charAt(0));
			}
		}
		GrinderPlayers.setBasics(httpReq,M);
		if(httpReq.isRequestParameter("RACE"))
		{
			String old=httpReq.getRequestParameter("RACE");
			if((old!=null)&&(CMClass.getRace(old)!=null))
				M.baseCharStats().setMyRace(CMClass.getRace(old));
		}
		if(httpReq.isRequestParameter("DEITY"))
		{
			String old=httpReq.getRequestParameter("DEITY");
			if((old!=null)&&(CMLib.map().getDeity(old)!=null))
				M.setWorshipCharID(CMLib.map().getDeity(old).Name());
		}
		if(httpReq.isRequestParameter("CLAN"))
		{
			String old=httpReq.getRequestParameter("CLAN");
			if((old!=null)&&(CMLib.clans().getClan(old)!=null))
				M.setClanID(CMLib.clans().getClan(old).clanID());
		}
		if(httpReq.isRequestParameter("ALIGNMENT"))
		{
			String old=httpReq.getRequestParameter("ALIGNMENT");
			Faction F=CMLib.factions().getFaction(CMLib.factions().AlignID());
		    if((F!=null)&&(old!=null)&&(old.length()>0))
				for(int v=1;v<Faction.ALIGN_NAMES.length;v++)
					if(Faction.ALIGN_NAMES[v].equalsIgnoreCase(old))
						CMLib.factions().setAlignment(M,v);
		}
		String error=GrinderExits.dispositions(M,httpReq,parms);
		if(error.length()>0) return error;
		error=GrinderMobs.senses(M,httpReq,parms);
		if(error.length()>0) return error;
		error=titleList(M,httpReq,parms);
		if(error.length()>0) return error;
		error=GrinderAreas.doAffectsNBehavs(M,httpReq,parms);
		if(error.length()>0) return error;
		error=GrinderMobs.factions(M,httpReq,parms);
		if(error.length()>0) return error;
		error=GrinderMobs.abilities(M,httpReq,parms);
		if(error.length()>0) return error;
		error=GrinderMobs.items(M,allitems,httpReq);
		if(error.length()>0) return error;
		error=GrinderMobs.expertiseList(M,httpReq,parms);
		if(error.length()>0) return error;
		error=classList(M,httpReq,parms);
		if(error.length()>0) return error;
		M.recoverEnvStats();
		M.recoverCharStats();
		M.recoverMaxState();
		M.recoverEnvStats();
		M.recoverCharStats();
		M.recoverMaxState();
		if(M.location()!=null)
			M.location().recoverRoomStats();
		CMLib.database().DBUpdatePlayer(M);
		Log.sysOut("Grinder",whom.Name()+" modified player "+M.Name());
		return "";
	}

}
