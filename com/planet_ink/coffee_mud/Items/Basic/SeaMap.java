package com.planet_ink.coffee_mud.Items.Basic;
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
import com.planet_ink.coffee_mud.Items.Basic.StdMap.MapRoom;
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

public class SeaMap extends BardMap
{
	@Override
	public String ID()
	{
		return "SeaMap";
	}

	public SeaMap()
	{
		super();
	}

	public char roomChar(Room room)
	{
		if(room==null)
			return ' ';
		switch(room.domainType())
		{
		case Room.DOMAIN_OUTDOORS_CITY:
			return '=';
		case Room.DOMAIN_OUTDOORS_WOODS:
			return 'T';
		case Room.DOMAIN_OUTDOORS_ROCKS:
			return ':';
		case Room.DOMAIN_OUTDOORS_PLAINS:
			return '_';
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
			return '~';
		case Room.DOMAIN_OUTDOORS_AIR:
			return ' ';
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			return '~';
		case Room.DOMAIN_OUTDOORS_JUNGLE:
			return 'J';
		case Room.DOMAIN_OUTDOORS_SEAPORT:
			return 'P';
		case Room.DOMAIN_OUTDOORS_SWAMP:
			return 'x';
		case Room.DOMAIN_OUTDOORS_DESERT:
			return '.';
		case Room.DOMAIN_OUTDOORS_HILLS:
			return 'h';
		case Room.DOMAIN_OUTDOORS_MOUNTAINS:
			return 'M';
		case Room.DOMAIN_OUTDOORS_SPACEPORT:
			return '@';
		case Room.DOMAIN_INDOORS_UNDERWATER:
			return '~';
		case Room.DOMAIN_INDOORS_AIR:
			return ' ';
		case Room.DOMAIN_INDOORS_WATERSURFACE:
			return '~';
		case Room.DOMAIN_INDOORS_STONE:
		case Room.DOMAIN_INDOORS_WOOD:
		case Room.DOMAIN_INDOORS_CAVE:
		case Room.DOMAIN_INDOORS_MAGIC:
		case Room.DOMAIN_INDOORS_METAL:
			return '#';
		default:
			return '?';
		}
	}
	
	@Override
	public StringBuffer[][] finishMapMaking(int width)
	{
		final Hashtable<Room,MapRoom> mapRooms=makeMapRooms(width);
		StringBuffer[][] map=new StringBuffer[0][0];
		if(mapRooms.size()>0)
		{
			placeRooms(mapRooms);
			final MapRoom[][] grid=rebuildGrid(mapRooms);
			if((grid.length==0)||(grid[0].length==0))
				return map;
			final int numXSquares=(int)Math.round(Math.floor(CMath.div(width-6,8)));
			final int numYSquares=((numXSquares/2)+1);
			final int xsize=grid.length/numXSquares;
			final int ysize=grid[0].length/numYSquares;
			if((xsize<0)||(ysize<0))
			{
				Log.errOut("StdMap","Error finishing " + xsize +"/"+ ysize+"/"+width);
				return map;
			}

			map=new StringBuffer[xsize+1][ysize+1];
			StringBuilder line1=new StringBuilder("");
			StringBuilder line2=new StringBuilder("");
			StringBuilder line3=new StringBuilder("");
			for(int y=0;y<grid[0].length;y++)
			{
				line1.setLength(0);
				line2.setLength(0);
				line3.setLength(0);
				final int ycoord=y/numYSquares;
				int lastX=-1;
				int xcoord=-1;
				for(int x=0;x<grid.length;x++)
				{
					xcoord=x/9;
					if(xcoord!=lastX)
					{
						if(lastX>=0)
						{
							if(map[lastX][ycoord]==null)
								map[lastX][ycoord]=new StringBuffer("");
							map[lastX][ycoord].append(line1.toString()+"\n\r"+line2.toString()+"\n\r"+line3.toString()+"\n\r");
						}
						lastX=xcoord;
						line1.setLength(0);
						line2.setLength(0);
						line3.setLength(0);
					}
					final MapRoom room=grid[x][y];
					if(room==null)
					{
						line1.append("   ");
						line2.append("   ");
						line3.append("   ");
					}
					else
					{
						final char roomC=roomChar(room.r);
						line1.append(" ").append(dirChar(Directions.NORTH,grid,x,y,' ')).append(" ");
						line2.append(dirChar(Directions.WEST,grid,x,y,' '))
							 .append(roomC).append(dirChar(Directions.EAST,grid,x,y,' '));
						line3.append(" ").append(dirChar(Directions.SOUTH,grid,x,y,' ')).append(" ");
					}
				}
				if(xcoord>=0)
				{
					if(map[xcoord][ycoord]==null)
						map[xcoord][ycoord]=new StringBuffer("");
					map[xcoord][ycoord].append(line1.toString()+"\n\r"+line2.toString()+"\n\r"+line3.toString()+"\n\r");
				}
			}
		}
		for(int x=0;x<map.length;x++)
		{
			for(int y=0;y<map[x].length;y++)
			{
				if(map[x][y]==null)
					map[x][y]=new StringBuffer("");
			}
		}
		return map;
	}
}
