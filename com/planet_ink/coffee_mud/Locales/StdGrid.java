package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Directions;
import com.planet_ink.coffee_mud.utils.Dice;
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
		this.descriptions=new String[numDs+1];
		x=newDescription.indexOf("<P>");
		numDs=0;
		while(x>=0)
		{
			this.descriptions[numDs]=newDescription.substring(0,x);
			numDs++;
			newDescription=newDescription.substring(x+3);
			x=newDescription.indexOf("<P>");
		}
		this.descriptions[numDs]=newDescription;
		if(numDs>0)
			this.description=this.descriptions[0];
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
		this.displayTexts=new String[numDs+1];
		x=newDisplayText.indexOf("<P>");
		numDs=0;
		while(x>=0)
		{
			this.displayTexts[numDs]=newDisplayText.substring(0,x);
			numDs++;
			newDisplayText=newDisplayText.substring(x+3);
			x=newDisplayText.indexOf("<P>");
		}
		this.displayTexts[numDs]=newDisplayText;
		if(numDs>0)
			this.displayText=this.displayTexts[0];
	}

	public Room getAltRoomFrom(Room loc)
	{
		int oldDirCode=-1;
		if(loc==null) return null;

		if(subMap==null) buildGrid();
		
		if(loc instanceof GridLocaleChild)
			loc=((GridLocaleChild)loc).parent();

		for(int x=0;x<Directions.NUM_DIRECTIONS;x++)
			if((doors[x]!=null)&&(doors[x]==loc))
			{
				oldDirCode=x;
				break;
			}
		if(oldDirCode<0) return null;

		return this.alts[oldDirCode];
	}

	protected static void linkRoom(Room room, Room loc, int dirCode)
	{
		if(loc==null) return;
		if(room==null) return;
		int opCode=Directions.getOpDirectionCode(dirCode);
		Exit o=(Exit)CMClass.getExit("Open").newInstance();
		if(room.rawDoors()[dirCode]!=null) return;
		room.rawDoors()[dirCode]=loc;
		room.rawExits()[dirCode]=o;
		if(loc.rawDoors()[opCode]!=null) return;
		loc.rawDoors()[opCode]=room;
		loc.rawExits()[opCode]=o;
	}

	public Room findCenterRoom(int dirCode)
	{
		int x=0;
		int y=0;
		switch(dirCode)
		{
		case Directions.NORTH:
			x=this.subMap.length/2;
			break;
		case Directions.SOUTH:
			x=this.subMap.length/2;
			y=this.subMap[0].length-1;
			break;
		case Directions.EAST:
			x=this.subMap.length-1;
			y=this.subMap[0].length/2;
			break;
		case Directions.WEST:
			y=this.subMap[0].length/2;
			break;
		case Directions.UP:
		case Directions.DOWN:
			x=this.subMap.length/2;
			y=this.subMap[0].length/2;
			break;
		}
		Room returnRoom=null;
		int xadjust=0;
		int yadjust=0;
		try
		{
			while(returnRoom==null)
			{
				if(this.subMap[x+xadjust][y+yadjust]!=null)
					returnRoom=this.subMap[x+xadjust][y+yadjust];
				else
				if(this.subMap[x-xadjust][y-yadjust]!=null)
					returnRoom=this.subMap[x-xadjust][y-yadjust];
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
			Room dirRoom=this.rawDoors()[d];
			if(dirRoom!=null)
			{
				this.alts[d]=this.findCenterRoom(d);
				switch(d)
				{
					case Directions.NORTH:
						for(int x=0;x<this.subMap.length;x++)
							this.linkRoom(this.subMap[x][0],dirRoom,d);
						break;
					case Directions.SOUTH:
						for(int x=0;x<this.subMap.length;x++)
							this.linkRoom(this.subMap[x][this.subMap[0].length-1],dirRoom,d);
						break;
					case Directions.EAST:
						for(int y=0;y<this.subMap[0].length;y++)
							this.linkRoom(this.subMap[this.subMap.length-1][y],dirRoom,d);
						break;
					case Directions.WEST:
						for(int y=0;y<this.subMap[0].length;y++)
							this.linkRoom(this.subMap[0][y],dirRoom,d);
						break;
					case Directions.UP:
						for(int x=0;x<this.subMap.length;x++)
							for(int y=0;y<this.subMap[0].length;y++)
								this.linkRoom(this.subMap[x][y],dirRoom,d);
						break;
					case Directions.DOWN:
						for(int x=0;x<this.subMap.length;x++)
							for(int y=0;y<this.subMap[0].length;y++)
								this.linkRoom(this.subMap[x][y],dirRoom,d);
						break;
				}
			}
		}
	}

	public void buildGrid()
	{
		this.clearGrid();
		this.subMap=new Room[this.size][this.size];
		for(int x=0;x<this.size;x++)
			for(int y=0;y<this.size;y++)
			{
				Room newRoom=getGridRoom(x,y);
				if(newRoom!=null)
				{
					this.subMap[x][y]=newRoom;
					if((y>0)&&(this.subMap[x][y-1]!=null))
						this.linkRoom(newRoom,this.subMap[x][y-1],Directions.NORTH);
					if((x>0)&&(this.subMap[x-1][y]!=null))
						this.linkRoom(newRoom,this.subMap[x-1][y],Directions.WEST);
					CMMap.map.addElement(newRoom);
				}
			}
		this.buildFinalLinks();
	}

	public void clearGrid()
	{
		if(this.subMap!=null)
		{
			for(int x=0;x<this.subMap.length;x++)
				for(int y=0;y<this.subMap[x].length;y++)
				{
					Room room=this.subMap[x][y];
					CMMap.map.remove(room);
				}
			this.subMap=null;
		}
		this.alts=new Room[Directions.NUM_DIRECTIONS];
	}

	protected Room getGridRoom(int x, int y)
	{
		if((x<0)||(y<0)||(y>=this.subMap[0].length)||(x>=this.subMap.length)) return null;
		GridChild gc=new GridChild();
		gc.parent=this;
		gc.setID("");
		gc.setArea(this.getArea());
		gc.setDisplayText(this.displayText);
		gc.setDescription(this.description);
		gc.domainCondition=this.domainCondition;
		gc.domainType=this.domainType;
		int c=-1;
		if(this.displayTexts!=null)
		if(this.displayTexts.length>0)
		{
			c=Dice.roll(1,this.displayTexts.length,0)-1;
			gc.setDisplayText(this.displayTexts[c]);
		}
		if(this.descriptions!=null)
		if(this.descriptions.length>0)
		{
			if((c<0)||(c>this.descriptions.length))
				c=Dice.roll(1,this.descriptions.length,0)-1;
			gc.setDescription(this.descriptions[c]);
		}

		return gc;
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;


		if((affect.targetMinor()==affect.TYP_ENTER)
		&&(affect.target()==this))
		{
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
				this.domainType=((Room)parent).domainType();
				this.domainCondition=((Room)parent).domainConditions();
				if(parent.baseEnvStats()!=null)
				{
					this.setBaseEnvStats(parent.baseEnvStats().cloneStats());
					this.recoverEnvStats();
				}
			}
			this.myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
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


		public void affectEnvStats(Environmental affected, EnvStats affectableStats)
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
