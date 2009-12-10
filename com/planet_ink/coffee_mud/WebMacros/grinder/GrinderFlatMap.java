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

	public double getDistanceFrom(int[] xy1,int[] xy2)
	{
		return Math.sqrt( ( ((double)(xy1[0]-xy2[0])) * ((double)(xy1[0]-xy2[0])) )
						+ ( ((double)(xy1[1]-xy2[1])) * ((double)(xy1[1]-xy2[1])) ) );
	}

    public void rebuildGrid()
    {
		if((areaMap==null)||(hashRooms==null))
			return;

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
                	room.xy=new int[2];
	                room.xy[0]=myxy[0]-boundsXYXY[0];
	                room.xy[1]=myxy[1]-boundsXYXY[1];
                }
            }
        }
        else
            placeRooms();

	    grid=new GrinderRoom[Xbound+1][Ybound+1];
	    for(int y=0;y<areaMap.size();y++)
	    {
	        GrinderRoom room=(GrinderRoom)areaMap.elementAt(y);
	        grid[room.xy[0]][room.xy[1]]=room;
	    }
    }


    public void placeRooms()
    {
        if((areaMap==null)||(hashRooms==null)||(area instanceof GridZones))
            return;

        Vector sets=new Vector();
        Vector bestSet=null;
        HashSet roomsDone=new HashSet();
        boolean didSomething=true;
        // first, cluster the rooms WITHOUT positioning them
        Vector finalCluster=new Vector();
        while((roomsDone.size()<areaMap.size())&&(didSomething))
        {
            didSomething=false;
            for(int i=0;i<areaMap.size();i++)
            {
                GrinderRoom room=(GrinderRoom)areaMap.elementAt(i);
                if(roomsDone.contains(room.roomID)) continue;
                Vector V=scoreRoom(hashRooms, room, roomsDone,false);
                if(bestSet==null)
                    bestSet=V;
                else
                if(bestSet.size()<V.size())
                    bestSet=V;
            }
            if(bestSet!=null)
            {
                if(bestSet.size()==1)
                    finalCluster.addElement(bestSet.firstElement());
                else
                {
                    GrinderRoom winnerR=(GrinderRoom)bestSet.firstElement();
                    scoreRoom(hashRooms, winnerR, roomsDone,true);
                    sets.addElement(bestSet);
                }
                for(int v=0;v<bestSet.size();v++)
                    roomsDone.add(((GrinderRoom)bestSet.elementAt(v)).roomID);
                bestSet=null;
                didSomething=true;
            }
        }
        // find leftover rooms and make them their own cluster
        for(int a=0;a<areaMap.size();a++)
            if(!roomsDone.contains(((GrinderRoom)areaMap.elementAt(a)).roomID))
                finalCluster.add((GrinderRoom)areaMap.elementAt(a));
        if(finalCluster.size()>0)
        {
            boolean[][] miniGrid=new boolean[finalCluster.size()+1][finalCluster.size()+1];
            int[] midXY=new int[2];
            midXY[0]=(int)Math.round(Math.floor(((double)finalCluster.size()+1.0)/2.0));
            midXY[1]=(int)Math.round(Math.floor(((double)finalCluster.size()+1.0)/2.0));
            ((GrinderRoom)finalCluster.firstElement()).xy=(int[])midXY.clone();
            miniGrid[midXY[0]][midXY[1]]=true;
            for(int f=1;f<finalCluster.size();f++)
            {
                int[] bestCoords=new int[]{miniGrid.length-1,miniGrid.length-1};
                for(int x=0;x<miniGrid.length;x++)
                    for(int y=0;y<miniGrid[x].length;y++)
                        if((!miniGrid[x][y])
                        &&(getDistanceFrom(bestCoords,midXY)>getDistanceFrom(new int[]{x,y},midXY)))
                            bestCoords=new int[]{x,y};
                miniGrid[bestCoords[0]][bestCoords[1]]=true;
                ((GrinderRoom)finalCluster.elementAt(f)).xy=(int[])bestCoords.clone();
            }
            for(int f=0;f<finalCluster.size();f++)
            {
                ((GrinderRoom)finalCluster.elementAt(f)).xy[0]*=2;
                ((GrinderRoom)finalCluster.elementAt(f)).xy[1]*=2;
            }
            sets.addElement(finalCluster);
        }
        clusterSet(sets);
    }

    public void placeRoomsII()
    {
        if((areaMap==null)||(hashRooms==null)||(area instanceof GridZones))
            return;

        Vector sets=new Vector();
        HashSet roomsDone=new HashSet();
        boolean didSomething=true;
        // first, cluster the rooms WITHOUT positioning them
        Vector finalCluster=new Vector();
        while((roomsDone.size()<areaMap.size())&&(didSomething))
        {
            didSomething=false;
            for(int i=0;i<areaMap.size();i++)
            {
                GrinderRoom room=(GrinderRoom)areaMap.elementAt(i);
                if(roomsDone.contains(room.roomID)) continue;
                Vector V=scoreRoomII(hashRooms, room, roomsDone);
                if((V!=null)&&(V.size()>0))
                {
                    if(V.size()==1)
                        finalCluster.addElement(V.firstElement());
                    else
                        sets.addElement(V);
                    didSomething=true;
                }
            }
        }
        // find leftover rooms and make them their own cluster
        for(int a=0;a<areaMap.size();a++)
            if(!roomsDone.contains(((GrinderRoom)areaMap.elementAt(a)).roomID))
                finalCluster.add((GrinderRoom)areaMap.elementAt(a));
        if(finalCluster.size()>0)
        {
            boolean[][] miniGrid=new boolean[finalCluster.size()+1][finalCluster.size()+1];
            int[] midXY=new int[2];
            midXY[0]=(int)Math.round(Math.floor(((double)finalCluster.size()+1.0)/2.0));
            midXY[1]=(int)Math.round(Math.floor(((double)finalCluster.size()+1.0)/2.0));
            ((GrinderRoom)finalCluster.firstElement()).xy=(int[])midXY.clone();
            miniGrid[midXY[0]][midXY[1]]=true;
            for(int f=1;f<finalCluster.size();f++)
            {
                int[] bestCoords=new int[]{miniGrid.length-1,miniGrid.length-1};
                for(int x=0;x<miniGrid.length;x++)
                    for(int y=0;y<miniGrid[x].length;y++)
                        if((!miniGrid[x][y])
                        &&(getDistanceFrom(bestCoords,midXY)>getDistanceFrom(new int[]{x,y},midXY)))
                            bestCoords=new int[]{x,y};
                miniGrid[bestCoords[0]][bestCoords[1]]=true;
                ((GrinderRoom)finalCluster.elementAt(f)).xy=(int[])bestCoords.clone();
            }
            for(int f=0;f<finalCluster.size();f++)
            {
                ((GrinderRoom)finalCluster.elementAt(f)).xy[0]*=2;
                ((GrinderRoom)finalCluster.elementAt(f)).xy[1]*=2;
            }
            sets.addElement(finalCluster);
        }
        clusterSet(sets);
    }

    public void clusterSet(Vector sets)
    {
        // figure out width height, and xy bounds
        // store them in a vector parallel to each
        Vector sizeInfo=new Vector(sets.size());
        GrinderRoom R=null;
        for(int s=0;s<sets.size();s++)
        {
            Vector set=(Vector)sets.elementAt(s);
            R=((GrinderRoom)set.elementAt(0));
            int[] minXY=new int[]{R.xy[0],R.xy[1]};
            int[] maxXY=new int[]{R.xy[0],R.xy[1]};
            for(int r=1;r<set.size();r++)
            {
                R=((GrinderRoom)set.elementAt(r));
                if(R.xy[0]<minXY[0]) minXY[0]=R.xy[0];
                if(R.xy[1]<minXY[1]) minXY[1]=R.xy[1];

                if(R.xy[0]>maxXY[0]) maxXY[0]=R.xy[0];
                if(R.xy[1]>maxXY[1]) maxXY[1]=R.xy[1];
            }
            int[] widthHeightXY=new int[2];
            widthHeightXY[0]=maxXY[0]-minXY[0];
            widthHeightXY[1]=maxXY[1]-minXY[1];
            for(int r=0;r<set.size();r++)
            {
                R=((GrinderRoom)set.elementAt(r));
                R.xy[0]-=minXY[0];
                R.xy[1]-=minXY[1];
            }
            sizeInfo.add(widthHeightXY);
        }

        // now cluster them into a top-level grid.. we'll trim the grid later.
        // yes, i know there must be a more efficient way...
        Vector[][] grid=new Vector[sets.size()+1][sets.size()+1];
        int[] midXY=new int[2];
        midXY[0]=(int)Math.round(Math.floor(((double)sets.size()+1.0)/2.0));
        midXY[1]=(int)Math.round(Math.floor(((double)sets.size()+1.0)/2.0));
        grid[midXY[0]][midXY[1]]=(Vector)sets.firstElement();
        int mostLeft=midXY[0];
        int mostTop=midXY[1];
        for(int a=1;a<sets.size();a++)
        {
            int[] bestCoords=new int[]{grid.length-1,grid.length-1};
            for(int x=0;x<grid.length;x++)
                for(int y=0;y<grid[x].length;y++)
                    if((grid[x][y]==null)
                    &&(getDistanceFrom(bestCoords,midXY)>getDistanceFrom(new int[]{x,y},midXY)))
                        bestCoords=new int[]{x,y};
            if(grid[bestCoords[0]][bestCoords[1]]!=null)
            {
                Log.errOut("GFlagMap","Lost a chunk of rooms to bad spiral positioning!!!");
                while(grid[bestCoords[0]][bestCoords[1]]!=null)
                {
                    bestCoords[0]=CMLib.dice().roll(1,grid.length-1,-1);
                    bestCoords[1]=CMLib.dice().roll(1,grid.length-1,-1);
                }
            }

            grid[bestCoords[0]][bestCoords[1]]=(Vector)sets.elementAt(a);
            if(bestCoords[0]<mostLeft)
                mostLeft=bestCoords[0];
            if(bestCoords[1]<mostTop)
                mostTop=bestCoords[1];
        }
        // JUST DO XS FIRST!!
        Vector set=null;
        int rightMostX=0;
        int nextRightMostX=0;
        for(int x=mostLeft;x<grid.length;x++)
        {
            for(int y=0;y<grid[x].length;y++)
            {
                set=grid[x][y];
                if(set!=null)
                    for(int r=0;r<set.size();r++)
                    {
                        R=((GrinderRoom)set.elementAt(r));
                        R.xy[0]+=rightMostX;
                        if(R.xy[0]>nextRightMostX)
                            nextRightMostX=R.xy[0];
                    }
            }
            rightMostX=nextRightMostX+2;
        }
        // now do YS
        int bottomMostY=0;
        int nextBottomMostY=0;
        for(int y=mostTop;y<grid[0].length;y++)
        {
            for(int x=0;x<grid.length;x++)
            {
                set=grid[x][y];
                if(set!=null)
                    for(int r=0;r<set.size();r++)
                    {
                        R=((GrinderRoom)set.elementAt(r));
                        R.xy[1]+=bottomMostY;
                        if(R.xy[1]>nextBottomMostY)
                            nextBottomMostY=R.xy[1];
                    }
            }
            bottomMostY=nextBottomMostY+2;
        }
        Xbound=nextRightMostX;
        Ybound=nextBottomMostY;
    }

    public void rePlaceRooms()
    {
        if(areaMap==null)
            return;
        grid=null;
        rebuildGrid();
        hashRooms=null;
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

    public boolean anythingThatDirection(GrinderRoom room, int direction)
    {
        GrinderDir D=room.doors[direction];
        if((D==null)||(D.room.length()==0))
            return false;
        return true;
    }

    public Vector scoreRoomII(Hashtable H, GrinderRoom room, HashSet roomsDone)
    {
        HashSet coordsDone=new HashSet();
        coordsDone.add(0+"/"+0);
        roomsDone.add(room.roomID);

        Vector V=new Vector();
        V.addElement(room);
        int startHere=0;
        room.xy=new int[2];
        GrinderRoom R2=null;
        GrinderRoom R3=null;
        while(startHere!=V.size())
        {
            int s=startHere;
            int size=V.size();
            startHere=size;
            for(;s<size;s++)
            {
                GrinderRoom R=(GrinderRoom)V.elementAt(s);
                for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
                    if((R.doors[d]!=null)
                    &&(R.doors[d].room!=null)
                    &&(R.doors[d].room.length()>0)
                    &&(!roomsDone.contains(R.doors[d].room))
                    &&(!roomsDone.contains(R.doors[d].room)))
                    {
                        R2=(GrinderRoom)H.get(R.doors[d].room);
                        if(R2==null) continue;
                        R2.xy=newXY(R.xy,d);
                        if(coordsDone.contains(R2.xy[0]+"/"+R2.xy[1]))
                        {
                            boolean adjust=false;
                            for(int v=0;v<V.size();v++)
                            {
                                adjust=false;
                                R3=(GrinderRoom)V.elementAt(v);
                                switch(d)
                                {
                                case Directions.NORTH: adjust=(R3.xy[1]<=R2.xy[1]); break;
                                case Directions.SOUTH: adjust=(R3.xy[1]>=R2.xy[1]); break;
                                case Directions.EAST: adjust=(R3.xy[0]>=R2.xy[0]); break;
                                case Directions.WEST: adjust=(R3.xy[0]<=R2.xy[0]); break;
                                case Directions.NORTHEAST: adjust=(R3.xy[1]<=R2.xy[1])||(R3.xy[0]>=R2.xy[0]); break;
                                case Directions.NORTHWEST: adjust=(R3.xy[1]<=R2.xy[1])||(R3.xy[0]<=R2.xy[0]); break;
                                case Directions.SOUTHEAST: adjust=(R3.xy[1]<=R2.xy[1]); break;
                                case Directions.SOUTHWEST: adjust=(R3.xy[1]<=R2.xy[1]); break;
                                case Directions.UP: adjust=(R3.xy[1]<=R2.xy[1]); break;
                                case Directions.DOWN: adjust=(R3.xy[1]>=R2.xy[1]); break;
                                }
                                if(adjust)
                                {
                                    //coordsDone.remove(R3.xy[0]+"/"+R3.xy[1]);
                                    R3.xy=newXY(R3.xy,d);
                                    coordsDone.add(R3.xy[0]+"/"+R3.xy[1]);
                                }
                            }
                        }
                        roomsDone.add(R2.roomID);
                        coordsDone.add(R2.xy[0]+"/"+R2.xy[1]);
                        V.addElement(R2);
                    }
            }
        }
        return V;
    }

	public Vector scoreRoom(Hashtable H, GrinderRoom room, HashSet roomsDone, boolean finalPosition)
	{
		HashSet coordsDone=new HashSet();
		coordsDone.add(0+"/"+0);

		HashSet myRoomsDone=new HashSet();
		myRoomsDone.add(room.roomID);

		Hashtable xys=new Hashtable();
		int[] xy=new int[2];
		if(finalPosition) room.xy=xy;
		xys.put(room.roomID,xy);

		Vector V=new Vector();
		V.addElement(room);
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
		 		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		 			if((d!=Directions.UP)
		 			&&(d!=Directions.DOWN)
		 			&&(R.doors[d]!=null)
		 			&&(R.doors[d].room!=null)
				 	&&(R.doors[d].room.length()>0)
		 			&&(!myRoomsDone.contains(R.doors[d].room))
		 			&&(!roomsDone.contains(R.doors[d].room)))
		 			{
		 				GrinderRoom R2=(GrinderRoom)H.get(R.doors[d].room);
		 				if(R2==null) continue;
		 				int[] xy2=newXY(xy,d);
		 				xys.put(R2.roomID,xy2);
		 				if(!coordsDone.contains(xy2[0]+"/"+xy2[1]))
		 				{
		 					if(finalPosition) R2.xy=xy2;
			 				myRoomsDone.add(R2.roomID);
			 				coordsDone.add(xy2[0]+"/"+xy2[1]);
			 				V.addElement(R2);
		 				}
		 			}
			}
		}
		return V;
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
        GrinderRoom GR=null;
        int[] xy=newXY(room.xy,d);
        if((xy[0]>=0)&&(xy[1]>=0)
        &&(xy[0]<grid.length)&&(xy[1]<grid[xy[0]].length))
        {
            GR=grid[xy[0]][xy[1]];
            if(GR!=null) return GR;
            xy=newXY(xy,d);
        }
        return null;
	}

	protected int findRelGridDir(GrinderRoom room, String roomID)
	{
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
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
	    if((dir==null)||(dir.room.length()==0))
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
	    if((dir.room.length()>0)&&((roomPointer==null)||(!roomPointer.roomID.equals(dir.room))))
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
