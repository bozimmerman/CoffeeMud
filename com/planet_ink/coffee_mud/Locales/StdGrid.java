package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

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
public class StdGrid extends StdRoom implements GridLocale
{
	public String ID(){return "StdGrid";}
	protected Room[][] subMap=null;
	protected Vector descriptions=new Vector();
	protected Vector displayTexts=new Vector();
	protected Vector gridexits=new Vector();
	protected int xsize=5;
	protected int ysize=5;

	public StdGrid()
	{
		super();
		myID=getClass().getName().substring(getClass().getName().lastIndexOf('.')+1);
	}

    protected void cloneFix(Room E)
    {
        super.cloneFix(E);
        if(E instanceof StdGrid)
        {
            descriptions=(Vector)((StdGrid)E).descriptions.clone();
            displayTexts=(Vector)((StdGrid)E).displayTexts.clone();
            gridexits=(Vector)((StdGrid)E).gridexits.clone();
        }
    }
	public String getChildLocaleID(){return "StdRoom";}

	public int xSize(){return xsize;}
	public int ySize(){return ysize;}
	public void setXSize(int x){ if(x>0)xsize=x; }
	public void setYSize(int y){ if(y>0)ysize=y; }

	public void setDescription(String newDescription)
	{
		super.setDescription(newDescription);
		descriptions=new Vector();
		int x=newDescription.indexOf("<P>");
		while(x>=0)
		{
			String s=newDescription.substring(0,x).trim();
			if(s.length()>0) descriptions.addElement(s);
			newDescription=newDescription.substring(x+3).trim();
			x=newDescription.indexOf("<P>");
		}
		if(newDescription.length()>0)
			descriptions.addElement(newDescription);
	}

	public void setDisplayText(String newDisplayText)
	{
		super.setDisplayText(newDisplayText);
		displayTexts=new Vector();
		int x=newDisplayText.indexOf("<P>");
		while(x>=0)
		{
			String s=newDisplayText.substring(0,x).trim();
			if(s.length()>0) displayTexts.addElement(s);
			newDisplayText=newDisplayText.substring(x+3).trim();
			x=newDisplayText.indexOf("<P>");
		}
		if(newDisplayText.length()>0)
			displayTexts.addElement(newDisplayText);
	}

	public Vector outerExits(){return (Vector)gridexits.clone();}
	public void addOuterExit(CMMap.CrossExit x){gridexits.addElement(x);}
	public void delOuterExit(CMMap.CrossExit x){gridexits.remove(x);}
	
	public Room getAltRoomFrom(Room loc, int direction)
	{
		if((loc==null)||(direction<0))
			return null;
		int opDirection=Directions.getOpDirectionCode(direction);
		
		getBuiltGrid();
		Room[][] grid=null;
		if(gridexits.size()>0)
		{
			grid=getBuiltGrid();
			String roomID=CMMap.getExtendedRoomID(loc);
			if(grid!=null)
				for(int d=0;d<gridexits.size();d++)
				{
					CMMap.CrossExit EX=(CMMap.CrossExit)gridexits.elementAt(d);
					if((!EX.out)
					&&(EX.destRoomID.equalsIgnoreCase(roomID))
					&&(EX.dir==direction)
					&&(EX.x>=0)&&(EX.y>=0)&&(EX.x<ySize())&&(EX.y<ySize())
					&&(grid[EX.x][EX.y]!=null))
						return grid[EX.x][EX.y];
				}
		}

		Room oldLoc=loc;
		if(loc.getGridParent()!=null)
			loc=loc.getGridParent();
		if((oldLoc!=loc)&&(loc instanceof GridLocale))
		{
			if(grid==null) grid=getBuiltGrid();
			if(grid!=null)
			{
				int y=((GridLocale)loc).getChildY(oldLoc);
				int x=((GridLocale)loc).getChildX(oldLoc);
				if((x>=0)&&(y>=0))
				switch(opDirection)
				{
				case Directions.EAST:
					if((((GridLocale)loc).ySize()==ySize()))
						return grid[grid.length-1][y];
					break;
				case Directions.WEST:
					if((((GridLocale)loc).ySize()==ySize()))
						return grid[0][y];
					break;
				case Directions.NORTHWEST:
					return grid[0][0];
				case Directions.NORTHEAST:
					return grid[grid.length-1][0];
				case Directions.NORTH:
					if((((GridLocale)loc).xSize()==xSize()))
						return grid[x][0];
					break;
				case Directions.SOUTH:
					if((((GridLocale)loc).xSize()==xSize()))
						return grid[x][grid[0].length-1];
					break;
				case Directions.SOUTHEAST:
					return grid[grid.length-1][grid[0].length-1];
				case Directions.SOUTHWEST:
					return grid[0][grid[0].length-1];
				}
			}
		}
		return findCenterRoom(opDirection);
	}

	protected Room[][] getBuiltGrid()
	{
		if(subMap==null) buildGrid();
		if(subMap!=null) return (Room[][])subMap.clone();
		return null;
	}

	public Room getRandomChild()
	{
		Vector V=getAllRooms();
		if(V.size()==0) return null;
		return (Room)V.elementAt(Dice.roll(1,V.size(),-1));
	}
	public Vector getAllRooms()
	{
		Vector V=new Vector();
		Room[][] subMap=getBuiltGrid();
		if(subMap!=null)
		for(int x=0;x<subMap.length;x++)
			for(int y=0;y<subMap[x].length;y++)
				V.addElement(subMap[x][y]);
		return V;
	}
	protected void halfLink(Room room, 
							Room loc, 
							int dirCode, 
							Exit o)
	{
		if(room==null) return;
		if(loc==null) return;
		if(room.rawDoors()[dirCode]!=null)
		{
			if(room.rawDoors()[dirCode].getGridParent()==null)
				return;
			if(room.rawDoors()[dirCode].getGridParent().isMyChild(room.rawDoors()[dirCode]))
				return;
			room.rawDoors()[dirCode]=null;
		}
		if(o==null) o=CMClass.getExit("Open");
		room.rawDoors()[dirCode]=alternativeLink(room,loc,dirCode);
		room.rawExits()[dirCode]=o;
	}

	protected Room alternativeLink(Room room, Room defaultRoom, int dir)
	{
		if((subMap!=null)&&(room.getGridParent()==this))
		for(int d=0;d<gridexits.size();d++)
		{
			CMMap.CrossExit EX=(CMMap.CrossExit)gridexits.elementAt(d);
			try{
				if((EX.out)&&(EX.dir==dir)
				&&(EX.x>=0)&&(EX.y>=0)&&(EX.x<xSize())&&(EX.y<ySize())
				&&(subMap[EX.x][EX.y]==room))
				{
					Room R=CMMap.getRoom(EX.destRoomID);
					if(R!=null)
					{
						if(R.getGridParent()!=null)
							return R.getGridParent();
						return R;
					}
				}
			}catch(Exception e){}
		}
		return defaultRoom;
	}
	
	protected void linkRoom(Room room, 
							Room loc, 
							int dirCode, 
							Exit o, 
							Exit ao)
	{
		if(loc==null) return;
		if(room==null) return;
		int opCode=Directions.getOpDirectionCode(dirCode);
		if(room.rawDoors()[dirCode]!=null)
		{
			if(room.rawDoors()[dirCode].getGridParent()==null)
				return;
			if(room.rawDoors()[dirCode].getGridParent().isMyChild(room.rawDoors()[dirCode]))
				return;
			room.rawDoors()[dirCode]=null;
		}
		if(o==null) o=CMClass.getExit("Open");
		room.rawDoors()[dirCode]=alternativeLink(room,loc,dirCode);
		room.rawExits()[dirCode]=o;
		if(loc.rawDoors()[opCode]!=null)
		{
			if(loc.rawDoors()[opCode].getGridParent()==null)
				return;
			if(loc.rawDoors()[opCode].getGridParent().isMyChild(loc.rawDoors()[opCode]))
				return;
			loc.rawDoors()[opCode]=null;
		}
		if(ao==null) ao=CMClass.getExit("Open");
		loc.rawDoors()[opCode]=alternativeLink(loc,room,opCode);
		loc.rawExits()[opCode]=ao;
	}

    protected int[] initCenterRoomXY(int dirCode)
    {
        int[] xy=new int[2];
        switch(dirCode)
        {
        case Directions.NORTHEAST:
            xy[0]=xSize()-1;
            break;
        case Directions.NORTHWEST:
            break;
        case Directions.NORTH:
            xy[0]=xSize()/2;
            break;
        case Directions.SOUTHEAST:
            xy[1]=ySize()-1;
            xy[0]=xSize()-1;
            break;
        case Directions.SOUTHWEST:
            xy[1]=ySize()-1;
            break;
        case Directions.SOUTH:
            xy[0]=xSize()/2;
            xy[1]=ySize()-1;
            break;
        case Directions.EAST:
            xy[0]=xSize()-1;
            xy[1]=ySize()/2;
            break;
        case Directions.WEST:
            xy[1]=ySize()/2;
            break;
        default:
            xy[0]=xSize()/2;
            xy[1]=ySize()/2;
            break;
        }
        return xy;
    }
    
    private static int[][] XY_ADJUSTMENT_CHART=null;
    private static int[] XY_ADJUSTMENT_DEFAULT={1,1};
    protected int[] initCenterRoomAdjustsXY(int dirCode)
    {
        if((dirCode<0)||(dirCode>=Directions.NUM_DIRECTIONS))
            return XY_ADJUSTMENT_DEFAULT;
        if((XY_ADJUSTMENT_CHART!=null)&&(dirCode<XY_ADJUSTMENT_CHART.length))
            return XY_ADJUSTMENT_CHART[dirCode];
        
        int[][] xy=new int[Directions.NUM_DIRECTIONS][2];
        for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
        {
            switch(d)
            {
            case Directions.NORTHEAST:
            case Directions.NORTH:
            case Directions.SOUTHEAST:
            case Directions.SOUTH:
                xy[d][0]=1;
                xy[d][1]=0;
                break;
            case Directions.NORTHWEST:
            case Directions.EAST:
            case Directions.SOUTHWEST:
            case Directions.WEST:
                xy[d][0]=0;
                xy[d][1]=1;
                break;
            default:
                xy[d][0]=1;
                xy[d][1]=1;
                break;
            }
        }
        XY_ADJUSTMENT_CHART=xy;
        return xy[dirCode];
    }
    
	protected Room findCenterRoom(int dirCode)
	{
        int[] xy=initCenterRoomXY(dirCode);
        int[] adjXY=initCenterRoomAdjustsXY(dirCode);
        
		Room returnRoom=null;
		int xadjust=0;
		int yadjust=0;
        boolean moveAndCheckAgain=true;
		while((subMap!=null)&&(moveAndCheckAgain))
		{
            moveAndCheckAgain=false;
            
            if(((xy[0]-xadjust)>=0)&&((xy[1]-yadjust)>=0))
            {
                moveAndCheckAgain=true;
                try{
                    if(subMap[xy[0]-xadjust][xy[1]-yadjust]!=null)
                        return subMap[xy[0]-xadjust][xy[1]-yadjust];
                }catch(Exception e){}
            }
            
            if(((xy[0]+xadjust)<xSize())&&((xy[1]+yadjust)<ySize()))
            {
                moveAndCheckAgain=true;
                try{
                    if(subMap[xy[0]+xadjust][xy[1]+yadjust]!=null)
                        return subMap[xy[0]+xadjust][xy[1]+yadjust];
                }catch(Exception e){}
            }
            if(moveAndCheckAgain)
            {
                xadjust+=adjXY[0];
                yadjust+=adjXY[1];
            }
		}
		return returnRoom;
	}

	protected void buildFinalLinks()
	{
		Exit ox=CMClass.getExit("Open");
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
		    if(d==Directions.GATE) continue;
			Room dirRoom=rawDoors()[d];
			Exit dirExit=rawExits()[d];
			if((dirExit==null)||(dirExit.hasADoor()))
				dirExit=ox;
			if(dirRoom!=null)
			{
				Exit altExit=dirRoom.rawExits()[Directions.getOpDirectionCode(d)];
				if(altExit==null) altExit=ox;
				switch(d)
				{
					case Directions.NORTH:
						for(int x=0;x<subMap.length;x++)
							linkRoom(subMap[x][0],dirRoom,d,dirExit,altExit);
						break;
					case Directions.SOUTH:
						for(int x=0;x<subMap.length;x++)
							linkRoom(subMap[x][subMap[x].length-1],dirRoom,d,dirExit,altExit);
						break;
					case Directions.NORTHEAST:
						linkRoom(subMap[subMap.length-1][0],dirRoom,d,dirExit,altExit);
						break;
					case Directions.NORTHWEST:
						linkRoom(subMap[0][0],dirRoom,d,dirExit,altExit);
						break;
					case Directions.SOUTHEAST:
						linkRoom(subMap[subMap.length-1][subMap[0].length-1],dirRoom,d,dirExit,altExit);
						break;
					case Directions.SOUTHWEST:
						linkRoom(subMap[0][subMap[0].length-1],dirRoom,d,dirExit,altExit);
						break;
					case Directions.EAST:
						for(int y=0;y<subMap[0].length;y++)
							linkRoom(subMap[subMap.length-1][y],dirRoom,d,dirExit,altExit);
						break;
					case Directions.WEST:
						for(int y=0;y<subMap[0].length;y++)
							linkRoom(subMap[0][y],dirRoom,d,dirExit,altExit);
						break;
					case Directions.UP:
						for(int x=0;x<subMap.length;x++)
							for(int y=0;y<subMap[x].length;y++)
								linkRoom(subMap[x][y],dirRoom,d,dirExit,altExit);
						break;
					case Directions.DOWN:
						for(int x=0;x<subMap.length;x++)
							for(int y=0;y<subMap[x].length;y++)
								linkRoom(subMap[x][y],dirRoom,d,dirExit,altExit);
						break;
				}
			}
		}
	}

	public void tryFillInExtraneousExternal(CMMap.CrossExit EX, Exit ox)
	{
		if(EX==null) return;
		Room linkFrom=null;
		if(subMap!=null)
			linkFrom=subMap[EX.x][EX.y];
		if(linkFrom!=null)
		{
			Room linkTo=CMMap.getRoom(EX.destRoomID);
			if((linkTo!=null)&&(linkTo.getGridParent()!=null)) 
				linkTo=linkTo.getGridParent();
			if((linkTo!=null)&&(linkFrom.rawDoors()[EX.dir]!=linkTo))
			{
				if(ox==null) ox=CMClass.getExit("Open");
				linkFrom.rawDoors()[EX.dir]=linkTo;
				linkFrom.rawExits()[EX.dir]=ox;
			}
		}
	}
	
	public void fillInTheExtraneousExternals(Room[][] subMap, Exit ox)
	{
		if(subMap!=null)
		for(int d=0;d<gridexits.size();d++)
		{
			CMMap.CrossExit EX=(CMMap.CrossExit)gridexits.elementAt(d);
			try{
				if(EX.out)
				switch(EX.dir)
				{
				case Directions.NORTH:
					if(EX.y==0)
						tryFillInExtraneousExternal(EX,ox);
					break;
				case Directions.SOUTH:
					if(EX.y==ySize()-1)
						tryFillInExtraneousExternal(EX,ox);
					break;
				case Directions.EAST:
					if(EX.x==xSize()-1)
						tryFillInExtraneousExternal(EX,ox);
					break;
				case Directions.WEST:
					if(EX.x==0)
						tryFillInExtraneousExternal(EX,ox);
					break;
				case Directions.NORTHEAST:
					if((EX.y==0)&&(EX.x==xSize()-1))
						tryFillInExtraneousExternal(EX,ox);
					break;
				case Directions.SOUTHWEST:
					if((EX.y==ySize()-1)&&(EX.x==0))
						tryFillInExtraneousExternal(EX,ox);
					break;
				case Directions.NORTHWEST:
					if((EX.y==0)&&(EX.x==0))
						tryFillInExtraneousExternal(EX,ox);
					break;
				case Directions.SOUTHEAST:
					if((EX.y==ySize()-1)&&(EX.x==xSize()-1))
						tryFillInExtraneousExternal(EX,ox);
					break;
				}
			}catch(Exception e){}
		}
	}
	
	public void buildGrid()
	{
		clearGrid(null);
		try
		{
			subMap=new Room[xsize][ysize];
			Exit ox=CMClass.getExit("Open");
			for(int x=0;x<subMap.length;x++)
				for(int y=0;y<subMap[x].length;y++)
				{
					Room newRoom=getGridRoom(x,y);
					if(newRoom!=null)
					{
						subMap[x][y]=newRoom;
						if((y>0)&&(subMap[x][y-1]!=null))
							linkRoom(newRoom,subMap[x][y-1],Directions.NORTH,ox,ox);
						if((x>0)&&(subMap[x-1][y]!=null))
							linkRoom(newRoom,subMap[x-1][y],Directions.WEST,ox,ox);
						if(Directions.NORTHEAST<Directions.NUM_DIRECTIONS)
						{
						    if((y>0)&&(x>0)&&(subMap[x-1][y-1]!=null))
								linkRoom(newRoom,subMap[x-1][y-1],Directions.NORTHWEST,ox,ox);
						    if(((y+1)<subMap[x].length)&&(x>0)&&(subMap[x-1][y+1]!=null))
								linkRoom(newRoom,subMap[x-1][y+1],Directions.SOUTHWEST,ox,ox);
						}
						CMMap.addRoom(newRoom);
					}
				}
			buildFinalLinks();
			fillInTheExtraneousExternals(subMap,ox);
		}
		catch(Exception e)
		{
		    Log.errOut("StdGrid",e);
			clearGrid(null);
		}
	}
	public boolean isMyChild(Room loc)
	{
		Room[][] subMap=getBuiltGrid();
		if(subMap!=null)
		for(int x=0;x<subMap.length;x++)
			for(int y=0;y<subMap[x].length;y++)
			{
				Room room=subMap[x][y];
				if(room==loc) return true;
			}
		return false;
	}

	public void clearGrid(Room backHere)
	{
		try
		{
			if(subMap!=null)
			for(int x=0;x<subMap.length;x++)
				for(int y=0;y<subMap[x].length;y++)
				{
					Room room=subMap[x][y];
					if(room!=null)
					{
						while(room.numInhabitants()>0)
						{
							MOB M=room.fetchInhabitant(0);
							if(M!=null)
							{
								if(backHere!=null)
									backHere.bringMobHere(M,false);
								else
								if((M.getStartRoom()==null)
								||(M.getStartRoom()==room)
								||(M.getStartRoom().ID().length()==0))
									M.destroy();
								else
									M.getStartRoom().bringMobHere(M,false);
                                if(room.isInhabitant(M))
                                {
                                    M.destroy();
                                    M.setLocation(null);
                                    if(room.isInhabitant(M))
                                        room.delInhabitant(M);
                                }
							}
						}
						while(room.numItems()>0)
						{
							Item I=room.fetchItem(0);
							if(I!=null)
							{
								if(backHere!=null)
									backHere.bringItemHere(I,Item.REFUSE_PLAYER_DROP);
								else
									I.destroy();
                                if(room.isContent(I))
                                {
                                    I.destroy();
                                    if(room.isContent(I))
                                    {
                                        I.setOwner(null);
                                        room.delItem(I);
                                    }
                                }
							}
						}
					}
					room.clearSky();
					room.destroyRoom();
					room.setGridParent(null);
			    }
			subMap=null;
		}
		catch(Exception e){}
	}

	public String getChildCode(Room loc)
	{
		if(roomID().length()==0) return "";
		Room[][] subMap=getBuiltGrid();
		if(subMap!=null)
		for(int x=0;x<subMap.length;x++)
			for(int y=0;y<subMap[x].length;y++)
				if(subMap[x][y]==loc)
					return roomID()+"#("+x+","+y+")";
		return "";

	}
	public int getChildX(Room loc)
	{
		if(roomID().length()==0) return -1;
		Room[][] subMap=getBuiltGrid();
		if(subMap!=null)
		for(int x=0;x<subMap.length;x++)
			for(int y=0;y<subMap[x].length;y++)
				if(subMap[x][y]==loc)
					return x;
		return -1;
	}
	public int getChildY(Room loc)
	{
		if(roomID().length()==0) return -1;
		Room[][] subMap=getBuiltGrid();
		if(subMap!=null)
		for(int x=0;x<subMap.length;x++)
			for(int y=0;y<subMap[x].length;y++)
				if(subMap[x][y]==loc)
					return y;
		return -1;
	}
	public Room getChild(String childCode)
	{
		if(childCode.equals(roomID()))
			return this;
		if(!childCode.startsWith(roomID()+"#("))
			return null;
		int len=roomID().length()+2;
		int comma=childCode.indexOf(',',len);
		if(comma<0) return null;
		Room[][] subMap=getBuiltGrid();
		int x=Util.s_int(childCode.substring(len,comma));
		int y=Util.s_int(childCode.substring(comma+1,childCode.length()-1));
		if(subMap!=null)
		if((x<subMap.length)&&(y<subMap[x].length))
			return subMap[x][y];
		return null;
	}

	protected Room getGridRoom(int x, int y)
	{
		if(subMap==null) subMap=new Room[xsize][ysize];
		if((x<0)||(y<0)||(y>=subMap[0].length)||(x>=subMap.length)) return null;
		Room gc=CMClass.getLocale(getChildLocaleID());
		gc.setRoomID("");
		gc.setGridParent(this);
		gc.setArea(getArea());
		gc.setDisplayText(displayText());
		gc.setDescription(description());
		int c=-1;
		if(displayTexts!=null)
		if(displayTexts.size()>0)
		{
			c=Dice.roll(1,displayTexts.size(),-1);
			gc.setDisplayText((String)displayTexts.elementAt(c));
		}
		if(descriptions!=null)
		if(descriptions.size()>0)
		{
			if((c<0)||(c>descriptions.size())||(descriptions.size()!=displayTexts.size()))
				c=Dice.roll(1,descriptions.size(),-1);
			gc.setDescription((String)descriptions.elementAt(c));
		}

		for(int a=0;a<numEffects();a++)
			gc.addEffect((Ability)fetchEffect(a).copyOf());
		for(int b=0;b<numBehaviors();b++)
			gc.addBehavior(fetchBehavior(b).copyOf());
		return gc;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target()==this))
		{
			MOB mob=msg.source();
			if((mob.location()!=null)&&(mob.location().roomID().length()>0))
			{
				int direction=-1;
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					if(mob.location().getRoomInDir(d)==this)
						direction=d;
				}
				if(direction<0)
				{
					mob.tell("Some great evil is preventing your movement that way.");
					return false;
				}
				msg.modify(msg.source(),
						  getAltRoomFrom(mob.location(),direction),
						  msg.tool(),
						  msg.sourceCode(),
						  msg.sourceMessage(),
						  msg.targetCode(),
						  msg.targetMessage(),
						  msg.othersCode(),
						  msg.othersMessage());
			}
		}
		return true;
	}
}
