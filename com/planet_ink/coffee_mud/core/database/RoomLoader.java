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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class RoomLoader
{
	private static int recordCount=1;
	private static int currentRecordPos=1;
	private static int updateBreak=1;
	private final static String zeroes="000000000000";

    public static Vector DBReadAreaData(String areaID, boolean reportStatus)
    {
        DBConnection D=null;
        Vector areas=new Vector();
        try
        {
            D=DBConnector.DBFetch();
            if(reportStatus)
                CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Counting Areas");
            ResultSet R=D.query("SELECT * FROM CMAREA"+((areaID==null)?"":" WHERE CMAREA='"+areaID+"'"));
            recordCount=DBConnector.getRecordCount(D,R);
            updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
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
                    continue;
                }
                A.setName(areaName);
                A.setClimateType((int)DBConnections.getLongRes(R,"CMCLIM"));
                A.setSubOpList(DBConnections.getRes(R,"CMSUBS"));
                A.setDescription(DBConnections.getRes(R,"CMDESC"));
                A.setMiscText(DBConnections.getRes(R,"CMROTX"));
                A.setTechLevel((int)DBConnections.getLongRes(R,"CMTECH"));
                A.tickControl(true);
                if(((currentRecordPos%updateBreak)==0)&&(reportStatus))
                    CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Loading Areas ("+currentRecordPos+" of "+recordCount+")");
                areas.addElement(A);
            }
            DBConnector.DBDone(D);
        }
        catch(SQLException sqle)
        {
            Log.errOut("Area",sqle);
            if(D!=null) DBConnector.DBDone(D);
            return null;
        }
        return areas;
    }
    
    public static void addRoom(Vector rooms, Room R)
    {
        Room R2=null;
        for(int i=0;i<rooms.size();i++)
        {
            R2=(Room)rooms.elementAt(i);
            if(R2.roomID().compareToIgnoreCase(R.roomID())>=0)
            {
                if(R2.roomID().compareToIgnoreCase(R.roomID())==0)
                    rooms.setElementAt(R,i);
                else
                    rooms.insertElementAt(R,i);
                return;
            }
        }
        rooms.addElement(R);
    }

    public static Room getRoom(Vector rooms, String roomID)
    {
        if(rooms.size()==0) return null;
        int start=0;
        int end=rooms.size()-1;
        while(start<=end)
        {
            int mid=(end+start)/2;
            int comp=((Room)rooms.elementAt(mid)).roomID().compareToIgnoreCase(roomID);
            if(comp==0)
                return (Room)rooms.elementAt(mid);
            else
            if(comp>0)
                end=mid-1;
            else
                start=mid+1;

        }
        return null;
    }
    
    public static Vector DBReadRoomData(String roomID, boolean reportStatus, Vector unknownAreas)
    {
        Vector rooms=new Vector();
        DBConnection D=null;
        try
        {
            D=DBConnector.DBFetch();
            if(reportStatus)
                CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Counting Rooms");
            ResultSet R=D.query("SELECT * FROM CMROOM"+((roomID==null)?"":" WHERE CMROID='"+roomID+"'"));
            recordCount=DBConnector.getRecordCount(D,R);
            updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
            while(R.next())
            {
                currentRecordPos=R.getRow();
                roomID=DBConnections.getRes(R,"CMROID");
                String localeID=DBConnections.getRes(R,"CMLOID");
                Room newRoom=CMClass.getLocale(localeID);
                if(newRoom==null)
                    Log.errOut("Room","Couldn't load room '"+roomID+"', localeID '"+localeID+"'.");
                else
                {
                    newRoom.setRoomID(roomID);
                    String areaName=DBConnections.getRes(R,"CMAREA");
                    Area myArea=CMLib.map().getArea(areaName);
                    if(myArea==null)
                    {
                        myArea=(Area)CMClass.getAreaType("StdArea").copyOf();
                        myArea.setName(areaName);
                        if((unknownAreas!=null)
                        &&(!unknownAreas.contains(areaName)))
                            unknownAreas.addElement(areaName);
                    }
                    newRoom.setArea(myArea);
                    newRoom.setDisplayText(DBConnections.getRes(R,"CMDESC1"));
                    if(CMProps.getBoolVar(CMProps.SYSTEMB_ROOMDNOCACHE))
                        newRoom.setDescription("");
                    else
                        newRoom.setDescription(DBConnections.getRes(R,"CMDESC2"));
                    newRoom.setMiscText(DBConnections.getRes(R,"CMROTX"));
                    RoomLoader.addRoom(rooms,newRoom);
                }
                if(((currentRecordPos%updateBreak)==0)&&(reportStatus))
                    CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Loading Rooms ("+currentRecordPos+" of "+recordCount+")");
            }
            DBConnector.DBDone(D);
        }
        catch(SQLException sqle)
        {
            Log.errOut("Room",sqle);
            if(D!=null) DBConnector.DBDone(D);
            return null;
        }
        return rooms;
    }

    public static void DBReadRoomExits(String roomID, Vector allRooms, boolean reportStatus)
    {
        DBConnection D=null;
        // now grab the exits
        try
        {
            D=DBConnector.DBFetch();
            if(reportStatus)
                CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Counting Exits");
            ResultSet R=D.query("SELECT * FROM CMROEX"+((roomID==null)?"":" WHERE CMROID='"+roomID+"'"));
            Room thisRoom=null;
            Room newRoom=null;
            recordCount=DBConnector.getRecordCount(D,R);
            updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
            while(R.next())
            {
                currentRecordPos=R.getRow();
                roomID=DBConnections.getRes(R,"CMROID");
                int direction=(int)DBConnections.getLongRes(R,"CMDIRE");
                thisRoom=getRoom(allRooms,roomID);
                if(thisRoom==null)
                    Log.errOut("Room","Couldn't set "+direction+" exit for unknown room '"+roomID+"'");
                else
                {
                    String exitID=DBConnections.getRes(R,"CMEXID");
                    String exitMiscText=DBConnections.getResQuietly(R,"CMEXTX");
                    String nextRoomID=DBConnections.getRes(R,"CMNRID");
                    newRoom=getRoom(allRooms,nextRoomID);
                    Exit newExit=CMClass.getExit(exitID);
                    if((newExit==null)&&(newRoom==null))
                        Log.errOut("Room","Couldn't find room '"+nextRoomID+"', exit type '"+exitID+"', direction: "+direction);
                    else
                    if((direction>255)&&(!(thisRoom instanceof GridLocale)))
                        Log.errOut("Room","Not GridLocale, tried "+direction+" exit for room '"+roomID+"'");
                    else
                    if(direction>255)
                    {
                        Vector CEs=CMParms.parseSemicolons(exitMiscText.trim(),true);
                        for(int ces=0;ces<CEs.size();ces++)
                        {
                            Vector SCE=CMParms.parse(((String)CEs.elementAt(ces)).trim());
                            WorldMap.CrossExit CE=new WorldMap.CrossExit();
                            if(SCE.size()<3) continue;
                            CE.x=CMath.s_int((String)SCE.elementAt(0));
                            CE.y=CMath.s_int((String)SCE.elementAt(1));
                            int codeddir=CMath.s_int((String)SCE.elementAt(2));
                            if(SCE.size()>=4)
                                CE.destRoomID=newRoom.roomID()+(String)SCE.elementAt(3);
                            else
                                CE.destRoomID=newRoom.roomID();
                            CE.out=(codeddir&256)==256;
                            CE.dir=codeddir&255;
                            ((GridLocale)thisRoom).addOuterExit(CE);
                            if((!CE.out)&&(!(newRoom instanceof GridLocale)))
                            {
                                newRoom.rawDoors()[CE.dir]=thisRoom;
                                newRoom.rawExits()[CE.dir]=CMClass.getExit("Open");
                            }
                        }
                    }
                    else
                    {
                        if(newExit!=null)
                            newExit.setMiscText(exitMiscText);
                        if(direction>=Directions.NUM_DIRECTIONS)
                            Log.errOut("RoomLoader",CMLib.map().getExtendedRoomID(thisRoom)+" has an invalid direction #"+direction);
                        else
                        {
                            thisRoom.rawDoors()[direction]=newRoom;
                            thisRoom.rawExits()[direction]=newExit;
                        }
                    }
                }
                if((currentRecordPos%updateBreak)==0)
                    CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Loading Exits ("+currentRecordPos+" of "+recordCount+")");
            }
            DBConnector.DBDone(D);
        }
        catch(SQLException sqle)
        {
            Log.errOut("Room",sqle);
            if(D!=null) DBConnector.DBDone(D);
        }
    }
    
	public static void DBRead()
	{
		while(CMLib.map().numAreas()>0)CMLib.map().delArea(CMLib.map().getFirstArea());

        Vector areas=RoomLoader.DBReadAreaData(null,true);
        if(areas==null) return;
        for(int a=0;a<areas.size();a++)
            CMLib.map().addArea((Area)areas.elementAt(a));
        areas.clear();

        Vector newAreasToCreate=new Vector();
        Vector rooms=RoomLoader.DBReadRoomData(null,true,newAreasToCreate);
        
		// handle stray areas
		for(Enumeration e=newAreasToCreate.elements();e.hasMoreElements();)
		{
			String areaName=(String)e.nextElement();
			Log.sysOut("Area","Creating unhandled area: "+areaName);
			Area realArea=DBCreate(areaName,"StdArea");
			for(Enumeration r=rooms.elements();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.getArea().Name().equals(areaName))
					R.setArea(realArea);
			}
		}
        
        RoomLoader.DBReadRoomExits(null,rooms,true);

		DBReadContent(null,rooms,true);

		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Finalizing room data)");

		for(Enumeration r=rooms.elements();r.hasMoreElements();)
		{
			Room thisRoom=(Room)r.nextElement();
			thisRoom.startItemRejuv();
			thisRoom.recoverRoomStats();
		}

		for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
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


    public static void DBReadContent(Room thisRoom, Vector rooms, boolean setStatus)
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
				CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Counting Items");
			ResultSet R=D.query("SELECT * FROM CMROIT"+((thisRoom==null)?"":" WHERE CMROID='"+thisRoom.roomID()+"'"));
			if(setStatus) recordCount=DBConnector.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
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
				Item newItem=CMClass.getItem(itemID);
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
					CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Loading Items ("+currentRecordPos+" of "+recordCount+")");
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
				CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Counting MOBS");
			ResultSet R=D.query("SELECT * FROM CMROCH"+((thisRoom==null)?"":" WHERE CMROID='"+thisRoom.roomID()+"'"));
			if(setStatus) recordCount=DBConnector.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
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

				MOB newMOB=CMClass.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("Room","Couldn't find MOB '"+MOBID+"'");
				else
				{
					itemNums.put(NUMID,newMOB);
					newMOB.setStartRoom(thisRoom);
					newMOB.setLocation(thisRoom);
					if((CMProps.getBoolVar(CMProps.SYSTEMB_MOBNOCACHE))
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
					CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Loading MOBs ("+currentRecordPos+" of "+recordCount+")");
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
			rooms=new Vector();
			rooms.addElement(thisRoom);
		}
		recordCount=rooms.size();
		updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
		currentRecordPos=0;
		for(Enumeration e=rooms.elements();e.hasMoreElements();)
		{
			if((((++currentRecordPos)%updateBreak)==0)&&(setStatus))
				CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Populating Rooms ("+(currentRecordPos)+" of "+recordCount+")");
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
			if((thisItem!=null)&&(!done.contains(""+thisItem))&&thisItem.savable())
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
			int ordinal=0;
			for(int v=0;v<exits.size();v++)
			{
				WorldMap.CrossExit CE=(WorldMap.CrossExit)exits.elementAt(v);
				Room R=CMLib.map().getRoom(CE.destRoomID);
				if(R==null) continue;
				if(R.getGridParent()!=null) R=R.getGridParent();
				if((R.roomID().length()>0)&&(!done.contains(R.roomID())))
				{
					done.add(R.roomID());
					HashSet oldStrs=new HashSet();
					for(int v2=0;v2<exits.size();v2++)
					{
						WorldMap.CrossExit CE2=(WorldMap.CrossExit)exits.elementAt(v2);
						if((CE2.destRoomID.equals(R.roomID())
						||(CE2.destRoomID.startsWith(R.roomID()+"#("))))
						{
							String str=CE2.x+" "+CE2.y+" "+((CE2.out?256:512)|CE2.dir)+" "+CE2.destRoomID.substring(R.roomID().length())+";";
							if(!oldStrs.contains(str))
								oldStrs.add(str);
						}
					}
					StringBuffer exitStr=new StringBuffer("");
					for(Iterator a=oldStrs.iterator();a.hasNext();)
						exitStr.append((String)a.next());
					DBConnector.update(
					"INSERT INTO CMROEX ("
					+"CMROID, "
					+"CMDIRE, "
					+"CMEXID, "
					+"CMEXTX, "
					+"CMNRID"
					+") values ("
					+"'"+room.roomID()+"',"
					+(256+(++ordinal))+","
					+"'Open',"
					+"'"+exitStr.toString()+"',"
					+"'"+R.roomID()+"')");
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
		
		if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBNOCACHE))
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
		if(CMProps.getBoolVar(CMProps.SYSTEMB_ROOMDNOCACHE))
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
		
		if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBNOCACHE))
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
		CMLib.map().addArea(A);
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
