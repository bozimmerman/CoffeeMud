package com.planet_ink.coffee_mud.web.macros.grinder;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class GrinderMap
{
    private Vector areaMap=null;
    public GrinderRoom[][] grid=null;
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
		if(areaMap==null) return;
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
            Log.errOut("GrinderMap",areaMap.size()-processed.size()+" room(s) were not placed.");
    }
	
	public StringBuffer getHTMLTable(ExternalHTTPRequests httpReq)
	{
		StringBuffer buf=new StringBuffer("");
		buf.append("<TABLE WIDTH="+((Xbound+1)*138)+" BORDER=0 CELLSPACING=0 CELLPADDING=0>");
		for(int y=0;y<=Ybound;y++)
		{
			// up=nwes
			// down=sewn
			for(int l=0;l<5;l++)
			{
				buf.append("<TR HEIGHT=24>");
				for(int x=0;x<=Xbound;x++)
				{
					GrinderRoom GR=grid[x][y];
					if(GR==null)
						buf.append("<TD COLSPAN=5 WIDTH=138><BR></TD>");
					else
					{
						int up=-1;
						int down=-1;
						if(GR.doors[Directions.UP]!=null)
							up=findRelGridDir(GR,GR.doors[Directions.UP].room);
						if(GR.doors[Directions.DOWN]!=null)
							down=findRelGridDir(GR,GR.doors[Directions.DOWN].room);
						if(up<0){
							if(down==Directions.NORTH)
								up=Directions.EAST;
							else
								up=Directions.NORTH;
						}
						if(down<0){
							if(up==Directions.SOUTH)
								down=Directions.WEST;
							else
								down=Directions.SOUTH;
						}
						switch(l)
						{
						case 0: // north, up
							{
							buf.append("<TD WIDTH=24><BR></TD>");
							buf.append("<TD WIDTH=30>"+getDoorLabelGif(Directions.NORTH,GR,httpReq)+"</TD>");
							buf.append("<TD WIDTH=30><BR></TD>");
							String alt="<BR>";
							if(up==Directions.NORTH) alt=getDoorLabelGif(Directions.UP,GR,httpReq);
							if(down==Directions.NORTH) alt=getDoorLabelGif(Directions.DOWN,GR,httpReq);
							buf.append("<TD WIDTH=30>"+alt+"</TD>");
							buf.append("<TD WIDTH=24><BR></TD>");
							}
							break;
						case 1: // west, east
							{
							buf.append("<TD WIDTH=24>"+getDoorLabelGif(Directions.WEST,GR,httpReq)+"</TD>");
							buf.append("<TD WIDTH=90 COLSPAN=3 ROWSPAN=3 BGCOLOR=#CCCCFF VALIGN=TOP>");
							String roomID=GR.roomID;
							if(roomID.startsWith(area.name()+"#"))
							    roomID=roomID.substring(roomID.indexOf("#"));
							buf.append("<a name=\""+URLEncoder.encode(GR.roomID)+"\" href=\"javascript:Clicked('rmmenu.cmvp','','"+URLEncoder.encode(GR.roomID)+"','');\"><FONT SIZE=-1><B>"+roomID+"</B></FONT></a><BR>");
							buf.append("<FONT SIZE=-2>("+CMClass.className(GR.room)+")<BR>");
							String displayText=GR.room.displayText();
							if(displayText.length()>20)	displayText=displayText.substring(0,20)+"...";
							buf.append(displayText+"</FONT></TD>");
							buf.append("<TD WIDTH=24>"+getDoorLabelGif(Directions.EAST,GR,httpReq)+"</TD>");
							}
							break;
						case 2: // nada
							buf.append("<TD WIDTH=24><BR></TD>");
							buf.append("<TD WIDTH=24><BR></TD>");
							break;
						case 3: // alt e,w
							{
							String alt="<BR>";
							if(up==Directions.WEST) alt=getDoorLabelGif(Directions.UP,GR,httpReq);
							if(down==Directions.WEST) alt=getDoorLabelGif(Directions.DOWN,GR,httpReq);
							buf.append("<TD WIDTH=24>"+alt+"</TD>");
							alt="<BR>";
							if(up==Directions.EAST) alt=getDoorLabelGif(Directions.UP,GR,httpReq);
							if(down==Directions.EAST) alt=getDoorLabelGif(Directions.DOWN,GR,httpReq);
							buf.append("<TD WIDTH=24>"+alt+"</TD>");
							}
							break;
						case 4: // south, down
							{
							buf.append("<TD WIDTH=24><BR></TD>");
							buf.append("<TD WIDTH=30>"+getDoorLabelGif(Directions.SOUTH,GR,httpReq)+"</TD>");
							buf.append("<TD WIDTH=30><BR></TD>");
							String alt="<BR>";
							if(up==Directions.SOUTH) alt=getDoorLabelGif(Directions.UP,GR,httpReq);
							if(down==Directions.SOUTH) alt=getDoorLabelGif(Directions.DOWN,GR,httpReq);
							buf.append("<TD WIDTH=30>"+alt+"</TD>");
							buf.append("<TD WIDTH=24><BR></TD>");
							}
							break;
						}
					}
				}
				buf.append("</TR>");
			}
		}
		buf.append("</TABLE>");
		return buf;
	}
									 
	private GrinderRoom getRoomInDir(GrinderRoom room, int d)
	{
	    switch(d)
	    {
	        case Directions.NORTH:
	            if(room.y>0)
	                return grid[room.x][room.y-1];
	            break;
	        case Directions.SOUTH:
	            if(room.y<Ybound)
	                return grid[room.x][room.y+1];
	            break;
	        case Directions.EAST:
	            if(room.x<Xbound)
	                return grid[room.x+1][room.y]; 
	            break;
	        case Directions.WEST:
	            if(room.x>0)
	                return grid[room.x-1][room.y]; 
	            break;
	    }
		return null;
	}
	
	private int findRelGridDir(GrinderRoom room, String roomID)
	{
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			GrinderRoom possRoom=getRoomInDir(room,d);
			if((possRoom!=null)&&(possRoom.roomID.equals(roomID)))
				return d;
		}
		return -1;
	}
	
    private String getDoorLabelGif(int d, GrinderRoom room, ExternalHTTPRequests httpReq)
	{
	    GrinderDir dir=(GrinderDir)room.doors[d];
	    String dirLetter=""+Directions.getDirectionName(d).toUpperCase().charAt(0);
		GrinderRoom roomPointer=null;
	    if((dir==null)||((dir!=null)&&(dir.room.length()==0)))
			return "<a href=\"javascript:Clicked('lnkxmenu.cmvp','"+Directions.getDirectionName(d)+"','"+URLEncoder.encode(room.roomID)+"','');\"><IMG BORDER=0 SRC=\"images/E"+dirLetter+".gif\"></a>";
	    else
	    if((d==Directions.UP)||(d==Directions.DOWN))
	    {
			int actualDir=findRelGridDir(room,dir.room);
			if(actualDir>=0) roomPointer=getRoomInDir(room,actualDir);
	    }
	    else
	        roomPointer=getRoomInDir(room,d);
		
	    if((dir.room.length()>0)&&((roomPointer==null)||((roomPointer!=null)&&(!roomPointer.roomID.equals(dir.room)))))
    	    dirLetter+="R";
		String theRest=".gif\" BORDER=0 ALT=\""+Directions.getDirectionName(d)+" to "+dir.room+"\"></a>";
    	Exit exit=dir.exit;
    	if(exit==null)
			return "<a href=\"javascript:Clicked('edxmenu.cmvp','"+Directions.getDirectionName(d)+"','"+URLEncoder.encode(room.roomID)+"','"+dir.room+"');\"><IMG SRC=\"images/U"+dirLetter+theRest;
    	else
    	if(exit.hasADoor())
			return "<a href=\"javascript:Clicked('edxmenu.cmvp','"+Directions.getDirectionName(d)+"','"+URLEncoder.encode(room.roomID)+"','"+dir.room+"');\"><IMG SRC=\"images/D"+dirLetter+theRest;
    	else
			return "<a href=\"javascript:Clicked('edxmenu.cmvp','"+Directions.getDirectionName(d)+"','"+URLEncoder.encode(room.roomID)+"','"+dir.room+"');\"><IMG SRC=\"images/O"+dirLetter+theRest;
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
