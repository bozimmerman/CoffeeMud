package com.planet_ink.coffee_mud.system;

import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
public class MOBloader
{
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
				PlayerStats pstats=new DefaultPlayerStats();
				mob.setPlayerStats(pstats);
				String username=DBConnections.getRes(R,"CMUSERID");
				String password=DBConnections.getRes(R,"CMPASS");
				mob.setName(username);
				pstats.setPassword(password);
				stats.setMyClasses(DBConnections.getRes(R,"CMCLAS"));
				stats.setStat(CharStats.STRENGTH,Util.s_int(DBConnections.getRes(R,"CMSTRE")));
				stats.setMyRace((Race)CMClass.getRace(DBConnections.getRes(R,"CMRACE")));
				stats.setStat(CharStats.DEXTERITY,Util.s_int(DBConnections.getRes(R,"CMDEXT")));
				stats.setStat(CharStats.CONSTITUTION,Util.s_int(DBConnections.getRes(R,"CMCONS")));
				stats.setStat(CharStats.GENDER,DBConnections.getRes(R,"CMGEND").charAt(0));
				stats.setStat(CharStats.WISDOM,Util.s_int(DBConnections.getRes(R,"CMWISD")));
				stats.setStat(CharStats.INTELLIGENCE,Util.s_int(DBConnections.getRes(R,"CMINTE")));
				stats.setStat(CharStats.CHARISMA,Util.s_int(DBConnections.getRes(R,"CMCHAR")));
				state.setHitPoints(Util.s_int(DBConnections.getRes(R,"CMHITP")));
				stats.setMyLevels(DBConnections.getRes(R,"CMLEVL"));
				int level=0;
				for(int i=0;i<mob.baseCharStats().numClasses();i++)
					level+=stats.getClassLevel(mob.baseCharStats().getMyClass(i));
				mob.baseEnvStats().setLevel(level);
				state.setMana(Util.s_int(DBConnections.getRes(R,"CMMANA")));
				state.setMovement(Util.s_int(DBConnections.getRes(R,"CMMOVE")));
				mob.setDescription(DBConnections.getRes(R,"CMDESC"));
				mob.setAlignment(Util.s_int(DBConnections.getRes(R,"CMALIG")));
				mob.setExperience(Util.s_int(DBConnections.getRes(R,"CMEXPE")));
				mob.setExpNextLevel(Util.s_int(DBConnections.getRes(R,"CMEXLV")));
				mob.setWorshipCharID(DBConnections.getRes(R,"CMWORS"));
				mob.setPractices(Util.s_int(DBConnections.getRes(R,"CMPRAC")));
				mob.setTrains(Util.s_int(DBConnections.getRes(R,"CMTRAI")));
				mob.setAgeHours(Util.s_long(DBConnections.getRes(R,"CMAGEH")));
				mob.setMoney(Util.s_int(DBConnections.getRes(R,"CMGOLD")));
				mob.setWimpHitPoint(Util.s_int(DBConnections.getRes(R,"CMWIMP")));
				mob.setQuestPoint(Util.s_int(DBConnections.getRes(R,"CMQUES")));
				String roomID=DBConnections.getRes(R,"CMROID");
				if(roomID==null) roomID="";
				int x=roomID.indexOf("||");
				if(x>=0)
				{
					mob.setLocation(CMMap.getRoom(roomID.substring(x+2)));
					roomID=roomID.substring(0,x);
				}
				mob.setStartRoom(CMMap.getRoom(roomID));
				pstats.setLastDateTime(Util.s_long(DBConnections.getRes(R,"CMDATE")));
				pstats.setChannelMask((int)DBConnections.getLongRes(R,"CMCHAN"));
				mob.baseEnvStats().setAttackAdjustment(Util.s_int(DBConnections.getRes(R,"CMATTA")));
				mob.baseEnvStats().setArmor(Util.s_int(DBConnections.getRes(R,"CMAMOR")));
				mob.baseEnvStats().setDamage(Util.s_int(DBConnections.getRes(R,"CMDAMG")));
				mob.setBitmap(Util.s_int(DBConnections.getRes(R,"CMBTMP")));
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
				pstats.setFriendsIgnoreStr(buf);
				stats.setSaves(DBConnections.getRes(R,"CMSAVE"));
				Vector V9=Util.parseSemicolons(XMLManager.returnXMLBlock(buf,"TATTS"),true);
				while(mob.numTattoos()>0)mob.delTattoo(mob.fetchTattoo(0));
				for(int v=0;v<V9.size();v++) mob.addTattoo((String)V9.elementAt(v));
			
				V9=Util.parseSemicolons(XMLManager.returnXMLBlock(buf,"EDUS"),true);
				while(mob.numEducations()>0)mob.delEducation(mob.fetchEducation(0));
				for(int v=0;v<V9.size();v++) mob.addEducation((String)V9.elementAt(v));

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

		DBReadUserOnly(mob);

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
				Item newItem=(Item)CMClass.getItem(itemID);
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
					Behavior newBehavior=(Behavior)CMClass.getBehavior(abilityID);
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
					Ability newAbility=(Ability)CMClass.getAbility(abilityID);
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

		if(CMMap.getPlayer(mob.Name())==null)
			CMMap.addPlayer(mob);
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
						cclass=CMClass.getCharClass(cclass.substring(x+1)).name();
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
						level+=Util.s_int(lvl.substring(0,x));
						lvl=lvl.substring(x+1);
						x=lvl.indexOf(";");
					}
					if(lvl.length()>0) level+=Util.s_int(lvl);
					thisUser.addElement(new Integer(level).toString());
					thisUser.addElement(DBConnections.getRes(R,"CMAGEH"));
					MOB M=(MOB)CMMap.getPlayer((String)thisUser.firstElement());
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
			head.append(Util.padRight("Race",8)+" ");
			head.append(Util.padRight("Class",10)+" ");
			head.append(Util.padRight("Lvl",4)+" ");
			head.append(Util.padRight("Exp/Lvl",17));
			head.append("] Character name\n\r");
			HashSet done=new HashSet();
			if(R!=null)
			while(R.next())
			{
				String username=DBConnections.getRes(R,"CMUSERID");
				MOB M=CMMap.getPlayer(username);
				if(M==null)
				{
					done.add(username);
					String cclass=DBConnections.getRes(R,"CMCLAS");
					int x=cclass.lastIndexOf(";");
					if((x>0)&&(x<cclass.length()-2))
						cclass=CMClass.getCharClass(cclass.substring(x+1)).name();
					String race=((Race)CMClass.getRace(DBConnections.getRes(R,"CMRACE"))).name();
					String lvl=DBConnections.getRes(R,"CMLEVL");
					x=lvl.indexOf(";");
					int level=0;
					while(x>=0)
					{
						level+=Util.s_int(lvl.substring(0,x));
						lvl=lvl.substring(x+1);
						x=lvl.indexOf(";");
					}
					if(lvl.length()>0) level+=Util.s_int(lvl);
					int exp=Util.s_int(DBConnections.getRes(R,"CMEXPE"));
					int exlv=Util.s_int(DBConnections.getRes(R,"CMEXLV"));
					head.append("[");
					head.append(Util.padRight(race,8)+" ");
					head.append(Util.padRight(cclass,10)+" ");
					head.append(Util.padRight(Integer.toString(level),4)+" ");
					head.append(Util.padRight(exp+"/"+exlv,17));
					head.append("] "+Util.padRight(username,15));
					head.append("\n\r");
				}
			}
			for(Enumeration e=CMMap.players();e.hasMoreElements();)
			{
				MOB M=(MOB)e.nextElement();
				if((M.getLiegeID().equals(liegeID))
				&&(!done.contains(M.Name())))
				{
					head.append("[");
					head.append(Util.padRight(M.charStats().getMyRace().name(),8)+" ");
					head.append(Util.padRight(M.charStats().getCurrentClass().name(),10)+" ");
					head.append(Util.padRight(""+M.envStats().level(),4)+" ");
					head.append(Util.padRight(M.getExperience()+"/"+M.getExpNextLevel(),17));
					head.append("] "+Util.padRight(M.name(),15));
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
				MOB newMOB=(MOB)CMClass.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("MOB","Couldn't find MOB '"+MOBID+"'");
				else
				{
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
					newMOB.recoverEnvStats();
					newMOB.recoverCharStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.setFollowing(mob);
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
				long lastDateTime=Util.s_long(DBConnections.getRes(R,"CMDATE"));
				int role=(int)DBConnector.getLongRes(R,"CMCLRO");
				members.addElement(username);
				roles.addElement(new Integer(role));
				MOB M=(MOB)CMMap.getPlayer(username);
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
		MOB M=CMMap.getPlayer(name);
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

		String strStartRoomID=(mob.getStartRoom()!=null)?CMMap.getExtendedRoomID(mob.getStartRoom()):"";
		String strOtherRoomID=(mob.location()!=null)?CMMap.getExtendedRoomID(mob.location()):"";
		StringBuffer pfxml=new StringBuffer(pstats.getFriendsIgnoreStr());
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
		
		DBConnector.update(
		"UPDATE CMCHAR SET"
		+"  CMPASS='"+pstats.password()+"'"
		+", CMCLAS='"+mob.baseCharStats().getMyClassesStr()+"'"
		+", CMSTRE="+mob.baseCharStats().getStat(CharStats.STRENGTH)
		+", CMRACE='"+mob.baseCharStats().getMyRace().ID()+"'"
		+", CMDEXT="+mob.baseCharStats().getStat(CharStats.DEXTERITY)
		+", CMCONS="+mob.baseCharStats().getStat(CharStats.CONSTITUTION)
		+", CMGEND='"+((char)mob.baseCharStats().getStat(CharStats.GENDER))+"'"
		+", CMWISD="+mob.baseCharStats().getStat(CharStats.WISDOM)
		+", CMINTE="+mob.baseCharStats().getStat(CharStats.INTELLIGENCE)
		+", CMCHAR="+mob.baseCharStats().getStat(CharStats.CHARISMA)
		+", CMHITP="+mob.baseState().getHitPoints()
		+", CMLEVL='"+mob.baseCharStats().getMyLevelsStr()+"'"
		+", CMMANA="+mob.baseState().getMana()
		+", CMMOVE="+mob.baseState().getMovement()
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
		+", CMSAVE='"+mob.baseCharStats().getSavesStr()+"'"
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
		Vector V=new Vector();
		V.addElement("DELETE FROM CMCHFO WHERE CMUSERID='"+mob.Name()+"'");
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB thisMOB=mob.fetchFollower(f);
			if((thisMOB!=null)&&(thisMOB.isMonster()))
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
		CommonMsgs.channel("WIZINFO","",mob.Name()+" has just been deleted.",true);
		CoffeeTables.bump(mob,CoffeeTables.STAT_PURGES);
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
		Vector V=new Vector();
		HashSet H=new HashSet();
		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability thisAbility=mob.fetchAbility(a);
			if((thisAbility!=null)&&(!thisAbility.isBorrowed(mob)))
			{
				int profficiency=thisAbility.profficiency();
				H.add(thisAbility.ID());
				
				
				Ability effectA=mob.fetchEffect(thisAbility.ID());
				if((effectA!=null)&&(!effectA.isBorrowed(mob))
				&&((!effectA.canBeUninvoked())&&(!effectA.isAutoInvoked())))
					profficiency=profficiency-200;
				
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
			&&(!thisAffect.isBorrowed(mob))
			&&((!thisAffect.canBeUninvoked())&&(!thisAffect.isAutoInvoked())))
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
			if(thisBehavior!=null)
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
		+"','"+((char)mob.baseCharStats().getStat(CharStats.GENDER))+"');");
	}

	public static boolean DBUserSearch(MOB mob, String Login)
	{
		DBConnection D=null;
		boolean returnable=false;

		if(mob!=null)
		{
			if(mob.playerStats()==null)
				mob.setPlayerStats(new DefaultPlayerStats());
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
		for(Enumeration e=CMMap.players();e.hasMoreElements();)
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
				String username=DBConnector.getRes(R,"CMUSERID");
				int btmp=Util.s_int(DBConnector.getRes(R,"CMBTMP"));
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
		for(Enumeration e=CMMap.players();e.hasMoreElements();)
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