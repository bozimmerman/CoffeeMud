package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.*;

import java.util.*;

public class Grid extends StdRoom implements GridLocale 
{
	protected Room[] alts=new Room[Directions.NUM_DIRECTIONS];
	protected Room[][] subMap=null;
	protected String[] descriptions=null;
	protected String[] displayTexts=null;
	protected int size=5;
	
	public Grid()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Environmental newInstance()
	{
		return new Grid();
	}
	
	public void setDescription(String newDescription)
	{
		super.setDescription(newDescription);
		int numDs=0;
		int x=newDescription.indexOf("<P>");
		while(x>=0)
		{
			numDs++;
			x=newDescription.indexOf("<P>",x+2);
		}
		descriptions=new String[numDs+1];
		x=newDescription.indexOf("<P>");
		numDs=0;
		while(x>=0)
		{
			descriptions[numDs]=newDescription.substring(0,x);
			numDs++;
			newDescription=newDescription.substring(x+3);
			x=newDescription.indexOf("<P>");
		}
		descriptions[numDs]=newDescription;
		if(numDs>0)
			description=descriptions[0];
	}
	
	public void setDisplayText(String newDisplayText)
	{
		super.setDisplayText(newDisplayText);
		int numDs=0;
		int x=newDisplayText.indexOf("<P>");
		while(x>=0)
		{
			numDs++;
			x=newDisplayText.indexOf("<P>",x+2);
		}
		displayTexts=new String[numDs+1];
		x=newDisplayText.indexOf("<P>");
		numDs=0;
		while(x>=0)
		{
			displayTexts[numDs]=newDisplayText.substring(0,x);
			numDs++;
			newDisplayText=newDisplayText.substring(x+3);
			x=newDisplayText.indexOf("<P>");
		}
		displayTexts[numDs]=newDisplayText;
		if(numDs>0)
			displayText=displayTexts[0];
	}
	
	public Room getAltRoomFrom(Room loc)
	{
		int oldDirCode=-1;
		if(loc==null) return null;
		
		if(loc instanceof GridLocaleChild)
			loc=((GridLocaleChild)loc).parent();
		
		for(int x=0;x<Directions.NUM_DIRECTIONS;x++)
			if((doors[x]!=null)&&(doors[x]==loc))
			{
				oldDirCode=x;
				break;
			}
		if(oldDirCode<0) return null;
		
		return alts[oldDirCode];
	}
	
	protected static void linkRoom(Room room, Room loc, int dirCode)
	{
		if(loc==null) return;
		if(room==null) return;
		int opCode=Directions.getOpDirectionCode(dirCode);
		Exit o=new Open();
		if(room.doors()[dirCode]!=null) return;
		room.doors()[dirCode]=loc;
		room.exits()[dirCode]=o;
		if(loc.doors()[opCode]!=null) return;
		loc.doors()[opCode]=room;
		loc.exits()[opCode]=o;
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
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room dirRoom=this.doors()[d];
			if(dirRoom!=null)
			{
				alts[d]=findCenterRoom(d);
				switch(d)
				{
					case Directions.NORTH:
						for(int x=0;x<subMap.length;x++)
							linkRoom(subMap[x][0],dirRoom,d);
						break;
					case Directions.SOUTH:
						for(int x=0;x<subMap.length;x++)
							linkRoom(subMap[x][subMap[0].length-1],dirRoom,d);
						break;
					case Directions.EAST:
						for(int y=0;y<subMap[0].length;y++)
							linkRoom(subMap[subMap.length-1][y],dirRoom,d);
						break;
					case Directions.WEST:
						for(int y=0;y<subMap[0].length;y++)
							linkRoom(subMap[0][y],dirRoom,d);
						break;
					case Directions.UP:
						for(int x=0;x<subMap.length;x++)
							for(int y=0;y<subMap[0].length;y++)
								linkRoom(subMap[x][y],dirRoom,d);
						break;
					case Directions.DOWN:
						for(int x=0;x<subMap.length;x++)
							for(int y=0;y<subMap[0].length;y++)
								linkRoom(subMap[x][y],dirRoom,d);
						break;
				}
			}
		}
	}
	
	public void buildGrid()
	{
		clearGrid();
		subMap=new Room[size][size];
		for(int x=0;x<size;x++)
			for(int y=0;y<size;y++)
			{
				Room newRoom=getGridRoom(x,y);
				if(newRoom!=null)
				{
					subMap[x][y]=newRoom;
					if((y>0)&&(subMap[x][y-1]!=null))
						linkRoom(newRoom,subMap[x][y-1],Directions.NORTH);
					if((x>0)&&(subMap[x-1][y]!=null))
						linkRoom(newRoom,subMap[x-1][y],Directions.WEST);
					MUD.map.addElement(newRoom);
				}
			}
		buildFinalLinks();
	}
	
	public void clearGrid()
	{
		if(subMap!=null)
		{
			for(int x=0;x<subMap.length;x++)
				for(int y=0;y<subMap[x].length;y++)
				{
					Room room=subMap[x][y];
					MUD.map.remove(room);
				}
			subMap=null;
		}
		alts=new Room[Directions.NUM_DIRECTIONS];
	}
	
	protected Room getGridRoom(int x, int y)
	{
		if((x<0)||(y<0)||(y>=subMap[0].length)||(x>=subMap.length)) return null;
		GridChild gc=new GridChild();
		gc.parent=this;
		gc.setID("");
		gc.setAreaID(this.getAreaID());
		gc.setDisplayText(displayText);
		gc.setDescription(description);
		int c=-1;
		if(displayTexts!=null)
		if(displayTexts.length>0)
		{
			c=Dice.roll(1,displayTexts.length,0)-1;
			gc.setDisplayText(displayTexts[c]);
		}
		if(descriptions!=null)
		if(descriptions.length>0)
		{
			if((c<0)||(c>descriptions.length))
				c=Dice.roll(1,descriptions.length,0)-1;
			gc.setDescription(descriptions[c]);
		}
		
		return gc;
	}
	
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		
		
		if((affect.targetCode()==affect.MOVE_ENTER)
		&&(affect.target()==this))
		{
			if(subMap==null)
				buildGrid();
			MOB mob=affect.source();
			if((mob.location()!=null)&&(mob.location().ID().length()>0))
			{
				Room altRoom=this.getAltRoomFrom(mob.location());
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
	
	protected class GridChild 
		extends StdRoom 
		implements GridLocaleChild
	{
		public Room parent=this;
		public GridChild()
		{
			super();
			if(parent!=null)
			{
				domainType=((Room)parent).domainType();
				domainCondition=((Room)parent).domainConditions();
				setBaseEnvStats(parent().baseEnvStats().cloneStats());
				recoverEnvStats();
			}
			myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		}
		public Environmental newInstance()
		{
			return new GridChild();
		}
		
		public boolean okAffect(Affect affect)
		{
			if(!super.okAffect(affect))
				return false;
			if(parent!=null)
				if(!parent.okAffect(affect))
					return false;
			return true;
		}
		
		public void affect(Affect affect)
		{
			super.affect(affect);
			if(parent!=null)
				parent.affect(affect);
		}
		
		
		public void affectEnvStats(Environmental affected, Stats affectableStats)
		{
			super.affectEnvStats(affected,affectableStats);
			if(parent!=null)
				parent.affectEnvStats(affected,affectableStats);
		}
		
		public void affectEnvStats(MOB affected, CharStats affectableStats)
		{
			super.affectCharStats(affected,affectableStats);
			if(parent!=null)
				parent.affectCharStats(affected,affectableStats);
		}
		
		public Room parent()
		{
			return parent;
		}
	}
}
