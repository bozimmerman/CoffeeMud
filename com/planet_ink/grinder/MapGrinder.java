package com.planet_ink.grinder;
import com.planet_ink.coffee_mud.utils.XMLManager;
import com.planet_ink.coffee_mud.utils.Directions;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;
import java.awt.Frame;
import javax.swing.JProgressBar;

public class MapGrinder extends Thread
{
	// The following function is a placeholder for control initialization.
	// You should call this function from a constructor or initialization function.
	public void vcInit() {
		//{{INIT_CONTROLS
		//}}
	}
	
    private static Vector areaMap=null;
    private static Room[][] grid=null;
    private static Hashtable hashRooms=null;
    public static int Xbound=0;
    public static int Ybound=0;
    public static MapGrinder saver=null;
    public static boolean shutDownPlease=false;
    public static boolean working=false;
    public GrinderMapLoader saverGML=null;
    public boolean saveErrorState=false;
    public boolean somethingWentDirty=false;
    public static Boolean mapIsSafe=new Boolean(true);
    
    public static void rebuildGrid(MUDGrinder forMe)
    {
	    // build grid!
	    int xoffset=0;
	    int yoffset=0;
	    for(int x=0;x<areaMap.size();x++)
	        if(((Room)areaMap.elementAt(x)).x<xoffset)
	            xoffset=((Room)areaMap.elementAt(x)).x;
        	            
	    for(int y=0;y<areaMap.size();y++)
	        if(((Room)areaMap.elementAt(y)).y<yoffset)
	            yoffset=((Room)areaMap.elementAt(y)).y;
        	            
	    xoffset=xoffset*-1;
	    yoffset=yoffset*-1;
        	    
	    Xbound=0;
	    Ybound=0;
	    for(int x=0;x<areaMap.size();x++)
	    {
	        Room room=(Room)areaMap.elementAt(x);
	        room.x=room.x+xoffset;
	        if(room.x>Xbound)
	            Xbound=room.x;
	        room.y=room.y+yoffset;
	        if(room.y>Ybound)
	            Ybound=room.y;
	    }
	    grid=new Room[Xbound+1][Ybound+1];
	    hashRooms=new Hashtable();
	    for(int y=0;y<areaMap.size();y++)
	    {
	        Room room=(Room)areaMap.elementAt(y);
	        grid[room.x][room.y]=room;
	        hashRooms.put(room.roomID,room);
	    }
	    
	    forMe.verticalScrollbar1.setMaximum(Ybound);
	    forMe.horizontalScrollbar1.setMaximum(Xbound);
	    //forMe.verticalScrollbar1.setValue(0);
	    //forMe.horizontalScrollbar1.setValue(0);
	    
    }
    
    public static void rePlaceRooms(MUDGrinder forMe)
    {
        if(areaMap==null)
            return;
        synchronized(mapIsSafe)
        {
            grid=null;
            hashRooms=null;
            placeRooms(forMe,null);
            rebuildGrid(forMe);
        }
    }
    
    public static synchronized Vector getMap(MUDGrinder forMe)
    {
        if(areaMap!=null)
            return areaMap;
        if(!TheGrinder.pickedAnArea())
            return null;
                
        areaMap=new Vector();
	    GrinderMapLoader GML=new GrinderMapLoader(forMe,areaMap,true);
	    GML.startReading();
        	    
        rebuildGrid(forMe);
        
	    if(saver==null)
	    {
	        saver=new MapGrinder();
	        saver.start();
	    }
        return areaMap;
    }
    
    public static Room[][] getGrid()
    {
        return grid;
    }
    
    public static Hashtable getHashRooms()
    {
        return hashRooms;
    }
    
    public static boolean saveIfAble(Frame MUDG)
    {
        if(saver!=null)
        {
            try
            {
                if(saver.somethingWentDirty)
                    Thread.sleep(1000);
            }
            catch(InterruptedException e)
            {
                
            }
            if(saver.working)
            {
                GrinderMapLoader GML=new GrinderMapLoader(MUDG,areaMap,true);
                saver.saverGML=GML;
                GML.label1.setText("Updating rooms...");
                GML.setVisible(true);
                saver.saverGML=null;
            }
            if(saver.saveErrorState)
            {
                GrinderOKBox.okMe(MUDG,"Bad bad thing.","An error occurred during processing. Some of your changes may not have saved. Please close this map and re-open it.  If the error persists, restart your server.");
                saver.saveErrorState=false;
                return false;
            }
        }
        return true;
    }
    
    
    public static boolean closeIfAble(Frame MUDG)
    {
        if(!saveIfAble(MUDG))
            return false;
        TheGrinder.setNewAreaName("");
        reset();
        return true;
    }
    
    public static boolean shutdownIfAble(Frame MUDG)
    {
        if(!closeIfAble(MUDG))
            return false;
        shutDownPlease=true;
        return true;
    }
    
    private static Room getProcessedRoomAt(Hashtable processed, int x, int y)
    {
        for(Enumeration e=processed.elements();e.hasMoreElements();)
        {
            MapGrinder.Room room=(MapGrinder.Room)e.nextElement();
            if((room.x==x)&&(room.y==y))
                return room;
        }
        return null;
    }
    
    public static MapGrinder.Room getRoom(Vector allRooms, String ID)
    {
        for(int r=0;r<allRooms.size();r++)
        {
            MapGrinder.Room room=(MapGrinder.Room)allRooms.elementAt(r);
            if(room.roomID.equalsIgnoreCase(ID))
                return room;
        }
        return null;
    }
    
    private final static int CLUSTERSIZE=3;
    
    private static boolean isEmptyCluster(Hashtable processed, int x, int y)
    {
        for(Enumeration e=processed.elements();e.hasMoreElements();)
        {
            MapGrinder.Room room=(MapGrinder.Room)e.nextElement();
            if((((room.x>x-CLUSTERSIZE)&&(room.x<x+CLUSTERSIZE))
            &&((room.y>y-CLUSTERSIZE)&&(room.y<y+CLUSTERSIZE)))
            ||((room.x==x)&&(room.y==y)))
                return false;
        }
        return true;
    }
    
    private static void findEmptyCluster(Hashtable processed, Vector XY)
    {
        int x=((Integer)XY.elementAt(0)).intValue();
        int y=((Integer)XY.elementAt(1)).intValue();
        int spacing=CLUSTERSIZE;
        while(true)
        {
            for(int i=0;i<8;i++)
            {
                int yadjust=0;
                int xadjust=0;
                switch(i)
                {
                    case 0: xadjust=1; break;
                    case 1: xadjust=1;yadjust=1; break;
                    case 2: yadjust=1; break;
                    case 3: xadjust=1;xadjust=-1; break;
                    case 4: xadjust=-1; break;
                    case 5: xadjust=-1;yadjust=-1; break;
                    case 6: yadjust=-1; break;
                    case 7: yadjust=-1;xadjust=1; break;
                }
                if(isEmptyCluster(processed,x+(spacing*xadjust),y+(spacing*yadjust)))
                {
                    XY.setElementAt(new Integer(x+(spacing*xadjust)),0);
                    XY.setElementAt(new Integer(y+(spacing*yadjust)),1);
                    return;
                }
            }
            spacing+=1;
        }
    }
    
    public static boolean anythingThatDirection(Room room, int direction)
    {
        Direction D=room.doors[direction];
        if((D==null)||((D!=null)&&(D.room.length()==0)))
            return false;
        return true;
    }
    
    public static void placeRooms(Frame grinder, JProgressBar JProgressBar1)
    {
        if(areaMap==null) return;
        if(areaMap.size()==0) return;
        
        for(int i=0;i<areaMap.size();i++)
        {
            Room room=(Room)areaMap.elementAt(i);
            room.x=0;
            room.y=0;
            for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
            {
                Direction dir=room.doors[d];
                if(dir!=null)
                    dir.positionedAlready=false;
            }
        }
        
        Hashtable processed=new Hashtable();
        boolean doneSomething=true;
        while((areaMap.size()>processed.size())&&(doneSomething))
        {
            doneSomething=false;
            for(int i=0;i<areaMap.size();i++)
            {
                Room room=(Room)areaMap.elementAt(i);
                if(processed.get(room.roomID)==null)
                {
                    placeRoom(room,0,0,processed,areaMap,JProgressBar1,true);
                    doneSomething=true;
                }
            }
        }
        if(areaMap.size()>processed.size())
            GrinderOKBox.okMe(grinder,"?!",areaMap.size()-processed.size()+" room(s) could not be placed.  I recommend restarting your server.");
    }
    
    public static void placeRoom(MapGrinder.Room room, 
                                int favoredX, 
                                int favoredY, 
                                Hashtable processed, 
                                Vector allRooms, 
                                JProgressBar bar, 
                                boolean doNotDefer)
    {
        if(room==null) return;
        
        if(bar!=null)
        {
            bar.setValue(bar.getValue()+1);
            bar.repaint();
        }
        
        Room anythingAt=getProcessedRoomAt(processed,favoredX,favoredY);
        if(anythingAt!=null)
        {
            // maybe someone else will take care of it?
            if(!doNotDefer)
                for(int r=0;r<allRooms.size();r++)
                {
                    Room roomToBlame=(Room)allRooms.elementAt(r);
                    if(roomToBlame!=room)
                        for(int rd=0;rd<Directions.NUM_DIRECTIONS;rd++)
                        {
                            Direction RD=roomToBlame.doors[rd];
                            if((RD!=null)&&(RD.room!=null)&&(RD.room.equals(room.roomID))&&(!RD.positionedAlready))
                                return;
                        }
                }
            // nope; nobody can.  It's up to this!
            Vector XY=new Vector();
            XY.addElement(new Integer(0));
            XY.addElement(new Integer(0));
            findEmptyCluster(processed,XY);
            room.x=((Integer)XY.elementAt(0)).intValue();
            room.y=((Integer)XY.elementAt(1)).intValue();
        }
        else
        {
            room.x=favoredX;
            room.y=favoredY;
        }
        
        // once done, is never undone.  A room is 
        // considered processed only once!
        processed.put(room.roomID,room);
        
        for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
        {
            String roomID=null;
            if(room.doors[d]!=null)
                roomID=((Direction)room.doors[d]).room;
                
            if((roomID!=null)&&(roomID.length()>0)&&(processed.get(roomID)==null))
            {
                Room nextRoom=getRoom(allRooms,roomID);
                if(nextRoom!=null)
                {
                    int newFavoredX=room.x;
                    int newFavoredY=room.y;
                    switch(d)
                    {
                        case Directions.NORTH:
                            newFavoredY--; break;
                        case Directions.SOUTH:
                            newFavoredY++; break;
                        case Directions.EAST:
                            newFavoredX++; break;
                        case Directions.WEST:
                            newFavoredX--; break;
                        case Directions.UP:
                            if(!anythingThatDirection(room,Directions.NORTH))
                                newFavoredY--;
                            else
                            if(!anythingThatDirection(room,Directions.WEST))
                                newFavoredX--;
                            else
                            if(!anythingThatDirection(room,Directions.EAST))
                                newFavoredX++;
                            else
                            if(!anythingThatDirection(room,Directions.SOUTH))
                                newFavoredY++;
                            break;
                        case Directions.DOWN:
                            if(!anythingThatDirection(room,Directions.SOUTH))
                                newFavoredY++;
                            else
                            if(!anythingThatDirection(room,Directions.EAST))
                                newFavoredX++;
                            else
                            if(!anythingThatDirection(room,Directions.WEST))
                                newFavoredX--;
                            else
                            if(!anythingThatDirection(room,Directions.NORTH))
                                newFavoredY--;
                            break;
                    }
                    room.doors[d].positionedAlready=true;
                    placeRoom(nextRoom,newFavoredX,newFavoredY,processed,allRooms,bar,false);
                }
            }
        }
    }
    
    public synchronized static void reset()
    {
        if(areaMap!=null)
        {
            synchronized(mapIsSafe)
            {
                areaMap=null;
                grid=null;
                hashRooms=null;
            }
        }
        else
        {
            areaMap=null;
            grid=null;
            hashRooms=null;
        }
    }
    
    public static StringBuffer updateRoomXML(Room room)
    {
		StringBuffer roomXML=new StringBuffer("");
		roomXML.append("<ROOM>");
		roomXML.append(XMLManager.convertXMLtoTag("ROOMID",room.roomID));
		roomXML.append(XMLManager.convertXMLtoTag("ROOMCLASS",room.classID));
		roomXML.append(XMLManager.convertXMLtoTag("ROOMAREA",TheGrinder.getAreaName()));
		roomXML.append(XMLManager.convertXMLtoTag("ROOMDISPLAYTEXT",room.displayText));
		roomXML.append(XMLManager.convertXMLtoTag("ROOMDESCRIPTION",room.description));
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
		    Direction door=room.doors[d];
			Exit exit=null;
			if(door!=null) exit=door.exit;
			roomXML.append("<ROOM"+Directions.getDirectionName(d).toUpperCase()+">");
			if((door!=null)&&(door.room!=null)&&(door.room.length()>0))
				roomXML.append(XMLManager.convertXMLtoTag("DOOR",door.room));
			else
				roomXML.append("<DOOR></DOOR>");
			roomXML.append("<EXIT>");
			if(exit!=null)
			{
				roomXML.append(XMLManager.convertXMLtoTag("EXITCLASS",exit.classID));
				roomXML.append(XMLManager.convertXMLtoTag("EXITTEXT",exit.miscText()));
			}
			roomXML.append("</EXIT>");
			roomXML.append("</ROOM"+Directions.getDirectionName(d).toUpperCase()+">");
		}
		roomXML.append("</ROOM>");
		return roomXML;
    }
    
    public static boolean createRoom(Room room)
    {
        room.roomID="NEW";
		StringBuffer roomXML=updateRoomXML(room);
        StringBuffer response=TheGrinder.safelyExpect(null,"ROOMXML "+roomXML,"</RESPONSE>");
        if(response==null)
          return false;
        room.roomID=XMLManager.returnXMLValue(response.toString(),"RESPONSE");
        if(room.roomID.equalsIgnoreCase("NEW"))
            return false;
        return true;
    }
    
    public static boolean updateRoom(Room room)
    {
        if(room.roomID.equalsIgnoreCase("new"))
            return createRoom(room);
		StringBuffer roomXML=updateRoomXML(room);
        StringBuffer response=TheGrinder.safelyExpect(null,"ROOMXML "+roomXML,"<RESPONSE>Done.</RESPONSE>");
        if(response==null)
          return false;
            
        room.dirty=false;
        return true;
    }
    public static Room loadRoom(String newRoomID)
    {
        String block="";
        StringBuffer roomData=TheGrinder.safelyExpect(null,"ROOMXML "+newRoomID,"</ROOM>");
        if(roomData!=null)
            block=XMLManager.returnXMLBlock(roomData.toString(),"ROOM");
        if((block!=null)&&(block.length()>0))
        {
            Room newRoom=new Room();
            newRoom.classID=XMLManager.returnXMLValue(block,"ROOMCLASS");
            newRoom.roomID=newRoomID;
            newRoom.displayText=XMLManager.returnXMLValue(block,"ROOMDISPLAYTEXT");
            newRoom.description=XMLManager.returnXMLValue(block,"ROOMDESCRIPTION");
            for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
            {
                Direction D=new Direction();
                String dirblock=XMLManager.returnXMLBlock(block,"ROOM"+Directions.getDirectionName(d).toUpperCase());
                if((dirblock!=null)&&(dirblock.length()>10))
                {
                    D.room=XMLManager.returnXMLValue(dirblock,"DOOR");
                    String exitblock=XMLManager.returnXMLBlock(dirblock,"EXIT");
                    if((exitblock!=null)&&(exitblock.length()>10))
                    {
                        Exit E=new Exit();
                        D.exit=E;
                        E.classID=XMLManager.returnXMLValue(exitblock,"EXITCLASS");
                        E.exitSame=XMLManager.returnXMLBoolean(exitblock,"EXITSAME");
                        E.hasADoor=XMLManager.returnXMLBoolean(exitblock,"EXITDOOR");
                        E.setMiscText(XMLManager.returnXMLValue(exitblock,"EXITTEXT"));
                    }
                }
                newRoom.doors[d]=D;
            }
            return newRoom;
        }
        return null;
    }
    public static boolean loadRoomItems(Room room)
    {
        if(room==null) return false;
        if(room.stuffLoaded) return true;
        //if(TheGrinder.safelyExpect(room.roomID,"RESETROOM","Done.")==null)
        //    return false;
        StringBuffer buf=TheGrinder.safelyExpect(null,"CONTENTXML "+room.roomID,"</ROOMCONTENTS>");
        if(buf==null) return false;
		/// do mobs.. 
		int num=1;
		String roomBlock=buf.toString();
		String mobBlock=XMLManager.returnXMLBlock(roomBlock,"ROOMMOBS");
		String mBlock="";
		if(mobBlock.length()>10)
			mBlock=XMLManager.returnXMLBlock(roomBlock,"ROOMMOB1");
	    room.mobs=new Vector();
		while(mBlock.length()>10)
		{
			String newClass=XMLManager.returnXMLValue(mBlock,"MOBCLASS");
			String newText=XMLManager.returnXMLValue(mBlock,"MOBTEXT");
			int newLevel=GenGrinder.s_int(XMLManager.returnXMLValue(mBlock,"MOBLEVEL"));
			int newAbility=GenGrinder.s_int(XMLManager.returnXMLValue(mBlock,"MOBABILITY"));
			int newRejuv=GenGrinder.s_int(XMLManager.returnXMLValue(mBlock,"MOBREJUV"));
			if(newRejuv<=0) newRejuv=Integer.MAX_VALUE;
			MOB newMOB=new MOB();
			newMOB.classID=newClass;
			newMOB.setMiscText(newText);
			newMOB.level=newLevel;
			newMOB.ability=newAbility;
			newMOB.rejuv=newRejuv;
			room.mobs.addElement(newMOB);
			//TheGrinder.addFiller(newMOB);
			
			// now rebuild!
			num++;
			mBlock=XMLManager.returnXMLBlock(mobBlock,"ROOMMOB"+num);
		}
			
			
			
		/// do items.. 
		num=1;
		String itemBlock=XMLManager.returnXMLBlock(roomBlock,"ROOMITEMS");
		String iBlock="";
		if(itemBlock.length()>10)
			iBlock=XMLManager.returnXMLBlock(roomBlock,"ROOMITEM1");
		room.items=new Vector();
		while(iBlock.length()>10)
		{
			String newClass=XMLManager.returnXMLValue(iBlock,"ITEMCLASS");
			String newText=XMLManager.returnXMLValue(iBlock,"ITEMTEXT");
			int newLevel=GenGrinder.s_int(XMLManager.returnXMLValue(iBlock,"ITEMLEVEL"));
			int newAbility=GenGrinder.s_int(XMLManager.returnXMLValue(iBlock,"ITEMABILITY"));
			int newRejuv=GenGrinder.s_int(XMLManager.returnXMLValue(iBlock,"ITEMREJUV"));
			if(newRejuv<=0) newRejuv=Integer.MAX_VALUE;
			int newUses=GenGrinder.s_int(XMLManager.returnXMLValue(iBlock,"ITEMUSES"));
			int newLocation=GenGrinder.s_int(XMLManager.returnXMLValue(iBlock,"ITEMLOCATION"));
				
			Item newItem=new Item();
			newItem.classID=newClass;
			newItem.setMiscText(newText);
			newItem.level=(newLevel);
			newItem.ability=(newAbility);
			newItem.rejuv=(newRejuv);
			newItem.usesRemaining=(newUses);
			newItem.locationNum=newLocation;
			room.items.addElement(newItem);
			//TheGrinder.addFiller(newItem);
			
			num++;
			iBlock=XMLManager.returnXMLBlock(itemBlock,"ROOMITEM"+num);
		}
		room.stuffLoaded=true;
		return true;
    }
    
    public static boolean deleteRoom(Room room)
    {
        String roomToCruise="Start";
        for(int r=0;r<areaMap.size();r++)
        {
            Room cruiser=(Room)areaMap.elementAt(r);
            if(!cruiser.deleted)
            {
                roomToCruise=cruiser.roomID;
                break;
            }
        }
        StringBuffer response=TheGrinder.safelyExpect(roomToCruise,"DESTROY ROOM "+room.roomID+" CONFIRMED","massive destruction");
        if(response==null)
            return false;
        return true;
    }
    
    public static boolean updateRoomItems(Room room)
    {
		StringBuffer roomXML=new StringBuffer("");
		roomXML.append("<ROOMID>"+room.roomID+"</ROOMID>");
		roomXML.append("<ROOMCONTENTS>");
		roomXML.append("<ROOMMOBS>");
		int num=0;
		for(int i=0;i<room.mobs.size();i++)
		{
			MOB mob=(MOB)room.mobs.elementAt(i);
			num++;
			roomXML.append("<ROOMMOB"+num+">");
			roomXML.append(XMLManager.convertXMLtoTag("MOBCLASS",mob.classID));
			roomXML.append(XMLManager.convertXMLtoTag("MOBTEXT",""+mob.miscText()));
			roomXML.append(XMLManager.convertXMLtoTag("MOBLEVEL",""+mob.level));
			roomXML.append(XMLManager.convertXMLtoTag("MOBABILITY",""+mob.ability));
			roomXML.append(XMLManager.convertXMLtoTag("MOBREJUV",""+mob.rejuv));
			roomXML.append("</ROOMMOB"+num+">");
		}
		roomXML.append("</ROOMMOBS>");
		roomXML.append("<ROOMITEMS>");
		num=0;
		for(int i=0;i<room.items.size();i++)
		{
			Item item=(Item)room.items.elementAt(i);
			num++;
			roomXML.append("<ROOMITEM"+num+">");
			roomXML.append(XMLManager.convertXMLtoTag("ITEMCLASS",item.classID));
			roomXML.append(XMLManager.convertXMLtoTag("ITEMTEXT",""+item.miscText()));
			roomXML.append(XMLManager.convertXMLtoTag("ITEMLEVEL",""+item.level));
			roomXML.append(XMLManager.convertXMLtoTag("ITEMABILITY",""+item.ability));
			roomXML.append(XMLManager.convertXMLtoTag("ITEMREJUV",""+item.rejuv));
			roomXML.append(XMLManager.convertXMLtoTag("ITEMUSES",""+item.usesRemaining));
			roomXML.append(XMLManager.convertXMLtoTag("ITEMLOCATION",""+item.locationNum));
			roomXML.append("</ROOMITEM"+num+">");
		}
			
		roomXML.append("</ROOMITEMS>");
		roomXML.append("</ROOMCONTENTS>");
        StringBuffer response=TheGrinder.safelyExpect(null,"CONTENTXML "+roomXML,"<RESPONSE>Done.</RESPONSE>");
        if(response==null)
            return false;
        
        room.dirtyItems=false;
        return true;
    }
    
    public static void setRoomDirty(Room room, boolean dirtyItems, boolean dirtyRoom)
    {
        if(room==null) return;
        if((!dirtyRoom)&&(!dirtyItems)) return;
        
        if(dirtyRoom)
            room.dirty=dirtyRoom;
        if(dirtyItems)
            room.dirtyItems=dirtyItems;
        if(saver!=null)
            saver.somethingWentDirty=true;
    }
    
    public static void setRoomDeleted(Room room)
    {
        room.deleted=true;
        room.x=Integer.MAX_VALUE;
        room.y=Integer.MAX_VALUE;
        if(saver!=null)
            saver.somethingWentDirty=true;
    }
    
    public void run()
    {
        while(!shutDownPlease)
        {
            if(!somethingWentDirty)
            {
                try
                {
                    this.sleep(1000);
                }
                catch(InterruptedException e)
                {
                    
                }
            }
            if((areaMap!=null)&&(grid!=null)&&(hashRooms!=null))
            synchronized(mapIsSafe)
            {
                working=true;
                boolean noerrors=true;
                while(somethingWentDirty)
                {
                    somethingWentDirty=false;
                    for(int r=0;r<areaMap.size();r++)
                    {
                        Room room=(Room)areaMap.elementAt(r);
                        if(saverGML!=null)
                        {
                            saverGML.JProgressBar1.setMaximum(areaMap.size());
                            saverGML.JProgressBar1.setValue(r);
                            saverGML.JProgressBar1.repaint();
                        }
                        if(room.deleted)
                            noerrors=noerrors&&deleteRoom(room);
                        else
                        {
                            if(room.dirty)
                                noerrors=noerrors&&updateRoom(room);
                            if(room.dirtyItems)
                                noerrors=noerrors&&updateRoomItems(room);
                        }
                    }
                }
                for(int r=areaMap.size()-1;r>=0;r--)
                {
                    Room room=(Room)areaMap.elementAt(r);
                    if(room.deleted)
                        areaMap.removeElement(room);
                }
                if(!noerrors)
                {
                    saveErrorState=true;
                    somethingWentDirty=true;
                }
                working=false;
                if(saverGML!=null)
                    saverGML.hide();
            }
        }
    }
    
    public  static class Direction implements Cloneable
    {
        String room="";
        Exit exit=null;
        
        public boolean positionedAlready=false;
    }
    
    public static class GenGen implements Cloneable
    {
        public int ability=0;
        public int rejuv=Integer.MAX_VALUE;
        public String name="";
        public String description="";
        public String displayText="";
        public String closedText="a closed door";
        public String doorName="door";
        public String openName="open";
        public String closeName="close";
        public String keyName="";
        public int level=0;
        public boolean hasADoor;
        public boolean hasALock;
        public boolean doorDefaultsClosed;
        public boolean doorDefaultsLocked;
        public boolean isReadable;
        public boolean levelRestricted;
        public boolean classRestricted;
        public boolean isTrapped;
        public int openDelayTicks=5;
		public String secretIdentity="";
		public boolean isGettable;
		public int baseGoldValue;
		public int weight;
		public int capacity;
		public int uses;
		public int sensesMask;
		public int disposition;
		public int nourishTotal; // liquid held
		public int nourishUse;
	    public String readableText="";
		public boolean isDroppable;
        public boolean isRemovable;
        public int alignment;
        public char gender='M';
        public int money;
        public int gold;
        public int armor;
        public double speed;
        public int attack;
        public int damage;
        public Vector behaviors;
        public Vector abilities;
        public Vector items;
        public Vector wornCodes;
        public int whatISell;
        public Vector inventory;
        public Vector numItems;
		public int materialCode;
		public boolean logicalAnd;
		public long properLocationBitmap;
		public int weaponType;
		public int weaponClassification;
    }
    
    public  static class Exit implements Cloneable
    {
        public String classID="StdExit";
        public boolean exitSame=true;
        public boolean hasADoor=false;
        
        private String text="";
        
        public String miscText()
        {
            return text;
        }
        
        public Exit getclone()
        {
            Exit newExit=new Exit();
            newExit.classID=classID;
            newExit.exitSame=exitSame;
            newExit.hasADoor=hasADoor;
            newExit.text=text;
            return newExit;
        }
        
        public void setMiscText(String newText)
        {
            text=newText;
            // don't forget to update "hasadoor"!
        }
    }
    
    public  static class Item implements Cloneable
    {
        public String classID="StdItem";
        public int ability=0;
        public int usesRemaining=Integer.MAX_VALUE;
        public int rejuv=Integer.MAX_VALUE;
        public int level=1;
        public int locationNum=0; // index from 1 through and including room.items.size
        
        private String text="";
        
        public String miscText()
        {
            return text;
        }
        
        public void copyInto(Item item)
        {
            item.classID=classID;
            item.ability=ability;
            item.usesRemaining=usesRemaining;
            item.rejuv=rejuv;
            item.level=level;
            item.locationNum=locationNum;
            item.setMiscText(text);
        }
        public Item cloneof()
        {
            Item item=new Item();
            copyInto(item);
            return item;
        }
        
        //non-generic
        public void setMiscText(String newText)
        {
            text=newText;
            
        }
    }
    public  static class MOB implements Cloneable
    {
        public String classID="StdMOB";
        public int ability=0;
        public int rejuv=Integer.MAX_VALUE;
        public int level=1;
        
        private String text="";
        
        public String miscText()
        {
            return text;
        }
        
        public void copyInto(MOB mob)
        {
            mob.classID=classID;
            mob.ability=ability;
            mob.rejuv=rejuv;
            mob.level=level;
            mob.setMiscText(text);
        }
        public MOB cloneof()
        {
            MOB mob=new MOB();
            copyInto(mob);
            return mob;
        }
        
        public void setMiscText(String newText)
        {
            text=newText;
        }
    }
    
    public static class Room implements Cloneable
    {
        public String classID="StdRoom";
        public int x;
        public int y;
        public boolean dirty=false;
        public boolean dirtyItems=false;
        public boolean deleted=false;
        public boolean stuffLoaded=false;
        public Vector mobs=new Vector();
        public Vector items=new Vector();
        
        public String roomID="NEW";
        public String displayText="New Room Name";
        public String description="Room Description";
        public Direction[] doors=new Direction[Directions.NUM_DIRECTIONS];
        
        public void copyInto(Room room)
        {
            room.classID=classID;
            room.x=x;
            room.y=y;
            room.dirty=dirty;
            room.dirtyItems=dirtyItems;
            room.deleted=deleted;
            room.stuffLoaded=stuffLoaded;
            room.mobs=new Vector();
            for(int i=0;i<mobs.size();i++)
            {
                MOB mob=(MOB)mobs.elementAt(i);
                room.mobs.addElement(mob.cloneof());
            }
            room.items=new Vector();
            for(int i=0;i<items.size();i++)
            {
                Item item=(Item)items.elementAt(i);
                room.items.addElement(item.cloneof());
            }
            room.roomID=roomID;
            room.displayText=displayText;
            room.description=description;
            for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
                room.doors[d]=doors[d];
        }
        
        public Room cloneof()
        {
            Room room=new Room();
            copyInto(room);
            return room;
        }
    }
	//{{DECLARE_CONTROLS
	//}}
}