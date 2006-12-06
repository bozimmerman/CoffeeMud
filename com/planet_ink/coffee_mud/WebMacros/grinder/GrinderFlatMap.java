package com.planet_ink.coffee_mud.WebMacros.grinder;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.net.*;
import java.util.*;
import java.net.URLEncoder;


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
	protected int[] boundsXYXY=null;

	public GrinderFlatMap()
	{
	}

	public GrinderFlatMap(Area A, int[] xyxy)
	{
		area=A;
		areaMap=new Vector();
		hashRooms=new Hashtable();
		Enumeration r=A.getProperMap();
		Room R=null;
		boundsXYXY=xyxy;
		
		boolean thinArea=CMath.bset(A.flags(),Area.FLAG_THIN);
		if(thinArea)
		{
			RoomnumberSet currentSet=A.getCachedRoomnumbers();
			String roomID=null;
			//RoomnumberSet loadRooms=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
			for(Enumeration e=A.getProperRoomnumbers().getRoomIDs();e.hasMoreElements();)
			{
				// this makes sure that, even though this is
				// an unloaded room, it is ALSO actually needed
				// for mapping.
				roomID=(String)e.nextElement();
				if((currentSet==null)||(!currentSet.contains(roomID)))
				{
					if(area instanceof GridZones)
					{
						if(xyxy!=null)
						{
							int[] thisXY=((GridZones)area).getRoomXY(roomID);
							if(thisXY==null) continue;
							if((thisXY[0]<xyxy[0])
							||(thisXY[1]<xyxy[1])
							||(thisXY[0]>xyxy[2])
							||(thisXY[1]>xyxy[3]))
								continue;
						}
						GrinderRoom GR=new GrinderRoom(roomID);
						areaMap.addElement(GR);
						hashRooms.put(GR.roomID,GR);
					}
					else
						CMLib.map().getRoom(roomID);
				}
			}
			r=A.getProperMap();
		}
		
		// appropriate thin rooms are already added at this point.
		// if this is a fat gridzone, or there are proper rooms
		// left, now is the time to siphon out the ones we need.
		if((area instanceof GridZones)&&(xyxy!=null))
		{
			Vector finalSet=new Vector();
			for(;r.hasMoreElements();)
			{
				R=(Room)r.nextElement();
				if(R.roomID().length()>0)
				{
					int[] thisXY=((GridZones)area).getRoomXY(R.roomID());
					if(thisXY==null) continue;
					if((thisXY[0]<xyxy[0])
					||(thisXY[1]<xyxy[1])
					||(thisXY[0]>=xyxy[2])
					||(thisXY[1]>=xyxy[3]))
					{
						// force the old ones to expire asap
						if((thinArea)&&(R.expirationDate()>0))
							R.setExpirationDate(System.currentTimeMillis());
						continue;
					}
					if((thinArea)&&(R.expirationDate()>0))
						R.setExpirationDate(System.currentTimeMillis()+(10*60*1000));
					finalSet.add(R);
				}
			}
			r=finalSet.elements();
		}
        if((area instanceof GridZones)&&(boundsXYXY==null))
        {
        	boundsXYXY=new int[4];
        	boundsXYXY[0]=0;
        	boundsXYXY[1]=0;
        	boundsXYXY[2]=((GridZones)area).xGridSize();
        	boundsXYXY[3]=((GridZones)area).yGridSize();
        }
		
		// no matter what, r is the way to go.
		// for thin rooms, the thin-unloaded are already in areaMap and hashRooms
		// for thin grid zones, the *correct* thin-unloaded rooms are also ready
		// for grid zones, r has had the inappropriate rooms siphoned out
		// all thats left to hash are the APPROPRIATE fat rooms!
		for(;r.hasMoreElements();)
		{
			R=(Room)r.nextElement();
			if(R.roomID().length()>0)
			{
				GrinderRoom GR=new GrinderRoom(R);
				areaMap.addElement(GR);
				hashRooms.put(GR.roomID,GR);
			}
		}
		//for(int a=0;a<areaMap.size();a++)
		//	((GrinderRoom)areaMap.elementAt(a)).score=scoreRoom((GrinderRoom)areaMap.elementAt(a), "X!X!X", new int[2], new HashSet(), new HashSet());
	}

    public void rebuildGrid()
    {
		if(areaMap==null) return;
		
        // very happy special case
        if(area instanceof GridZones)
        {
            Xbound=(boundsXYXY[2]-boundsXYXY[0]);
            Ybound=(boundsXYXY[3]-boundsXYXY[1]);
            for(int i=areaMap.size()-1;i>=0;i--)
            {
                GrinderRoom room=(GrinderRoom)areaMap.elementAt(i);
                int[] myxy=((GridZones)area).getRoomXY(room.roomID);
				if(myxy==null) continue;
                if((myxy[0]<boundsXYXY[0])||(myxy[1]<boundsXYXY[1])||(myxy[0]>=boundsXYXY[2])||(myxy[1]>=boundsXYXY[3]))
                	areaMap.remove(room);
                else
                {
	                room.x=myxy[0]-boundsXYXY[0];
	                room.y=myxy[1]-boundsXYXY[1];
                }
            }
        }
        else
        {
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
        placeRooms();
        hashRooms=null;
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
            	        doneSomething=true;
            	        block.add(gridEdgeR);
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
    	if(area instanceof GridZones) return;
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
    
    public void optiPositionRooms()
    {
        for(int a=0;a<areaMap.size();a++)
            ((GrinderRoom)areaMap.elementAt(a)).optiXY=null;
        GrinderRoom R=null;
        GrinderRoom R2=null;
        for(int a=0;a<areaMap.size();a++)
        {
            R=(GrinderRoom)areaMap.elementAt(a);
            if(R.optiXY==null)
            {
                for(int a2=a+1;a2<areaMap.size();a2++)
                {
                    R2=((GrinderRoom)areaMap.elementAt(a));
                    if(R2.optiXY!=null)
                    for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
                        if((R2.doors[d]!=null)
                        &&(R2.doors[d].room!=null)
                        &&(R2.doors[d].room.equals(R.roomID)))
                            R.optiXY=newXY(R2.optiXY,d);
                }
                if(R.optiXY==null)
                    optiPositionRoom((GrinderRoom)areaMap.elementAt(a),new int[2],hashRooms,false);
            }
        }
    }
    public void optiPositionRoom(GrinderRoom room, int[] xy, Hashtable H, boolean ignoreUPDOWN)
    {
        GrinderRoom R2=null;
        for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
            if((!ignoreUPDOWN)||((d!=Directions.UP)&&(d!=Directions.DOWN)))
            {
                if((room.doors[d]!=null)
                &&(room.doors[d].room!=null)
                &&(room.doors[d].room.length()>0))
                {
                    R2=(GrinderRoom)H.get(room.doors[d].room);
                    if((R2!=null)
                    &&(R2.optiXY==null))
                    {
                        R2.optiXY=newXY(xy,d);
                        optiPositionRoom(room,R2.optiXY,H,ignoreUPDOWN);
                    }
                }
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

	public Vector scoreRoom(Hashtable H, GrinderRoom room)
	{
		HashSet coordsDone=new HashSet();
		coordsDone.add(0+"/"+0);
		HashSet roomsDone=new HashSet();
		Hashtable xys=new Hashtable();
		roomsDone.add(room.roomID);
		Vector V=new Vector();
		V.addElement(room);
		int[] xy=new int[2];
		xys.put(room.roomID,xy);
		int startHere=0;
		while(startHere!=V.size())
		{
			int s=startHere;
			int size=V.size();
			startHere=size;
			for(;s<size;s++)
			{
				GrinderRoom R=(GrinderRoom)V.elementAt(s);
				xy=(int[])xys.get(R.roomID);
		 		for(int d=0;d<4;d++)
		 			if((R.doors[d]!=null)
		 			&&(R.doors[d].room!=null)
				 	&&(R.doors[d].room.length()>0)
		 			&&(!roomsDone.contains(R.doors[d].room)))
		 			{
		 				GrinderRoom R2=(GrinderRoom)H.get(R.doors[d].room);
		 				if(R2==null) continue;
		 				int[] xy2=newXY(xy,d);
		 				xys.put(R2.roomID,xy2);
		 				if(!coordsDone.contains(xy2[0]+"/"+xy2[1]))
		 				{
			 				roomsDone.add(R2.roomID);
			 				coordsDone.add(xy2[0]+"/"+xy2[1]);
			 				V.addElement(R2);
		 				}
		 			}
			}
		}
		return V;
	}
	
	public void specialSortAreaMap()
	{
        Vector sets=new Vector();
        for(int i=0;i<areaMap.size();i++)
        {
            GrinderRoom room=(GrinderRoom)areaMap.elementAt(i);
            Vector V=scoreRoom(hashRooms, room);
            if(sets.size()==0) 
            	sets.addElement(V);
            else
            for(int s=0;s<sets.size();s++)
            	if(((Vector)sets.elementAt(s)).size()<=V.size())
            	{ sets.insertElementAt(V,s); break;}
        }
        Vector newAreaMap=new Vector();
        HashSet newAreaMapDone=new HashSet();
        for(int s=0;s<sets.size();s++)
        {
        	Vector V=(Vector)sets.elementAt(s);
        	for(int v=0;v<V.size();v++)
        		if(!newAreaMapDone.contains(V.elementAt(v)))
        		{
        			newAreaMap.add(V.elementAt(v));
        			newAreaMapDone.add(V.elementAt(v));
        		}
        }
        for(int i=0;i<areaMap.size();i++)
        	if(!newAreaMapDone.contains(areaMap.elementAt(i)))
        		newAreaMap.add(areaMap.elementAt(i));
        areaMap=newAreaMap;
	}
	
	public void placeRooms()
    {
        if(areaMap==null) return;
        if(areaMap.size()==0) return;
        
        // very happy special case
        if(area instanceof GridZones) return;

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
        
        specialSortAreaMap();
        
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
		buf.append("<TABLE WIDTH="+((Xbound+1)*130)+" BORDER=0 CELLSPACING=0 CELLPADDING=0>");
		for(int l=0;l<5;l++)
		{
			buf.append("<TR HEIGHT=1>");
			for(int x=Xstart;x<=Xbound;x++)
				buf.append("<TD WIDTH=20><BR></TD><TD WIDTH=30><BR></TD><TD WIDTH=30><BR></TD><TD WIDTH=30><BR></TD><TD WIDTH=20><BR></TD>");
			buf.append("</TR>");
		}
		for(int y=Ystart;y<=Ybound;y++)
		{
			// up=nwes
			// down=sewn
			for(int l=0;l<5;l++)
			{
				buf.append("<TR HEIGHT=20>");
				for(int x=Xstart;x<=Xbound;x++)
				{
					GrinderRoom GR=grid[x][y];
					if(GR==null)
						buf.append("<TD COLSPAN=5"+((boundsXYXY!=null)?" ID=X"+(x+boundsXYXY[0])+"_"+(y+boundsXYXY[1]):"")+"><BR></TD>");
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
							buf.append("<TD>"+getDoorLabelGif(Directions.NORTHWEST,GR,httpReq)+"</TD>");
							buf.append("<TD><BR></TD>");
							buf.append("<TD>"+getDoorLabelGif(Directions.NORTH,GR,httpReq)+"</TD>");
							String alt="<BR>";
							if(up==Directions.NORTH) alt=getDoorLabelGif(Directions.UP,GR,httpReq);
							if(down==Directions.NORTH) alt=getDoorLabelGif(Directions.DOWN,GR,httpReq);
							buf.append("<TD>"+alt+"</TD>");
							buf.append("<TD>"+getDoorLabelGif(Directions.NORTHEAST,GR,httpReq)+"</TD>");
							}
							break;
						case 1: // west, east
							{
							buf.append("<TD><BR></TD>");
							buf.append("<TD COLSPAN=3 ROWSPAN=3 VALIGN=TOP ");
							buf.append(roomColorStyle(GR));
							buf.append(">");
							String roomID=GR.roomID;
							if(roomID.startsWith(area.Name()+"#"))
							    roomID=roomID.substring(roomID.indexOf("#"));
							try
							{
								buf.append("<a name=\""+URLEncoder.encode(GR.roomID,"UTF-8")+"\" href=\"javascript:RC('"+GR.roomID+"');\"><FONT SIZE=-1><B>"+roomID+"</B></FONT></a><BR>");
							}
							catch(java.io.UnsupportedEncodingException e)
							{
								Log.errOut("GrinderMap","Wrong Encoding");
							}
							buf.append("<FONT SIZE=-2>("+CMClass.classID(GR.room())+")<BR>");
							String displayText=GR.room().displayText();
							if(displayText.length()>20)	displayText=displayText.substring(0,20)+"...";
							buf.append(displayText+"</FONT></TD>");
							buf.append("<TD><BR></TD>");
							}
							break;
						case 2: // nada
							buf.append("<TD>"+getDoorLabelGif(Directions.WEST,GR,httpReq)+"</TD>");
							buf.append("<TD>"+getDoorLabelGif(Directions.EAST,GR,httpReq)+"</TD>");
							break;
						case 3: // alt e,w
							{
							String alt="<BR>";
							if(up==Directions.WEST) alt=getDoorLabelGif(Directions.UP,GR,httpReq);
							if(down==Directions.WEST) alt=getDoorLabelGif(Directions.DOWN,GR,httpReq);
							buf.append("<TD>"+alt+"</TD>");
							alt="<BR>";
							if(up==Directions.EAST) alt=getDoorLabelGif(Directions.UP,GR,httpReq);
							if(down==Directions.EAST) alt=getDoorLabelGif(Directions.DOWN,GR,httpReq);
							buf.append("<TD>"+alt+"</TD>");
							}
							break;
						case 4: // south, down
							{
							buf.append("<TD>"+getDoorLabelGif(Directions.SOUTHWEST,GR,httpReq)+"</TD>");
							buf.append("<TD><BR></TD>");
							buf.append("<TD>"+getDoorLabelGif(Directions.SOUTH,GR,httpReq)+"</TD>");
							String alt="<BR>";
							if(up==Directions.SOUTH) alt=getDoorLabelGif(Directions.UP,GR,httpReq);
							if(down==Directions.SOUTH) alt=getDoorLabelGif(Directions.DOWN,GR,httpReq);
							buf.append("<TD>"+alt+"</TD>");
							buf.append("<TD>"+getDoorLabelGif(Directions.SOUTHEAST,GR,httpReq)+"</TD>");
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
		switch (GR.room().domainType())
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
	    if((room==null)
	    ||(room.doors==null)
        ||(d>=room.doors.length)) return "";
	    GrinderDir dir=room.doors[d];
	    String dirLetter=""+Directions.getDirectionChar(d);
		GrinderRoom roomPointer=null;
	    if((dir==null)||((dir!=null)&&(dir.room.length()==0)))
			return "<a href=\"javascript:EC('"+Directions.getDirectionName(d)+"','"+room.roomID+"');\"><IMG BORDER=0 SRC=\"images/E"+dirLetter+".gif\"></a>";
	    else
	    if((d==Directions.UP)||(d==Directions.DOWN))
	    {
			int actualDir=findRelGridDir(room,dir.room);
			if(actualDir>=0) roomPointer=getRoomInDir(room,actualDir);
	    }
	    else
	        roomPointer=getRoomInDir(room,d);

	    String dirName=Directions.getDirectionName(d);
	    if((dir.room.length()>0)&&((roomPointer==null)||((roomPointer!=null)&&(!roomPointer.roomID.equals(dir.room)))))
    	    dirLetter+="R";
		String theRest=".gif\" BORDER=0 ALT=\""+dirName+" to "+dir.room+"\"></a>";
    	Exit exit=dir.exit;
    	if(exit==null)
			return "<a href=\"javascript:CEX('"+dirName+"','"+room.roomID+"','"+dir.room+"');\"><IMG SRC=\"images/U"+dirLetter+theRest;
    	else
    	if(exit.hasADoor())
			return "<a href=\"javascript:CEX('"+dirName+"','"+room.roomID+"','"+dir.room+"');\"><IMG SRC=\"images/D"+dirLetter+theRest;
    	else
			return "<a href=\"javascript:CEX('"+dirName+"','"+room.roomID+"','"+dir.room+"');\"><IMG SRC=\"images/O"+dirLetter+theRest;
    }
    
    
    public int[] newXY(int[] xy, int dir)
    {
    	xy=(int[])xy.clone();
        switch(dir)
        {
            case Directions.NORTH:
                xy[1]--; break;
            case Directions.SOUTH:
            	xy[1]++; break;
            case Directions.EAST:
                xy[0]++; break;
            case Directions.WEST:
            	xy[0]--; break;
			case Directions.NORTHEAST:
				xy[1]--; xy[0]++; break;
			case Directions.NORTHWEST:
				xy[1]--; xy[0]--;break;
			case Directions.SOUTHEAST:
				xy[1]++; xy[0]++; break;
			case Directions.SOUTHWEST:
				xy[1]++; xy[0]--; break;
            case Directions.UP:
            	xy[1]--;
                break;
            case Directions.DOWN:
            	xy[1]++;
                break;
        }
        return xy;
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
						case Directions.NORTHEAST:
							newFavoredY--; newFavoredX++; break;
						case Directions.NORTHWEST:
							newFavoredY--; newFavoredX--;break;
						case Directions.SOUTHEAST:
							newFavoredY++; newFavoredX++; break;
						case Directions.SOUTHWEST:
							newFavoredY++; newFavoredX--; break;
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
		buf.append("<TR HEIGHT=" + roomSize + ">");
		for (int x = 0; x <= Xbound; x++)
			buf.append("<TD WIDTH=" + roomSize + " HEIGHT=" + roomSize +"></TD>");
		buf.append("</TR>");
		for (int y = 0; y <= Ybound; y++)
		{
			buf.append("<TR HEIGHT=" + roomSize + ">");
			for (int x = 0; x <= Xbound; x++)
			{
				GrinderRoom GR = grid[x][y];
				String tdins=(boundsXYXY!=null)?" ID=X"+(x+boundsXYXY[0])+"_"+(y+boundsXYXY[1]):"";
				if (GR == null)
					buf.append("<TD"+tdins+"></TD>");
				  else
				  {
					buf.append("<TD"+tdins+" ");
					if(!GR.isRoomGood())
						buf.append("BGCOLOR=BLACK");
					else
						buf.append(roomColorStyle(GR));
					buf.append("></TD>");
				}
			}
			buf.append("</TR>");
		}
		buf.append("</TABLE>");
		return buf;
	}
}
