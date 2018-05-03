package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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
public class Whirlpool extends StdGrid
{
	@Override
	public String ID()
	{
		return "Whirlpool";
	}

	public Whirlpool()
	{
		super();
		basePhyStats.setWeight(30);
		recoverPhyStats();
		climask=Places.CLIMASK_NORMAL;
	}

	@Override
	public int domainType()
	{
		return Room.DOMAIN_OUTDOORS_WATERSURFACE;
	}

	@Override
	public String getGridChildLocaleID()
	{
		return "WaterSurface";
	}
	
	protected boolean linkRoomOpen(int[] curXY, int dir)
	{
		if((subMap != null)
		&&(curXY[0] >=0)&&(curXY[0]<xsize)
		&&(curXY[1] >=0)&&(curXY[1]<ysize)
		&&(dir>=0))
		{
			Room fromRoom = subMap[curXY[0]][curXY[1]];
			Room toRoom = null;
			int[] testXY = Arrays.copyOf(curXY,curXY.length);
			switch(dir)
			{
			case Directions.NORTH:
				if(curXY[1]>0)
				{
					testXY[1]--;
					toRoom=subMap[testXY[0]][testXY[1]];
				}
				break;
			case Directions.SOUTH:
				if(curXY[1]<ysize-1)
				{
					testXY[1]++;
					toRoom=subMap[testXY[0]][testXY[1]];
				}
				break;
			case Directions.EAST:
				if(curXY[0]<xsize-1)
				{
					testXY[0]++;
					toRoom=subMap[testXY[0]][testXY[1]];
				}
				break;
			case Directions.WEST:
				if(curXY[0]>0)
				{
					testXY[0]--;
					toRoom=subMap[testXY[0]][testXY[1]];
				}
				break;
			}
			if(toRoom != null)
			{
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					if(toRoom.getRawExit(d)!=null)
						return false;
				}
				Behavior currents=CMClass.getBehavior("WaterCurrents");
				if(currents != null)
				{
					currents.setParms("minticks=1 maxticks=2 chance=75 "+CMLib.directions().getDirectionName(dir));
					currents.setSavable(true);
					fromRoom.addBehavior(currents);
					fromRoom.setDescription(fromRoom.description() +L("%0D^HThe swirling waters are pulling you "+CMLib.directions().getDirectionName(dir)+".^?"));
				}
				curXY[0]=testXY[0];
				curXY[1]=testXY[1];
				Exit o=CMClass.getExit("Open");
				this.linkRoom(fromRoom, toRoom, dir, o, o);
				return true;
			}
		}
		return false;
	}
	
	protected void buildWhirl()
	{
		int[] curXY=new int[]{xsize-1,0};
		int dirs[]=new int[]{Directions.WEST,Directions.SOUTH,Directions.EAST,Directions.NORTH};
		int num=xsize-1;
		Room lastRoom=null;
		while(num>0)
		{
			for(int dir : dirs)
			{
				for(int i=0;i<num;i++)
				{
					if(!linkRoomOpen(curXY,dir))
					{
						num--;
						break;
					}
					else
					{
						lastRoom=subMap[curXY[0]][curXY[1]];
					}
				}
			}
		}
		int dir = Directions.DOWN;
		while(lastRoom != null)
		{
			lastRoom.giveASky(0);
			Room downRoom = lastRoom.getRoomInDir(dir);
			if(downRoom != null)
			{
				Behavior currents=CMClass.getBehavior("WaterCurrents");
				if(currents != null)
				{
					currents.setParms("minticks=1 maxticks=2 chance=75 BOATS "+CMLib.directions().getDirectionName(dir));
					currents.setSavable(true);
					lastRoom.addBehavior(currents);
					lastRoom.setDescription(lastRoom.description() +L("%0D^HThe swirling waters are pulling you "+CMLib.directions().getDirectionName(dir)+".^?"));
				}
			}
			lastRoom=downRoom;
		}
	}
	
	@Override
	protected void buildFinalLinks()
	{
		// ignore what the base class does..
		if(subMap!=null)
		{
			final Room theEntryRoom = subMap[xsize-1][0];
			for(int dir=0;dir<Directions.NUM_DIRECTIONS();dir++)
			{
				final Exit ox=CMClass.getExit("Open");
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					if(d==Directions.GATE)
						continue;
					final Room dirRoom=rawDoors()[d];
					Exit dirExit=getRawExit(d);
					if((dirExit==null)||(dirExit.hasADoor()))
						dirExit=ox;
					if(dirRoom!=null)
					{
						Exit altExit=dirRoom.getRawExit(Directions.getOpDirectionCode(d));
						if(altExit==null)
							altExit=ox;
						switch(d)
						{
						case Directions.NORTH:
						case Directions.SOUTH:
						case Directions.NORTHEAST:
						case Directions.NORTHWEST:
						case Directions.SOUTHEAST:
						case Directions.SOUTHWEST:
						case Directions.EAST:
						case Directions.WEST:
							linkRoom(theEntryRoom,dirRoom,d,dirExit,altExit);
							break;
						}
					}
				}
			}
		}
	}
	
	@Override
	public void buildGrid()
	{
		clearGrid(null);
		try
		{
			subMap=new Room[xsize][ysize];
			for(int x=0;x<subMap.length;x++)
			{
				for(int y=0;y<subMap[x].length;y++)
				{
					final Room newRoom=getGridRoom(x,y);
					if(newRoom!=null)
					{
						subMap[x][y]=newRoom;
					}
				}
			}
			
			buildWhirl();
			buildFinalLinks();
		}
		catch(final Exception e)
		{
			clearGrid(null);
		}
	}
}
