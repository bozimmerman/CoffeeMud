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

import com.planet_ink.coffee_mud.Libraries.CMCatalog.CataDataImpl;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class RoomLoader
{
	protected DBConnector DB=null;
	public RoomLoader(DBConnector newDB)
	{
		DB=newDB;
	}
	private int recordCount=1;
	private int currentRecordPos=1;
	private int updateBreak=1;
	private final static String zeroes="000000000000";

    public Vector DBReadAreaData(String areaID, boolean reportStatus)
    {
        DBConnection D=null;
        Vector areas=new Vector();
        try
        {
            D=DB.DBFetch();
            if(reportStatus)
                CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Counting Areas");
            ResultSet R=D.query("SELECT * FROM CMAREA"+((areaID==null)?"":" WHERE CMAREA='"+areaID+"'"));
            recordCount=DB.getRecordCount(D,R);
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
                A.setAreaState(Area.STATE_ACTIVE);
                if(((currentRecordPos%updateBreak)==0)&&(reportStatus))
                    CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Loading Areas ("+currentRecordPos+" of "+recordCount+")");
                areas.addElement(A);
            }
            DB.DBDone(D);
        }
        catch(SQLException sqle)
        {
            Log.errOut("Area",sqle);
            if(D!=null) DB.DBDone(D);
            return null;
        }
        return areas;
    }
    
    protected void addRoom(Vector rooms, Room R)
    {
    	try {
	        String roomID=R.roomID();
	        int start=0;
	        int end=rooms.size()-1;
	        int lastStart=0;
	        int lastEnd=rooms.size()-1;
	        int comp=-1;
	        int mid=-1;
	        while(start<=end)
	        {
	            mid=(end+start)/2;
	            comp=((Room)rooms.elementAt(mid)).roomID().compareToIgnoreCase(roomID);
	            if(comp==0)
	                break;
	            else
	            if(comp>0)
	            {
	                lastEnd=end;
	                end=mid-1;
	            }
	            else
	            {
	                lastStart=start;
	                start=mid+1;
	            }
	        }
	        if(comp==0)
	            rooms.setElementAt(R,mid);
	        else
	        {
	            if(mid>=0)
	                for(comp=lastStart;comp<=lastEnd;comp++)
	                    if(((Room)rooms.elementAt(comp)).roomID().compareToIgnoreCase(roomID)>0)
	                    {
	                        rooms.insertElementAt(R,comp);
	                        return;
	                    }
	            rooms.addElement(R);
	        }
	    }
    	catch(Throwable t){ t.printStackTrace();}
    }
	    
    public Room getRoom(Vector rooms, String roomID)
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
    
    public RoomnumberSet DBReadAreaRoomList(String areaName, boolean reportStatus)
    {
    	RoomnumberSet roomSet=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
        DBConnection D=null;
        try
        {
            D=DB.DBFetch();
            if(reportStatus)
                CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Fetching roomnums for "+areaName);
            ResultSet R=D.query("SELECT * FROM CMROOM"+((areaName==null)?"":" WHERE CMAREA='"+areaName+"'"));
            while(R.next())
            	roomSet.add(DBConnections.getRes(R,"CMROID"));
		    DB.DBDone(D);
		}
		catch(SQLException sqle)
		{
		    Log.errOut("RoomSet",sqle);
		    if(D!=null) DB.DBDone(D);
		    return null;
		}
		return roomSet;
    }
    
    public Vector DBReadRoomData(String singleRoomIDtoLoad, boolean reportStatus)
    { 
    	return DBReadRoomData(singleRoomIDtoLoad,null,reportStatus,null,null);
    }
    
    public Room DBReadRoomObject(String roomIDtoLoad, boolean reportStatus)
    {
        DBConnection D=null;
        try
        {
            D=DB.DBFetch();
            if(reportStatus)
                CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Counting Rooms");
            ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMROID='"+roomIDtoLoad+"'");
            recordCount=DB.getRecordCount(D,R);
            updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
            String roomID=null;
            if(R.next())
            {
                currentRecordPos=R.getRow();
                roomID=DBConnections.getRes(R,"CMROID");
                String localeID=DBConnections.getRes(R,"CMLOID");
                //String areaName=DBConnections.getRes(R,"CMAREA");
                Room newRoom=CMClass.getLocale(localeID);
                if(newRoom==null)
                    Log.errOut("Room","Couldn't load room '"+roomID+"', localeID '"+localeID+"'.");
                else
                {
                    newRoom.setRoomID(roomID);
                    newRoom.setDisplayText(DBConnections.getRes(R,"CMDESC1"));
                    if(CMProps.getBoolVar(CMProps.SYSTEMB_ROOMDNOCACHE))
                        newRoom.setDescription("");
                    else
                        newRoom.setDescription(DBConnections.getRes(R,"CMDESC2"));
                    newRoom.setMiscText(DBConnections.getRes(R,"CMROTX"));
                }
                if(((currentRecordPos%updateBreak)==0)&&(reportStatus))
                    CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Loading Rooms ("+currentRecordPos+" of "+recordCount+")");
                return newRoom;
            }
        }
        catch(SQLException sqle)
        {
            Log.errOut("Room",sqle);
        }
        finally
        {
	        if(D!=null) DB.DBDone(D);
        }
        return null;
    }
    
    public Vector DBReadRoomData(String singleRoomIDtoLoad,
								 RoomnumberSet roomsToLoad,
								 boolean reportStatus, 
								 Vector unknownAreas, 
								 RoomnumberSet unloadedRooms)
    {
        Vector rooms=new Vector();
        DBConnection D=null;
        try
        {
            D=DB.DBFetch();
            if(reportStatus)
                CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Counting Rooms");
            ResultSet R=D.query("SELECT * FROM CMROOM"+((singleRoomIDtoLoad==null)?"":" WHERE CMROID='"+singleRoomIDtoLoad+"'"));
            recordCount=DB.getRecordCount(D,R);
            updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
            String roomID=null;
            while(R.next())
            {
                currentRecordPos=R.getRow();
                roomID=DBConnections.getRes(R,"CMROID");
                if((roomsToLoad!=null)&&(!roomsToLoad.contains(roomID)))
                	continue;
                String localeID=DBConnections.getRes(R,"CMLOID");
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
                myArea.addProperRoomnumber(roomID);
                if(CMath.bset(myArea.flags(),Area.FLAG_THIN))
                {
                	if(unloadedRooms!=null)
                	{
                		if(!unloadedRooms.contains(roomID))
                			unloadedRooms.add(roomID);
                		continue;
                	}
                }
                Room newRoom=CMClass.getLocale(localeID);
                if(newRoom==null)
                    Log.errOut("Room","Couldn't load room '"+roomID+"', localeID '"+localeID+"'.");
                else
                {
                    newRoom.setRoomID(roomID);
                    newRoom.setArea(myArea);
                    newRoom.setDisplayText(DBConnections.getRes(R,"CMDESC1"));
                    if(CMProps.getBoolVar(CMProps.SYSTEMB_ROOMDNOCACHE))
                        newRoom.setDescription("");
                    else
                        newRoom.setDescription(DBConnections.getRes(R,"CMDESC2"));
                    newRoom.setMiscText(DBConnections.getRes(R,"CMROTX"));
                    addRoom(rooms,newRoom);
                }
                if(((currentRecordPos%updateBreak)==0)&&(reportStatus))
                    CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Loading Rooms ("+currentRecordPos+" of "+recordCount+")");
            }
        }
        catch(SQLException sqle)
        {
            Log.errOut("Room",sqle);
            rooms=null;
        }
        finally
        {
	        if(D!=null) DB.DBDone(D);
        }
        return rooms;
    }

    public void DBReadRoomExits(String roomID, Vector allRooms, boolean reportStatus)
    { DBReadRoomExits(roomID,allRooms,reportStatus,null);}
    
    public void DBReadRoomExits(String roomID, Vector allRooms, boolean reportStatus, RoomnumberSet unloadedRooms)
    {
        DBConnection D=null;
        // now grab the exits
        try
        {
            D=DB.DBFetch();
            if(reportStatus)
                CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Counting Exits");
            ResultSet R=D.query("SELECT * FROM CMROEX"+((roomID==null)?"":" WHERE CMROID='"+roomID+"'"));
            Room thisRoom=null;
            Room newRoom=null;
            recordCount=DB.getRecordCount(D,R);
            updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
            while(R.next())
            {
                currentRecordPos=R.getRow();
                roomID=DBConnections.getRes(R,"CMROID");
                int direction=(int)DBConnections.getLongRes(R,"CMDIRE");
                thisRoom=getRoom(allRooms,roomID);
                if(thisRoom==null)
                {
            		if((unloadedRooms!=null)&&(!unloadedRooms.contains(roomID)))
	                    Log.errOut("Room","Couldn't set "+direction+" exit for unknown room '"+roomID+"'");
                }
                else
                {
                    String exitID=DBConnections.getRes(R,"CMEXID");
                    String exitMiscText=DBConnections.getResQuietly(R,"CMEXTX");
                    String nextRoomID=DBConnections.getRes(R,"CMNRID");
                    newRoom=getRoom(allRooms,nextRoomID);
                    Exit newExit=CMClass.getExit(exitID);
            		if(newRoom==null)
            		{
            			if(((unloadedRooms!=null)&&(unloadedRooms.contains(nextRoomID)))
            			||(CMath.bset(thisRoom.getArea().flags(),Area.FLAG_THIN)))
            			{
            				newRoom=CMClass.getLocale("ThinRoom");
            				newRoom.setRoomID(nextRoomID);
            				newRoom.setArea(thisRoom.getArea());
            			}
            		}
            				
                    if((newExit==null)&&(newRoom==null))
                        Log.errOut("Room",roomID+":no room&exit to '"+nextRoomID+"', exit type '"+exitID+"', direction: "+direction);
                    else
                    if((direction>255)&&(!(thisRoom instanceof GridLocale)))
                        Log.errOut("Room","Not GridLocale, tried "+direction+" exit for room '"+roomID+"'");
                    else
                    if((direction>255)&&(newRoom!=null))
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
                                newRoom.setRawExit(CE.dir,CMClass.getExit("Open"));
                            }
                        }
                    }
                    else
                    {
                        if(newExit!=null)
                            newExit.setMiscText(exitMiscText);
                        if(direction>=Directions.NUM_DIRECTIONS())
                            Log.errOut("RoomLoader",CMLib.map().getExtendedRoomID(thisRoom)+" has an invalid direction #"+direction);
                        else
                        {
                            thisRoom.rawDoors()[direction]=newRoom;
                            thisRoom.setRawExit(direction,newExit);
                        }
                    }
                }
                if(reportStatus&&((currentRecordPos%updateBreak)==0))
                    CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Loading Exits ("+currentRecordPos+" of "+recordCount+")");
            }
        }
        catch(SQLException sqle)
        {
            Log.errOut("Room",sqle);
        }
        finally
        {
            if(D!=null) DB.DBDone(D);
        }
    }
    
	public void DBReadAllRooms(RoomnumberSet set)
	{
		Vector areas=null;
        Vector newAreasToCreate=new Vector();
		if(set==null)
		{
			while(CMLib.map().numAreas()>0)CMLib.map().delArea(CMLib.map().getFirstArea());
	
	        areas=DBReadAreaData(null,true);
	        if(areas==null) return;
	        for(int a=0;a<areas.size();a++)
	            CMLib.map().addArea((Area)areas.elementAt(a));
	        areas.clear();
		}

        RoomnumberSet unloadedRooms=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
        Vector rooms=DBReadRoomData(null,set,set==null,newAreasToCreate,unloadedRooms);
        
		// handle stray areas
		for(Enumeration e=newAreasToCreate.elements();e.hasMoreElements();)
		{
			String areaName=(String)e.nextElement();
			Log.sysOut("Area","Creating unhandled area: "+areaName);
			Area A=CMClass.getAreaType("StdArea");
			A.setName(areaName);
			DBCreate(A);
			CMLib.map().addArea(A);
			for(Enumeration r=rooms.elements();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.getArea().Name().equals(areaName))
					R.setArea(A);
			}
		}
        
        DBReadRoomExits(null,rooms,set==null,unloadedRooms);

		DBReadContent(null,null,rooms,unloadedRooms,set==null);

		if(set==null)
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Finalizing room data)");

		for(Enumeration r=rooms.elements();r.hasMoreElements();)
		{
			Room thisRoom=(Room)r.nextElement();
			thisRoom.startItemRejuv();
			thisRoom.recoverRoomStats();
		}

		if(set==null)
			for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
				((Area)a.nextElement()).getAreaStats();
	}
    
	public String DBReadRoomDesc(String roomID)
	{
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROOM WHERE CMROID='"+roomID+"'");
			if(R.next())
			{
				String txt=DBConnections.getRes(R,"CMDESC2");
				DB.DBDone(D);
				return txt;
			}
			DB.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DB.DBDone(D);
		}
		return null;
	}
	
	public String DBReadRoomMOBData(String roomID, String mobID)
	{
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMROCH WHERE CMROID='"+roomID+"'");
			while(R.next())
			{
				String NUMID=DBConnections.getRes(R,"CMCHNM");
				if(NUMID.equalsIgnoreCase(mobID))
				{
					String txt=DBConnections.getRes(R,"CMCHTX");
					DB.DBDone(D);
					return txt;
				}
			}
			DB.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
			if(D!=null) DB.DBDone(D);
		}
		return null;
	}

	private void fixItemKeys(Hashtable itemLocs, Hashtable itemNums)
	{
		for(Enumeration e=itemLocs.keys();e.hasMoreElements();)
		{
			Item keyItem=(Item)e.nextElement();
			String location=(String)itemLocs.get(keyItem);
			Environmental container=(Environmental)itemNums.get(location);
			if((container instanceof Container)&&(((Container)container).capacity()>0))
				keyItem.setContainer((Container)container);
			else
			if(container instanceof Rideable)
				keyItem.setRiding((Rideable)container);
			else
			if(container instanceof Item)
				keyItem.setContainer((Item)container);
		}
	}

	private void fixMOBRides(Hashtable mobRides, Hashtable itemNums)
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

	private void fixContentContainers(Hashtable content, Hashtable stuff, String roomID, Room room, boolean debug)
	{
		String lastName=null;
		Hashtable itemLocs=null;
		Hashtable mobRides=null;
        if(room != null)
    		for(Enumeration i=content.elements();i.hasMoreElements();)
    		{
    			Environmental E=(Environmental)i.nextElement();
    			if((debug)&&((lastName==null)||(!lastName.equals(E.Name()))))
                {lastName=E.Name(); Log.debugOut("RoomLoader","Loading object(s): "+E.Name());}
    			if(E instanceof Item)
    				room.addItem((Item)E);
    			else
                {
                    ((MOB)E).setStartRoom(room);
    				((MOB)E).bringToLife(room,true);
                }
    		}
		itemLocs=(Hashtable)stuff.get("LOCSFOR"+roomID.toUpperCase());
		mobRides=(Hashtable)stuff.get("RIDESFOR"+roomID.toUpperCase());
		if(itemLocs!=null)
		{
			fixItemKeys(itemLocs,content);
			if(room!=null)
			{
				room.recoverRoomStats();
				room.recoverRoomStats();
			}
		}
		if(mobRides!=null)
			fixMOBRides(mobRides,content);
	}
	
	public void DBReadCatalogs()
	{
		DBReadContent("CATALOG_MOBS",null,null,null,true);
		DBReadContent("CATALOG_ITEMS",null,null,null,true);
	}
	
    public void DBReadContent(String thisRoomID, Room thisRoom, Vector rooms, RoomnumberSet unloadedRooms, boolean setStatus)
	{
		boolean debug=Log.debugChannelOn()&&(CMSecurity.isDebugging("DBROOMPOP"));
		if(debug||(Log.debugChannelOn()&&(CMSecurity.isDebugging("DBROOMS"))))
			Log.debugOut("RoomLoader","Reading content of "+((thisRoomID!=null)?thisRoomID:"ALL"));
		
		Hashtable stuff=new Hashtable();
        Hashtable itemNums=null;
        Hashtable cataData=null;
        Hashtable itemLocs=null;
		Hashtable mobRides=null;
		
		boolean catalog=((thisRoomID!=null)&&(thisRoomID.startsWith("CATALOG_")));

		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			if(setStatus)
				CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Counting Items");
			ResultSet R=D.query("SELECT * FROM CMROIT"+((thisRoomID==null)?"":" WHERE CMROID='"+thisRoomID+"'"));
			if(setStatus) recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String roomID=DBConnections.getRes(R,"CMROID");
				if((unloadedRooms!=null)&&(unloadedRooms.contains(roomID)))
					continue;
				itemNums=(Hashtable)stuff.get("NUMSFOR"+roomID.toUpperCase());
				if(itemNums==null)
				{
					itemNums=new Hashtable();
					stuff.put("NUMSFOR"+roomID.toUpperCase(),itemNums);
				}
				itemLocs=(Hashtable)stuff.get("LOCSFOR"+roomID.toUpperCase());
				if(itemLocs==null)
				{
					itemLocs=new Hashtable();
					stuff.put("LOCSFOR"+roomID.toUpperCase(),itemLocs);
				}
				String itemNum=DBConnections.getRes(R,"CMITNM");
				String itemID=DBConnections.getRes(R,"CMITID");
				Item newItem=CMClass.getItem(itemID);
				if(newItem==null)
					Log.errOut("Room","Couldn't find item '"+itemID+"'");
				else
				{
					newItem.setDatabaseID(itemNum);
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
					try {
						if(catalog)
						{
						    String text=DBConnections.getResQuietly(R,"CMITTX");
						    int x=text.lastIndexOf("<CATALOGDATA>");
						    if((x>0)&&(text.indexOf("</CATALOGDATA>",x)>0))
					        {
				                cataData=(Hashtable)stuff.get("CATADATAFOR"+roomID.toUpperCase());
				                if(cataData==null)
				                {
				                    cataData=new Hashtable();
				                    stuff.put("CATADATAFOR"+roomID.toUpperCase(),cataData);
				                }
				                cataData.put(itemNum,text.substring(x));
				                text=text.substring(0,x);
					        }
	                        newItem.setMiscText(text);
						}
						else
	    					newItem.setMiscText(DBConnections.getResQuietly(R,"CMITTX"));
						newItem.baseEnvStats().setRejuv((int)DBConnections.getLongRes(R,"CMITRE"));
						newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
						newItem.baseEnvStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
						newItem.baseEnvStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
						newItem.baseEnvStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
						newItem.recoverEnvStats();
	                } catch(Exception e) { Log.errOut("RoomLoader",e); itemNums.remove(itemNum);}
				}
				if(((currentRecordPos%updateBreak)==0)&&(setStatus))
					CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Loading Items ("+currentRecordPos+" of "+recordCount+")");
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			if(D!=null) DB.DBDone(D);
		}

		// now grab the inhabitants
		try
		{
			D=DB.DBFetch();
			if(setStatus)
				CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Counting MOBS");
			ResultSet R=D.query("SELECT * FROM CMROCH"+((thisRoomID==null)?"":" WHERE CMROID='"+thisRoomID+"'"));
			if(setStatus) recordCount=DB.getRecordCount(D,R);
			updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String roomID=DBConnections.getRes(R,"CMROID");
				if((unloadedRooms!=null)&&(unloadedRooms.contains(roomID)))
					continue;
				String NUMID=DBConnections.getRes(R,"CMCHNM");
				String MOBID=DBConnections.getRes(R,"CMCHID");

				itemNums=(Hashtable)stuff.get("NUMSFOR"+roomID.toUpperCase());
				if(itemNums==null)
				{
					itemNums=new Hashtable();
					stuff.put("NUMSFOR"+roomID.toUpperCase(),itemNums);
				}
				mobRides=(Hashtable)stuff.get("RIDESFOR"+roomID.toUpperCase());
				if(mobRides==null)
				{
					mobRides=new Hashtable();
					stuff.put("RIDESFOR"+roomID.toUpperCase(),mobRides);
				}

				MOB newMOB=CMClass.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("Room","Couldn't find MOB '"+MOBID+"'");
				else
				{
					newMOB.setDatabaseID(NUMID);
					itemNums.put(NUMID,newMOB);
                    if(thisRoom!=null)
                    {
                        newMOB.setStartRoom(thisRoom);
                        newMOB.setLocation(thisRoom);
                    }
                    try {
						if((CMProps.getBoolVar(CMProps.SYSTEMB_MOBNOCACHE))
						&&(!catalog)
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
                    } catch(Exception e) { Log.errOut("RoomLoader",e); itemNums.remove(NUMID);}
				}
				if(((currentRecordPos%updateBreak)==0)&&(setStatus))
					CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Loading MOBs ("+currentRecordPos+" of "+recordCount+")");
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Room",sqle);
		}
		finally
		{
			if(D!=null) DB.DBDone(D);
		}
		if(thisRoom!=null)
		{
			rooms=new Vector();
			rooms.addElement(thisRoom);
		}
		if(rooms!=null) recordCount=rooms.size();
		updateBreak=CMath.s_int("1"+zeroes.substring(0,(""+(recordCount/100)).length()-1));
		currentRecordPos=0;

		itemNums=(Hashtable)stuff.get("NUMSFORCATALOG_ITEMS");
        cataData=(Hashtable)stuff.get("CATADATAFORCATALOG_ITEMS");
		if((itemNums!=null)&&(thisRoomID!=null)&&(thisRoomID.equals("CATALOG_ITEMS")))
		{
            String itemNum;
            Item I;
            String data;
			fixContentContainers(itemNums,stuff,"CATALOG_ITEMS",null,debug);
			for(Enumeration e=itemNums.keys();e.hasMoreElements();)
			{
			    itemNum=(String)e.nextElement();
			    I=(Item)itemNums.get(itemNum);
			    data=(String)((cataData!=null)?cataData.get(itemNum):null);
			    Item oldI=CMLib.catalog().getCatalogItem(I.Name());
			    if((oldI!=null)&&(I.databaseID().length()>0)&&(!oldI.databaseID().equals(I.databaseID())))
			    	DBDeleteRoomItem("CATALOG_ITEMS", I);
			    else	
			    {
					CMLib.catalog().submitToCatalog(I);
					if((data!=null)&&(data.length()>0))
					{
						CatalogLibrary.CataData dataI=CMLib.catalog().getCatalogItemData(I.Name());
						if(dataI!=null)
							dataI.build(data);
					}
			    }
			}
		}

		// load mob catalog
		itemNums=(Hashtable)stuff.get("NUMSFORCATALOG_MOBS");
        cataData=(Hashtable)stuff.get("CATADATAFORCATALOG_MOBS");
		if((itemNums!=null)&&(thisRoomID!=null)&&(thisRoomID.equals("CATALOG_MOBS")))
		{
            String itemNum;
            MOB M;
            String data;
			fixContentContainers(itemNums,stuff,"CATALOG_MOBS",null,debug);
			for(Enumeration e=itemNums.keys();e.hasMoreElements();)
			{
                itemNum=(String)e.nextElement();
                M=(MOB)itemNums.get(itemNum);
                data=(String)((cataData!=null)?cataData.get(itemNum):null);
                MOB oldM=CMLib.catalog().getCatalogMob(M.Name());
			    if((oldM!=null)&&(M.databaseID().length()>0)&&(!oldM.databaseID().equals(M.databaseID())))
			    	DBDeleteRoomMOB("CATALOG_MOBS", M);
			    else	
			    {
					CMLib.catalog().submitToCatalog(M);
	                if((data!=null)&&(data.length()>0))
	                {
						CatalogLibrary.CataData dataM=CMLib.catalog().getCatalogMobData(M.Name());
						if(dataM!=null)
							dataM.build(data);
	                }
			    }
			}
		}
		
		// now load the rooms
		if(rooms!=null)
		for(Enumeration e=rooms.elements();e.hasMoreElements();)
		{
			if((((++currentRecordPos)%updateBreak)==0)&&(setStatus))
				CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Populating Rooms ("+(currentRecordPos)+" of "+recordCount+")");
			Room room=(Room)e.nextElement();
			if(debug) Log.debugOut("RoomLoader","Populating room: "+room.roomID());
			itemNums=(Hashtable)stuff.get("NUMSFOR"+room.roomID().toUpperCase());
			if(itemNums!=null)
				fixContentContainers(itemNums,stuff,room.roomID(),room,debug);
		}
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done reading content of "+((thisRoomID!=null)?thisRoomID:"ALL"));
	}

    
    private Vector DBGetContents(Room room)
    {
		if((!room.savable())||(room.amDestroyed())) return new Vector();
		Vector contents=new Vector();
		for(int i=0;i<room.numItems();i++)
		{
			Item thisItem=room.fetchItem(i);
			if((thisItem!=null)&&(!contents.contains(thisItem))&&thisItem.savable())
				contents.addElement(thisItem);
		}
		return contents;
    }
    
    protected String getDBCreateItemString(String roomID, Item thisItem)
    {
        boolean catalog=((roomID!=null)&&(roomID.startsWith("CATALOG_")));
		thisItem.setExpirationDate(0); // saved items won't clear!
		Environmental container=thisItem.container();
		if((container==null)
		&&(thisItem.riding()!=null)
		&&(thisItem.riding().savable()))
		{
			Room room=CMLib.map().roomLocation(thisItem);
			if(((room!=null)&&(room.isHere(thisItem.riding())))
			||(CMLib.catalog().isCatalogObj(thisItem.riding())))
				container=thisItem.riding();
		}
		String itemID=""+thisItem;
		thisItem.setDatabaseID(itemID);
		String text=thisItem.text();
		if(catalog)
		{
			CatalogLibrary.CataData dataI=CMLib.catalog().getCatalogItemData(thisItem.Name());
			if(dataI!=null)
		        text+=dataI.data();
		}
		return
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
		+"'"+roomID+"',"
		+"'"+itemID+"',"
		+"'"+thisItem.ID()+"',"
		+"'"+((container!=null)?(""+container):"")+"',"
		+"'"+text+" ',"
		+thisItem.baseEnvStats().rejuv()+","
		+thisItem.usesRemaining()+","
		+thisItem.baseEnvStats().level()+","
		+thisItem.baseEnvStats().ability()+","
		+thisItem.baseEnvStats().height()+")";
    }
	public void DBCreateThisItem(String roomID, Item thisItem)
	{
		DB.update(getDBCreateItemString(roomID,thisItem));
	}
	
	public void DBUpdateTheseItems(Room room, Vector items)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Start item update for room "+room.roomID());
		Vector statements=new Vector();
		statements.addElement("DELETE FROM CMROIT WHERE CMROID='"+room.roomID()+"'");
		for(int i=0;i<items.size();i++)
		{
			Item thisItem=(Item)items.elementAt(i);
			statements.addElement(getDBCreateItemString(room.roomID(),thisItem));
		}
		DB.update(CMParms.toStringArray(statements));
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Finished items update for room "+room.roomID());
	}
	
	public void DBUpdateItems(Room room)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		DBUpdateTheseItems(room,DBGetContents(room));
	}

	public void DBUpdateExits(Room room)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROEX")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Starting exit update for room "+room.roomID());
		Vector statements=new Vector();
		statements.addElement("DELETE FROM CMROEX WHERE CMROID='"+room.roomID()+"'");
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Exit thisExit=room.getRawExit(d);
			Room thisRoom=room.rawDoors()[d];
			if(((thisRoom!=null)||(thisExit!=null))
			   &&((thisRoom==null)||(thisRoom.savable())))
			{
				statements.addElement(
				"INSERT INTO CMROEX ("
				+"CMROID, "
				+"CMDIRE, "
				+"CMEXID, "
				+"CMEXTX, "
				+"CMNRID"
				+") values ("
				+"'"+room.roomID()+"',"
				+d+","
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
				if((R.savable())&&(!done.contains(R.roomID())))
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
					statements.addElement(
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
		DB.update(CMParms.toStringArray(statements));
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROEX")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Finished exit update for room "+room.roomID());
	}

	public void DBCreateThisMOB(String roomID, MOB thisMOB)
	{
		DB.update(getDBCreateMOBString(roomID,thisMOB));
	}
	
	public String getDBCreateMOBString(String roomID, MOB thisMOB)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Creating mob "+thisMOB.name()+" for room "+roomID);
        boolean catalog=((roomID!=null)&&(roomID.startsWith("CATALOG_")));
        
		String ride=null;
		if(thisMOB.riding()!=null)
			ride=""+thisMOB.riding();
		else
		if(thisMOB.amFollowing()!=null)
			ride=""+thisMOB.amFollowing();
		else
			ride="";
		String mobID=""+thisMOB;
		thisMOB.setDatabaseID(mobID);
		
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Created mob "+thisMOB.name()+" for room "+roomID);
		
		if((CMProps.getBoolVar(CMProps.SYSTEMB_MOBNOCACHE))&&(!catalog))
		   thisMOB.setMiscText("%DBID>"+roomID+mobID.substring(mobID.indexOf("@")));
		
		return
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
		+"'"+roomID+"',"
		+"'"+mobID+"',"
		+"'"+CMClass.classID(thisMOB)+"',"
		+"'"+thisMOB.text()+" ',"
		+thisMOB.baseEnvStats().level()+","
		+thisMOB.baseEnvStats().ability()+","
		+thisMOB.baseEnvStats().rejuv()+","
		+"'"+ride+"'"
		+")";
	}
	
	public void DBUpdateTheseMOBs(Room room, Vector mobs)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Updating mobs for room "+room.roomID());
		if(mobs==null) mobs=new Vector();
		Vector statements=new Vector();
		statements.addElement("DELETE FROM CMROCH WHERE CMROID='"+room.roomID()+"'");
		for(int m=0;m<mobs.size();m++)
		{
			MOB thisMOB=(MOB)mobs.elementAt(m);
			statements.addElement(getDBCreateMOBString(room.roomID(),thisMOB));
		}
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating mobs for room "+room.roomID());
		DB.update(CMParms.toStringArray(statements));
	}

	public void DBUpdateMOBs(Room room)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		Vector mobs=new Vector();
		for(int m=0;m<room.numInhabitants();m++)
		{
			MOB thisMOB=room.fetchInhabitant(m);
			if((thisMOB!=null)&&(thisMOB.savable()))
				mobs.addElement(thisMOB);
		}
		DBUpdateTheseMOBs(room,mobs);
	}


	public void DBUpdateAll(Room room)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		DBUpdateRoom(room);
		DBUpdateMOBs(room);
		DBUpdateExits(room);
		DBUpdateItems(room);
	}

	public void DBUpdateRoom(Room room)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Start updating room "+room.roomID());
		DB.update(
		"UPDATE CMROOM SET "
		+"CMLOID='"+CMClass.classID(room)+"',"
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


	public void DBReCreate(Room room, String oldID)
	{
		if((!room.savable())||(room.amDestroyed())) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Recreating room "+room.roomID());
		
		DB.update(
		"UPDATE CMROOM SET "
		+"CMROID='"+room.roomID()+"', "
		+"CMAREA='"+room.getArea().Name()+"' "
		+"WHERE CMROID='"+oldID+"'");
		
		if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBNOCACHE))
			for(int m=0;m<room.numInhabitants();m++)
			{
				MOB M=room.fetchInhabitant(m);
				if((M!=null)&&(M.savable()))
					M.setMiscText(M.text());
			}
		
		DB.update(
		"UPDATE CMROCH SET "
		+"CMROID='"+room.roomID()+"' "
		+"WHERE CMROID='"+oldID+"'");

		DB.update(
		"UPDATE CMROEX SET "
		+"CMROID='"+room.roomID()+"' "
		+"WHERE CMROID='"+oldID+"'");
		
		DB.update(
		"UPDATE CMROEX SET "
		+"CMNRID='"+room.roomID()+"' "
		+"WHERE CMNRID='"+oldID+"'");
		
		DB.update(
		"UPDATE CMROIT SET "
		+"CMROID='"+room.roomID()+"' "
		+"WHERE CMROID='"+oldID+"'");
		
		DB.update(
		"UPDATE CMCHAR SET "
		+"CMROID='"+room.roomID()+"' "
		+"WHERE CMROID='"+oldID+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done recreating room "+room.roomID());
	}

	public void DBCreate(Area A)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Creating area "+A.name());
		if((A==null)||(A.name().length()==0)) {
			Log.errOut("RoomLoader","Unable to create area "+((A!=null)?A.name():"null"));
			return;
		}

		//CMLib.map().addArea(A); // not sure why I ever toyed with this idea, but apparantly I did.
		DB.update(
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
        A.setAreaState(Area.STATE_ACTIVE);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done creating area "+A.name());
	}

	public void DBUpdate(String keyName,Area A)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Updating area "+A.name());
		boolean ignoreType=CMSecurity.isDisabled("FATAREAS")||CMSecurity.isDisabled("THINAREAS");
		DB.update(
		"UPDATE CMAREA SET "
		+"CMAREA='"+A.Name()+"',"
		+(ignoreType?"":"CMTYPE='"+A.ID()+"',")
		+"CMCLIM="+A.climateType()+","
		+"CMSUBS='"+A.getSubOpList()+"',"
		+"CMDESC='"+A.description()+" ',"
		+"CMROTX='"+A.text()+" ',"
		+"CMTECH="+A.getTechLevel()+" "
		+"WHERE CMAREA='"+keyName+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating area "+A.name());
	}

	public void DBDeleteRoomItem(String roomID, Item item)
	{
		String keyName=item.databaseID();
		if(keyName.length()==0) keyName=""+item;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROIT")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating item "+item.name()+" in room "+roomID);
		DB.update(
		"DELETE FROM CMROIT "
		+"WHERE CMROID='"+roomID+"' "
		+"AND CMITNM='"+keyName+"'");
	}
	
	public void DBUpdateRoomItem(String roomID, Item item)
	{
		if((roomID==null)||(!item.savable())||(item.amDestroyed())) return;
		synchronized(roomID.toUpperCase().intern())
		{
			DBDeleteRoomItem(roomID,item);
			if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROIT")||CMSecurity.isDebugging("DBROOMS")))
				Log.debugOut("RoomLoader","Continue updating item "+item.name()+" in room "+roomID);
			DBCreateThisItem(roomID,item);
			if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROIT")||CMSecurity.isDebugging("DBROOMS")))
				Log.debugOut("RoomLoader","Done updating item "+item.name()+" in room "+roomID);
		}
	}
	
	public void DBDeleteRoomMOB(String roomID, MOB mob)
	{
		String keyName=mob.databaseID();
		if(keyName.length()==0) keyName=""+mob;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating mob "+mob.name()+" in room "+roomID);
		DB.update(
		"DELETE FROM CMROCH "
		+"WHERE CMROID='"+roomID+"' "
		+"AND CMCHNM='"+keyName+"'");
	}
	
	public void DBUpdateRoomMOB(String roomID, MOB mob)
	{
		if((roomID==null)||(!mob.savable())||(mob.amDestroyed())) return;
		DBDeleteRoomMOB(roomID, mob);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Continue updating mob "+mob.name()+" in room "+roomID);
		DBCreateThisMOB(roomID,mob);
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done updating mob "+mob.name()+" in room "+roomID);
	}

	public void DBDelete(Area A)
	{
		if(A==null) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Destroying area "+A.name());
        A.setAreaState(Area.STATE_STOPPED);
		DB.update("DELETE FROM CMAREA WHERE CMAREA='"+A.Name()+"'");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMAREA")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done destroying area "+A.name()+".");
	}


	public void DBCreate(Room room)
	{
		if(!room.savable()) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Creating new room "+room.roomID());
		DB.update(
		"INSERT INTO CMROOM ("
		+"CMROID,"
		+"CMLOID,"
		+"CMAREA,"
		+"CMDESC1,"
		+"CMDESC2,"
		+"CMROTX"
		+") values ("
		+"'"+room.roomID()+"',"
		+"'"+CMClass.classID(room)+"',"
		+"'"+room.getArea().Name()+"',"
		+"'"+room.displayText()+" ',"
		+"'"+room.description()+" ',"
		+"'"+room.text()+" ')");
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROOM")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done creating new room "+room.roomID());
	}

	public void DBDelete(Room room)
	{
		if(!room.savable()) return;
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Destroying room "+room.roomID());
		room.destroy();
		DB.update("DELETE FROM CMROCH WHERE CMROID='"+room.roomID()+"'");
		DB.update("DELETE FROM CMROIT WHERE CMROID='"+room.roomID()+"'");
		DB.update("DELETE FROM CMROEX WHERE CMROID='"+room.roomID()+"'");
		DB.update("DELETE FROM CMROOM WHERE CMROID='"+room.roomID()+"'");
		room.destroy();
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging("CMROCH")||CMSecurity.isDebugging("DBROOMS")))
			Log.debugOut("RoomLoader","Done gestroying room "+room.roomID());
	}
}
