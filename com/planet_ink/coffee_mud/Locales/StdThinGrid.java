package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2004 Bo Zimmerman

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
public class StdThinGrid extends StdRoom implements GridLocale
{
	public String ID(){return "StdThinGrid";}
	
	public final static long EXPIRATION=60000;
	
	protected Vector descriptions=new Vector();
	protected Vector displayTexts=new Vector();
	
	protected int xsize=5;
	protected int ysize=5;
	
	protected final DVector rooms=new DVector(4);

	public StdThinGrid()
	{
		super();
		myID=getClass().getName().substring(getClass().getName().lastIndexOf('.')+1);
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

	protected Room getGridRoomIfExists(int x, int y)
	{
		synchronized(rooms)
		{
			for(int i=0;i<rooms.size();i++)
			{
				if((((Integer)rooms.elementAt(i,2)).intValue()==x)
				&&(((Integer)rooms.elementAt(i,3)).intValue()==y))
					return (Room)rooms.elementAt(i,1);
			}
			return null;
		}
	}
	
	protected static boolean cleanRoom(Room R)
	{
		if(R.getGridParent()==null) return true;
		if(R.numInhabitants()>0) return false;
		if(R.numItems()>0) return false;
		for(int a=0;a<R.numEffects();a++)
		{
			Ability A=R.fetchEffect(a);
			if((A!=null)&&(A.isBorrowed(R)))
				return false;
		}
		if(R.getGridParent() instanceof StdThinGrid)
		{
			StdThinGrid STG=(StdThinGrid)R.getGridParent();
			synchronized(STG.rooms)
			{
				for(int i=0;i<STG.rooms.size();i++)
				{
					if(STG.rooms.elementAt(i,1)==R)
					{
						long l=((Long)STG.rooms.elementAt(i,4)).longValue();
						if((System.currentTimeMillis()-l)<EXPIRATION)
							return false;
					}
				}
			}
		}
		return true;
	}
	
	protected static boolean cleanRoomCenter(Room R)
	{
		if(!cleanRoom(R)) 
			return false;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R2=(Room)R.rawDoors()[d];
			if((R2!=null)&&(!cleanRoom(R2)))
				return false;
		}
		return true;
	}
	
	protected static Room getMakeFreeRoom(String locale)
	{
		Vector set=null;
		synchronized(used)
		{
			set=(Vector)used.get(locale);
			if(set==null)
			{
				set=new Vector();
				used.put(locale,set);
			}
		}
		Integer totalSize=null;
		synchronized(totalSizes)
		{
			totalSize=(Integer)totalSizes.get(locale);
			if(totalSize==null)
			{
				totalSize=new Integer(0);
				totalSizes.put(locale,totalSize);
			}
		}
		synchronized(set)
		{
			if(set.size()>=((totalSize.intValue()/DIVISOR)+5))
			{
				for(int i=0;i<set.size();i++)
				{
					Room R=(Room)set.firstElement();
					set.removeElementAt(0);
					set.addElement(R);
					if(cleanRoomCenter(R))
						return R;
				}
			}
			Room R=CMClass.getLocale(locale);
			set.addElement(R);
			return R;
		}
	}
	
	protected void fixRoomData(Room R)
	{
		R.setGridParent(this);
		R.setArea(getArea());
		R.setRoomID("");
		R.setDisplayText(displayText());
		R.setDescription(description());
		int c=-1;
		if(displayTexts!=null)
		if(displayTexts.size()>0)
		{
			c=Dice.roll(1,displayTexts.size(),-1);
			R.setDisplayText((String)displayTexts.elementAt(c));
		}
		if(descriptions!=null)
		if(descriptions.size()>0)
		{
			if((c<0)||(c>descriptions.size())||(descriptions.size()!=displayTexts.size()))
				c=Dice.roll(1,descriptions.size(),-1);
			R.setDescription((String)descriptions.elementAt(c));
		}

		for(int a=0;a<numEffects();a++)
			R.addEffect((Ability)fetchEffect(a).copyOf());
		for(int b=0;b<numBehaviors();b++)
			R.addBehavior((Behavior)fetchBehavior(b).copyOf());
	}
	
	protected Room getMakeSingleGridRoom(int x, int y)
	{
		if((x<0)||(y<0)||(y>=ySize())||(x>=xSize())) 
			return null;
		
		synchronized(rooms)
		{
			Room R=getGridRoomIfExists(x,y);
			if(R==null)
			{
				R=getMakeFreeRoom(getChildLocaleID());
				if(R==null) return null;
				rooms.addElement(R,new Integer(x),new Integer(y),new Long(System.currentTimeMillis()));
			}
			return R;
		}
	}
	
	protected void fillExitsOfGridRoom(Room R, int x, int y)
	{
		Exit ox=CMClass.getExit("Open");
		Room R2=null;
		if(y>0)
		{
			R2=getMakeSingleGridRoom(x,y-1);
			if(R2!=null) linkRoom(R,R2,Directions.NORTH,ox,ox);
		}
		if(x>0)
		{
			R2=getMakeSingleGridRoom(x-1,y);
			if(R2!=null) linkRoom(R,R2,Directions.WEST,ox,ox);
		}
		if(y<(ySize()-1))
		{
			R2=getMakeSingleGridRoom(x,y+1);
			if(R2!=null) linkRoom(R,R2,Directions.SOUTH,ox,ox);
		}
		if(x<(xSize()-1))
		{
			R2=getMakeSingleGridRoom(x+1,y);
			if(R2!=null) linkRoom(R,R2,Directions.EAST,ox,ox);
		}
	}
	
	protected Room getMakeGridRoom(int x, int y)
	{
		if((x<0)||(y<0)||(y>=ySize())||(x>=xSize())) 
			return null;
		
		synchronized(rooms)
		{
			if(registeredSize!=(ySize()*xSize()))
			{
				Integer totalSize=null;
				synchronized(totalSizes)
				{
					totalSize=(Integer)totalSizes.get(getChildLocaleID());
					if(totalSize==null)
					{
						totalSize=new Integer(0);
						totalSizes.put(getChildLocaleID(),totalSize);
					}
					totalSizes.remove(getChildLocaleID());
					int newSize=totalSize.intValue()-registeredSize;
					newSize+=(ySize()*xSize());
					totalSizes.put(getChildLocaleID(),new Integer(newSize));
				}
				registeredSize=(ySize()*xSize());
			}
			Room R=getMakeSingleGridRoom(x,y);
			if(R==null) return null;
			fillExitsOfGridRoom(R,x,y);
			return R;
		}
	}
	
	public Room getAltRoomFrom(Room loc, int direction)
	{
		if((loc==null)||(direction<0))
			return null;
		int opDirection=Directions.getOpDirectionCode(direction);
		
		Room oldLoc=loc;
		if(loc.getGridParent()!=null)
			loc=loc.getGridParent();
		if((oldLoc!=loc)&&(loc instanceof GridLocale))
		{
			int y=((GridLocale)loc).getChildY(oldLoc);
			int x=((GridLocale)loc).getChildX(oldLoc);
			if((x>=0)&&(y>=0))
			switch(opDirection)
			{
			case Directions.EAST:
				if((((GridLocale)loc).ySize()==ySize()))
					return getMakeGridRoom(x-1,y);
				break;
			case Directions.WEST:
				if((((GridLocale)loc).ySize()==ySize()))
					return getMakeGridRoom(0,y);
				break;
			case Directions.NORTH:
				if((((GridLocale)loc).xSize()==xSize()))
					return getMakeGridRoom(x,0);
				break;
			case Directions.SOUTH:
				if((((GridLocale)loc).xSize()==xSize()))
					return getMakeGridRoom(x,y-1);
				break;
			}
		}
		int x=0;
		int y=0;
		switch(opDirection)
		{
		case Directions.NORTH:
			x=x/2;
			break;
		case Directions.SOUTH:
			x=x/2;
			y=y-1;
			break;
		case Directions.EAST:
			x=x-1;
			y=y/2;
			break;
		case Directions.WEST:
			y=y/2;
			break;
		case Directions.UP:
		case Directions.DOWN:
			x=x/2;
			y=y/2;
			break;
		}
		return getMakeGridRoom(x,y);
	}

	public Vector getAllRooms()
	{
		Vector V=new Vector();
		if(rooms.size()==0) getMakeGridRoom(0,0);
		for(int i=0;i<rooms.size();i++)
			V.addElement(rooms.elementAt(i,1));
		return V;
	}
	protected static void halfLink(Room room, Room loc, int dirCode, Exit o)
	{
		if(room==null) return;
		if(loc==null) return;
		if(room.rawDoors()[dirCode]!=null) return;
		if(o==null) o=(Exit)CMClass.getExit("Open");
		room.rawDoors()[dirCode]=loc;
		room.rawExits()[dirCode]=o;
	}

	protected static void linkRoom(Room room, Room loc, int dirCode, Exit o, Exit ao)
	{
		if(loc==null) return;
		if(room==null) return;
		int opCode=Directions.getOpDirectionCode(dirCode);
		if(room.rawDoors()[dirCode]!=null) return;
		if(o==null) o=(Exit)CMClass.getExit("Open");
		room.rawDoors()[dirCode]=loc;
		room.rawExits()[dirCode]=o;
		if(loc.rawDoors()[opCode]!=null) return;
		if(ao==null) ao=(Exit)CMClass.getExit("Open");
		loc.rawDoors()[opCode]=room;
		loc.rawExits()[opCode]=ao;
	}

	public void buildGrid()
	{
		clearGrid();
	}
	
	public boolean isMyChild(Room loc)
	{
		for(int i=0;i<rooms.size();i++)
			if(loc==rooms.elementAt(i,1))
				return true;
		return false;
	}

	public void clearGrid()
	{
		try
		{
			while(rooms.size()>0)
			{
				Room room=(Room)rooms.elementAt(0,1);
				rooms.removeElementAt(0);
				while(room.numInhabitants()>0)
				{
					MOB M=room.fetchInhabitant(0);
					if(M!=null)
					if((M.getStartRoom()==null)
					||(M.getStartRoom()==room)
					||(M.getStartRoom().ID().length()==0))
						M.destroy();
					else
						M.getStartRoom().bringMobHere(M,false);
				}
				while(room.numItems()>0)
				{
					Item I=room.fetchItem(0);
					if(I!=null) I.destroy();
				}
				room.setGridParent(null);
			}
		}
		catch(Exception e){}
	}

	public String getChildCode(Room loc)
	{
		if(roomID().length()==0) return "";
		DVector rs=rooms.copyOf();
		for(int i=0;i<rs.size();i++)
			if(rs.elementAt(i,1)==loc)
				return roomID()+"#("+((Integer)rs.elementAt(i,1)).intValue()+","+((Integer)rs.elementAt(i,2)).intValue()+")";
		return "";

	}
	
	public int getChildX(Room loc)
	{
		if(roomID().length()==0) return -1;
		DVector rs=rooms.copyOf();
		for(int i=0;i<rs.size();i++)
			if(rs.elementAt(i,1)==loc)
				return ((Integer)rs.elementAt(i,1)).intValue();
		return -1;
	}
	
	public int getChildY(Room loc)
	{
		if(roomID().length()==0) return -1;
		DVector rs=rooms.copyOf();
		for(int i=0;i<rs.size();i++)
			if(rs.elementAt(i,1)==loc)
				return ((Integer)rs.elementAt(i,2)).intValue();
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
		int x=Util.s_int(childCode.substring(len,comma));
		int y=Util.s_int(childCode.substring(comma+1,childCode.length()-1));
		return getMakeGridRoom(x,y);
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
				else
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
