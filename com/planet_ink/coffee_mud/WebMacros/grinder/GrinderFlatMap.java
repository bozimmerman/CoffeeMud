package com.planet_ink.coffee_mud.web.macros.grinder;
import java.net.*;
import java.util.*;
import java.net.URLEncoder;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class GrinderFlatMap
{
    protected Vector areaMap=null;
	protected Hashtable hashRooms=null;
    private GrinderRoom[][] grid=null;
    protected int Xbound=0;
    protected int Ybound=0;
    protected int Ystart=0;
    protected int Xstart=0;
	protected Area area=null;
	protected boolean debug = false;

	public GrinderFlatMap()
	{
	}
	
	public GrinderFlatMap(Area A)
	{
		area=A;
		areaMap=new Vector();
		hashRooms=new Hashtable();
		for(Enumeration r=A.getProperMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(R.roomID().length()>0)
			{
				GrinderRoom GR=new GrinderRoom(R);
				areaMap.addElement(GR);
				hashRooms.put(GR.roomID,GR);
			}
		}
	}

    public void rebuildGrid()
    {
		if(areaMap==null) 
		    return;
	    // build grid!
	    int xoffset=0;
	    int yoffset=0;
	    for(int x=0;x<areaMap.size();x++)
		{
			GrinderRoom GR=(GrinderRoom)areaMap.elementAt(x);
	        if(GR.x<xoffset) xoffset=GR.x;
	        if(GR.y<yoffset) yoffset=GR.y;
		}

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
	    for(int y=0;y<areaMap.size();y++)
	    {
	        GrinderRoom room=(GrinderRoom)areaMap.elementAt(y);
	        grid[room.x][room.y]=room;
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

    protected GrinderRoom getProcessedRoomAt(Hashtable processed, int x, int y)
    {
        for(Enumeration e=processed.elements();e.hasMoreElements();)
        {
            GrinderRoom room=(GrinderRoom)e.nextElement();
            if((room.x==x)&&(room.y==y))
                return room;
        }
        return null;
    }

    public GrinderRoom crowdInY(int x)
    {
        for(int y=Ystart;y<=Ybound;y++)
            if(grid[x][y]!=null)  return grid[x][y];
        if(x==Xstart) 
            Xstart++; 
        else 
            Xbound--;
        return null;
    }
    public GrinderRoom crowdInX(int y)
    {
        for(int x=Xstart;x<=Xbound;x++)
            if(grid[x][y]!=null)
                return grid[x][y];
        if(y==Ystart) 
            Ystart++; 
        else 
            Ybound--;
        return null;
    }
    
    
    public HashSet getCrowdedBlock(GrinderRoom startR)
    {
        HashSet block=new HashSet();
        block.add(startR);
        boolean doneSomething=true;
        GrinderRoom R=null;
        GrinderRoom gridEdgeR=null;
        GrinderDir dir=null;
        while(doneSomething)
        {
            doneSomething=false;
            for(Iterator i=block.iterator();i.hasNext();)
            {
                R=(GrinderRoom)i.next();
                for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
                {
                    gridEdgeR=getRoomInDir(startR,d);
                    dir=R.doors[d];
                    if((gridEdgeR!=null)
                    &&(dir!=null)
                    &&(dir.room!=null)
                    &&(dir.room.length()>0)
                    &&(!block.contains(gridEdgeR)))
                    {
                		gridEdgeR=null;
                	    if((d==Directions.UP)||(d==Directions.DOWN))
                	    {
                			int actualDir=findRelGridDir(startR,dir.room);
                			if(actualDir>=0) gridEdgeR=getRoomInDir(startR,actualDir);
                	    }
                	    else
                	        gridEdgeR=getRoomInDir(startR,d);

                	    if((dir.room.length()>0)&&((gridEdgeR==null)||((gridEdgeR!=null)&&(!gridEdgeR.roomID.equals(dir.room)))))
                    	    continue;
                	    else
                	    {
                	        doneSomething=true;
                	        block.add(gridEdgeR);
                	    }
                    }
                }
            }
        }
        return block;
    }
    
    public GrinderRoom closestToCenter(HashSet block)
    {
        GrinderRoom R=null;
        int centerX=Xstart+((Xbound-Xstart)/2);
        int centerY=Ystart+((Ybound-Ystart)/2);
        double bestDistance=Double.MAX_VALUE;
        GrinderRoom winnerR=null;
        for(Iterator i=block.iterator();i.hasNext();)
        {
            R=(GrinderRoom)i.next();
            double diff=new Integer(((R.x>centerX)?R.x-centerX:centerX-R.x)
         		   +((R.y>centerY)?R.y-centerY:centerY-R.y)).doubleValue()/2.0;
            if(diff<bestDistance)
            {
                bestDistance=diff;
                winnerR=R;
            }
        }
        return winnerR;
    }
    
    public boolean moveCrowdToBestBlock(GrinderRoom R)
    {
        HashSet block=getCrowdedBlock(R);
        if((block==null)||(block.size()==0)) return false;
        GrinderRoom closestR=closestToCenter(block);
        int[] newBlock=findEmptyCrowdBlock(block,closestR);
        if(newBlock!=null)
        {
            // remove from old grid
            for(Iterator i=block.iterator();i.hasNext();)
            {
                R=(GrinderRoom)i.next();
                if(grid[R.x][R.y]!=R)
                    System.out.println("You found the problem!");
                grid[R.x][R.y]=null;
            }
            int xworkdiff=newBlock[0]-closestR.x;
            int yworkdiff=newBlock[1]-closestR.y;
            for(Iterator i=block.iterator();i.hasNext();)
            {
                R=(GrinderRoom)i.next();
                R.x+=xworkdiff;
                R.y+=yworkdiff;
                grid[R.x][R.y]=R;
            }
            return true;
        }
        return false;
    }
    
    public int[] findEmptyCrowdBlock(HashSet block, GrinderRoom bestR)
    {
        int[] found=null;
        if(bestR==null) return null;
        int centerX=Xstart+((Xbound-Xstart)/2);
        int centerY=Ystart+((Ybound-Ystart)/2);
        double bestdiff=new Integer(((bestR.x>centerX)?bestR.x-centerX:centerX-bestR.x)
     		   +((bestR.y>centerY)?bestR.y-centerY:centerY-bestR.y)).doubleValue()/2.0;
        double diff=0.0;
        GrinderRoom R=null;
        boolean good=false;
        int xworkdiff=0;
        int yworkdiff=0;
        int newx=0;
        int newy=0;
        for(int x=Xstart;x<=Xbound;x++)
            for(int y=Ystart;y<=Ybound;y++)
	        {
	            diff=new Integer(((x>centerX)?x-centerX:centerX-x)
          		   +((y>centerY)?y-centerY:centerY-y)).doubleValue()/2.0;
	            if(diff<bestdiff)
	            {
	                good=true;
	                xworkdiff=x-bestR.x;
	                yworkdiff=y-bestR.y;
	                for(Iterator i=block.iterator();i.hasNext();)
	                {
	                    R=(GrinderRoom)i.next();
	                    newx=R.x+xworkdiff;
	                    newy=R.y+yworkdiff;
	                    if((newx<=Xstart)
	                    ||(newx>=Xbound)
	                    ||(newy<=Ystart)
	                    ||(newy>=Ybound))
	                    { good=false; break;}
	                        
	                    if(((grid[newx][newy]!=null)&&(!block.contains(grid[newx][newy])))
	                    ||((grid[newx-1][newy]!=null)&&(!block.contains(grid[newx-1][newy])))
	                    ||((grid[newx+1][newy]!=null)&&(!block.contains(grid[newx+1][newy])))
	                    ||((grid[newx][newy-1]!=null)&&(!block.contains(grid[newx][newy-1])))
	                    ||((grid[newx][newy+1]!=null)&&(!block.contains(grid[newx][newy+1]))))
	                    { good=false; break;}
	                }
	                if(good)
	                {
	                    bestdiff=diff;
	                    if(found==null) found=new int[2];
	                    found[0]=x;
	                    found[1]=y;
	                }
	            }
	        }
        return found;
    }
    
    public void crowdMap()
    {
        boolean somethingDone=true;
        GrinderRoom R=null;
        while(somethingDone)
        {
            somethingDone=false;
            R=crowdInY(Xstart);
            if(R!=null)
            {
                if(moveCrowdToBestBlock(R))
                    somethingDone=true;
            }
            else
                somethingDone=true;
            R=crowdInY(Xbound);
            if(R!=null)
            {
                if(moveCrowdToBestBlock(R))
                    somethingDone=true;
            }
            else
                somethingDone=true;
            R=crowdInX(Ystart);
            if(R!=null)
            {
                if(moveCrowdToBestBlock(R))
                    somethingDone=true;
            }
            else
                somethingDone=true;
            R=crowdInX(Ybound);
            if(R!=null)
            {
                if(moveCrowdToBestBlock(R))
                    somethingDone=true;
            }
            else
                somethingDone=true;
        }
    }

    public GrinderRoom getRoom(String ID)
    {
		if((hashRooms!=null)&&(hashRooms.containsKey(ID)))
		   return (GrinderRoom)hashRooms.get(ID);

		if(areaMap!=null)
			for(int r=0;r<areaMap.size();r++)
			{
			    GrinderRoom room=(GrinderRoom)areaMap.elementAt(r);
			    if(room.roomID.equalsIgnoreCase(ID))
			        return room;
			}
        return null;
    }

    protected final static int CLUSTERSIZE=3;

    protected boolean isEmptyCluster(Hashtable processed, int x, int y)
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

    protected void findEmptyCluster(Hashtable processed, Vector XY)
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

 	public void getRadiantRooms(GrinderRoom room,
								Vector rooms,
								int maxDepth)
	{
		int depth=0;
		if(room==null) return;
		if(rooms.contains(room)) return;
		rooms.addElement(room);
		int min=0;
		int size=rooms.size();
		while(depth<maxDepth)
		{
			for(int r=min;r<size;r++)
			{
				GrinderRoom R1=(GrinderRoom)rooms.elementAt(r);
				if(R1!=null)
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					{
						GrinderDir R=R1.doors[d];
						GrinderRoom GR=((R!=null)?getRoom(R.room):null);
						if((GR!=null)&&(!rooms.contains(GR)))
							rooms.addElement(GR);
					}
			}
			min=size;
			size=rooms.size();
			depth++;
		}
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
                if(!processed.containsKey(room.roomID))
                {
                    placeRoom(room,0,0,processed,true,true,0);
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
		for(int y=Ystart;y<=Ybound;y++)
		{
			// up=nwes
			// down=sewn
			for(int l=0;l<5;l++)
			{
				buf.append("<TR HEIGHT=24>");
				for(int x=Xstart;x<=Xbound;x++)
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
							buf.append("<TD WIDTH=90 COLSPAN=3 ROWSPAN=3 VALIGN=TOP ");
							buf.append(roomColorStyle(GR));
							buf.append(">");
							String roomID=GR.roomID;
							if(roomID.startsWith(area.Name()+"#"))
							    roomID=roomID.substring(roomID.indexOf("#"));
							try
							{
								buf.append("<a name=\""+URLEncoder.encode(GR.roomID,"UTF-8")+"\" href=\"javascript:Clicked('rmmenu.cmvp','','"+GR.roomID+"','');\"><FONT SIZE=-1><B>"+roomID+"</B></FONT></a><BR>");
							}
							catch(java.io.UnsupportedEncodingException e)
							{
								Log.errOut("GrinderMap","Wrong Encoding");
							}
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

	
	protected String roomColorStyle(GrinderRoom GR)
	{
		switch (GR.room.domainType()) 
		{
		case Room.DOMAIN_INDOORS_AIR:
			return ("BGCOLOR=\"#FFFFFF\"");
		case Room.DOMAIN_INDOORS_MAGIC:
			return ("BGCOLOR=\"#996600\"");
		case Room.DOMAIN_INDOORS_METAL:
			return ("BGCOLOR=\"#996600\"");
		case Room.DOMAIN_INDOORS_CAVE:
			return ("BGCOLOR=\"#CC99FF\"");
		case Room.DOMAIN_INDOORS_STONE:
			return ("BGCOLOR=\"#CC00FF\"");
		case Room.DOMAIN_INDOORS_UNDERWATER:
			return ("BGCOLOR=\"#6666CC\"");
		case Room.DOMAIN_INDOORS_WATERSURFACE:
			return ("BGCOLOR=\"#3399CC\"");
		case Room.DOMAIN_INDOORS_WOOD:
			return ("BGCOLOR=\"#999900\"");
		case Room.DOMAIN_OUTDOORS_AIR:
			return ("BGCOLOR=\"#FFFFFF\"");
		case Room.DOMAIN_OUTDOORS_CITY:
			return ("BGCOLOR=\"#CCCCCC\"");
		case Room.DOMAIN_OUTDOORS_SPACEPORT:
			return ("BGCOLOR=\"#CCCCCC\"");
		case Room.DOMAIN_OUTDOORS_DESERT:
			return ("BGCOLOR=\"#FFFF66\"");
		case Room.DOMAIN_OUTDOORS_HILLS:
			return ("BGCOLOR=\"#99CC33\"");
		case Room.DOMAIN_OUTDOORS_JUNGLE:
			return ("BGCOLOR=\"#669966\"");
		case Room.DOMAIN_OUTDOORS_MOUNTAINS:
			return ("BGCOLOR=\"#996600\"");
		case Room.DOMAIN_OUTDOORS_PLAINS:
			return ("BGCOLOR=\"#00FF00\"");
		case Room.DOMAIN_OUTDOORS_ROCKS:
			return ("BGCOLOR=\"#996600\"");
		case Room.DOMAIN_OUTDOORS_SWAMP:
			return ("BGCOLOR=\"#006600\"");
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
			return ("BGCOLOR=\"#6666CC\"");
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			return ("BGCOLOR=\"#3399CC\"");
		case Room.DOMAIN_OUTDOORS_WOODS:
			return ("BGCOLOR=\"#009900\"");
		default:
			return ("BGCOLOR=\"#CCCCFF\"");
		}
	}
	
	protected GrinderRoom getRoomInDir(GrinderRoom room, int d)
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
	        case Directions.NORTHWEST:
	            if((room.y>0)&&(room.x>0))
	                return grid[room.x-1][room.y-1];
	            break;
	        case Directions.SOUTHWEST:
	            if((room.y<Ybound)&&(room.x>0))
	                return grid[room.x-1][room.y+1];
	            break;
	        case Directions.NORTHEAST:
	            if((room.y>0)&&(room.x<Xbound))
	                return grid[room.x+1][room.y-1];
	            break;
	        case Directions.SOUTHEAST:
	            if((room.y<Ybound)&&(room.x<Xbound))
	                return grid[room.x+1][room.y+1];
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

	protected int findRelGridDir(GrinderRoom room, String roomID)
	{
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			GrinderRoom possRoom=getRoomInDir(room,d);
			if((possRoom!=null)&&(possRoom.roomID.equals(roomID)))
				return d;
		}
		return -1;
	}

    protected String getDoorLabelGif(int d, GrinderRoom room, ExternalHTTPRequests httpReq)
	{
	    GrinderDir dir=room.doors[d];
	    String dirLetter=""+Directions.getDirectionName(d).toUpperCase().charAt(0);
		GrinderRoom roomPointer=null;
	    if((dir==null)||((dir!=null)&&(dir.room.length()==0)))
			return "<a href=\"javascript:Clicked('lnkxmenu.cmvp','"+Directions.getDirectionName(d)+"','"+room.roomID+"','');\"><IMG BORDER=0 SRC=\"images/E"+dirLetter+".gif\"></a>";
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
			return "<a href=\"javascript:Clicked('edxmenu.cmvp','"+Directions.getDirectionName(d)+"','"+room.roomID+"','"+dir.room+"');\"><IMG SRC=\"images/U"+dirLetter+theRest;
    	else
    	if(exit.hasADoor())
			return "<a href=\"javascript:Clicked('edxmenu.cmvp','"+Directions.getDirectionName(d)+"','"+room.roomID+"','"+dir.room+"');\"><IMG SRC=\"images/D"+dirLetter+theRest;
    	else
			return "<a href=\"javascript:Clicked('edxmenu.cmvp','"+Directions.getDirectionName(d)+"','"+room.roomID+"','"+dir.room+"');\"><IMG SRC=\"images/O"+dirLetter+theRest;
    }

	public void placeRoom(GrinderRoom room,
                                int favoredX,
                                int favoredY,
                                Hashtable processed,
                                boolean doNotDefer,
								boolean passTwo,
								int depth)
    {
        if(room==null) return;
        if(depth>500) return;
        GrinderRoom anythingAt=getProcessedRoomAt(processed,favoredX,favoredY);
        if(anythingAt!=null)
        {
            // maybe someone else will take care of it?
            if(!doNotDefer)
                for(int r=0;r<areaMap.size();r++)
                {
                    GrinderRoom roomToBlame=(GrinderRoom)areaMap.elementAt(r);
                    if(roomToBlame!=room)
                        for(int rd=0;rd<Directions.NUM_DIRECTIONS;rd++)
                        {
                            GrinderDir RD=roomToBlame.doors[rd];
                            if((RD!=null)
							&&(RD.room!=null)
							&&(!RD.positionedAlready)
							&&(RD.room.equals(room.roomID)))
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
                roomID=room.doors[d].room;

            if((roomID!=null)
			&&(roomID.length()>0)
			&&(processed.get(roomID)==null)
            &&(passTwo||((d!=Directions.UP)&&(d!=Directions.DOWN))))
            {
				GrinderRoom nextRoom=getRoom(roomID);
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
                    placeRoom(nextRoom,newFavoredX,newFavoredY,processed,false,passTwo,depth+1);
                }
            }
        }
    }
	
	public StringBuffer getHTMLMap(ExternalHTTPRequests httpReq) 
	{
		return getHTMLMap(httpReq, 4);
	}

	// this is much like getHTMLTable, but tiny rooms for world map viewing. No exits or ID's for now.
	public StringBuffer getHTMLMap(ExternalHTTPRequests httpReq, int roomSize) 
	{
		StringBuffer buf = new StringBuffer("");
		buf.append("<TABLE WIDTH=" + ( (Xbound + 1) * roomSize) +
		           " BORDER=0 CELLSPACING=0 CELLPADDING=0>");
		for (int y = 0; y <= Ybound; y++) 
		{
			buf.append("<TR HEIGHT=" + roomSize + ">");
			for (int x = 0; x <= Xbound; x++) 
			{
				GrinderRoom GR = grid[x][y];
				if (GR == null) 
					buf.append("<TD WIDTH=" + roomSize + " HEIGHT=" + roomSize +
					           "><font size=1>&nbsp;</font></TD>");
				  else 
				  {
					buf.append("<TD WIDTH=" + roomSize + " HEIGHT=" + roomSize + " ");
					buf.append(roomColorStyle(GR));
					buf.append("><font size=1>&nbsp;</font></TD>");
				}
			}
			buf.append("</TR>");
		}
		buf.append("</TABLE>");
		return buf;
	}
}
