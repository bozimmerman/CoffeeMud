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
   Copyright 2001-2025 Bo Zimmerman

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
public class EndlessSky extends StdGrid
{
	@Override
	public String ID()
	{
		return "EndlessSky";
	}

	protected boolean crossLinked=false;

	public EndlessSky()
	{
		super();
		setMovementCost(1);
		recoverPhyStats();
		setDisplayText("Up in the sky");
		setDescription("");
		xsize=CMProps.getIntVar(CMProps.Int.SKYSIZE);
		ysize=CMProps.getIntVar(CMProps.Int.SKYSIZE);
		if(xsize<0)
			xsize=xsize*-1;
		if(ysize<0)
			ysize=ysize*-1;
		if((xsize==0)||(ysize==0))
		{
			xsize=3;
			ysize=3;
		}
	}

	@Override
	public int domainType()
	{
		return Room.DOMAIN_OUTDOORS_AIR;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		final boolean success = InTheAir.isOkAirAffect(this,msg);
		InTheAir.airAffects(this,msg);
		return success;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		InTheAir.airAffects(this,msg);
	}

	@Override
	public String getGridChildLocaleID()
	{
		return "InTheAir";
	}

	@Override
	protected Room findCenterRoom(final int dirCode)
	{
		if(dirCode!=Directions.DOWN)
			return super.findCenterRoom(dirCode);
		final Room[][] subMap=this.subMap;
		if(subMap != null)
			return subMap[subMap.length-1][subMap[0].length-1];
		return null;
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
					for (final Room[] element : subMap)
						linkRoom(element[0],dirRoom,d,dirExit,altExit);
						break;
					case Directions.SOUTH:
					for (final Room[] element : subMap)
						linkRoom(element[subMap[0].length-1],dirRoom,d,dirExit,altExit);
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
						linkRoom(subMap[0][0],dirRoom,d,dirExit,altExit);
						break;
					case Directions.DOWN:
						linkRoom(subMap[subMap.length-1][subMap[0].length-1],dirRoom,d,dirExit,altExit);
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
			final Exit ox=CMClass.getExit("Open");
			subMap=new Room[xsize][ysize];
			for(int x=0;x<subMap.length;x++)
			{
				for(int y=0;y<subMap[x].length;y++)
				{
					final Room newRoom=getGridRoom(x,y);
					if(newRoom!=null)
					{
						subMap[x][y]=newRoom;
						if((y>0)&&(subMap[x][y-1]!=null))
						{
							linkRoom(newRoom,subMap[x][y-1],Directions.NORTH,ox,ox);
							linkRoom(newRoom,subMap[x][y-1],Directions.UP,ox,ox);
						}
						if((x>0)&&(subMap[x-1][y]!=null))
							linkRoom(newRoom,subMap[x-1][y],Directions.WEST,ox,ox);

						if((y>0)&&(x>0)&&(subMap[x-1][y-1]!=null)&&(Directions.NORTHWEST<Directions.NUM_DIRECTIONS()))
							linkRoom(newRoom,subMap[x-1][y-1],Directions.NORTHWEST,ox,ox);

						if((y>0)&&(x<subMap.length-1)&&(subMap[x+1][y-1]!=null)&&(Directions.NORTHEAST<Directions.NUM_DIRECTIONS()))
							linkRoom(newRoom,subMap[x+1][y-1],Directions.NORTHEAST,ox,ox);

					}
				}
			}
			buildFinalLinks();
			if((subMap[0][0]!=null)
			&&(subMap[0][0].rawDoors()[Directions.UP]==null)
			&&(xsize>1))
				linkRoom(subMap[0][0],subMap[1][0],Directions.UP,ox,ox);
			for(int y=0;y<subMap[0].length;y++)
				linkRoom(subMap[0][y],subMap[subMap.length-1][y],Directions.WEST,ox,ox);
			for (final Room[] element : subMap)
				linkRoom(element[0],element[element.length-1],Directions.NORTH,ox,ox);
			for(int x=1;x<subMap.length;x++)
				linkRoom(subMap[x][0],subMap[x-1][subMap[x-1].length-1],Directions.UP,ox,ox);
			if(Directions.NORTHWEST<Directions.NUM_DIRECTIONS())
				linkRoom(subMap[0][0],subMap[subMap.length-1][subMap[0].length-1],Directions.NORTHWEST,ox,ox);
			if(Directions.NORTHEAST<Directions.NUM_DIRECTIONS())
				linkRoom(subMap[subMap.length-1][0],subMap[0][subMap[0].length-1],Directions.NORTHEAST,ox,ox);
		}
		catch(final Exception e)
		{
			clearGrid(null);
		}
	}
}
