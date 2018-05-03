package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.GridZones.XYVector;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.GridLocale.CrossExit;
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
public class UnderWaterColumnGrid extends UnderWaterGrid
{
	@Override
	public String ID()
	{
		return "UnderWaterColumnGrid";
	}

	public UnderWaterColumnGrid()
	{
		super();
	}

	@Override
	protected void buildFinalLinks()
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
						for(int x=0;x<subMap.length;x++)
						{
							for(int y=0;y<subMap.length;y++)
								linkRoom(subMap[x][y],dirRoom,d,dirExit,altExit);
						}
						break;
					case Directions.SOUTH:
						for(int x=0;x<subMap.length;x++)
						{
							for(int y=0;y<subMap.length;y++)
								linkRoom(subMap[x][y],dirRoom,d,dirExit,altExit);
						}
						break;
					case Directions.EAST:
						for(int x=0;x<subMap.length;x++)
						{
							for(int y=0;y<subMap.length;y++)
								linkRoom(subMap[x][y],dirRoom,d,dirExit,altExit);
						}
						break;
					case Directions.WEST:
						for(int x=0;x<subMap.length;x++)
						{
							for(int y=0;y<subMap.length;y++)
								linkRoom(subMap[x][y],dirRoom,d,dirExit,altExit);
						}
						break;
					case Directions.UP:
						linkRoom(subMap[0][0],dirRoom,d,dirExit,altExit);
						break;
					case Directions.DOWN:
						linkRoom(subMap[subMap.length-1][subMap[0].length-1],dirRoom,d,dirExit,altExit);
						break;
					case Directions.NORTHEAST:
						for(int x=0;x<subMap.length;x++)
						{
							for(int y=0;y<subMap.length;y++)
								linkRoom(subMap[x][y],dirRoom,d,dirExit,altExit);
						}
						break;
					case Directions.NORTHWEST:
						for(int x=0;x<subMap.length;x++)
						{
							for(int y=0;y<subMap.length;y++)
								linkRoom(subMap[x][y],dirRoom,d,dirExit,altExit);
						}
						break;
					case Directions.SOUTHEAST:
						for(int x=0;x<subMap.length;x++)
						{
							for(int y=0;y<subMap.length;y++)
								linkRoom(subMap[x][y],dirRoom,d,dirExit,altExit);
						}
						break;
					case Directions.SOUTHWEST:
						for(int x=0;x<subMap.length;x++)
						{
							for(int y=0;y<subMap.length;y++)
								linkRoom(subMap[x][y],dirRoom,d,dirExit,altExit);
						}
						break;
				}
			}
		}
	}

	@Override
	public Room getAltRoomFrom(Room loc, int direction)
	{
		if((loc==null)||(direction<0))
			return null;
		final int opDirection=Directions.getOpDirectionCode(direction);

		getBuiltGrid();
		Room[][] grid=null;
		if(gridexits.size()>0)
		{
			grid=getBuiltGrid();
			final String roomID=CMLib.map().getExtendedRoomID(loc);
			if(grid!=null)
			{
				for(int d=0;d<gridexits.size();d++)
				{
					final CrossExit EX=gridexits.get(d);
					if((!EX.out)
					&&(EX.destRoomID.equalsIgnoreCase(roomID))
					&&(EX.dir==direction)
					&&(EX.x>=0)&&(EX.y>=0)&&(EX.x<xGridSize())&&(EX.y<yGridSize())
					&&(grid[EX.x][EX.y]!=null))
						return grid[EX.x][EX.y];
				}
			}
		}

		final Room oldLoc=loc;
		if(loc.getGridParent()!=null)
			loc=loc.getGridParent();
		if((oldLoc!=loc)&&(loc instanceof GridLocale))
		{
			if(grid==null)
				grid=getBuiltGrid();
			if(grid!=null)
			{
				final XYVector xy=((GridLocale)loc).getRoomXY(oldLoc);
				if((xy!=null)&&(xy.x>=0)&&(xy.y>=0))
				{
					switch(opDirection)
					{
					case Directions.UP:
						return grid[grid.length-1][grid[0].length-1];
					case Directions.DOWN:
						return grid[0][0];
					case Directions.GATE:
						break;
					default:
					{
						int otherX=xy.x;
						int otherY=xy.y;
						if(otherX >= xGridSize())
							otherX=xGridSize()-1;
						if(otherY >= yGridSize())
							otherY=yGridSize()-1;
						return grid[otherX][otherY];
					}
					}
				}
			}
		}
		return findCenterRoom(opDirection);
	}

	@Override
	public void buildGrid()
	{
		clearGrid(null);
		try
		{
			subMap=new Room[xsize][ysize];
			final Exit ox=CMClass.getExit("Open");
			for(int x=0;x<subMap.length;x++)
			{
				for(int y=0;y<subMap[x].length;y++)
				{
					final Room newRoom=getGridRoom(x,y);
					if(newRoom!=null)
					{
						subMap[x][y]=newRoom;
						if((x>0)||(y>0))
						{
							int nextX = x-1;
							int nextY = y;
							if(nextX <0)
							{
								nextX = subMap.length-1;
								nextY--;
							}
							if(subMap[nextX][nextY]!=null)
								linkRoom(newRoom,subMap[nextX][nextY],Directions.UP,ox,ox);
						}
					}
				}
			}
			buildFinalLinks();
		}
		catch(final Exception e)
		{
			clearGrid(null);
		}
	}
}
