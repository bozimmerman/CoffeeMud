package com.planet_ink.coffee_mud.core.database;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.database.DBConnector.DBPreparedBatchEntry;
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
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

/*
 * Copyright 2000-2012 Bo Zimmerman Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
public class MOBloader
{
	protected DBConnector DB=null;

	public MOBloader(DBConnector newDB)
	{
		DB=newDB;
	}
	protected Room emptyRoom=null;

	public boolean DBReadUserOnly(MOB mob)
	{
		if(mob.Name().length()==0) return false;
		boolean found=false;
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+mob.Name()+"'");
			if(R.next())
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
				mob.basePhyStats().setLevel(level);
				state.setMana(CMath.s_int(DBConnections.getRes(R,"CMMANA")));
				state.setMovement(CMath.s_int(DBConnections.getRes(R,"CMMOVE")));
				mob.setDescription(DBConnections.getRes(R,"CMDESC"));
				int align=(CMath.s_int(DBConnections.getRes(R,"CMALIG")));
				if((CMLib.factions().getFaction(CMLib.factions().AlignID())!=null)&&(align>=0))
					CMLib.factions().setAlignmentOldRange(mob,align);
				mob.setExperience(CMath.s_int(DBConnections.getRes(R,"CMEXPE")));
				//mob.setExpNextLevel(CMath.s_int(DBConnections.getRes(R,"CMEXLV")));
				mob.setWorshipCharID(DBConnections.getRes(R,"CMWORS"));
				mob.setPractices(CMath.s_int(DBConnections.getRes(R,"CMPRAC")));
				mob.setTrains(CMath.s_int(DBConnections.getRes(R,"CMTRAI")));
				mob.setAgeMinutes(CMath.s_long(DBConnections.getRes(R,"CMAGEH")));
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
				mob.basePhyStats().setAttackAdjustment(CMath.s_int(DBConnections.getRes(R,"CMATTA")));
				mob.basePhyStats().setArmor(CMath.s_int(DBConnections.getRes(R,"CMAMOR")));
				mob.basePhyStats().setDamage(CMath.s_int(DBConnections.getRes(R,"CMDAMG")));
				mob.setBitmap(CMath.s_int(DBConnections.getRes(R,"CMBTMP")));
				mob.setLiegeID(DBConnections.getRes(R,"CMLEIG"));
				mob.basePhyStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
				mob.basePhyStats().setWeight((int)DBConnections.getLongRes(R,"CMWEIT"));
				pstats.setPrompt(DBConnections.getRes(R,"CMPRPT"));
				String colorStr=DBConnections.getRes(R,"CMCOLR");
				if((colorStr!=null)&&(colorStr.length()>0)&&(!colorStr.equalsIgnoreCase("NULL"))) pstats.setColorStr(colorStr);
				pstats.setLastIP(DBConnections.getRes(R,"CMLSIP"));
				mob.setClan("", Integer.MIN_VALUE); // delete all sequence
				pstats.setEmail(DBConnections.getRes(R,"CMEMAL"));
				String buf=DBConnections.getRes(R,"CMPFIL");
				pstats.setXML(buf);
				stats.setNonBaseStatsFromString(DBConnections.getRes(R,"CMSAVE"));
				List<String> V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"TATTS"),true);
				for(Enumeration<MOB.Tattoo> e=mob.tattoos();e.hasMoreElements();)
					mob.delTattoo(e.nextElement());
				for(int v=0;v<V9.size();v++)
					mob.addTattoo(parseTattoo((String)V9.get(v)));
				V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"EDUS"),true);
				mob.delAllExpertises();
				for(int v=0;v<V9.size();v++)
					mob.addExpertise((String)V9.get(v));
				if(pstats.getBirthday()==null)
					stats.setStat(CharStats.STAT_AGE,
						pstats.initializeBirthday((int)Math.round(CMath.div(mob.getAgeMinutes(),60.0)),stats.getMyRace()));
				mob.setImage(CMLib.xml().returnXMLValue(buf,"IMG"));
				List<XMLLibrary.XMLpiece> CleanXML=CMLib.xml().parseAllXML(DBConnections.getRes(R,"CMMXML"));
				R.close();
				if(pstats.getSavedPose().length()>0)
					mob.setDisplayText(pstats.getSavedPose());
				CMLib.coffeeMaker().setFactionFromXML(mob,CleanXML);
				found=true;
			}
			R.close();
			R=D.query("SELECT * FROM CMCHCL WHERE CMUSERID='"+mob.Name()+"'");
			while(R.next())
			{
				String clanID=DBConnections.getRes(R,"CMCLAN");
				int clanRole = (int)DBConnections.getLongRes(R,"CMCLRO");
				Clan C=CMLib.clans().getClan(clanID);
				if(C!=null) mob.setClan(C.clanID(), clanRole);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return found;
	}

	public void DBRead(MOB mob)
	{
		if(mob.Name().length()==0) return;
		if(emptyRoom==null) emptyRoom=CMClass.getLocale("StdRoom");
		int oldDisposition=mob.basePhyStats().disposition();
		mob.basePhyStats().setDisposition(PhyStats.IS_NOT_SEEN|PhyStats.IS_SNEAKING);
		mob.phyStats().setDisposition(PhyStats.IS_NOT_SEEN|PhyStats.IS_SNEAKING);
		CMLib.players().addPlayer(mob);
		DBReadUserOnly(mob);
		mob.recoverPhyStats();
		mob.recoverCharStats();
		Room oldLoc=mob.location();
		boolean inhab=false;
		if(oldLoc!=null) inhab=oldLoc.isInhabitant(mob);
		mob.setLocation(emptyRoom);
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHIT WHERE CMUSERID='"+mob.Name()+"'");
			Hashtable<String,Item> itemNums=new Hashtable<String,Item>();
			Hashtable<Item,String> itemLocs=new Hashtable<Item,String>();
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
					boolean addToMOB=true;
					String text=DBConnections.getResQuietly(R,"CMITTX");
					if(text.startsWith("<ROOM"))
					{
						int roomX=text.indexOf("/>");
						if(roomX>=0)
						{
							String roomXML=text.substring(0,roomX+2);
							text=text.substring(roomX+2);
							List<XMLLibrary.XMLpiece> xml=CMLib.xml().parseAllXML(roomXML);
							if((xml!=null)&&(xml.size()>0))
							{
								final String roomID=xml.get(0).parms.get("ID");
								final long expirationDate=CMath.s_long(xml.get(0).parms.get("EXPIRE"));
								final Room itemR=CMLib.map().getRoom(roomID);
								if(itemR!=null)
								{
									itemR.addItem(newItem);
									newItem.setExpirationDate(expirationDate);
									addToMOB=false;
								}
							}
						}
					}
					newItem.setMiscText(text);
					String loc=DBConnections.getResQuietly(R,"CMITLO");
					if(loc.length()>0)
					{
						Item container=(Item)itemNums.get(loc);
						if(container instanceof Container)
							newItem.setContainer((Container)container);
						else
							itemLocs.put(newItem,loc);
					}
					newItem.wearAt((int)DBConnections.getLongRes(R,"CMITWO"));
					newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
					newItem.basePhyStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
					newItem.basePhyStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
					newItem.basePhyStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
					newItem.recoverPhyStats();
					if(addToMOB)
						mob.addItem(newItem);
					else
						mob.playerStats().getExtItems().addItem(newItem);
				}
			}
			for(Enumeration<Item> e=itemLocs.keys();e.hasMoreElements();)
			{
				Item keyItem=(Item)e.nextElement();
				String location=(String)itemLocs.get(keyItem);
				Item container=(Item)itemNums.get(location);
				if(container instanceof Container)
				{
					keyItem.setContainer((Container)container);
					keyItem.recoverPhyStats();
					container.recoverPhyStats();
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		D=null;
		if(oldLoc!=null)
		{
			mob.setLocation(oldLoc);
			if(inhab&&(!oldLoc.isInhabitant(mob))) 
				oldLoc.addInhabitant(mob);
		}
		// now grab the abilities
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAB WHERE CMUSERID='"+mob.Name()+"'");
			while(R.next())
			{
				String abilityID=DBConnections.getRes(R,"CMABID");
				int proficiency=(int)DBConnections.getLongRes(R,"CMABPF");
				if((proficiency==Integer.MIN_VALUE)||(proficiency==Integer.MIN_VALUE+1))
				{
					if(abilityID.equalsIgnoreCase("ScriptingEngine"))
					{
						if(CMClass.getCommon("DefaultScriptingEngine")==null)
							Log.errOut("MOB","Couldn't find scripting engine!");
						else
						{

							String xml=DBConnections.getRes(R,"CMABTX");
							if(xml.length()>0)
								CMLib.coffeeMaker().setGenScripts(mob,CMLib.xml().parseAllXML(xml),true);
						}
					}
					else
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
				}
				else
				{
					Ability newAbility=CMClass.getAbility(abilityID);
					if(newAbility==null)
						Log.errOut("MOB","Couldn't find ability '"+abilityID+"'");
					else
					{
						if((proficiency<0)||(proficiency==Integer.MAX_VALUE))
						{
							if(proficiency==Integer.MAX_VALUE)
							{
								newAbility.setProficiency(CMLib.ableMapper().getMaxProficiency(newAbility.ID()));
								mob.addNonUninvokableEffect(newAbility);
								newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
							}else
							{
								proficiency=proficiency+200;
								newAbility.setProficiency(proficiency);
								newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
								Ability newAbility2=(Ability)newAbility.copyOf();
								mob.addNonUninvokableEffect(newAbility);
								mob.addAbility(newAbility2);
							}
						}else
						{
							newAbility.setProficiency(proficiency);
							newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
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
		finally
		{
			DB.DBDone(D);
		}
		D=null;
		mob.basePhyStats().setDisposition(oldDisposition);
		mob.recoverCharStats();
		mob.recoverPhyStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		if(mob.baseCharStats()!=null)
		{
			mob.baseCharStats().getCurrentClass().startCharacter(mob,false,true);
			int oldWeight=mob.basePhyStats().weight();
			int oldHeight=mob.basePhyStats().height();
			mob.baseCharStats().getMyRace().startRacing(mob,true);
			if(oldWeight>0) mob.basePhyStats().setWeight(oldWeight);
			if(oldHeight>0) mob.basePhyStats().setHeight(oldHeight);
		}
		mob.recoverCharStats();
		mob.recoverPhyStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		// wont add if same name already exists
	}

	public List<String> getUserList()
	{
		DBConnection D=null;
		Vector<String> V=new Vector<String>();
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null) while(R.next())
			{
				String username=DBConnections.getRes(R,"CMUSERID");
				V.addElement(username);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return V;
	}

	protected PlayerLibrary.ThinPlayer parseThinUser(ResultSet R)
	{
		try
		{
			PlayerLibrary.ThinPlayer thisUser=new PlayerLibrary.ThinPlayer();
			thisUser.name=DBConnections.getRes(R,"CMUSERID");
			String cclass=DBConnections.getRes(R,"CMCLAS");
			int x=cclass.lastIndexOf(';');
			CharClass C=null;
			if((x>0)&&(x<cclass.length()-2))
			{
				C=CMClass.getCharClass(cclass.substring(x+1));
				if(C!=null) cclass=C.name();
			}
			thisUser.charClass=(cclass);
			String rrace=DBConnections.getRes(R,"CMRACE");
			Race R2=CMClass.getRace(rrace);
			if(R2!=null)
				thisUser.race=(R2.name());
			else
				thisUser.race=rrace;
			List<String> lvls=CMParms.parseSemicolons(DBConnections.getRes(R,"CMLEVL"), true);
			thisUser.level=0;
			for(String lvl : lvls)
				thisUser.level+=CMath.s_int(lvl);
			thisUser.age=(int)DBConnections.getLongRes(R,"CMAGEH");
			MOB M=CMLib.players().getPlayer((String)thisUser.name);
			if((M!=null)&&(M.lastTickedDateTime()>0))
				thisUser.last=M.lastTickedDateTime();
			else
				thisUser.last=DBConnections.getLongRes(R,"CMDATE");
			String lsIP=DBConnections.getRes(R,"CMLSIP");
			thisUser.email=DBConnections.getRes(R,"CMEMAL");
			thisUser.ip=lsIP;
			return thisUser;
		}catch(Exception e)
		{
			Log.errOut("MOBloader",e);
		}
		return null;
	}
	
	public PlayerLibrary.ThinPlayer getThinUser(String name)
	{
		DBConnection D=null;
		PlayerLibrary.ThinPlayer thisUser=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+name+"'");
			if(R!=null) while(R.next())
				thisUser=parseThinUser(R);
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return thisUser;
	}
	
	public List<PlayerLibrary.ThinPlayer> getExtendedUserList()
	{
		DBConnection D=null;
		Vector<PlayerLibrary.ThinPlayer> allUsers=new Vector<PlayerLibrary.ThinPlayer>();
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null) while(R.next())
			{
				PlayerLibrary.ThinPlayer thisUser=parseThinUser(R);
				if(thisUser != null)
					allUsers.addElement(thisUser);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return allUsers;
	}

	public MOB.Tattoo parseTattoo(String tattoo)
	{
		if(tattoo==null)
			return new MOB.Tattoo("");
		int tickDown = 0;
		if((tattoo.length()>0)
		&&(Character.isDigit(tattoo.charAt(0))))
		{
			int x=tattoo.indexOf(' ');
			if((x>0)
			&&(CMath.isNumber(tattoo.substring(0,x).trim())))
			{
				tickDown=CMath.s_int(tattoo.substring(0,x));
				tattoo=tattoo.substring(x+1).trim();
			}
		}
		return new MOB.Tattoo(tattoo, tickDown);
	}
	
	public void vassals(MOB mob, String liegeID)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMLEIG='"+liegeID+"'");
			StringBuilder head=new StringBuilder("");
			head.append("[");
			head.append(CMStrings.padRight("Race",8)+" ");
			head.append(CMStrings.padRight("Class",10)+" ");
			head.append(CMStrings.padRight("Lvl",4)+" ");
			head.append(CMStrings.padRight("Exp/Lvl",17));
			head.append("] Character name\n\r");
			HashSet<String> done=new HashSet<String>();
			if(R!=null) while(R.next())
			{
				String username=DBConnections.getRes(R,"CMUSERID");
				MOB M=CMLib.players().getPlayer(username);
				if(M==null)
				{
					done.add(username);
					String cclass=DBConnections.getRes(R,"CMCLAS");
					int x=cclass.lastIndexOf(';');
					if((x>0)&&(x<cclass.length()-2)) cclass=CMClass.getCharClass(cclass.substring(x+1)).name();
					String race=(CMClass.getRace(DBConnections.getRes(R,"CMRACE"))).name();
					List<String> lvls=CMParms.parseSemicolons(DBConnections.getRes(R,"CMLEVL"), true);
					int level=0;
					for(String lvl : lvls)
						level+=CMath.s_int(lvl);
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
			for(Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
			{
				MOB M=(MOB)e.nextElement();
				if((M.getLiegeID().equals(liegeID))&&(!done.contains(M.Name())))
				{
					head.append("[");
					head.append(CMStrings.padRight(M.charStats().getMyRace().name(),8)+" ");
					head.append(CMStrings.padRight(M.charStats().getCurrentClass().name(M.charStats().getCurrentClassLevel()),10)+" ");
					head.append(CMStrings.padRight(""+M.phyStats().level(),4)+" ");
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
		finally
		{
			DB.DBDone(D);
		}
	}

	public DVector worshippers(String deityID)
	{
		DBConnection D=null;
		DVector DV=new DVector(4);
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMWORS='"+deityID+"'");
			if(R!=null) while(R.next())
			{
				String username=DBConnections.getRes(R,"CMUSERID");
				String cclass=DBConnections.getRes(R,"CMCLAS");
				int x=cclass.lastIndexOf(';');
				if((x>0)&&(x<cclass.length()-2)) cclass=CMClass.getCharClass(cclass.substring(x+1)).name();
				String race=(CMClass.getRace(DBConnections.getRes(R,"CMRACE"))).name();
				List<String> lvls=CMParms.parseSemicolons(DBConnections.getRes(R,"CMLEVL"), true);
				int level=0;
				for(String lvl : lvls)
					level+=CMath.s_int(lvl);
				DV.addElement(username,
							  cclass,
							  ""+level,
							  race);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return DV;
	}

	public List<MOB> DBScanFollowers(MOB mob)
	{
		DBConnection D=null;
		Vector<MOB> V=new Vector<MOB>();
		// now grab the followers
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHFO WHERE CMUSERID='"+mob.Name()+"'");
			while(R.next())
			{
				String MOBID=DBConnections.getRes(R,"CMFOID");
				MOB newMOB=CMClass.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("MOB","Couldn't find MOB '"+MOBID+"'");
				else
				{
					newMOB.setMiscText(DBConnections.getResQuietly(R,"CMFOTX"));
					newMOB.basePhyStats().setLevel(((int)DBConnections.getLongRes(R,"CMFOLV")));
					newMOB.basePhyStats().setAbility((int)DBConnections.getLongRes(R,"CMFOAB"));
					newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
					newMOB.recoverPhyStats();
					newMOB.recoverCharStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					V.addElement(newMOB);
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return V;
	}
	
	public void DBReadFollowers(MOB mob, boolean bringToLife)
	{
		Room location=mob.location();
		if(location==null) location=mob.getStartRoom();
		List<MOB> V=DBScanFollowers(mob);
		for(int v=0;v<V.size();v++) {
			MOB newMOB=(MOB)V.get(v);
			Room room=(location==null)?newMOB.getStartRoom():location;
			newMOB.setStartRoom(room);
			newMOB.setLocation(room);
			newMOB.setFollowing(mob);
			if((newMOB.getStartRoom()!=null)
			&&(CMLib.law().doesHavePriviledgesHere(mob,newMOB.getStartRoom()))
			&&((newMOB.location()==null)
					||(!CMLib.law().doesHavePriviledgesHere(mob,newMOB.location()))))
				newMOB.setLocation(newMOB.getStartRoom());
			if(bringToLife)
			{
				newMOB.bringToLife(mob.location(),true);
				mob.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
			}
		}
	}

	public void DBUpdateEmail(MOB mob)
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		DB.update("UPDATE CMCHAR SET CMEMAL='"+pstats.getEmail()+"' WHERE CMUSERID='"+mob.Name()+"'");
	}

	public List<Clan.MemberRecord> DBClanMembers(String clan)
	{
		List<Clan.MemberRecord> members = new Vector<Clan.MemberRecord>();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHCL where CMCLAN='"+clan+"'");
			if(R!=null) while(R.next())
			{
				String username=DB.getRes(R,"CMUSERID");
				int clanRole = (int)DBConnections.getLongRes(R,"CMCLRO");
				if(clanRole >= 7) //TODO: deprecate this at some point
					clanRole = CMath.bitNumber(clanRole); 
				Clan.MemberRecord member = new Clan.MemberRecord(username,clanRole);
				members.add(member);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return members;
	}

	public void DBUpdateClan(String name, String clan, int role)
	{
		MOB M=CMLib.players().getPlayer(name);
		if(M!=null)
		{
			M.setClan(clan, role);
		}
		List<String> clanStatements=new LinkedList<String>();
		clanStatements.add("DELETE FROM CMCHCL WHERE CMUSERID='"+name+"' AND CMCLAN='"+clan+"'");
		clanStatements.add("INSERT INTO CMCHCL (CMUSERID, CMCLAN, CMCLRO) values ('"+name+"','"+clan+"',"+role+")");
		DB.update(clanStatements.toArray(new String[0]));
	}

	public void DBUpdate(MOB mob)
	{
		DBUpdateJustMOB(mob);
		PlayerStats pStats = mob.playerStats();
		if((mob.Name().length()==0)||(pStats==null)) return;
		DBUpdateItems(mob);
		DBUpdateAbilities(mob);
		pStats.setLastUpdated(System.currentTimeMillis());
		PlayerAccount account = pStats.getAccount();
		if(account != null)
		{
			DBUpdateAccount(account);
			account.setLastUpdated(System.currentTimeMillis());
		}
	}

	public void DBUpdatePassword(String name, String password)
	{
		name=CMStrings.capitalizeAndLower(name);
		DB.update("UPDATE CMCHAR SET CMPASS='"+password+"' WHERE CMUSERID='"+name.replace('\'', 'n')+"'");
	}

	private String getPlayerStatsXML(MOB mob)
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return "";
		StringBuilder pfxml=new StringBuilder(pstats.getXML());
		if(mob.tattoos().hasMoreElements())
		{
			pfxml.append("<TATTS>");
			for(Enumeration<MOB.Tattoo> e=mob.tattoos();e.hasMoreElements();)
				pfxml.append(e.nextElement().toString()+";");
			pfxml.append("</TATTS>");
		}
		if(mob.expertises().hasMoreElements())
		{
			pfxml.append("<EDUS>");
			for(Enumeration<String> x=mob.expertises();x.hasMoreElements();)
				pfxml.append(x.nextElement()).append(';');
			pfxml.append("</EDUS>");
		}
		pfxml.append(CMLib.xml().convertXMLtoTag("IMG",mob.rawImage()));
		return pfxml.toString();
	}
	
	public void DBUpdateJustPlayerStats(MOB mob)
	{
		if(mob.Name().length()==0)
		{
			DBCreateCharacter(mob);
			return;
		}
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		String pfxml=getPlayerStatsXML(mob);
		DB.updateWithClobs("UPDATE CMCHAR SET CMPFIL=? WHERE CMUSERID='"+mob.Name()+"'", pfxml.toString());
	}
	
	public void DBUpdateJustMOB(MOB mob)
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
		
		if((mob.location()!=null)
		&&(mob.location().getArea()!=null)
		&&(CMath.bset(mob.location().getArea().flags(),Area.FLAG_INSTANCE_PARENT)
			||CMath.bset(mob.location().getArea().flags(),Area.FLAG_INSTANCE_CHILD)))
			strOtherRoomID=strStartRoomID;
		
		String pfxml=getPlayerStatsXML(mob);
		StringBuilder cleanXML=new StringBuilder();
		cleanXML.append(CMLib.coffeeMaker().getFactionXML(mob));
		DB.updateWithClobs(
				 "UPDATE CMCHAR SET  CMPASS='"+pstats.getPasswordStr()+"'"
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
				+", CMALIG=-1"
				+", CMEXPE="+mob.getExperience()
				+", CMEXLV="+mob.getExpNextLevel()
				+", CMWORS='"+mob.getWorshipCharID()+"'"
				+", CMPRAC="+mob.getPractices()
				+", CMTRAI="+mob.getTrains()
				+", CMAGEH="+mob.getAgeMinutes()
				+", CMGOLD="+mob.getMoney()
				+", CMWIMP="+mob.getWimpHitPoint()
				+", CMQUES="+mob.getQuestPoint()
				+", CMROID='"+strStartRoomID+"||"+strOtherRoomID+"'"
				+", CMDATE='"+pstats.lastDateTime()+"'"
				+", CMCHAN="+pstats.getChannelMask()
				+", CMATTA="+mob.basePhyStats().attackAdjustment()
				+", CMAMOR="+mob.basePhyStats().armor()
				+", CMDAMG="+mob.basePhyStats().damage()
				+", CMBTMP="+mob.getBitmap()
				+", CMLEIG='"+mob.getLiegeID()+"'"
				+", CMHEIT="+mob.basePhyStats().height()
				+", CMWEIT="+mob.basePhyStats().weight()
				+", CMPRPT=?"
				+", CMCOLR=?"
				+", CMLSIP='"+pstats.lastIP()+"'"
				+", CMEMAL=?"
				+", CMPFIL=?"
				+", CMSAVE=?"
				+", CMMXML=?"
				+", CMDESC=?"
				+"  WHERE CMUSERID='"+mob.Name()+"'"
				,new String[][]{{pstats.getPrompt(),pstats.getColorStr(),pstats.getEmail(),
								 pfxml,mob.baseCharStats().getNonBaseStatsAsString(),cleanXML.toString(),mob.description()}});
		List<String> clanStatements=new LinkedList<String>();
		clanStatements.add("DELETE FROM CMCHCL WHERE CMUSERID='"+mob.Name()+"'");
		for(Pair<Clan,Integer> p : mob.clans())
			clanStatements.add("INSERT INTO CMCHCL (CMUSERID, CMCLAN, CMCLRO) values ('"+mob.Name()+"','"+p.first.clanID()+"',"+p.second.intValue()+")");
		DB.update(clanStatements.toArray(new String[0]));
	}

	protected String getDBItemUpdateString(final MOB mob, final Item thisItem)
	{
		CMLib.catalog().updateCatalogIntegrity(thisItem);
		String container=((thisItem.container()!=null)?(""+thisItem.container()):"");
		return "INSERT INTO CMCHIT (CMUSERID, CMITNM, CMITID, CMITTX, CMITLO, CMITWO, "
		+"CMITUR, CMITLV, CMITAB, CMHEIT"
		+") values ('"+mob.Name()+"','"+(thisItem)+"','"+thisItem.ID()+"',?,'"+container+"',"+thisItem.rawWornCode()+","
		+thisItem.usesRemaining()+","+thisItem.basePhyStats().level()+","+thisItem.basePhyStats().ability()+","
		+thisItem.basePhyStats().height()+")";
	}
	
	private List<DBPreparedBatchEntry> getDBItemUpdateStrings(MOB mob)
	{
		HashSet<String> done=new HashSet<String>();
		List<DBPreparedBatchEntry> strings=new LinkedList<DBPreparedBatchEntry>();
		for(int i=0;i<mob.numItems();i++)
		{
			final Item thisItem=mob.getItem(i);
			if((thisItem!=null)&&(!done.contains(""+thisItem))&&(thisItem.isSavable()))
			{
				CMLib.catalog().updateCatalogIntegrity(thisItem);
				final String str=getDBItemUpdateString(mob,thisItem);
				strings.add(new DBPreparedBatchEntry(str,thisItem.text()+" "));
				done.add(""+thisItem);
			}
		}
		final PlayerStats pStats=mob.playerStats();
		if(pStats !=null)
		{
			final ItemCollection coll=pStats.getExtItems();
			final List<Item> finalCollection=new LinkedList<Item>();
			final List<Item> extraItems=new LinkedList<Item>();
			for(int i=coll.numItems()-1;i>=0;i--)
			{
				final Item thisItem=coll.getItem(i);
				if(thisItem!=null)
				{
					final Item cont=thisItem.ultimateContainer(null);
					if(cont.owner() instanceof Room)
						finalCollection.add(thisItem);
				}
			}
			for(final Item thisItem : finalCollection)
			{
				if(thisItem instanceof Container)
				{
					List<Item> contents=((Container)thisItem).getContents();
					for(Item I : contents)
						if(!finalCollection.contains(I))
							extraItems.add(I);
				}
			}
			finalCollection.addAll(extraItems);
			for(final Item thisItem : finalCollection)
			{
				if(!done.contains(""+thisItem))
				{
					CMLib.catalog().updateCatalogIntegrity(thisItem);
					final Item cont=thisItem.ultimateContainer(null);
					final String str=getDBItemUpdateString(mob,thisItem);
					final String text="<ROOM ID=\""+CMLib.map().getExtendedRoomID((Room)cont.owner())+"\" EXPIRE="+thisItem.expirationDate()+" />"+thisItem.text();
					strings.add(new DBPreparedBatchEntry(str,text));
					done.add(""+thisItem);
				}
			}
		}
		return strings;
	}

	public void DBUpdateItems(MOB mob)
	{
		if(mob.Name().length()==0) return;
		List<DBPreparedBatchEntry> statements=new LinkedList<DBPreparedBatchEntry>();
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMCHIT WHERE CMUSERID='"+mob.Name()+"'"));
		statements.addAll(getDBItemUpdateStrings(mob));
		DB.updateWithClobs(statements);
	}

	// this method is unused, but is a good idea of how to collect riders, followers, carts, etc.
	protected void addFollowerDependent(PhysicalAgent P, DVector list, String parent)
	{
		if(P==null) return;
		if(list.contains(P)) return;
		if((P instanceof MOB)
		&&((!((MOB)P).isMonster())||(((MOB)P).isPossessing())))
			return;
		CMLib.catalog().updateCatalogIntegrity(P);
		String myCode=""+(list.size()-1);
		list.addElement(P,CMClass.classID(P)+"#"+myCode+parent);
		if(P instanceof Rideable)
		{
			Rideable R=(Rideable)P;
			for(int r=0;r<R.numRiders();r++)
				addFollowerDependent(R.fetchRider(r),list,"@"+myCode+"R");
		}
		if(P instanceof Container)
		{
			Container C=(Container)P;
			List<Item> contents=C.getContents();
			for(int c=0;c<contents.size();c++)
				addFollowerDependent((Item)contents.get(c),list,"@"+myCode+"C");
		}

	}

	public void DBUpdateFollowers(MOB mob)
	{
		if((mob==null)||(mob.Name().length()==0)) return;
		List<DBPreparedBatchEntry> statements=new LinkedList<DBPreparedBatchEntry>();
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMCHFO WHERE CMUSERID='"+mob.Name()+"'"));
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB thisMOB=mob.fetchFollower(f);
			if((thisMOB!=null)&&(thisMOB.isMonster())&&(!thisMOB.isPossessing()))
			{
				CMLib.catalog().updateCatalogIntegrity(thisMOB);
				String str="INSERT INTO CMCHFO (CMUSERID, CMFONM, CMFOID, CMFOTX, CMFOLV, CMFOAB"
				+") values ('"+mob.Name()+"',"+f+",'"+CMClass.classID(thisMOB)+"',?,"
				+thisMOB.basePhyStats().level()+","+thisMOB.basePhyStats().ability()+")";
				statements.add(new DBPreparedBatchEntry(str,thisMOB.text()+" "));
			}
		}
		DB.updateWithClobs(statements);
	}

	public void DBNameChange(String oldName, String newName)
	{
		if((oldName==null)
		||(oldName.trim().length()==0)
		||(oldName.indexOf('\'')>=0)
		||(newName==null)
		||(newName.trim().length()==0)
		||(newName.indexOf('\'')>=0))
			return;
		DB.update("UPDATE CMCHAB SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");
		DB.update("UPDATE CMCHAR SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");
		DB.update("UPDATE CMCHAR SET CMWORS='"+newName+"' WHERE CMWORS='"+oldName+"'");
		DB.update("UPDATE CMCHAR SET CMLEIG='"+newName+"' WHERE CMLEIG='"+oldName+"'");
		DB.update("UPDATE CMCHCL SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");
			
		DB.update("UPDATE CMCHFO SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");
		DB.update("UPDATE CMCHIT SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");
		DB.update("UPDATE CMJRNL SET CMFROM='"+newName+"' WHERE CMFROM='"+oldName+"'");
		DB.update("UPDATE CMJRNL SET CMTONM='"+newName+"' WHERE CMTONM='"+oldName+"'");
		DB.update("UPDATE CMPDAT SET CMPLID='"+newName+"' WHERE CMPLID='"+oldName+"'");
	}
	
	public void DBDelete(MOB mob, boolean deleteAssets)
	{
		if(mob.Name().length()==0) return;
		List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.PLAYERPURGES);
		for(int i=0;i<channels.size();i++)
			CMLib.commands().postChannel((String)channels.get(i),mob.clans(),mob.Name()+" has just been deleted.",true);
		CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_PURGES);
		DB.update("DELETE FROM CMCHAR WHERE CMUSERID='"+mob.Name()+"'");
		DB.update("DELETE FROM CMCHCL WHERE CMUSERID='"+mob.Name()+"'");
		mob.delAllItems(false);
		for(int i=0;i<mob.numItems();i++)
		{
			final Item I=mob.getItem(i);
			if(I!=null) I.setContainer(null);
		}
		mob.delAllItems(false);
		DBUpdateItems(mob);
		while(mob.numFollowers()>0)
		{
			MOB follower=mob.fetchFollower(0);
			if(follower!=null) follower.setFollowing(null);
		}
		if(deleteAssets)
		{
			DBUpdateFollowers(mob);
		}
		mob.delAllAbilities();
		DBUpdateAbilities(mob);
		if(deleteAssets)
		{
			CMLib.database().DBDeletePlayerJournals(mob.Name());
			CMLib.database().DBDeletePlayerData(mob.Name());
		}
		PlayerStats pstats = mob.playerStats();
		if(pstats!=null)
		{
			PlayerAccount account = pstats.getAccount();
			if(account != null)
			{
				account.delPlayer(mob);
				DBUpdateAccount(account);
				account.setLastUpdated(System.currentTimeMillis());
			}
		}
		if(deleteAssets)
		{
			for(int q=0;q<CMLib.quests().numQuests();q++)
			{
				Quest Q=CMLib.quests().fetchQuest(q);
				if(Q.wasWinner(mob.Name()))
					Q.declareWinner("-"+mob.Name());
			}
		}
	}

	public void DBUpdateAbilities(MOB mob)
	{
		if(mob.Name().length()==0) return;
		List<DBPreparedBatchEntry> statements=new LinkedList<DBPreparedBatchEntry>();
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMCHAB WHERE CMUSERID='"+mob.Name()+"'"));
		HashSet<String> H=new HashSet<String>();
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability thisAbility=mob.fetchAbility(a);
			if((thisAbility!=null)&&(thisAbility.isSavable()))
			{
				int proficiency=thisAbility.proficiency();
				Ability effectA=mob.fetchEffect(thisAbility.ID());
				if(effectA!=null)
				{
					if((effectA.isSavable())&&(!effectA.canBeUninvoked())&&(!effectA.isAutoInvoked())) proficiency=proficiency-200;
				}
				H.add(thisAbility.ID());
				String str="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
				+") values ('"+mob.Name()+"','"+thisAbility.ID()+"',"+proficiency+",?)";
				statements.add(new DBPreparedBatchEntry(str,thisAbility.text()));
			}
		}
		for(final Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(!H.contains(A.ID()))&&(A.isSavable())&&(!A.canBeUninvoked()))
			{
				String str="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
				+") values ('"+mob.Name()+"','"+A.ID()+"',"+Integer.MAX_VALUE+",?)";
				statements.add(new DBPreparedBatchEntry(str,A.text()));
			}
		}
		for(Enumeration<Behavior> e=mob.behaviors();e.hasMoreElements();)
		{
			Behavior B=e.nextElement();
			if((B!=null)&&(B.isSavable()))
			{
				String str="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
				+") values ('"+mob.Name()+"','"+B.ID()+"',"+(Integer.MIN_VALUE+1)+",?"
				+")";
				statements.add(new DBPreparedBatchEntry(str,B.getParms()));
			}
		}
		String scriptStuff = CMLib.coffeeMaker().getGenScripts(mob,true);
		if(scriptStuff.length()>0)
		{
			String str="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
			+") values ('"+mob.Name()+"','ScriptingEngine',"+(Integer.MIN_VALUE+1)+",?"
			+")";
			statements.add(new DBPreparedBatchEntry(str,scriptStuff));
		}

		DB.updateWithClobs(statements);
	}

	public void DBCreateCharacter(MOB mob)
	{
		if(mob.Name().length()==0) return;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		DB.update("INSERT INTO CMCHAR (CMUSERID, CMPASS, CMCLAS, CMRACE, CMGEND "
				+") VALUES ('"+mob.Name()+"','"+pstats.getPasswordStr()+"','"+mob.baseCharStats().getMyClassesStr()
				+"','"+mob.baseCharStats().getMyRace().ID()+"','"+((char)mob.baseCharStats().getStat(CharStats.STAT_GENDER))
				+"')");
		PlayerAccount account = pstats.getAccount();
		if(account != null)
		{
			account.addNewPlayer(mob);
			DBUpdateAccount(account);
			account.setLastUpdated(System.currentTimeMillis());
		}
	}

	public void DBUpdateAccount(PlayerAccount account)
	{
		if(account == null) return;
		String characters = CMParms.toSemicolonList(account.getPlayers());
		DB.updateWithClobs("UPDATE CMACCT SET CMPASS='"+account.getPasswordStr()+"',  CMCHRS=?,  CMAXML=?  WHERE CMANAM='"+account.accountName()+"'",
				new String[][]{{characters,account.getXML()}});
	}

	public void DBDeleteAccount(PlayerAccount account)
	{
		if(account == null) return;
		DB.update("DELETE FROM CMACCT WHERE CMANAM='"+account.accountName()+"'");
	}

	public void DBCreateAccount(PlayerAccount account)
	{
		if(account == null) return;
		account.setAccountName(CMStrings.capitalizeAndLower(account.accountName()));
		String characters = CMParms.toSemicolonList(account.getPlayers());
		DB.updateWithClobs("INSERT INTO CMACCT (CMANAM, CMPASS, CMCHRS, CMAXML) "
				+"VALUES ('"+account.accountName()+"','"+account.getPasswordStr()+"',?,?)",new String[][]{{characters,account.getXML()}});
	}
	
	public PlayerAccount MakeAccount(String username, ResultSet R) throws SQLException
	{
		PlayerAccount account = null;
		account = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
		String password=DB.getRes(R,"CMPASS");
		String chrs=DB.getRes(R,"CMCHRS");
		String xml=DB.getRes(R,"CMAXML");
		Vector<String> names = new Vector<String>();
		if(chrs!=null) names.addAll(CMParms.parseSemicolons(chrs,true));
		account.setAccountName(CMStrings.capitalizeAndLower(username));
		account.setPassword(password);
		account.setPlayerNames(names);
		account.setXML(xml);
		return account;
	}

	public PlayerAccount DBReadAccount(String Login)
	{
		DBConnection D=null;
		PlayerAccount account = null;
		try
		{
			// why in the hell is this a memory scan?
			// case insensitivity from databases configured almost
			// certainly by amateurs is the answer. That, and fakedb 
			// doesn't understand 'LIKE'
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMACCT WHERE CMANAM='"+CMStrings.replaceAll(CMStrings.capitalizeAndLower(Login),"\'", "n")+"'");
			if(R!=null) while(R.next())
			{
				String username=DB.getRes(R,"CMANAM");
				if(Login.equalsIgnoreCase(username))
					account = MakeAccount(username,R);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return account;
	}
	
	public List<PlayerAccount> DBListAccounts(String mask)
	{
		DBConnection D=null;
		PlayerAccount account = null;
		Vector<PlayerAccount> accounts = new Vector<PlayerAccount>();
		if(mask!=null) mask=mask.toLowerCase();
		try
		{
			// why in the hell is this a memory scan?
			// case insensitivity from databases configured almost
			// certainly by amateurs is the answer. That, and fakedb 
			// doesn't understand 'LIKE'
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMACCT");
			if(R!=null) while(R.next())
			{
				String username=DB.getRes(R,"CMANAM");
				if((mask==null)||(mask.length()==0)||(username.toLowerCase().indexOf(mask)>=0))
				{
					account = MakeAccount(username,R);
					accounts.add(account);
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return accounts;
	}
	
	public PlayerLibrary.ThinnerPlayer DBUserSearch(String Login)
	{
		DBConnection D=null;
		String buf=null;
		PlayerLibrary.ThinnerPlayer thinPlayer = null;
		try
		{
			// why in the hell is this a memory scan?
			// case insensitivity from databases configured almost
			// certainly by amateurs is the answer. That, and fakedb 
			// doesn't understand 'LIKE'
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+CMStrings.capitalizeAndLower(Login).replace('\'', 'n')+"'");
			if(R!=null) while(R.next())
			{
				String username=DB.getRes(R,"CMUSERID");
				thinPlayer = new PlayerLibrary.ThinnerPlayer();
				String password=DB.getRes(R,"CMPASS");
				String email=DB.getRes(R,"CMEMAL");
				thinPlayer.name=username;
				thinPlayer.password=password;
				thinPlayer.email=email;
				// Acct Exp Code
				buf=DBConnections.getRes(R,"CMPFIL");
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		if((buf!=null)&&(thinPlayer!=null))
		{
			PlayerAccount acct = null;
			thinPlayer.accountName = CMLib.xml().returnXMLValue(buf,"ACCOUNT");
			if((thinPlayer.accountName!=null)&&(thinPlayer.accountName.length()>0))
				acct = CMLib.players().getLoadAccount(thinPlayer.accountName);
			if((acct != null)&&(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1))
				thinPlayer.expiration=acct.getAccountExpiration();
			else
			if(CMLib.xml().returnXMLValue(buf,"ACCTEXP").length()>0)
				thinPlayer.expiration=CMath.s_long(CMLib.xml().returnXMLValue(buf,"ACCTEXP"));
			else
			{
				Calendar C=Calendar.getInstance();
				C.add(Calendar.DATE,CMProps.getIntVar(CMProps.SYSTEMI_TRIALDAYS));
				thinPlayer.expiration=C.getTimeInMillis();
			}
		}
		return thinPlayer;
	}

	public String[] DBFetchEmailData(String name)
	{
		String[] data=new String[2];
		for(Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
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
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+name.replace('\'', 'n')+"'");
			if(R!=null) while(R.next())
			{
				// String username=DB.getRes(R,"CMUSERID");
				int btmp=CMath.s_int(DB.getRes(R,"CMBTMP"));
				String temail=DB.getRes(R,"CMEMAL");
				data[0]=temail;
				data[1]=""+((btmp&MOB.ATT_AUTOFORWARD)==MOB.ATT_AUTOFORWARD);
				return data;
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public String DBEmailSearch(String email)
	{
		DBConnection D=null;
		for(Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
		{
			MOB M=(MOB)e.nextElement();
			if((M.playerStats()!=null)&&(M.playerStats().getEmail().equalsIgnoreCase(email))) return M.Name();
		}
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null) while(R.next())
			{
				String username=DB.getRes(R,"CMUSERID");
				String temail=DB.getRes(R,"CMEMAL");
				if(temail.equalsIgnoreCase(email))
				{
					return username;
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}
}
