package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.application.*;

import java.util.*;

public class EndlessSky extends Grid
{
	public EndlessSky()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_AIR;
		domainCondition=Room.CONDITION_NORMAL;
		setDisplayText("Up in the sky");
		setDescription("");
		size=3;
	}
	public Environmental newInstance()
	{
		return new EndlessSky();
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		
		if((affect.targetCode()==affect.MOVE_ENTER)
		&&(affect.target() instanceof Room)
		&&((affect.target()==this)
		   ||(affect.target() instanceof GridChild)))
		{
			MOB mob=affect.source();
			Room room=(Room)affect.target();
			if(mob.location()!=room.getRoom(Directions.UP))
				if(!Sense.isFlying(mob))
				{
					mob.tell("You can't fly.");
					return false;
				}
		}
		return true;
	}
	
	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affect.target() instanceof Item)&&(!Sense.isFlying(affect.target())&&(affect.targetCode()==Affect.HANDS_DROP))&&(affect.source()!=null)&&(affect.source().location()!=null))
			Falling.startFalling(affect.target(),affect.source().location());
		else
		if(this.isInhabitant(affect.source())
		   ||((affect.source().location()!=null)&&(affect.source().location() instanceof GridChild)))
		{
			MOB mob=affect.source();
			if((!Sense.isFlying(mob))
			&&(getRoom(Directions.DOWN)!=null)
			&&(getExit(Directions.DOWN)!=null)
			&&(getExit(Directions.DOWN).isOpen()))
			{
				Falling.startFalling(mob,null);
			}
		}
	}
	
	protected void buildFinalLinks()
	{
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room dirRoom=this.doors()[d];
			if(dirRoom!=null)
			{
				if(d!=Directions.DOWN)
					alts[d]=findCenterRoom(d);
				else
					alts[d]=subMap[subMap.length-1][subMap[0].length-1];

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
						linkRoom(subMap[0][0],dirRoom,d);
						break;
					case Directions.DOWN:
						linkRoom(subMap[subMap.length-1][subMap[0].length-1],dirRoom,d);
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
					{
						linkRoom(newRoom,subMap[x][y-1],Directions.NORTH);
						linkRoom(newRoom,subMap[x][y-1],Directions.UP);
					}
					
					if((x>0)&&(subMap[x-1][y]!=null))
						linkRoom(newRoom,subMap[x-1][y],Directions.WEST);
					MUD.map.addElement(newRoom);
				}
			}
		buildFinalLinks();
		if((subMap[0][0]!=null)&&(subMap[0][0].doors()[Directions.UP]==null))
			linkRoom(subMap[0][0],subMap[1][0],Directions.UP);
		for(int y=0;y<subMap[0].length;y++)
			linkRoom(subMap[0][y],subMap[subMap.length-1][y],Directions.WEST);
		for(int x=0;x<subMap.length;x++)
			linkRoom(subMap[x][0],subMap[x][subMap.length-1],Directions.NORTH);
		for(int x=1;x<subMap.length;x++)
			linkRoom(subMap[x][0],subMap[x-1][subMap.length-1],Directions.UP);
	}
}
