package com.planet_ink.coffee_mud.web.macros.grinder;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class GrinderMap
{
    private Vector areaMap=null;
    private GrinderRoom[][] grid=null;
    private Hashtable hashRooms=null;
    public int Xbound=0;
    public int Ybound=0;
	public Area area=null;
	
	public GrinderMap(Area A)
	{
		area=A;
		areaMap=new Vector();
		Vector rooms=A.getMyMap();
		for(int r=0;r<rooms.size();r++)
		{
			Room R=(Room)rooms.elementAt(r);
			GrinderRoom GR=new GrinderRoom(R);
			areaMap.addElement(GR);
		}
	}
	
    public void rebuildGrid()
    {
	    // build grid!
	    int xoffset=0;
	    int yoffset=0;
	    for(int x=0;x<areaMap.size();x++)
	        if(((GrinderRoom)areaMap.elementAt(x)).x<xoffset)
	            xoffset=((GrinderRoom)areaMap.elementAt(x)).x;
        	            
	    for(int y=0;y<areaMap.size();y++)
	        if(((GrinderRoom)areaMap.elementAt(y)).y<yoffset)
	            yoffset=((GrinderRoom)areaMap.elementAt(y)).y;
        	            
	    xoffset=xoffset*-1;
	    yoffset=yoffset*-1;
        	    
	    Xbound=0;
	    Ybound=0;
	    for(int x=0;x<areaMap.size();x++)
	    {
	        GrinderRoom room=(GrinderRoom)areaMap.elementAt(x);
	        room.x=room.x+xoffset;
	        if(room.x>Xbound)
	            Xbound=room.x;
	        room.y=room.y+yoffset;
	        if(room.y>Ybound)
	            Ybound=room.y;
	    }
	    grid=new GrinderRoom[Xbound+1][Ybound+1];
	    hashRooms=new Hashtable();
	    for(int y=0;y<areaMap.size();y++)
	    {
	        GrinderRoom room=(GrinderRoom)areaMap.elementAt(y);
	        grid[room.x][room.y]=room;
	        hashRooms.put(room.roomID,room);
	    }
    }
    
    public void rePlaceRooms()
    {
        if(areaMap==null)
            return;
        grid=null;
        hashRooms=null;
        placeRooms();
        rebuildGrid();
    }
	
    private GrinderRoom getProcessedRoomAt(Hashtable processed, int x, int y)
    {
        for(Enumeration e=processed.elements();e.hasMoreElements();)
        {
            GrinderRoom room=(GrinderRoom)e.nextElement();
            if((room.x==x)&&(room.y==y))
                return room;
        }
        return null;
    }
    
    public GrinderRoom getRoom(Vector allRooms, String ID)
    {
        for(int r=0;r<allRooms.size();r++)
        {
            GrinderRoom room=(GrinderRoom)allRooms.elementAt(r);
            if(room.roomID.equalsIgnoreCase(ID))
                return room;
        }
        return null;
    }
    
    private final static int CLUSTERSIZE=3;
    
    private boolean isEmptyCluster(Hashtable processed, int x, int y)
    {
        for(Enumeration e=processed.elements();e.hasMoreElements();)
        {
            GrinderRoom room=(GrinderRoom)e.nextElement();
            if((((room.x>x-CLUSTERSIZE)&&(room.x<x+CLUSTERSIZE))
            &&((room.y>y-CLUSTERSIZE)&&(room.y<y+CLUSTERSIZE)))
            ||((room.x==x)&&(room.y==y)))
                return false;
        }
        return true;
    }
    
    private void findEmptyCluster(Hashtable processed, Vector XY)
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
    
    public boolean anythingThatDirection(GrinderRoom room, int direction)
    {
        GrinderDir D=room.doors[direction];
        if((D==null)||((D!=null)&&(D.room.length()==0)))
            return false;
        return true;
    }
    
    public void placeRooms()
    {
        if(areaMap==null) return;
        if(areaMap.size()==0) return;
        
        for(int i=0;i<areaMap.size();i++)
        {
            GrinderRoom room=(GrinderRoom)areaMap.elementAt(i);
            room.x=0;
            room.y=0;
            for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
            {
                GrinderDir dir=room.doors[d];
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
                GrinderRoom room=(GrinderRoom)areaMap.elementAt(i);
                if(processed.get(room.roomID)==null)
                {
                    placeRoom(room,0,0,processed,areaMap,true);
                    doneSomething=true;
                }
            }
        }
        if(areaMap.size()>processed.size())
            Log.errOut("GrinderMap",areaMap.size()-processed.size()+" room(s) could not be placed.  I recommend restarting your server.");
    }
    
    public void placeRoom(GrinderRoom room, 
                                int favoredX, 
                                int favoredY, 
                                Hashtable processed, 
                                Vector allRooms, 
                                boolean doNotDefer)
    {
        if(room==null) return;
        
        GrinderRoom anythingAt=getProcessedRoomAt(processed,favoredX,favoredY);
        if(anythingAt!=null)
        {
            // maybe someone else will take care of it?
            if(!doNotDefer)
                for(int r=0;r<allRooms.size();r++)
                {
                    GrinderRoom roomToBlame=(GrinderRoom)allRooms.elementAt(r);
                    if(roomToBlame!=room)
                        for(int rd=0;rd<Directions.NUM_DIRECTIONS;rd++)
                        {
                            GrinderDir RD=roomToBlame.doors[rd];
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
                roomID=((GrinderDir)room.doors[d]).room;
                
            if((roomID!=null)&&(roomID.length()>0)&&(processed.get(roomID)==null))
            {
                GrinderRoom nextRoom=getRoom(allRooms,roomID);
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
                    placeRoom(nextRoom,newFavoredX,newFavoredY,processed,allRooms,false);
                }
            }
        }
    }
}
