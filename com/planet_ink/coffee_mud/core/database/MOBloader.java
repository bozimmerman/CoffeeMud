package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.sql.*;
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
public class MOBloader
{
	protected static Room emptyRoom=null;
	public static boolean DBReadUserOnly(MOB mob)
	{
		if(mob.Name().length()==0) return false;
		boolean found=false;
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+mob.Name()+"'");
			while(R.next())
			{
				CharStats stats=mob.baseCharStats();
				CharState state=mob.baseState();
				PlayerStats pstats=(PlayerStats)CMClass.getCommon("DefaultPlayerStats");
				mob.setPlayerStats(pstats);
				String username=DBConnections.getRes(R,"CMUSERID");
				String password=DBConnections.getRes(R,"CMPASS");
				mob.setName(username);
				pstats.setPassword(password);
				stats.setMyClasses(DBConnections.getRes(R,"CMCLAS"));
				stats.setStat(CharStats.STAT_STRENGTH,CMath.s_int(DBConnections.getRes(R,"CMSTRE")));
				stats.setMyRace(CMClass.getRace(DBConnections.getRes(R,"CMRACE")));
				stats.setStat(CharStats.STAT_DEXTERITY,CMath.s_int(DBConnections.getRes(R,"CMDEXT")));
				stats.setStat(CharStats.STAT_CONSTITUTION,CMath.s_int(DBConnections.getRes(R,"CMCONS")));
				stats.setStat(CharStats.STAT_GENDER,DBConnections.getRes(R,"CMGEND").charAt(0));
				stats.setStat(CharStats.STAT_WISDOM,CMath.s_int(DBConnections.getRes(R,"CMWISD")));
				stats.setStat(CharStats.STAT_INTELLIGENCE,CMath.s_int(DBConnections.getRes(R,"CMINTE")));
				stats.setStat(CharStats.STAT_CHARISMA,CMath.s_int(DBConnections.getRes(R,"CMCHAR")));
				state.setHitPoints(CMath.s_int(DBConnections.getRes(R,"CMHITP")));
				stats.setMyLevels(DBConnections.getRes(R,"CMLEVL"));
				int level=0;
				for(int i=0;i<mob.baseCharStats().numClasses();i++)
					level+=stats.getClassLevel(mob.baseCharStats().getMyClass(i));
				mob.baseEnvStats().setLevel(level);
				state.setMana(CMath.s_int(DBConnections.getRes(R,"CMMANA")));
				state.setMovement(CMath.s_int(DBConnections.getRes(R,"CMMOVE")));
				mob.setDescription(DBConnections.getRes(R,"CMDESC"));
				int align=(CMath.s_int(DBConnections.getRes(R,"CMALIG")));
				if((CMLib.factions().getFaction(CMLib.factions().AlignID())!=null)&&(align>=0))
				    CMLib.factions().setAlignmentOldRange(mob,align);
				mob.setExperience(CMath.s_int(DBConnections.getRes(R,"CMEXPE")));
				mob.setExpNextLevel(CMath.s_int(DBConnections.getRes(R,"CMEXLV")));
				mob.setWorshipCharID(DBConnections.getRes(R,"CMWORS"));
				mob.setPractices(CMath.s_int(DBConnections.getRes(R,"CMPRAC")));
				mob.setTrains(CMath.s_int(DBConnections.getRes(R,"CMTRAI")));
				mob.setAgeHours(CMath.s_long(DBConnections.getRes(R,"CMAGEH")));
				mob.setMoney(CMath.s_int(DBConnections.getRes(R,"CMGOLD")));
				mob.setWimpHitPoint(CMath.s_int(DBConnections.getRes(R,"CMWIMP")));
				mob.setQuestPoint(CMath.s_int(DBConnections.getRes(R,"CMQUES")));
				String roomID=DBConnections.getRes(R,"CMROID");
				if(roomID==null) roomID="";
				int x=roomID.indexOf("||");
				if(x>=0)
				{
					mob.setLocation(CMLib.map().getRoom(roomID.substring(x+2)));
					roomID=roomID.substring(0,x);
				}
				mob.setStartRoom(CMLib.map().getRoom(roomID));
				pstats.setLastDateTime(CMath.s_long(DBConnections.getRes(R,"CMDATE")));
				pstats.setChannelMask((int)DBConnections.getLongRes(R,"CMCHAN"));
				mob.baseEnvStats().setAttackAdjustment(CMath.s_int(DBConnections.getRes(R,"CMATTA")));
				mob.baseEnvStats().setArmor(CMath.s_int(DBConnections.getRes(R,"CMAMOR")));
				mob.baseEnvStats().setDamage(CMath.s_int(DBConnections.getRes(R,"CMDAMG")));
				mob.setBitmap(CMath.s_int(DBConnections.getRes(R,"CMBTMP")));
				mob.setLiegeID(DBConnections.getRes(R,"CMLEIG"));
				mob.baseEnvStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
				mob.baseEnvStats().setWeight((int)DBConnections.getLongRes(R,"CMWEIT"));
				pstats.setPrompt(DBConnections.getRes(R,"CMPRPT"));
				String colorStr=DBConnections.getRes(R,"CMCOLR");
				if((colorStr!=null)&&(colorStr.length()>0)&&(!colorStr.equalsIgnoreCase("NULL")))
					pstats.setColorStr(colorStr);
				pstats.setLastIP(DBConnections.getRes(R,"CMLSIP"));
				mob.setClanID(DBConnections.getRes(R,"CMCLAN"));
				mob.setClanRole((int)DBConnections.getLongRes(R,"CMCLRO"));
				pstats.setEmail(DBConnections.getRes(R,"CMEMAL"));
				String buf=DBConnections.getRes(R,"CMPFIL");
				pstats.setXML(buf);
				stats.setSavesFromString(DBConnections.getRes(R,"CMSAVE"));
				Vector V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"TATTS"),true);
				while(mob.numTattoos()>0)mob.delTattoo(mob.fetchTattoo(0));
				for(int v=0;v<V9.size();v++) mob.addTattoo((String)V9.elementAt(v));

				V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"EDUS"),true);
				while(mob.numEducations()>0)mob.delEducation(mob.fetchEducation(0));
				for(int v=0;v<V9.size();v++) mob.addEducation((String)V9.elementAt(v));
				if(pstats.getBirthday()==null)
				    stats.setStat(CharStats.STAT_AGE,pstats.initializeBirthday((int)Math.round(CMath.div(mob.getAgeHours(),60.0)),stats.getMyRace()));
				mob.setImage(CMLib.xml().returnXMLValue(buf,"IMG"));
				Vector CleanXML=CMLib.xml().parseAllXML(DBConnections.getRes(R,"CMMXML"));
				CMLib.coffeeMaker().setFactionFromXML(mob,CleanXML);

				found=true;
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		return found;
	}

	public static void DBRead(MOB mob)
	{
		if(mob.Name().length()==0) return;
		if(emptyRoom==null) emptyRoom=CMClass.getLocale("StdRoom");
		int oldDisposition=mob.baseEnvStats().disposition();
		mob.baseEnvStats().setDisposition(EnvStats.IS_NOT_SEEN|EnvStats.IS_SNEAKING);
		mob.envStats().setDisposition(EnvStats.IS_NOT_SEEN|EnvStats.IS_SNEAKING);
		DBReadUserOnly(mob);
		Room oldLoc=mob.location();
		boolean inhab=false;
		if(oldLoc!=null)
			inhab=oldLoc.isInhabitant(mob);
		mob.setLocation(emptyRoom);

		DBConnection D=null;
		// now grab the items
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHIT WHERE CMUSERID='"+mob.Name()+"'");
			Hashtable itemNums=new Hashtable();
			Hashtable itemLocs=new Hashtable();
			while(R.next())
			{
				String itemNum=DBConnections.getRes(R,"CMITNM");
				String itemID=DBConnections.getRes(R,"CMITID");
				Item newItem=CMClass.getItem(itemID);
				if(newItem==null)
					Log.errOut("MOB","Couldn't find item '"+itemID+"'");
				else
				{
					itemNums.put(itemNum,newItem);
					newItem.setMiscText(DBConnections.getResQuietly(R,"CMITTX"));
					String loc=DBConnections.getResQuietly(R,"CMITLO");
					if(loc.length()>0)
					{
						Item container=(Item)itemNums.get(loc);
						if(container!=null)
							newItem.setContainer(container);
						else
							itemLocs.put(newItem,loc);
					}
					newItem.wearAt((int)DBConnections.getLongRes(R,"CMITWO"));
					newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
					newItem.baseEnvStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
					newItem.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
					newItem.baseEnvStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
					newItem.recoverEnvStats();
					mob.addInventory(newItem);
				}
			}
			for(Enumeration e=itemLocs.keys();e.hasMoreElements();)
			{
				Item keyItem=(Item)e.nextElement();
				String location=(String)itemLocs.get(keyItem);
				Item container=(Item)itemNums.get(location);
				if(container!=null)
				{
					keyItem.setContainer(container);
					keyItem.recoverEnvStats();
					container.recoverEnvStats();
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		D=null;
		
		mob.setLocation(oldLoc);
		if(inhab&&(!oldLoc.isInhabitant(mob)))
			oldLoc.addInhabitant(mob);

		// now grab the abilities
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAB WHERE CMUSERID='"+mob.Name()+"'");
			while(R.next())
			{
				String abilityID=DBConnections.getRes(R,"CMABID");
				int profficiency=(int)DBConnections.getLongRes(R,"CMABPF");
				if(profficiency==Integer.MIN_VALUE)
				{
					Behavior newBehavior=CMClass.getBehavior(abilityID);
					if(newBehavior==null)
						Log.errOut("MOB","Couldn't find behavior '"+abilityID+"'");
					else
					{
						newBehavior.setParms(DBConnections.getRes(R,"CMABTX"));
						mob.addBehavior(newBehavior);
					}
				}
				else
				{
					Ability newAbility=CMClass.getAbility(abilityID);
					if(newAbility==null)
						Log.errOut("MOB","Couldn't find ability '"+abilityID+"'");
					else
					{
						if((profficiency<0)||(profficiency==Integer.MAX_VALUE))
						{
							if(profficiency==Integer.MAX_VALUE)
							{
								newAbility.setProfficiency(100);
								mob.addNonUninvokableEffect(newAbility);
								newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
							}
							else
							{
								profficiency=profficiency+200;
								newAbility.setProfficiency(profficiency);
								newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
								
								Ability newAbility2=(Ability)newAbility.copyOf();
								mob.addNonUninvokableEffect(newAbility);
								newAbility2.recoverEnvStats();
								mob.addAbility(newAbility2);
							}
						}
						else
						{
							newAbility.setProfficiency(profficiency);
							newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
							newAbility.recoverEnvStats();
							mob.addAbility(newAbility);
						}
					}
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
        D=null;

        
		mob.baseEnvStats().setDisposition(oldDisposition);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.resetToMaxState();

		if(mob.baseCharStats()!=null)
		{
			mob.baseCharStats().getCurrentClass().startCharacter(mob,false,true);
			int oldWeight=mob.baseEnvStats().weight();
			int oldHeight=mob.baseEnvStats().height();
			mob.baseCharStats().getMyRace().startRacing(mob,true);
			if(oldWeight>0) mob.baseEnvStats().setWeight(oldWeight);
			if(oldHeight>0) mob.baseEnvStats().setHeight(oldHeight);
		}

		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.resetToMaxState();

		if(CMLib.map().getPlayer(mob.Name())==null)
			CMLib.map().addPlayer(mob);
	}

	public static Vector userList()
	{
		DBConnection D=null;
		Vector V=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null)
			while(R.next())
			{
				String username=DBConnections.getRes(R,"CMUSERID");
				V.addElement(username);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		return V;
	}

	public static Vector getUserList()
	{
		DBConnection D=null;
		Vector allUsers=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			CharClass C=null;
			ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null)
			while(R.next())
			{
				Vector thisUser=new Vector();
				try{
					thisUser.addElement(DBConnections.getRes(R,"CMUSERID"));
					String cclass=DBConnections.getRes(R,"CMCLAS");
					int x=cclass.lastIndexOf(";");
					if((x>0)&&(x<cclass.length()-2))
					{
						C=CMClass.getCharClass(cclass.substring(x+1));
						if(C!=null)	cclass=C.name();
					}
					thisUser.addElement(cclass);
					String rrace=DBConnections.getRes(R,"CMRACE");
					Race R2=CMClass.getRace(rrace);
					if(R2!=null)
						thisUser.addElement(R2.name());
					else
						thisUser.addElement(rrace);
					String lvl=DBConnections.getRes(R,"CMLEVL");
					x=lvl.indexOf(";");
					int level=0;
					while(x>=0)
					{
						level+=CMath.s_int(lvl.substring(0,x));
						lvl=lvl.substring(x+1);
						x=lvl.indexOf(";");
					}
					if(lvl.length()>0) level+=CMath.s_int(lvl);
					thisUser.addElement(new Integer(level).toString());
					thisUser.addElement(DBConnections.getRes(R,"CMAGEH"));
					MOB M=CMLib.map().getPlayer((String)thisUser.firstElement());
					if((M!=null)&&(M.lastTickedDateTime()>0))
						thisUser.addElement(""+M.lastTickedDateTime());
					else
						thisUser.addElement(DBConnections.getRes(R,"CMDATE"));
					thisUser.addElement(DBConnections.getRes(R,"CMEMAL"));
					allUsers.addElement(thisUser);
				}
				catch(Exception e){Log.errOut("MOBloader",e);}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		return allUsers;
	}

	public static void vassals(MOB mob, String liegeID)
	{
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMLEIG='"+liegeID+"'");
			StringBuffer head=new StringBuffer("");
			head.append("[");
			head.append(CMStrings.padRight("Race",8)+" ");
			head.append(CMStrings.padRight("Class",10)+" ");
			head.append(CMStrings.padRight("Lvl",4)+" ");
			head.append(CMStrings.padRight("Exp/Lvl",17));
			head.append("] Character name\n\r");
			HashSet done=new HashSet();
			if(R!=null)
			while(R.next())
			{
				String username=DBConnections.getRes(R,"CMUSERID");
				MOB M=CMLib.map().getPlayer(username);
				if(M==null)
				{
					done.add(username);
					String cclass=DBConnections.getRes(R,"CMCLAS");
					int x=cclass.lastIndexOf(";");
					if((x>0)&&(x<cclass.length()-2))
						cclass=CMClass.getCharClass(cclass.substring(x+1)).name();
					String race=(CMClass.getRace(DBConnections.getRes(R,"CMRACE"))).name();
					String lvl=DBConnections.getRes(R,"CMLEVL");
					x=lvl.indexOf(";");
					int level=0;
					while(x>=0)
					{
						level+=CMath.s_int(lvl.substring(0,x));
						lvl=lvl.substring(x+1);
						x=lvl.indexOf(";");
					}
					if(lvl.length()>0) level+=CMath.s_int(lvl);
					int exp=CMath.s_int(DBConnections.getRes(R,"CMEXPE"));
					int exlv=CMath.s_int(DBConnections.getRes(R,"CMEXLV"));
					head.append("[");
					head.append(CMStrings.padRight(race,8)+" ");
					head.append(CMStrings.padRight(cclass,10)+" ");
					head.append(CMStrings.padRight(Integer.toString(level),4)+" ");
					head.append(CMStrings.padRight(exp+"/"+exlv,17));
					head.append("] "+CMStrings.padRight(username,15));
					head.append("\n\r");
				}
			}
			for(Enumeration e=CMLib.map().players();e.hasMoreElements();)
			{
				MOB M=(MOB)e.nextElement();
				if((M.getLiegeID().equals(liegeID))
				&&(!done.contains(M.Name())))
				{
					head.append("[");
					head.append(CMStrings.padRight(M.charStats().getMyRace().name(),8)+" ");
					head.append(CMStrings.padRight(M.charStats().getCurrentClass().name(M.charStats().getCurrentClassLevel()),10)+" ");
					head.append(CMStrings.padRight(""+M.envStats().level(),4)+" ");
					head.append(CMStrings.padRight(M.getExperience()+"/"+M.getExpNextLevel(),17));
					head.append("] "+CMStrings.padRight(M.name(),15));
					head.append("\n\r");
				}
			}
			mob.tell(head.toString());
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
	}

	public static void DBReadFollowers(MOB mob, boolean bringToLife)
	{
		Room location=mob.location();
		if(location==null)
			location=mob.getStartRoom();
		DBConnection D=null;
		// now grab the followers
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHFO WHERE CMUSERID='"+mob.Name()+"'");
			while(R.next())
			{
				String MOBID=DBConnections.getRes(R,"CMFOID");
				MOB newMOB=CMClass.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("MOB","Couldn't find MOB '"+MOBID+"'");
				else
				{
				    Room room=(location==null)?newMOB.getStartRoom():location;
				    newMOB.setStartRoom(room);
				    newMOB.setLocation(room);
					newMOB.setMiscText(DBConnections.getResQuietly(R,"CMFOTX"));
					newMOB.baseEnvStats().setLevel(((int)DBConnections.getLongRes(R,"CMFOLV")));
					newMOB.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMFOAB"));
					newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
					newMOB.recoverEnvStats();
					newMOB.recoverCharStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.setFollowing(mob);
					if((newMOB.getStartRoom()!=null)
					&&(CMLib.utensils().doesHavePriviledgesHere(mob,newMOB.getStartRoom()))
					&&((newMOB.location()==null)||(!CMLib.utensils().doesHavePriviledgesHere(mob,newMOB.location()))))
					    newMOB.setLocation(newMOB.getStartRoom());
					if(bringToLife)
					{
						newMOB.bringToLife(mob.location(),true);
						mob.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
					}
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
	}

	public static void DBUpdateEmail(MOB mob)
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;

		DBConnector.update(
		"UPDATE CMCHAR SET"
		+"  CMEMAL='"+pstats.getEmail()+"'"
		+"  WHERE CMUSERID='"+mob.Name()+"'");
	}


	public static void DBClanFill(String clan, Vector members, Vector roles, Vector lastDates)
	{
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR where CMCLAN='"+clan+"'");
			if(R!=null)
			while(R.next())
			{
				String username=DBConnector.getRes(R,"CMUSERID");
				long lastDateTime=CMath.s_long(DBConnections.getRes(R,"CMDATE"));
				int role=(int)DBConnector.getLongRes(R,"CMCLRO");
				members.addElement(username);
				roles.addElement(new Integer(role));
				MOB M=CMLib.map().getPlayer(username);
				if((M!=null)&&(M.lastTickedDateTime()>0))
					lastDates.addElement(new Long(M.lastTickedDateTime()));
				else
					lastDates.addElement(new Long(lastDateTime));
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
	}

	public static void DBUpdateClan(String name, String clan, int role)
	{
		MOB M=CMLib.map().getPlayer(name);
		if(M!=null)
		{
			M.setClanID(clan);
			M.setClanRole(role);
		}

		DBConnector.update(
		"UPDATE CMCHAR SET"
		+"  CMCLAN='"+clan+"',"
		+"  CMCLRO="+role+""
		+"  WHERE CMUSERID='"+name+"'");
	}

	public static void DBUpdate(MOB mob)
	{
		DBUpdateJustMOB(mob);
		if((mob.Name().length()==0)
		||(mob.playerStats()==null))
			return;
		DBUpdateItems(mob);
		DBUpdateAbilities(mob);
		mob.playerStats().setUpdated(System.currentTimeMillis());
	}

	public static void DBUpdatePassword(MOB mob)
	{
		if(mob.Name().length()==0) return;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;

		DBConnector.update(
		"UPDATE CMCHAR SET"
		+"  CMPASS='"+pstats.password()+"'"
		+"  WHERE CMUSERID='"+mob.Name()+"'");
	}
	
	public static void DBUpdateJustMOB(MOB mob)
	{
		if(mob.Name().length()==0)
		{
			DBCreateCharacter(mob);
			return;
		}

		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;

		String strStartRoomID=(mob.getStartRoom()!=null)?CMLib.map().getExtendedRoomID(mob.getStartRoom()):"";
		String strOtherRoomID=(mob.location()!=null)?CMLib.map().getExtendedRoomID(mob.location()):"";
		StringBuffer pfxml=new StringBuffer(pstats.getXML());
		if(mob.numTattoos()>0)
		{
			pfxml.append("<TATTS>");
			for(int i=0;i<mob.numTattoos();i++)
				pfxml.append(mob.fetchTattoo(i)+";");
			pfxml.append("</TATTS>");
		}
		if(mob.numEducations()>0)
		{
			pfxml.append("<EDUS>");
			for(int i=0;i<mob.numEducations();i++)
				pfxml.append(mob.fetchEducation(i)+";");
			pfxml.append("</EDUS>");
		}
		pfxml.append(CMLib.xml().convertXMLtoTag("IMG",mob.rawImage()));

		StringBuffer cleanXML=new StringBuffer();
		cleanXML.append(CMLib.coffeeMaker().getFactionXML(mob));

		DBConnector.update(
		"UPDATE CMCHAR SET"
		+"  CMPASS='"+pstats.password()+"'"
		+", CMCLAS='"+mob.baseCharStats().getMyClassesStr()+"'"
		+", CMSTRE="+mob.baseCharStats().getStat(CharStats.STAT_STRENGTH)
		+", CMRACE='"+mob.baseCharStats().getMyRace().ID()+"'"
		+", CMDEXT="+mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY)
		+", CMCONS="+mob.baseCharStats().getStat(CharStats.STAT_CONSTITUTION)
		+", CMGEND='"+((char)mob.baseCharStats().getStat(CharStats.STAT_GENDER))+"'"
		+", CMWISD="+mob.baseCharStats().getStat(CharStats.STAT_WISDOM)
		+", CMINTE="+mob.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE)
		+", CMCHAR="+mob.baseCharStats().getStat(CharStats.STAT_CHARISMA)
		+", CMHITP="+mob.baseState().getHitPoints()
		+", CMLEVL='"+mob.baseCharStats().getMyLevelsStr()+"'"
		+", CMMANA="+mob.baseState().getMana()
		+", CMMOVE="+mob.baseState().getMovement()
		+", CMALIG="+"-1"
		+", CMEXPE="+mob.getExperience()
		+", CMEXLV="+mob.getExpNextLevel()
		+", CMWORS='"+mob.getWorshipCharID()+"'"
		+", CMPRAC="+mob.getPractices()
		+", CMTRAI="+mob.getTrains()
		+", CMAGEH="+mob.getAgeHours()
		+", CMGOLD="+mob.getMoney()
		+", CMWIMP="+mob.getWimpHitPoint()
		+", CMQUES="+mob.getQuestPoint()
		+", CMROID='"+strStartRoomID+"||"+strOtherRoomID+"'"
		+", CMDATE='"+pstats.lastDateTime()+"'"
		+", CMCHAN="+pstats.getChannelMask()
		+", CMATTA="+mob.baseEnvStats().attackAdjustment()
		+", CMAMOR="+mob.baseEnvStats().armor()
		+", CMDAMG="+mob.baseEnvStats().damage()
		+", CMBTMP="+mob.getBitmap()
		+", CMLEIG='"+mob.getLiegeID()+"'"
		+", CMHEIT="+mob.baseEnvStats().height()
		+", CMWEIT="+mob.baseEnvStats().weight()
		+", CMPRPT='"+pstats.getPrompt()+"'"
		+", CMCOLR='"+pstats.getColorStr()+"'"
		+", CMCLAN='"+mob.getClanID()+"'"
		+", CMLSIP='"+pstats.lastIP()+"'"
		+", CMCLRO="+mob.getClanRole()
		+", CMEMAL='"+pstats.getEmail()+"'"
		+", CMPFIL='"+pfxml.toString()+"'"
		+", CMSAVE='"+mob.baseCharStats().getSavesAsString()+"'"
		+", CMMXML='"+cleanXML.toString()+"'"
		+"  WHERE CMUSERID='"+mob.Name()+"'");

		DBConnector.update(
		"UPDATE CMCHAR SET"
		+" CMDESC='"+mob.description()+"'"
		+" WHERE CMUSERID='"+mob.Name()+"'");
	}

	private static void DBUpdateContents(MOB mob, Vector V)
	{
		Vector done=new Vector();
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item thisItem=mob.fetchInventory(i);
			if((thisItem!=null)
			&&(!done.contains(""+thisItem))
			&&(thisItem.savable()))
			{
				String
				str="INSERT INTO CMCHIT ("
				+"CMUSERID, "
				+"CMITNM, "
				+"CMITID, "
				+"CMITTX, "
				+"CMITLO, "
				+"CMITWO, "
				+"CMITUR, "
				+"CMITLV, "
				+"CMITAB, "
				+"CMHEIT"
				+") values ("
				+"'"+mob.Name()+"',"
				+"'"+(thisItem)+"',"
				+"'"+thisItem.ID()+"',"
				+"'"+thisItem.text()+" ',"
				+"'"+((thisItem.container()!=null)?(""+thisItem.container()):"")+"',"
				+thisItem.rawWornCode()+","
				+thisItem.usesRemaining()+","
				+thisItem.baseEnvStats().level()+","
				+thisItem.baseEnvStats().ability()+","
				+thisItem.baseEnvStats().height()+")";
				if(!V.contains(str))
					V.addElement(str);
				done.addElement(""+thisItem);
			}
		}
	}

	public static void DBUpdateItems(MOB mob)
	{
		if(mob.Name().length()==0) return;
		DBConnector.update("DELETE FROM CMCHIT WHERE CMUSERID='"+mob.Name()+"'");
		try{Thread.sleep(mob.inventorySize());}catch(Exception e){}
		if(DBConnector.queryRows("SELECT * FROM CMCHIT  WHERE CMUSERID='"+mob.Name()+"'")>0)
			Log.errOut("Failed to update items for mob "+mob.Name()+".");
		Vector V=new Vector();
		if(mob.inventorySize()>0)
			DBUpdateContents(mob,V);
		for(int v=0;v<V.size();v++)
			DBConnector.update((String)V.elementAt(v));
	}

	public static void DBUpdateFollowers(MOB mob)
	{
		if((mob==null)||(mob.Name().length()==0))
			return;
		DBConnector.update("DELETE FROM CMCHFO WHERE CMUSERID='"+mob.Name()+"'");
		if(DBConnector.queryRows("SELECT * FROM CMCHFO  WHERE CMUSERID='"+mob.Name()+"'")>0)
			Log.errOut("Failed to update followers for mob "+mob.Name()+".");
		Vector V=new Vector();
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB thisMOB=mob.fetchFollower(f);
			if((thisMOB!=null)&&(thisMOB.isMonster())&&(!thisMOB.isPossessing()))
			{
				String
				str="INSERT INTO CMCHFO ("
				+"CMUSERID, "
				+"CMFONM, "
				+"CMFOID, "
				+"CMFOTX, "
				+"CMFOLV, "
				+"CMFOAB"
				+") values ("
				+"'"+mob.Name()+"',"
				+f+","
				+"'"+CMClass.className(thisMOB)+"',"
				+"'"+thisMOB.text()+" ',"
				+thisMOB.baseEnvStats().level()+","
				+thisMOB.baseEnvStats().ability()
				+")";
				V.addElement(str);
			}
		}
		for(int v=0;v<V.size();v++)
			DBConnector.update((String)V.elementAt(v));
	}

	public static void DBDelete(MOB mob)
	{
		if(mob.Name().length()==0) return;
        Vector channels=CMLib.channels().getFlaggedChannelNames("PLAYERPURGES");
        for(int i=0;i<channels.size();i++)
                CMLib.commands().postChannel((String)channels.elementAt(i),mob.getClanID(),mob.Name()+" has just been deleted.",true);
		CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_PURGES);
		DBConnector.update("DELETE FROM CMCHAR WHERE CMUSERID='"+mob.Name()+"'");
		while(mob.inventorySize()>0)
		{
			Item thisItem=mob.fetchInventory(0);
			if(thisItem!=null)
			{
				thisItem.setContainer(null);
				mob.delInventory(thisItem);
			}
		}
		DBUpdateItems(mob);

		while(mob.numFollowers()>0)
		{
			MOB follower=mob.fetchFollower(0);
			if(follower!=null)
				follower.setFollowing(null);
		}
		DBUpdateFollowers(mob);

		while(mob.numLearnedAbilities()>0)
		{
			Ability A=mob.fetchAbility(0);
			if(A!=null)
				mob.delAbility(A);
		}
		DBUpdateAbilities(mob);
		JournalLoader.DBDeletePlayerData(mob.Name());
		DataLoader.DBDeletePlayer(mob.Name());
	}

	public static void DBUpdateAbilities(MOB mob)
	{
		if(mob.Name().length()==0) return;
		DBConnector.update("DELETE FROM CMCHAB WHERE CMUSERID='"+mob.Name()+"'");
		if(DBConnector.queryRows("SELECT * FROM CMCHAB  WHERE CMUSERID='"+mob.Name()+"'")>0)
			Log.errOut("Failed to update abilities for mob "+mob.Name()+".");
		Vector V=new Vector();
		HashSet H=new HashSet();
		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability thisAbility=mob.fetchAbility(a);
			if((thisAbility!=null)&&(thisAbility.savable()))
			{
				int profficiency=thisAbility.profficiency();
                Ability effectA=mob.fetchEffect(thisAbility.ID());
                if(effectA!=null)
                {
                    if((effectA.savable())
                    &&(!effectA.canBeUninvoked())
                    &&(!effectA.isAutoInvoked()))
                        profficiency=profficiency-200;
                }
				H.add(thisAbility.ID());
				
				
				String
				str="INSERT INTO CMCHAB ("
				+"CMUSERID, "
				+"CMABID, "
				+"CMABPF,"
				+"CMABTX"
				+") values ("
				+"'"+mob.Name()+"',"
				+"'"+thisAbility.ID()+"',"
				+profficiency+",'"
				+thisAbility.text()+"'"
				+")";
				V.addElement(str);
			}
		}
		for(int a=0;a<mob.numEffects();a++)
		{
			Ability thisAffect=mob.fetchEffect(a);
			if((thisAffect!=null)
			&&(!H.contains(thisAffect.ID()))
			&&(thisAffect.savable())
			&&(!thisAffect.canBeUninvoked()))
			{
				String
				str="INSERT INTO CMCHAB ("
				+"CMUSERID, "
				+"CMABID, "
				+"CMABPF,"
				+"CMABTX"
				+") values ("
				+"'"+mob.Name()+"',"
				+"'"+thisAffect.ID()+"',"
				+Integer.MAX_VALUE+",'"
				+thisAffect.text()+"'"
				+")";
				V.addElement(str);
			}
		}
		for(int b=0;b<mob.numBehaviors();b++)
		{
			Behavior thisBehavior=mob.fetchBehavior(b);
			if((thisBehavior!=null)&&(thisBehavior.isSavable()))
			{
				String
				str="INSERT INTO CMCHAB ("
				+"CMUSERID, "
				+"CMABID, "
				+"CMABPF,"
				+"CMABTX"
				+") values ("
				+"'"+mob.Name()+"',"
				+"'"+thisBehavior.ID()+"',"
				+Integer.MIN_VALUE+",'"
				+thisBehavior.getParms()+"'"
				+")";
				V.addElement(str);
			}
		}
		for(int v=0;v<V.size();v++)
			DBConnector.update((String)V.elementAt(v));
	}

	public static void DBCreateCharacter(MOB mob)
	{
		if(mob.Name().length()==0) return;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;

		DBConnector.update(
		"INSERT INTO CMCHAR ("
		+"CMUSERID, "
		+"CMPASS, "
		+"CMCLAS, "
		+"CMRACE, "
		+"CMGEND "
		+") VALUES ('"
		+mob.Name()
		+"','"+pstats.password()
		+"','"+mob.baseCharStats().getMyClassesStr()
		+"','"+mob.baseCharStats().getMyRace().ID()
		+"','"+((char)mob.baseCharStats().getStat(CharStats.STAT_GENDER))+"')");
	}

	public static boolean DBUserSearch(MOB mob, String Login)
	{
		DBConnection D=null;
		boolean returnable=false;

		if(mob!=null)
		{
			if(mob.playerStats()==null)
				mob.setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
			mob.setName("");
			mob.playerStats().setPassword("");
		}
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null)
			while(R.next())
			{
				String username=DBConnector.getRes(R,"CMUSERID");
				if(Login.equalsIgnoreCase(username))
				{
					returnable=true;
					if(mob!=null)
					{
						String password=DBConnector.getRes(R,"CMPASS");
						String email=DBConnector.getRes(R,"CMEMAL");
						mob.setName(username);
						mob.playerStats().setPassword(password);
						mob.playerStats().setEmail(email);
                        // Acct Exp Code
                        String buf=DBConnections.getRes(R,"CMPFIL");
                        if(CMLib.xml().returnXMLValue(buf,"ACCTEXP").length()>0)
                            mob.playerStats().setAccountExpiration(CMath.s_long(CMLib.xml().returnXMLValue(buf,"ACCTEXP")));
                        else
                        {
                            Calendar C=Calendar.getInstance();
                            C.add(Calendar.DATE,15);
                            mob.playerStats().setAccountExpiration(C.getTimeInMillis());
                        }
					}
					break;
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		return returnable;
	}

	public static String[] DBFetchEmailData(String name)
	{
		String[] data=new String[2];
		for(Enumeration e=CMLib.map().players();e.hasMoreElements();)
		{
			MOB M=(MOB)e.nextElement();
			if((M.Name().equalsIgnoreCase(name))&&(M.playerStats()!=null))
			{
				data[0]=M.playerStats().getEmail();
				data[1]=""+((M.getBitmap()&MOB.ATT_AUTOFORWARD)==MOB.ATT_AUTOFORWARD);
				return data;
			}
		}
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+name+"'");
			if(R!=null)
			while(R.next())
			{
				//String username=DBConnector.getRes(R,"CMUSERID");
				int btmp=CMath.s_int(DBConnector.getRes(R,"CMBTMP"));
				String temail=DBConnector.getRes(R,"CMEMAL");
				R.close();
				DBConnector.DBDone(D);
				data[0]=temail;
				data[1]=""+((btmp&MOB.ATT_AUTOFORWARD)==MOB.ATT_AUTOFORWARD);
				return data;
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		return null;
	}
	public static String DBEmailSearch(String email)
	{
		DBConnection D=null;
		for(Enumeration e=CMLib.map().players();e.hasMoreElements();)
		{
			MOB M=(MOB)e.nextElement();
			if((M.playerStats()!=null)&&(M.playerStats().getEmail().equalsIgnoreCase(email)))
				return M.Name();
		}
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null)
			while(R.next())
			{
				String username=DBConnector.getRes(R,"CMUSERID");
				String temail=DBConnector.getRes(R,"CMEMAL");
				if(temail.equalsIgnoreCase(email))
				{
					R.close();
					DBConnector.DBDone(D);
					return username;
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		return null;
	}
}
