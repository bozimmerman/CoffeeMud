package com.planet_ink.coffee_mud.system;

import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
public class RoomLoader
{
	private static int recordCount=1;
	private static int currentRecordPos=1;
	private static int updateBreak=1;
	private final static String zeroes="000000000000";

	public static void DBRead(Host myHost)
	{
		Hashtable hash=new Hashtable();
		while(CMMap.numAreas()>0)CMMap.delArea(CMMap.getFirstArea());

		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMAREA");
			R.last(); recordCount=R.getRow(); R.beforeFirst();
			updateBreak=Util.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String areaName=DBConnections.getRes(R,"CMAREA");
				String areaType=DBConnections.getRes(R,"CMTYPE");
				Area A=CMClass.getAreaType(areaType);
				if(A==null) A=CMClass.getAreaType("StdArea");
				if(A==null)
				{
					Log.errOut("Could not create area: "+areaName);
					return;
				}
				A.setName(areaName);
				CMMap.addArea(A);
				A.setClimateType((int)DBConnections.getLongRes(R,"CMCLIM"));
				A.setSubOpList(DBConnections.getRes(R,"CMSUBS"));
				A.setDescription(DBConnections.getRes(R,"CMDESC"));
				A.setMiscText(DBConnections.getRes(R,"CMROTX"));
				A.setTechLevel((int)DBConnections.getLongRes(R,"CMTECH"));
				A.tickControl(true);
				if((currentRecordPos%updateBreak)==0)
					myHost.setGameStatusStr("Booting: Loading Areas ("+currentRecordPos+" of "+recordCount+")");
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Area",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}

		Hashtable newAreasToCreate=new Hashtable();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROOM");
			R.last(); recordCount=R.getRow(); R.beforeFirst();
			updateBreak=Util.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String roomID=DBConnections.getRes(R,"CMROID");
				String localeID=DBConnections.getRes(R,"CMLOID");
				Room newRoom=(Room)CMClass.getLocale(localeID);
				if(newRoom==null)
					Log.errOut("Room","Couldn't load room '"+roomID+"', localeID '"+localeID+"'.");
				else
				{
					newRoom.setRoomID(roomID);
					String areaName=DBConnections.getRes(R,"CMAREA");
					Area myArea=CMMap.getArea(areaName);
					if(myArea==null)
					{
						myArea=(Area)CMClass.getAreaType("StdArea").copyOf();
						myArea.setName(areaName);
						if(!newAreasToCreate.containsKey(areaName))
							newAreasToCreate.put(areaName,areaName);
					}
					newRoom.setArea(myArea);
					newRoom.setDisplayText(DBConnections.getRes(R,"CMDESC1"));
					newRoom.setDescription(DBConnections.getRes(R,"CMDESC2"));
					newRoom.setMiscText(DBConnections.getRes(R,"CMROTX"));
					hash.put(roomID,newRoom);
				}
				if((currentRecordPos%updateBreak)==0)
					myHost.setGameStatusStr("Booting: Loading Rooms ("+currentRecordPos+" of "+recordCount+")");
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}

		// handle stray areas
		for(Enumeration e=newAreasToCreate.elements();e.hasMoreElements();)
		{
			String areaName=(String)e.nextElement();
			Log.sysOut("Area","Creating unhandled area: "+areaName);
			Area realArea=DBCreate(areaName,"StdArea");
			for(Enumeration r=hash.elements();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.getArea().Name().equals(areaName))
					R.setArea(realArea);
			}
		}

		// now grab the exits
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROEX");
			Room thisRoom=null;
			Room newRoom=null;
			R.last(); recordCount=R.getRow(); R.beforeFirst();
			updateBreak=Util.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String roomID=DBConnections.getRes(R,"CMROID");
				thisRoom=(Room)hash.get(roomID);
				if(thisRoom==null)
					Log.errOut("Room","Couldn't set exit for unknown room '"+roomID+"'");
				else
				{
					int direction=(int)DBConnections.getLongRes(R,"CMDIRE");
					String exitID=DBConnections.getRes(R,"CMEXID");
					String exitMiscText=DBConnections.getResQuietly(R,"CMEXTX");
					String nextRoomID=DBConnections.getRes(R,"CMNRID");
					Exit newExit=CMClass.getExit(exitID);
					newRoom=(Room)hash.get(nextRoomID);

					if((newExit==null)&&(newRoom==null))
						Log.errOut("Room","Couldn't find room '"+nextRoomID+"', exit type '"+exitID+"', direction: "+direction);
					else
					if(newExit!=null)
						newExit.setMiscText(exitMiscText);
					thisRoom.rawDoors()[direction]=newRoom;
					thisRoom.rawExits()[direction]=newExit;
				}
				if((currentRecordPos%updateBreak)==0)
					myHost.setGameStatusStr("Booting: Loading Exits ("+currentRecordPos+" of "+recordCount+")");
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}

		DBReadContent(null,hash,myHost);

		myHost.setGameStatusStr("Booting: Finalizing room data)");

		while(CMMap.numRooms()>0) CMMap.delRoom(CMMap.getFirstRoom());
		for(Enumeration r=hash.elements();r.hasMoreElements();)
		{
			Room thisRoom=(Room)r.nextElement();
			thisRoom.startItemRejuv();
			CMMap.addRoom(thisRoom);
			thisRoom.recoverRoomStats();
		}

		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			((Area)a.nextElement()).getAreaStats();
	}

	private static void fixItemKeys(Hashtable itemLocs, Hashtable itemNums)
	{
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

	private static void fixMOBRides(Hashtable mobRides, Hashtable itemNums)
	{
		for(Enumeration e=mobRides.keys();e.hasMoreElements();)
		{
			MOB M=(MOB)e.nextElement();
			String ride=(String)mobRides.get(M);
			if(ride!=null)
			{
				Environmental E=(Environmental)itemNums.get(ride);
				if(E!=null)
				{
					if(E instanceof Rideable)
						M.setRiding((Rideable)E);
					else
					if(E instanceof MOB)
						M.setFollowing((MOB)E);
				}
			}
		}
	}

	public static void DBReadContent(Room thisRoom, Hashtable rooms)
	{
		DBReadContent(thisRoom,rooms,null);
	}

	public static void DBReadContent(Room thisRoom, Hashtable rooms, Host myHost)
	{

		Hashtable stuff=new Hashtable();
		Hashtable itemNums=null;
		Hashtable itemLocs=null;
		Hashtable mobRides=null;

		DBConnection D=null;
		// now grab the items
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROIT"+((thisRoom==null)?"":" WHERE CMROID='"+thisRoom.roomID()+"'"));
			R.last(); recordCount=R.getRow(); R.beforeFirst();
			updateBreak=Util.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String roomID=DBConnections.getRes(R,"CMROID");
				itemNums=(Hashtable)stuff.get("NUMSFOR"+roomID);
				if(itemNums==null)
				{
					itemNums=new Hashtable();
					stuff.put("NUMSFOR"+roomID,itemNums);
				}
				itemLocs=(Hashtable)stuff.get("LOCSFOR"+roomID);
				if(itemLocs==null)
				{
					itemLocs=new Hashtable();
					stuff.put("LOCSFOR"+roomID,itemLocs);
				}
				String itemNum=DBConnections.getRes(R,"CMITNM");
				String itemID=DBConnections.getRes(R,"CMITID");
				Item newItem=(Item)CMClass.getItem(itemID);
				if(newItem==null)
					Log.errOut("Room","Couldn't find item '"+itemID+"'");
				else
				{
					itemNums.put(itemNum,newItem);
					String loc=DBConnections.getResQuietly(R,"CMITLO");
					if(loc.length()>0)
					{
						Item container=(Item)itemNums.get(loc);
						if(container!=null)
							newItem.setContainer(container);
						else
							itemLocs.put(newItem,loc);
					}
					newItem.setMiscText(DBConnections.getResQuietly(R,"CMITTX"));
					newItem.baseEnvStats().setRejuv((int)DBConnections.getLongRes(R,"CMITRE"));
					newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
					newItem.baseEnvStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
					newItem.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
					newItem.baseEnvStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
					newItem.recoverEnvStats();
				}
				if(((currentRecordPos%updateBreak)==0)&&(myHost!=null))
					myHost.setGameStatusStr("Booting: Loading Items ("+currentRecordPos+" of "+recordCount+")");
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}

		// now grab the inhabitants
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROCH"+((thisRoom==null)?"":" WHERE CMROID='"+thisRoom.roomID()+"'"));
			R.last(); recordCount=R.getRow(); R.beforeFirst();
			updateBreak=Util.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String roomID=DBConnections.getRes(R,"CMROID");
				String NUMID=DBConnections.getRes(R,"CMCHNM");
				String MOBID=DBConnections.getRes(R,"CMCHID");

				itemNums=(Hashtable)stuff.get("NUMSFOR"+roomID);
				if(itemNums==null)
				{
					itemNums=new Hashtable();
					stuff.put("NUMSFOR"+roomID,itemNums);
				}
				mobRides=(Hashtable)stuff.get("RIDESFOR"+roomID);
				if(mobRides==null)
				{
					mobRides=new Hashtable();
					stuff.put("RIDESFOR"+roomID,mobRides);
				}

				MOB newMOB=(MOB)CMClass.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("Room","Couldn't find MOB '"+MOBID+"'");
				else
				{
					itemNums.put(NUMID,newMOB);
					newMOB.setStartRoom(thisRoom);
					newMOB.setLocation(thisRoom);
					newMOB.setMiscText(DBConnections.getResQuietly(R,"CMCHTX"));
					newMOB.baseEnvStats().setLevel(((int)DBConnections.getLongRes(R,"CMCHLV")));
					newMOB.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMCHAB"));
					newMOB.baseEnvStats().setRejuv((int)DBConnections.getLongRes(R,"CMCHRE"));
					String ride=DBConnections.getRes(R,"CMCHRI");
					if((ride!=null)&&(ride.length()>0))
						mobRides.put(newMOB,ride);
					newMOB.recoverCharStats();
					newMOB.recoverEnvStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
				}
				if(((currentRecordPos%updateBreak)==0)&&(myHost!=null))
					myHost.setGameStatusStr("Booting: Loading MOBs ("+currentRecordPos+" of "+recordCount+")");
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
		if(thisRoom!=null)
		{
			rooms=new Hashtable();
			rooms.put(thisRoom.roomID(),thisRoom);
		}
		recordCount=rooms.size();
		updateBreak=Util.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
		currentRecordPos=0;
		for(Enumeration e=rooms.elements();e.hasMoreElements();)
		{
			if((((++currentRecordPos)%updateBreak)==0)&&(myHost!=null))
				myHost.setGameStatusStr("Booting: Populating Rooms ("+(currentRecordPos)+" of "+recordCount+")");
			Room room=(Room)e.nextElement();
			itemNums=(Hashtable)stuff.get("NUMSFOR"+room.roomID());
			if(itemNums!=null)
			{
				for(Enumeration i=itemNums.elements();i.hasMoreElements();)
				{
					Environmental E=(Environmental)i.nextElement();
					if(E instanceof Item)
						room.addItem((Item)E);
					else
						((MOB)E).bringToLife(room,true);
				}
				itemLocs=(Hashtable)stuff.get("LOCSFOR"+room.roomID());
				mobRides=(Hashtable)stuff.get("RIDESFOR"+room.roomID());
				if(itemLocs!=null)
					fixItemKeys(itemLocs,itemNums);
				if(mobRides!=null)
					fixMOBRides(mobRides,itemNums);
			}
		}
	}

	private static void DBUpdateContents(Room room)
	{
		DBConnection D=null;
		String sql=null;
		Vector done=new Vector();
		for(int i=0;i<room.numItems();i++)
		{
			Item thisItem=room.fetchItem(i);
			if((thisItem!=null)&&(!done.contains(""+thisItem)))
			{
				done.addElement(""+thisItem);
				thisItem.setDispossessionTime(0); // saved items won't clear!
				try
				{
					D=DBConnector.DBFetch();
					sql=
					"INSERT INTO CMROIT ("
					+"CMROID, "
					+"CMITNM, "
					+"CMITID, "
					+"CMITLO, "
					+"CMITTX, "
					+"CMITRE, "
					+"CMITUR, "
					+"CMITLV, "
					+"CMITAB, "
					+"CMHEIT"
					+") values ("
					+"'"+room.roomID()+"',"
					+"'"+thisItem+"',"
					+"'"+thisItem.ID()+"',"
					+"'"+((thisItem.container()!=null)?(""+thisItem.container()):"")+"',"
					+"'"+thisItem.text()+" ',"
					+thisItem.baseEnvStats().rejuv()+","
					+thisItem.usesRemaining()+","
					+thisItem.baseEnvStats().level()+","
					+thisItem.baseEnvStats().ability()+","
					+thisItem.baseEnvStats().height()+")";
					D.update(sql);
					DBConnector.DBDone(D);
				}
				catch(SQLException sqle)
				{
					Log.errOut("Room",sql);
					Log.errOut("Room","UpdateItems"+sqle);
					if(D!=null) DBConnector.DBDone(D);
				}
			}
		}
	}

	public static void DBUpdateItems(Room room)
	{
		if(room.roomID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMROIT WHERE CMROID='"+room.roomID()+"'");
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room","UpdateItems"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
		DBUpdateContents(room);
	}

	public static void DBUpdateExits(Room room)
	{
		if(room.roomID().length()==0) return;
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMROEX WHERE CMROID='"+room.roomID()+"'");
			DBConnector.DBDone(D);
			for(int e=0;e<Directions.NUM_DIRECTIONS;e++)
			{
				Exit thisExit=room.rawExits()[e];
				Room thisRoom=room.rawDoors()[e];
				if(((thisRoom!=null)||(thisExit!=null))
				   &&((thisRoom==null)||(thisRoom.roomID().length()>0)))
				{
					D=DBConnector.DBFetch();
					str="INSERT INTO CMROEX ("
					+"CMROID, "
					+"CMDIRE, "
					+"CMEXID, "
					+"CMEXTX, "
					+"CMNRID"
					+") values ("
					+"'"+room.roomID()+"',"
					+e+","
					+"'"+((thisExit==null)?" ":thisExit.ID())+"',"
					+"'"+((thisExit==null)?" ":thisExit.text())+" ',"
					+"'"+((thisRoom==null)?" ":thisRoom.roomID())+"')";
					D.update(str);
					DBConnector.DBDone(D);
				}
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",str);
			Log.errOut("Room","UpdateExits"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}

	private static void DBCreateThisMOB(Room room, MOB thisMOB)
	{
		DBConnection D=null;
		String str=null;
		String ride=null;
		if(thisMOB.riding()!=null)
			ride=""+thisMOB.riding();
		else
		if(thisMOB.amFollowing()!=null)
			ride=""+thisMOB.amFollowing();
		else
			ride="";
		try
		{
			D=DBConnector.DBFetch();
			str=
			 "INSERT INTO CMROCH ("
			 +"CMROID, "
			 +"CMCHNM, "
			 +"CMCHID, "
			 +"CMCHTX, "
			 +"CMCHLV, "
			 +"CMCHAB, "
			 +"CMCHRE, "
			 +"CMCHRI "
			 +") values ("
			 +"'"+room.roomID()+"',"
			 +"'"+thisMOB+"',"
			 +"'"+CMClass.className(thisMOB)+"',"
			 +"'"+thisMOB.text()+" ',"
			 +thisMOB.baseEnvStats().level()+","
			 +thisMOB.baseEnvStats().ability()+","
			 +thisMOB.baseEnvStats().rejuv()+","
			 +"'"+ride+"'"
			 +")";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",str);
			Log.errOut("Room","CrRoomMOB"+sqle.getMessage());
			if(D!=null) DBConnector.DBDone(D);
		}
	}
	public static void DBUpdateTheseMOBs(Room room, Vector mobs)
	{
		if(room.roomID().length()==0) return;
		if(mobs==null) mobs=new Vector();
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			str="DELETE FROM CMROCH WHERE CMROID='"+room.roomID()+"'";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",str);
			Log.errOut("Room","UpdateMOBs"+sqle.getMessage());
			if(D!=null) DBConnector.DBDone(D);
		}
		for(int m=0;m<mobs.size();m++)
		{
			MOB thisMOB=(MOB)mobs.elementAt(m);
			DBCreateThisMOB(room,thisMOB);
		}
	}

	public static void DBUpdateMOBs(Room room)
	{
		if(room.roomID().length()==0) return;
		Vector mobs=new Vector();
		for(int m=0;m<room.numInhabitants();m++)
		{
			MOB thisMOB=room.fetchInhabitant(m);
			if((thisMOB!=null)&&(thisMOB.isEligibleMonster()))
				mobs.addElement(thisMOB);
		}
		DBUpdateTheseMOBs(room,mobs);
	}


	public static void DBUpdateAll(Room room)
	{
		if(room.roomID().length()==0) return;
		DBUpdateRoom(room);
		DBUpdateMOBs(room);
		DBUpdateExits(room);
		DBUpdateItems(room);
	}


	public static void DBUpdateRoom(Room room)
	{
		if(room.roomID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			String str="UPDATE CMROOM SET "
					+"CMLOID='"+CMClass.className(room)+"',"
					+"CMAREA='"+room.getArea().Name()+"',"
					+"CMDESC1='"+room.displayText()+" ',"
					+"CMDESC2='"+room.description()+" ',"
					+"CMROTX='"+room.text()+" '"
					+"WHERE CMROID='"+room.roomID()+"'";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
	}


	public static void DBReCreate(Room room, String oldID)
	{
		if(room.roomID().length()==0) return;
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("UPDATE CMROOM SET "
					+"CMROID='"+room.roomID()+"', "
					+"CMAREA='"+room.getArea().Name()+"' "
					+"WHERE CMROID='"+oldID+"'");
			DBConnector.DBDone(D);

			D=DBConnector.DBFetch();
			D.update("UPDATE CMROCH SET "
					+"CMROID='"+room.roomID()+"' "
					+"WHERE CMROID='"+oldID+"'");
			DBConnector.DBDone(D);

			D=DBConnector.DBFetch();

			D.update("UPDATE CMROEX SET "
					+"CMROID='"+room.roomID()+"' "
					+"WHERE CMROID='"+oldID+"'");
			
			DBConnector.DBDone(D);

			D=DBConnector.DBFetch();
			D.update("UPDATE CMROEX SET "
					+"CMNRID='"+room.roomID()+"' "
					+"WHERE CMNRID='"+oldID+"'");
			DBConnector.DBDone(D);
			
			D=DBConnector.DBFetch();
			D.update("UPDATE CMROIT SET "
					+"CMROID='"+room.roomID()+"' "
					+"WHERE CMROID='"+oldID+"'");
			DBConnector.DBDone(D);

			D=DBConnector.DBFetch();
			D.update("UPDATE CMCHAR SET "
					+"CMROID='"+room.roomID()+"' "
					+"WHERE CMROID='"+oldID+"'");
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
	}

	public static Area DBCreate(String areaName, String areaType)
	{
		Area A=CMClass.getAreaType(areaType);
		if(A==null) A=CMClass.getAreaType("StdArea");
		if((A==null)||(areaName.length()==0)) return null;

		A=(Area)A.copyOf();
		A.setName(areaName);
		CMMap.addArea(A);
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			str="INSERT INTO CMAREA ("
			+"CMAREA,"
			+"CMTYPE,"
			+"CMCLIM,"
			+"CMSUBS,"
			+"CMDESC,"
			+"CMROTX,"
			+"CMTECH"
			+") values ("
			+"'"+A.Name()+"',"
			+"'"+A.ID()+"',"
			+""+A.climateType()+","
			+"'"+A.getSubOpList()+"',"
			+"'"+A.description()+" ',"
			+"'"+A.text()+" ',"
			+A.getTechLevel()+")";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Area",str);
			Log.errOut("Area",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
		if(A==null) return null;
		A.tickControl(true);
		return A;
	}

	public static void DBUpdate(String keyName,Area A)
	{
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			str="UPDATE CMAREA SET "
				+"CMAREA='"+A.Name()+"',"
				+"CMTYPE='"+A.ID()+"',"
				+"CMCLIM="+A.climateType()+","
				+"CMSUBS='"+A.getSubOpList()+"',"
				+"CMDESC='"+A.description()+" ',"
				+"CMROTX='"+A.text()+" ',"
				+"CMTECH="+A.getTechLevel()+" "
				+"WHERE CMAREA='"+keyName+"'";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Area",str);
			Log.errOut("Area",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
	}

	public static void DBUpdateRoomMOB(String keyName, Room room, MOB mob)
	{
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			str="DELETE FROM CMROCH "
				+"WHERE CMROID='"+room.roomID()+"' "
				+"AND CMCHNM='"+keyName+"'";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("UpRoomMOB",str);
			Log.errOut("UpRoomMOB",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
		DBCreateThisMOB(room,mob);
	}

	public static void DBDelete(Area A)
	{
		if(A==null) return;
		A.tickControl(false);
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMAREA WHERE CMAREA='"+A.Name()+"'");
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
	}


	public static void DBCreate(Room room, String LocaleID)
	{
		if(room.roomID().length()==0) return;
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			str="INSERT INTO CMROOM ("
			+"CMROID,"
			+"CMLOID,"
			+"CMAREA,"
			+"CMDESC1,"
			+"CMDESC2,"
			+"CMROTX"
			+") values ("
			+"'"+room.roomID()+"',"
			+"'"+LocaleID+"',"
			+"'"+room.getArea().Name()+"',"
			+"'"+room.displayText()+" ',"
			+"'"+room.description()+" ',"
			+"'"+room.text()+" ')";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",str);
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}

	}

	public static void DBDelete(Room room)
	{
		if(room.roomID().length()==0) return;
		DBConnection D=null;
		try
		{
			while(room.numInhabitants()>0)
			{
				MOB inhab=room.fetchInhabitant(0);
				if(inhab!=null)
					room.delInhabitant(inhab);
			}
			DBUpdateMOBs(room);

			for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
			{
				room.rawExits()[i]=null;
				room.rawDoors()[i]=null;
			}
			DBUpdateExits(room);

			while(room.numItems()>0)
			{
				Item thisItem=room.fetchItem(0);
				if(thisItem!=null)
				{
					thisItem.setContainer(null);
					room.delItem(thisItem);
				}
			}
			DBUpdateItems(room);

			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMROOM WHERE CMROID='"+room.roomID()+"'");
			DBConnector.DBDone(D);

		}
		catch(SQLException sqle)
		{
			Log.errOut("Room","Delete"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}
}