package com.planet_ink.coffee_mud.db;

import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
public class MOBloader
{
	
	public static Hashtable MOBs=new Hashtable();
	
	public static void DBRead(MOB mob)
	{
		if(mob.ID().length()==0) return;
		
		DBConnection D=null;
		try
		{
			D=MUD.DBs.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+mob.ID()+"'");
			while(R.next())
			{
				CharStats stats=mob.baseCharStats();
				CharState state=mob.maxState();
				String username=DBConnections.getRes(R,"CMUSERID");
				String password=DBConnections.getRes(R,"CMPASS");
				String classID=DBConnections.getRes(R,"CMCLAS");
				stats.setMyClass((CharClass)MUD.getCharClass(classID));
				stats.setStrength(Util.s_int(DBConnections.getRes(R,"CMSTRE")));
				stats.setMyRace((Race)MUD.getRace(DBConnections.getRes(R,"CMRACE")));
				stats.setDexterity(Util.s_int(DBConnections.getRes(R,"CMDEXT")));
				stats.setConstitution(Util.s_int(DBConnections.getRes(R,"CMCONS")));
				stats.setGender(DBConnections.getRes(R,"CMGEND").charAt(0));
				stats.setWisdom(Util.s_int(DBConnections.getRes(R,"CMWISD")));
				stats.setIntelligence(Util.s_int(DBConnections.getRes(R,"CMINTE")));
				stats.setCharisma(Util.s_int(DBConnections.getRes(R,"CMCHAR")));
				state.setHitPoints(Util.s_int(DBConnections.getRes(R,"CMHITP")));
				mob.baseEnvStats().setLevel(Util.s_int(DBConnections.getRes(R,"CMLEVL")));
				state.setMana(Util.s_int(DBConnections.getRes(R,"CMMANA")));
				state.setMovement(Util.s_int(DBConnections.getRes(R,"CMMOVE")));
				mob.setDescription(DBConnections.getRes(R,"CMDESC"));
				mob.setAlignment(Util.s_int(DBConnections.getRes(R,"CMALIG")));
				mob.setExperience(Util.s_int(DBConnections.getRes(R,"CMEXPE")));
				mob.setExpNextLevel(Util.s_int(DBConnections.getRes(R,"CMEXLV")));
				mob.setWorshipCharID(DBConnections.getRes(R,"CMWORS"));
				mob.setPractices(Util.s_int(DBConnections.getRes(R,"CMPRAC")));
				mob.setTrains(Util.s_int(DBConnections.getRes(R,"CMTRAI")));
				mob.setAgeHours(Long.parseLong(DBConnections.getRes(R,"CMAGEH")));
				mob.setMoney(Util.s_int(DBConnections.getRes(R,"CMGOLD")));
				mob.setWimpHitPoint(Util.s_int(DBConnections.getRes(R,"CMWIMP")));
				mob.setQuestPoint(Util.s_int(DBConnections.getRes(R,"CMQUES")));
				mob.setStartRoom(MUD.getRoom(DBConnections.getRes(R,"CMROID")));
				Calendar lastDateTime=(Calendar)new IQCalendar();
				lastDateTime=(Calendar)IQCalendar.string2Date(DBConnections.getRes(R,"CMDATE"));
				mob.setChannelMask(DBConnections.getLongRes(R,"CMCHAN"));
				mob.setUserInfo(username,password,mob.lastDateTime());
				mob.baseEnvStats().setAttackAdjustment(Util.s_int(DBConnections.getRes(R,"CMATTA")));
				mob.baseEnvStats().setArmor(Util.s_int(DBConnections.getRes(R,"CMAMOR")));
				mob.baseEnvStats().setDamage(Util.s_int(DBConnections.getRes(R,"CMDAMG")));
			}
			MUD.DBs.DBDone(D);					
		}
		catch(SQLException sqle)
		{
			Log.errOut("MOB",sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
		
		// now grab the items
		try
		{
			D=MUD.DBs.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHIT WHERE CMUSERID='"+mob.ID()+"' order by CMITNM");
			Hashtable itemNums=new Hashtable();
			while(R.next())
			{
				int itemNumber=(int)DBConnections.getLongRes(R,"CMITNM");
				String itemID=DBConnections.getRes(R,"CMITID");
				Item newItem=(Item)MUD.getItem(itemID);
				if(newItem==null)
					Log.errOut("MOB","Couldn't find item '"+itemID+"'");
				else
				{
					newItem=(Item)newItem.newInstance();
					newItem.setMiscText(DBConnections.getResQuietly(R,"CMITTX"));
					int locationNumber=(int)DBConnections.getLongRes(R,"CMITLO");
					newItem.setLocation((Item)itemNums.get(new Integer(locationNumber)));
					newItem.wear((int)DBConnections.getLongRes(R,"CMITWO"));
					newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
					newItem.baseEnvStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
					newItem.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
					newItem.recoverEnvStats();
					mob.addInventory(newItem);
					itemNums.put(new Integer(itemNumber),newItem);
				}
			}
			MUD.DBs.DBDone(D);					
		}
		catch(SQLException sqle)
		{
			Log.errOut("MOB",sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
		
		// now grab the abilities
		try
		{
			D=MUD.DBs.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAB WHERE CMUSERID='"+mob.ID()+"'");
			while(R.next())
			{
				String abilityID=DBConnections.getRes(R,"CMABID");
				Ability newAbility=(Ability)MUD.getAbility(abilityID);
				if(newAbility==null)
					Log.errOut("MOB","Couldn't find ability '"+abilityID+"'");
				else
				{
					newAbility=(Ability)newAbility.newInstance();
					newAbility.baseEnvStats().setLevel((int)DBConnections.getLongRes(R,"CMABLVL"));
					newAbility.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMABAB"));
					newAbility.setUsesRemaining((int)DBConnections.getLongRes(R,"CMABUR"));
					newAbility.setProfficiency((int)DBConnections.getLongRes(R,"CMABPF"));
					newAbility.recoverEnvStats();
					mob.addAbility(newAbility);
				}
			}
			MUD.DBs.DBDone(D);					
		}
		catch(SQLException sqle)
		{
			Log.errOut("MOB",sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
		mob.baseCharStats().getMyRace().setWeight(mob);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		
		if(MOBs.get(mob.ID())==null)
			MOBs.put(mob.ID(),mob);
	}
	
	public static void DBReadFollowers(MOB mob)
	{
		DBConnection D=null;
		// now grab the followers
		Room location=mob.location();
		if(location==null)
			location=mob.getStartRoom();
		try
		{
			D=MUD.DBs.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHFO WHERE CMUSERID='"+mob.ID()+"' order by CMFONM");
			while(R.next())
			{
				String MOBID=DBConnections.getRes(R,"CMFOID");
				MOB newMOB=(MOB)MUD.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("MOB","Couldn't find MOB '"+MOBID+"'");
				else
				{
					newMOB=(MOB)newMOB.newInstance();
					if(location==null)
					{
						newMOB.setStartRoom(newMOB.getStartRoom());
						newMOB.setLocation(newMOB.getStartRoom());
					}
					else
					{
						newMOB.setStartRoom(location);
						newMOB.setLocation(location);
					}
					newMOB.setMiscText(DBConnections.getResQuietly(R,"CMFOTX"));
					newMOB.baseEnvStats().setLevel(((int)DBConnections.getLongRes(R,"CMFOLV")));
					newMOB.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMFOAB"));
					newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
					newMOB.baseCharStats().getMyRace().setWeight(newMOB);
					newMOB.recoverCharStats();
					newMOB.recoverMaxState();
					newMOB.recoverEnvStats();
					newMOB.setFollowing(mob);
					newMOB.bringToLife(mob.location());
				}
			}
			MUD.DBs.DBDone(D);					
		}
		catch(SQLException sqle)
		{
			Log.errOut("MOB",sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
		
	}
	
	public static void DBUpdate(MOB mob)
	{
		if(mob.ID().length()==0)
		{
			DBCreateCharacter(mob); 
			return;
		}
		
		DBConnection D=null;
		String strStartRoomID=""; 
		if(mob.getStartRoom()!=null) 
			strStartRoomID=mob.getStartRoom().ID();
		try
		{
			D=MUD.DBs.DBFetch();
			D.update("UPDATE CMCHAR SET"
			+"  CMPASS='"+mob.password()+"'"
			+", CMCLAS='"+Util.id(mob.baseCharStats().getMyClass())+"'"
			+", CMSTRE="+mob.baseCharStats().getStrength()
			+", CMRACE='"+Util.id(mob.baseCharStats().getMyRace())+"'"
			+", CMDEXT="+mob.baseCharStats().getDexterity()
			+", CMCONS="+mob.baseCharStats().getConstitution()
			+", CMGEND='"+mob.baseCharStats().getGender()+"'"
			+", CMWISD="+mob.baseCharStats().getWisdom()
			+", CMINTE="+mob.baseCharStats().getIntelligence()
			+", CMCHAR="+mob.baseCharStats().getCharisma()
			+", CMHITP="+mob.maxState().getHitPoints()
			+", CMLEVL="+mob.baseEnvStats().level()
			+", CMMANA="+mob.maxState().getMana()
			+", CMMOVE="+mob.maxState().getMovement()
			+", CMALIG="+mob.getAlignment()
			+", CMEXPE="+mob.getExperience()
			+", CMEXLV="+mob.getExpNextLevel()
			+", CMWORS='"+mob.getWorshipCharID()+"'"
			+", CMPRAC="+mob.getPractices()
			+", CMTRAI="+mob.getTrains()
			+", CMAGEH="+mob.getAgeHours()
			+", CMGOLD="+mob.getMoney()
			+", CMWIMP="+mob.getWimpHitPoint()
			+", CMQUES="+mob.getQuestPoint()
			+", CMROID='"+strStartRoomID+"'"
			+", CMDATE='"+new IQCalendar(mob.lastDateTime()).d2mysqlString()+"'"
			+", CMCHAN="+mob.getChannelMask()
			+", CMATTA="+mob.baseEnvStats().attackAdjustment()
			+", CMAMOR="+mob.baseEnvStats().armor()
			+", CMDAMG="+mob.baseEnvStats().damage()
			+" WHERE CMUSERID='"+mob.ID()+"'");
			MUD.DBs.DBDone(D);
			
			D=MUD.DBs.DBFetch();
			D.update("UPDATE CMCHAR SET"
			+" CMDESC='"+mob.description()+"'"
			+" WHERE CMUSERID='"+mob.ID()+"'");
			MUD.DBs.DBDone(D);					
		}
		catch(SQLException sqle)
		{
			Log.errOut("MOB","UpdateStats:"+sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
		DBUpdateItems(mob);
		DBUpdateAbilities(mob);
	}
	private static int DBUpdateContents(MOB mob, 
										Item item, 
										int itemNumber)
		throws SQLException
	{
		DBConnection D=null;
		int newItemNumber=itemNumber;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item thisItem=mob.fetchInventory(i);
			if(thisItem.location()==item)
			{
				D=MUD.DBs.DBFetch();
				D.update("INSERT INTO CMCHIT ("
						 +"CMUSERID, "
						 +"CMITNM, "
						 +"CMITID, "
						 +"CMITTX, "
						 +"CMITLO, "
						 +"CMITWO, "
						 +"CMITUR, "
						 +"CMITLV, "
						 +"CMITAB"
						 +") values ("
						 +"'"+mob.ID()+"',"
						 +(++newItemNumber)+","
						 +"'"+thisItem.ID()+"',"
						 +"'"+thisItem.text()+" ',"
						 +Integer.toString(itemNumber)+","
						 +thisItem.rawWornCode()+","
						 +thisItem.usesRemaining()+","
						 +thisItem.baseEnvStats().level()+","
						 +thisItem.baseEnvStats().ability()+")");
				MUD.DBs.DBDone(D);
				newItemNumber=DBUpdateContents(mob,thisItem,newItemNumber);
			}
		}
		return newItemNumber;
	}
	
	public static void DBUpdateItems(MOB mob)
	{
		if(mob.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=MUD.DBs.DBFetch();
			D.update("DELETE FROM CMCHIT WHERE CMUSERID='"+mob.ID()+"'");
			if(MUD.DBConfirmDeletions)
			{
				ResultSet R=D.query("SELECT * FROM CMCHIT WHERE CMUSERID='"+mob.ID()+"'");
				if(R.next())
					Log.errOut("DBUpdateItems","Delete Failed.");
			}
			MUD.DBs.DBDone(D);
			if(mob.inventorySize()>0)
				DBUpdateContents(mob,null,-1);
		}
		catch(SQLException sqle)
		{
			Log.errOut("MOB","UpdateItems"+sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
	}
	
	public static void DBUpdateFollowers(MOB mob)
	{
		if(mob.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=MUD.DBs.DBFetch();
			D.update("DELETE FROM CMCHFO WHERE CMUSERID='"+mob.ID()+"'");
			if(MUD.DBConfirmDeletions)
			{
				ResultSet R=D.query("SELECT * FROM CMCHFO WHERE CMUSERID='"+mob.ID()+"'");
				if(R.next())
					Log.errOut("DBUpdateFollowers","Delete Failed.");
			}
			MUD.DBs.DBDone(D);
			for(int f=0;f<mob.numFollowers();f++)
			{
				MOB thisMOB=mob.fetchFollower(f);
				if(thisMOB.isMonster())
				{
					D=MUD.DBs.DBFetch();
					D.update("INSERT INTO CMCHFO ("
							 +"CMUSERID, "
							 +"CMFONM, "
							 +"CMFOID, "
							 +"CMFOTX, "
							 +"CMFOLV, "
							 +"CMFOAB"
							 +") values ("
							 +"'"+mob.ID()+"',"
							 +f+","
							 +"'"+INI.className(thisMOB)+"',"
							 +"'"+thisMOB.text()+" ',"
							 +thisMOB.baseEnvStats().level()+","
							 +thisMOB.baseEnvStats().ability()
							 +")");
					MUD.DBs.DBDone(D);
				}
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("MOB","UpdateFollowers"+sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
	}
	
	public static void DBDelete(MOB mob)
	{
		if(mob.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=MUD.DBs.DBFetch();
			D.update("DELETE FROM CMCHAR WHERE CMUSERID='"+mob.ID()+"'");
			if(MUD.DBConfirmDeletions)
			{
				ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+mob.ID()+"'");
				if(R.next())
					Log.errOut("DBDeleteMOB","Delete Failed.");
			}
			MUD.DBs.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("MOB","DBDelete"+sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
		while(mob.inventorySize()>0)
		{
			Item thisItem=mob.fetchInventory(0);
			thisItem.setLocation(null);
			mob.delInventory(thisItem);
		}
		DBUpdateItems(mob);
		
		while(mob.numFollowers()>0)
			mob.fetchFollower(0).setFollowing(null);
		DBUpdateFollowers(mob);
		
		while(mob.numAbilities()>0)
			mob.delAbility(mob.fetchAbility(0));
		DBUpdateAbilities(mob);
	}
	
	public static void DBUpdateAbilities(MOB mob)
	{
		if(mob.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=MUD.DBs.DBFetch();
			D.update("DELETE FROM CMCHAB WHERE CMUSERID='"+mob.ID()+"'");
			if(MUD.DBConfirmDeletions)
			{
				ResultSet R=D.query("SELECT * FROM CMCHAB WHERE CMUSERID='"+mob.ID()+"'");
				if(R.next())
					Log.errOut("DBUpdateAbilitiess","Delete Failed.");
			}
			MUD.DBs.DBDone(D);
			for(int a=0;a<mob.numAbilities();a++)
			{
				Ability thisAbility=mob.fetchAbility(a);
				D=MUD.DBs.DBFetch();
				D.update("INSERT INTO CMCHAB ("
						 +"CMUSERID, "
						 +"CMABID, "
						 +"CMABLVL, "
						 +"CMABAB, "
						 +"CMABUR,"
						 +"CMABPF"
						 +") values ("
						 +"'"+mob.ID()+"',"
						 +"'"+thisAbility.ID()+"',"
						 +thisAbility.baseEnvStats().level()+","
						 +thisAbility.baseEnvStats().ability()+","
						 +thisAbility.usesRemaining()+","
						 +thisAbility.profficiency()
						 +")");
				MUD.DBs.DBDone(D);
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("MOB","UpdateAbilities"+sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
	}
	
	public static void DBCreateCharacter(MOB mob)
	{
		if(mob.ID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=MUD.DBs.DBFetch();
			
			D.update("INSERT INTO CMCHAR ("
			+"CMUSERID, "
			+"CMPASS, "
			+"CMCLAS, "
			+"CMRACE, "
			+"CMGEND "
			+") VALUES ('"
			+mob.ID()
			+"','"+mob.password()
			+"','"+Util.id(mob.baseCharStats().getMyClass())
			+"','"+Util.id(mob.baseCharStats().getMyRace())
			+"','"+mob.baseCharStats().getGender()+"');");
			MUD.DBs.DBDone(D);
			DBUpdate(mob);
		}
		catch(SQLException sqle)
		{
			Log.errOut("MOB","Create:"+sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
	}

	public static void DBUserSearch(MOB mob, String Login)
	{
		DBConnection D=null;
		mob.setUserInfo("","",Calendar.getInstance());
		try
		{
			D=MUD.DBs.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+Login+"'");
			while(R.next())
			{
				String username=MUD.DBs.getRes(R,"CMUSERID");
				String password=MUD.DBs.getRes(R,"CMPASS");
				Calendar newCalendar=(Calendar)IQCalendar.string2Date(MUD.DBs.getRes(R,"CMDATE"));
				mob.setUserInfo(username,password,newCalendar);
			}
			MUD.DBs.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("MOB",sqle);
			if(D!=null) MUD.DBs.DBDone(D);
		}
	}
	
}
