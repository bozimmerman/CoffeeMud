package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdGrid extends StdRoom implements GridLocale
{
	public String ID(){return "StdGrid";}
	protected Room[] alts=new Room[Directions.NUM_DIRECTIONS];
	protected Room[][] subMap=null;
	protected Vector descriptions=new Vector();
	protected Vector displayTexts=new Vector();
	protected int xsize=5;
	protected int ysize=5;

	public StdGrid()
	{
		super();
		myID=getClass().getName().substring(getClass().getName().lastIndexOf('.')+1);
	}
	public Environmental newInstance()
	{
		return new StdGrid();
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

	public Room getAltRoomFrom(Room loc)
	{
		int oldDirCode=-1;
		if(loc==null) return null;

		if(subMap==null) buildGrid();

		if(loc.roomID().length()==0) // might be a child of an adjacent grid!
		{
			for(int x=0;x<Directions.NUM_DIRECTIONS-1;x++)
				if((doors[x]!=null)
				   &&(doors[x] instanceof GridLocale)
				   &&(((GridLocale)doors[x]).isMyChild(loc)))
				{
					loc=doors[x];
					oldDirCode=x;
					break;
				}
		}

		if(oldDirCode<0)
			for(int x=0;x<Directions.NUM_DIRECTIONS-1;x++)
				if((doors[x]!=null)&&(doors[x]==loc))
				{
					oldDirCode=x;
					break;
				}
		if(oldDirCode<0) return null;

		return alts[oldDirCode];
	}

	public Vector getAllRooms()
	{
		if(subMap==null) buildGrid();
		Vector V=new Vector();
		for(int x=0;x<subMap.length;x++)
			for(int y=0;y<subMap[x].length;y++)
				V.addElement(subMap[x][y]);
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

	public Room findCenterRoom(int dirCode)
	{
		int x=0;
		int y=0;
		switch(dirCode)
		{
		case Directions.NORTH:
			x=subMap.length/2;
			break;
		case Directions.SOUTH:
			x=subMap.length/2;
			y=subMap[0].length-1;
			break;
		case Directions.EAST:
			x=subMap.length-1;
			y=subMap[0].length/2;
			break;
		case Directions.WEST:
			y=subMap[0].length/2;
			break;
		case Directions.UP:
		case Directions.DOWN:
			x=subMap.length/2;
			y=subMap[0].length/2;
			break;
		}
		Room returnRoom=null;
		int xadjust=0;
		int yadjust=0;
		try
		{
			while(returnRoom==null)
			{
				if(subMap[x+xadjust][y+yadjust]!=null)
					returnRoom=subMap[x+xadjust][y+yadjust];
				else
				if(subMap[x-xadjust][y-yadjust]!=null)
					returnRoom=subMap[x-xadjust][y-yadjust];
				else
				{
					switch(dirCode)
					{
					case Directions.NORTH:
					case Directions.SOUTH:
						xadjust++;
						break;
					case Directions.EAST:
					case Directions.WEST:
						yadjust++;
						break;
					case Directions.UP:
					case Directions.DOWN:
						xadjust++;
						yadjust++;
						break;
					}

				}
			}
		}
		catch(Exception e)
		{
		}
		return returnRoom;
	}

	protected void buildFinalLinks()
	{
		Exit ox=CMClass.getExit("Open");
		for(int d=0;d<Directions.NUM_DIRECTIONS-1;d++)
		{
			Room dirRoom=rawDoors()[d];
			Exit dirExit=rawExits()[d];
			if((dirExit==null)||(dirExit.hasADoor()))
				dirExit=ox;
			if(dirRoom!=null)
			{
				alts[d]=findCenterRoom(d);
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

	public void buildGrid()
	{
		clearGrid();
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
					CMMap.addRoom(newRoom);
				}
			}
		buildFinalLinks();
	}
	public boolean isMyChild(Room loc)
	{
		if(subMap==null) buildGrid();
		if(subMap!=null)
			for(int x=0;x<subMap.length;x++)
				for(int y=0;y<subMap[x].length;y++)
				{
					Room room=subMap[x][y];
					if(room==loc) return true;
				}
		return false;
	}

	public void clearGrid()
	{
		if(subMap!=null)
		{
			for(int x=0;x<subMap.length;x++)
				for(int y=0;y<subMap[x].length;y++)
				{
					Room room=subMap[x][y];
					CMMap.delRoom(room);
				}
			subMap=null;
		}
		alts=new Room[Directions.NUM_DIRECTIONS];
	}

	public String getChildCode(Room loc)
	{
		if(roomID().length()==0) return "";
		if(subMap==null) buildGrid();
		for(int x=0;x<subMap.length;x++)
			for(int y=0;y<subMap[x].length;y++)
				if(subMap[x][y]==loc)
					return roomID()+"#("+x+","+y+")";
		return "";
		
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
		if(subMap==null) buildGrid();
		int x=Util.s_int(childCode.substring(len,comma));
		int y=Util.s_int(childCode.substring(comma+1,childCode.length()-1));
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
		
		for(int a=0;a<numAffects();a++)
			gc.addAffect((Ability)fetchAffect(a).copyOf());
		for(int b=0;b<numBehaviors();b++)
			gc.addBehavior((Behavior)fetchBehavior(b).copyOf());
		return gc;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;


		if((affect.targetMinor()==affect.TYP_ENTER)
		&&(affect.target()==this))
		{
			MOB mob=affect.source();
			if((mob.location()!=null)&&(mob.location().roomID().length()>0))
			{
				Room altRoom=getAltRoomFrom(mob.location());
				if(altRoom==null)
				{
					mob.tell("Some great evil is preventing your movement that way.");
					return false;
				}
				else
				affect.modify(affect.source(),
							  altRoom,
							  affect.tool(),
							  affect.sourceCode(),
							  affect.sourceMessage(),
							  affect.targetCode(),
							  affect.targetMessage(),
							  affect.othersCode(),
							  affect.othersMessage());
			}
		}
		return true;
	}
}
