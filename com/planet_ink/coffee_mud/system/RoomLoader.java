package com.planet_ink.coffee_mud.system;

import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class RoomLoader
{
	private static int recordCount=1;
	private static int currentRecordPos=1;
	private static int updateBreak=1;
	private final static String zeroes="000000000000";

	public static void DBRead()
	{
		Hashtable hash=new Hashtable();
		while(CMMap.numAreas()>0)CMMap.delArea(CMMap.getFirstArea());

		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: Counting Areas");
			ResultSet R=D.query("SELECT * FROM CMAREA");
			recordCount=DBConnector.getRecordCount(D,R);
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
					CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: Loading Areas ("+currentRecordPos+" of "+recordCount+")");
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
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: Counting Rooms");
			ResultSet R=D.query("SELECT * FROM CMROOM");
			recordCount=DBConnector.getRecordCount(D,R);
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
					if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_ROOMDNOCACHE))
						newRoom.setDescription("");
					else
						newRoom.setDescription(DBConnections.getRes(R,"CMDESC2"));
					newRoom.setMiscText(DBConnections.getRes(R,"CMROTX"));
					hash.put(roomID,newRoom);
				}
				if((currentRecordPos%updateBreak)==0)
					CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: Loading Rooms ("+currentRecordPos+" of "+recordCount+")");
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
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: Counting Exits");
			ResultSet R=D.query("SELECT * FROM CMROEX");
			Room thisRoom=null;
			Room newRoom=null;
			recordCount=DBConnector.getRecordCount(D,R);
			updateBreak=Util.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String roomID=DBConnections.getRes(R,"CMROID");
				int direction=(int)DBConnections.getLongRes(R,"CMDIRE");
				if(roomID.endsWith(")")
				&&(roomID.indexOf("#(")>0)
				&&(direction>255))
				{
					thisRoom=(Room)hash.get(roomID.substring(0,roomID.indexOf("#(")));
					if(thisRoom==null)
						Log.errOut("Room","Couldn't set "+Directions.getDirectionName(direction)+" exit for unknown room '"+roomID+"'");
					else
					if(!(thisRoom instanceof GridLocale))
						Log.errOut("Room","Not GridLocale, tried "+Directions.getDirectionName(direction)+" exit for unknown room '"+roomID+"'");
					else
					{
						String nextRoomID=DBConnections.getRes(R,"CMNRID");
						if(nextRoomID.endsWith(")")
						&&(nextRoomID.indexOf("#(")>0))
							newRoom=(Room)hash.get(nextRoomID.substring(0,nextRoomID.indexOf("#(")));
						else
							newRoom=(Room)hash.get(nextRoomID);
						if(newRoom==null)
							Log.errOut("Room","Couldn't find room '"+nextRoomID+"', exit type inner, direction: "+direction);
						else
						{
							CMMap.CrossExit CE=new CMMap.CrossExit();
							int len=thisRoom.roomID().length()+2;
							int comma=roomID.indexOf(',',len);
							if(comma>0)
							{
								CE.x=Util.s_int(roomID.substring(len,comma));
								CE.y=Util.s_int(roomID.substring(comma+1,roomID.length()-1));
								CE.destRoomID=nextRoomID;
								CE.out=(direction&256)==256;
								CE.dir=direction%255;
								((GridLocale)thisRoom).addOuterExit(CE);
							}
						}
					}
				}
				else
				{
					thisRoom=(Room)hash.get(roomID);
					if(thisRoom==null)
						Log.errOut("Room","Couldn't set "+Directions.getDirectionName(direction)+" exit for unknown room '"+roomID+"'");
					else
					{
						String exitID=DBConnections.getRes(R,"CMEXID");
						String exitMiscText=DBConnections.getResQuietly(R,"CMEXTX");
						String nextRoomID=DBConnections.getRes(R,"CMNRID");
						newRoom=(Room)hash.get(nextRoomID);
						Exit newExit=CMClass.getExit(exitID);
						if((newExit==null)&&(newRoom==null))
							Log.errOut("Room","Couldn't find room '"+nextRoomID+"', exit type '"+exitID+"', direction: "+direction);
						else
						if(newExit!=null)
							newExit.setMiscText(exitMiscText);
						
						thisRoom.rawDoors()[direction]=newRoom;
						thisRoom.rawExits()[direction]=newExit;
					}
				}
				if((currentRecordPos%updateBreak)==0)
					CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: Loading Exits ("+currentRecordPos+" of "+recordCount+")");
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}

		DBReadContent(null,hash,true);

		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: Finalizing room data)");

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
	public static String DBReadRoomDesc(String roomID)
	{
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMROID='"+roomID+"'");
			if(R.next())
			{
				String txt=DBConnections.getRes(R,"CMDESC2");
				R.close();
				DBConnector.DBDone(D);
				return txt;
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
		return null;
	}
	
	public static String DBReadRoomMOBData(String roomID, String mobID)
	{
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROCH WHERE CMROID='"+roomID+"'");
			while(R.next())
			{
				String NUMID=DBConnections.getRes(R,"CMCHNM");
				if(NUMID.equalsIgnoreCase(mobID))
				{
					String txt=DBConnections.getRes(R,"CMCHTX");
					R.close();
					DBConnector.DBDone(D);
					return txt;
				}
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
		return null;
	}

	private static void fixItemKeys(Hashtable itemLocs, Hashtable itemNums)
	{
		for(Enumeration e=itemLocs.keys();e.hasMoreElements();)
		{
			Item keyItem=(Item)e.nextElement();
			String location=(String)itemLocs.get(keyItem);
			Item container=(Item)itemNums.get(location);
			if(container!=null)
				keyItem.setContainer(container);
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

	public static void DBReadContent(Room thisRoom, Hashtable rooms, boolean setStatus)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Reading content of "+((thisRoom!=null)?thisRoom.roomID():"ALL"));
		
		Hashtable stuff=new Hashtable();
		Hashtable itemNums=null;
		Hashtable itemLocs=null;
		Hashtable mobRides=null;

		DBConnection D=null;
		// now grab the items
		try
		{
			D=DBConnector.DBFetch();
			if(setStatus)
				CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: Counting Items");
			ResultSet R=D.query("SELECT * FROM CMROIT"+((thisRoom==null)?"":" WHERE CMROID='"+thisRoom.roomID()+"'"));
			if(setStatus) recordCount=DBConnector.getRecordCount(D,R);
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
				if(((currentRecordPos%updateBreak)==0)&&(setStatus))
					CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: Loading Items ("+currentRecordPos+" of "+recordCount+")");
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
			if(setStatus)
				CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: Counting MOBS");
			ResultSet R=D.query("SELECT * FROM CMROCH"+((thisRoom==null)?"":" WHERE CMROID='"+thisRoom.roomID()+"'"));
			if(setStatus) recordCount=DBConnector.getRecordCount(D,R);
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
					if((CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBNOCACHE))
					&&(NUMID.indexOf(MOBID+"@")>=0))
						newMOB.setMiscText("%DBID>"+roomID+NUMID.substring(NUMID.indexOf("@")));
					else
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
				if(((currentRecordPos%updateBreak)==0)&&(setStatus))
					CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: Loading MOBs ("+currentRecordPos+" of "+recordCount+")");
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
			if((((++currentRecordPos)%updateBreak)==0)&&(setStatus))
				CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: Populating Rooms ("+(currentRecordPos)+" of "+recordCount+")");
			Room room=(Room)e.nextElement();
			itemNums=(Hashtable)stuff.get("NUMSFOR"+room.roomID());
			if(itemNums!=null)
			{
				for(Enumeration i=itemNums.elements();i.hasMoreElements();)
				{
					Environmental E=(Environmental)i.nextElement();
					if(E instanceof Item)
					{
						room.addItem((Item)E);
					}
					else
						((MOB)E).bringToLife(room,true);
				}
				itemLocs=(Hashtable)stuff.get("LOCSFOR"+room.roomID());
				mobRides=(Hashtable)stuff.get("RIDESFOR"+room.roomID());
				if(itemLocs!=null)
				{
					fixItemKeys(itemLocs,itemNums);
					room.recoverRoomStats();
					room.recoverRoomStats();
				}
					
				if(mobRides!=null)
					fixMOBRides(mobRides,itemNums);
			}
		}
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done reading content of "+((thisRoom!=null)?thisRoom.roomID():"ALL"));
	}

	private static void DBUpdateContents(Room room)
	{
		Vector done=new Vector();
		for(int i=0;i<room.numItems();i++)
		{
			Item thisItem=room.fetchItem(i);
			if((thisItem!=null)&&(!done.contains(""+thisItem)))
			{
				done.addElement(""+thisItem);
				thisItem.setDispossessionTime(0); // saved items won't clear!
				DBConnector.update(
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
				+thisItem.baseEnvStats().height()+")");
			}
		}
	}

	public static void DBUpdateItems(Room room)
	{
		if(room.roomID().length()==0) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Start item update for room "+room.roomID());
		DBConnector.update("DELETE FROM CMROIT WHERE CMROID='"+room.roomID()+"'");
		try{Thread.sleep(room.numItems());}catch(Exception e){}
		if(DBConnector.queryRows("SELECT * FROM CMROIT  WHERE CMROID='"+room.roomID()+"'")>0)
			Log.errOut("Failed to update items for room "+room.roomID()+".");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Continue item update for room "+room.roomID());
		DBUpdateContents(room);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Finished item update for room "+room.roomID());
	}

	public static void DBUpdateExits(Room room)
	{
		if(room.roomID().length()==0) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROEX")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Starting exit update for room "+room.roomID());
		DBConnector.update("DELETE FROM CMROEX WHERE CMROID='"+room.roomID()+"'");
		if(DBConnector.queryRows("SELECT * FROM CMROEX  WHERE CMROID='"+room.roomID()+"'")>0)
			Log.errOut("Failed to update exits for room "+room.roomID()+".");
		for(int e=0;e<Directions.NUM_DIRECTIONS;e++)
		{
			Exit thisExit=room.rawExits()[e];
			Room thisRoom=room.rawDoors()[e];
			if(((thisRoom!=null)||(thisExit!=null))
			   &&((thisRoom==null)||(thisRoom.roomID().length()>0)))
			{
				DBConnector.update(
				"INSERT INTO CMROEX ("
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
				+"'"+((thisRoom==null)?" ":thisRoom.roomID())+"')");
			}
		}
		if(room instanceof GridLocale)
		{
			Vector exits=((GridLocale)room).outerExits();
			HashSet done=new HashSet();
			for(int v=0;v<exits.size();v++)
			{
				CMMap.CrossExit CE=(CMMap.CrossExit)exits.elementAt(v);
				int dir=CE.dir|(CE.out?256:512);
				if(!done.contains(room.roomID()+"#("+CE.x+","+CE.y+")-"+dir))
				{
					done.add(room.roomID()+"#("+CE.x+","+CE.y+")-"+dir);
					DBConnector.update(
					"INSERT INTO CMROEX ("
					+"CMROID, "
					+"CMDIRE, "
					+"CMEXID, "
					+"CMEXTX, "
					+"CMNRID"
					+") values ("
					+"'"+room.roomID()+"#("+CE.x+","+CE.y+")',"
					+dir+","
					+"'',"
					+"'',"
					+"'"+CE.destRoomID+"')");
				}
				
			}
		}
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROEX")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Finished exit update for room "+room.roomID());
	}

	private static void DBCreateThisMOB(Room room, MOB thisMOB)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Creating mob "+thisMOB.name()+" for room "+room.roomID());
		String ride=null;
		if(thisMOB.riding()!=null)
			ride=""+thisMOB.riding();
		else
		if(thisMOB.amFollowing()!=null)
			ride=""+thisMOB.amFollowing();
		else
			ride="";
		String mobID=""+thisMOB;
		
		DBConnector.update(
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
		+"'"+mobID+"',"
		+"'"+CMClass.className(thisMOB)+"',"
		+"'"+thisMOB.text()+" ',"
		+thisMOB.baseEnvStats().level()+","
		+thisMOB.baseEnvStats().ability()+","
		+thisMOB.baseEnvStats().rejuv()+","
		+"'"+ride+"'"
		+")");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Created mob "+thisMOB.name()+" for room "+room.roomID());
		
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBNOCACHE))
		   thisMOB.setMiscText("%DBID>"+room.roomID()+mobID.substring(mobID.indexOf("@")));
	}
	public static void DBUpdateTheseMOBs(Room room, Vector mobs)
	{
		if(room.roomID().length()==0) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Updating mobs for room "+room.roomID());
		if(mobs==null) mobs=new Vector();
		DBConnector.update("DELETE FROM CMROCH WHERE CMROID='"+room.roomID()+"'");
		if(DBConnector.queryRows("SELECT * FROM CMROCH  WHERE CMROID='"+room.roomID()+"'")>0)
			Log.errOut("Failed to update mobs for room "+room.roomID()+".");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Continue updating mobs for room "+room.roomID());
		for(int m=0;m<mobs.size();m++)
		{
			MOB thisMOB=(MOB)mobs.elementAt(m);
			DBCreateThisMOB(room,thisMOB);
		}
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating mobs for room "+room.roomID());
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
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Start updating room "+room.roomID());
		DBConnector.update(
		"UPDATE CMROOM SET "
		+"CMLOID='"+CMClass.className(room)+"',"
		+"CMAREA='"+room.getArea().Name()+"',"
		+"CMDESC1='"+room.displayText()+" ',"
		+"CMDESC2='"+room.description()+" ',"
		+"CMROTX='"+room.text()+" '"
		+"WHERE CMROID='"+room.roomID()+"'");
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_ROOMDNOCACHE))
			room.setDescription("");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating room "+room.roomID());
	}


	public static void DBReCreate(Room room, String oldID)
	{
		if(room.roomID().length()==0) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Recreating room "+room.roomID());
		
		DBConnector.update(
		"UPDATE CMROOM SET "
		+"CMROID='"+room.roomID()+"', "
		+"CMAREA='"+room.getArea().Name()+"' "
		+"WHERE CMROID='"+oldID+"'");
		
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBNOCACHE))
			for(int m=0;m<room.numInhabitants();m++)
			{
				MOB M=room.fetchInhabitant(m);
				if((M!=null)&&(M.isEligibleMonster()))
					M.setMiscText(M.text());
			}
		
		DBConnector.update(
		"UPDATE CMROCH SET "
		+"CMROID='"+room.roomID()+"' "
		+"WHERE CMROID='"+oldID+"'");

		DBConnector.update(
		"UPDATE CMROEX SET "
		+"CMROID='"+room.roomID()+"' "
		+"WHERE CMROID='"+oldID+"'");
		
		if(room instanceof GridLocale)
		{
			Vector V=((GridLocale)room).outerExits();
			for(int d=0;d<V.size();d++)
			{
				CMMap.CrossExit CE=(CMMap.CrossExit)V.elementAt(d);
				DBConnector.update(
				"UPDATE CMROEX SET "
				+"CMROID='"+room.roomID()+"#("+CE.x+","+CE.y+")' "
				+"WHERE CMROID='"+oldID+"#("+CE.x+","+CE.y+")'");
				
				DBConnector.update(
				"UPDATE CMROEX SET "
				+"CMNRID='"+room.roomID()+"#("+CE.x+","+CE.y+")' "
				+"WHERE CMNRID='"+oldID+"#("+CE.x+","+CE.y+")'");
			}
		}

		DBConnector.update(
		"UPDATE CMROEX SET "
		+"CMNRID='"+room.roomID()+"' "
		+"WHERE CMNRID='"+oldID+"'");
		
		DBConnector.update(
		"UPDATE CMROIT SET "
		+"CMROID='"+room.roomID()+"' "
		+"WHERE CMROID='"+oldID+"'");
		
		DBConnector.update(
		"UPDATE CMCHAR SET "
		+"CMROID='"+room.roomID()+"' "
		+"WHERE CMROID='"+oldID+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done recreating room "+room.roomID());
	}

	public static Area DBCreate(String areaName, String areaType)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Creating area "+areaName);
		Area A=CMClass.getAreaType(areaType);
		if(A==null) A=CMClass.getAreaType("StdArea");
		if((A==null)||(areaName.length()==0)) return null;

		A=(Area)A.copyOf();
		A.setName(areaName);
		CMMap.addArea(A);
		DBConnector.update(
		"INSERT INTO CMAREA ("
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
		+A.getTechLevel()+")");
		if(A==null) return null;
		A.tickControl(true);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done creating area "+areaName);
		return A;
	}

	public static void DBUpdate(String keyName,Area A)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Updating area "+A.name());
		DBConnector.update(
		"UPDATE CMAREA SET "
		+"CMAREA='"+A.Name()+"',"
		+"CMTYPE='"+A.ID()+"',"
		+"CMCLIM="+A.climateType()+","
		+"CMSUBS='"+A.getSubOpList()+"',"
		+"CMDESC='"+A.description()+" ',"
		+"CMROTX='"+A.text()+" ',"
		+"CMTECH="+A.getTechLevel()+" "
		+"WHERE CMAREA='"+keyName+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating area "+A.name());
	}

	public static void DBUpdateRoomMOB(String keyName, Room room, MOB mob)
	{
		if((room==null)||(room.roomID().length()==0)) return;
		
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating mob "+mob.name()+" in room "+room.roomID());
		DBConnector.update(
		"DELETE FROM CMROCH "
		+"WHERE CMROID='"+room.roomID()+"' "
		+"AND CMCHNM='"+keyName+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Continue updating mob "+mob.name()+" in room "+room.roomID());
		DBCreateThisMOB(room,mob);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating mob "+mob.name()+" in room "+room.roomID());
	}

	public static void DBDelete(Area A)
	{
		if(A==null) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Destroying area "+A.name());
		A.tickControl(false);
		DBConnector.update("DELETE FROM CMAREA WHERE CMAREA='"+A.Name()+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done destroying area "+A.name()+".");
	}


	public static void DBCreate(Room room, String LocaleID)
	{
		if(room.roomID().length()==0) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Creating new room "+room.roomID());
		DBConnector.update(
		"INSERT INTO CMROOM ("
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
		+"'"+room.text()+" ')");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done creating new room "+room.roomID());
	}

	public static void DBDelete(Room room)
	{
		if(room.roomID().length()==0) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Destroying room "+room.roomID());
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

		DBConnector.update("DELETE FROM CMROOM WHERE CMROID='"+room.roomID()+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done gestroying room "+room.roomID());
	}
}
