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
   Copyright 2002-2018 Bo Zimmerman

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

public class StdMaze extends StdGrid
{
	@Override
	public String ID()
	{
		return "StdMaze";
	}

	public StdMaze()
	{
		super();
	}

	@Override
	protected Room getGridRoom(int x, int y)
	{
		final Room R=super.getGridRoom(x,y);
		if((R!=null)&&(!CMath.bset(R.phyStats().sensesMask(),PhyStats.SENSE_ROOMUNEXPLORABLE)))
		{
			R.basePhyStats().setSensesMask(R.basePhyStats().sensesMask()|PhyStats.SENSE_ROOMUNEXPLORABLE);
			R.phyStats().setSensesMask(R.phyStats().sensesMask()|PhyStats.SENSE_ROOMUNEXPLORABLE);
		}
		return R;
	}

	@Override
	protected Room findCenterRoom(int dirCode)
	{
		final Room dirRoom=rawDoors()[dirCode];
		if(dirRoom!=null)
		{
			final Room altR=super.findCenterRoom(dirCode);
			if(altR!=null)
			{
				final Exit ox=CMClass.getExit("Open");
				linkRoom(altR,dirRoom,dirCode,ox,ox);
				return altR;
			}
		}
		return null;
	}

	protected boolean goodDir(int x, int y, int dirCode)
	{
		if(dirCode==Directions.UP)
			return false;
		if(dirCode==Directions.DOWN)
			return false;
		if(dirCode>=Directions.GATE)
			return false;
		if((x==0)&&(dirCode==Directions.WEST))
			return false;
		if((y==0)&&(dirCode==Directions.NORTH))
			return false;
		if((x>=(subMap.length-1))&&(dirCode==Directions.EAST))
			return false;
		if((y>=(subMap[0].length-1))&&(dirCode==Directions.SOUTH))
			return false;
		return true;
	}

	protected Room roomDir(int x, int y, int dirCode)
	{
		if(!goodDir(x,y,dirCode))
			return null;
		return subMap[getX(x,dirCode)][getY(y,dirCode)];
	}

	protected int getY(int y, int dirCode)
	{
		switch(dirCode)
		{
		case Directions.NORTH:
			return y-1;
		case Directions.SOUTH:
			return y+1;
		}
		return y;
	}

	protected int getX(int x, int dirCode)
	{
		switch(dirCode)
		{
		case Directions.EAST:
			return x+1;
		case Directions.WEST:
			return x-1;
		}
		return x;
	}

	protected void mazify(Set<Room> visited, int x, int y)
	{
		if(visited.contains(subMap[x][y]))
			return;
		final Room room=subMap[x][y];
		visited.add(room);
		final Exit ox=CMClass.getExit("Open");

		boolean okRoom=true;
		while(okRoom)
		{
			okRoom=false;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if(d==Directions.GATE)
					continue;
				final Room possRoom=roomDir(x,y,d);
				if(possRoom!=null)
				{
					if(!visited.contains(possRoom))
					{
						okRoom=true;
						break;
					}
				}
			}
			if(okRoom)
			{
				Room goRoom=null;
				int dirCode=-1;
				while(goRoom==null)
				{
					final int d=CMLib.dice().roll(1,Directions.NUM_DIRECTIONS(),0)-1;
					final Room possRoom=roomDir(x,y,d);
					if(possRoom!=null)
					{
						if(!visited.contains(possRoom))
						{
							goRoom=possRoom;
							dirCode=d;
						}
					}
				}
				linkRoom(room,goRoom,dirCode,ox,ox);
				mazify(visited,getX(x,dirCode),getY(y,dirCode));
			}
		}
	}

	protected void buildMaze()
	{
		final Set<Room> visited=new HashSet<Room>();
		final int x=xsize/2;
		final int y=ysize/2;
		mazify(visited,x,y);
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
						subMap[x][y]=newRoom;
				}
			}
			buildMaze();
			buildFinalLinks();
		}
		catch(final Exception e)
		{
			clearGrid(null);
		}
	}
}
