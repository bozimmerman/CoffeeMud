package com.planet_ink.coffee_mud.Areas;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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
public class StdGridArea extends StdArea implements Area, GridZones {

	@Override
	public String ID()
	{
		return "StdGridArea";
	}

	public final static String ZEROES="0000000000";
	protected int xSize=100;
	protected int ySize=100;
	protected int yLength=3;

	@Override
	public CMObject newInstance()
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.FATAREAS)
		&&(ID().equals("StdGridArea")))
		{
			final Area A=CMClass.getAreaType("StdThinGridArea");
			if(A!=null)
				return A;
		}
		return super.newInstance();
	}

	@Override
	public String getNewRoomID(Room startRoom, int direction)
	{
		final XYVector xy=posFromRoomID(startRoom);
		int xChange=0;
		int yChange=0;
		switch(direction)
		{
		case Directions.NORTH: yChange-=1; break;
		case Directions.SOUTH: yChange+=1; break;
		case Directions.EAST: xChange+=1; break;
		case Directions.WEST: xChange-=1; break;
		case Directions.NORTHWEST: yChange-=1; xChange-=1; break;
		case Directions.NORTHEAST: yChange-=1; xChange+=1; break;
		case Directions.SOUTHWEST: yChange+=1; xChange-=1; break;
		case Directions.SOUTHEAST: yChange+=1; xChange+=1; break;
		default: break;
		}
		if(isMyGridChild(startRoom))
		{
			// PERFECT condition
			if((xy!=null)&&(xy.x>=0))
			{
				String newID=roomIDFromPos(xy.x+xChange,xy.y+yChange);
				if((newID!=null)&&(newID.length()>0))
				{
					newID=Name()+"#"+newID;
					final Room duplicateRoom=getRoom(newID);
					if((duplicateRoom==null)
					||(duplicateRoom==startRoom))
						return newID;
				}
			}
		}
		// next in line
		for(int x=0;x<xGridSize();x++)
		{
			for(int y=0;y<yGridSize();y++)
			{
				final Room R=getRoom(Name()+"#"+roomIDFromPos(x,y));
				if(R==null)
					return Name()+"#"+roomIDFromPos(x,y);
			}
		}
		// not even a next in line exists!
		return "";
	}

	protected String roomIDFromPos(int x, int y)
	{
		if((x<0)||(y<0)||(y>=yGridSize())||(x>=xGridSize()))
			return null;
		final String s=Integer.toString(y);
		if(x>0)
			return x+(ZEROES.substring(ZEROES.length()-(yLength-s.length())))+s;
		return s;
	}

	@Override
	public Room getGridChild(int x, int y)
	{
		final String roomID=roomIDFromPos(x,y);
		if(roomID==null)
			return null;
		return getRoom(Name()+"#"+roomID);
	}

	@Override
	public XYVector getRoomXY(String roomID)
	{
		if(roomID.length()==0)
			return null;
		if(roomID.endsWith(")"))
		{
			final int y=roomID.lastIndexOf("#(");
			if(y>0)
				roomID=roomID.substring(0,y);
		}
		final int x=roomID.indexOf('#');
		if(x<0)
			return null;
		if(!roomID.substring(0,x).equalsIgnoreCase(Name()))
			return null;
		roomID=roomID.substring(x+1);
		if(!CMath.isNumber(roomID))
			return null;
		final int len=(""+ySize).length();
		if(roomID.length()<=len)
			return new XYVector(0,CMath.s_int(roomID));
		final String xStr=roomID.substring(0,roomID.length()-len);
		String yStr=roomID.substring(roomID.length()-len);
		while(yStr.startsWith("0"))
			yStr=yStr.substring(1);
		final XYVector xy = new XYVector(CMath.s_int(xStr),CMath.s_int(yStr));
		if((xy.x<0)||(xy.y<0)||(xy.x>=xGridSize())||(xy.y>=yGridSize()))
			return null;
		return xy;
	}

	@Override
	public XYVector getRoomXY(Room room)
	{
		return posFromRoomID(room);
	}

	protected XYVector posFromRoomID(Room loc)
	{
		if(loc==null)
			return null;
		String roomID=loc.roomID();
		if(roomID.length()==0)
			roomID=getGridChildCode(loc);
		return getRoomXY(roomID);
	}

	@Override
	public int getGridChildX(Room loc)
	{
		return posFromRoomID(loc).x;
	}

	@Override
	public int getGridChildY(Room loc)
	{
		return posFromRoomID(loc).y;
	}

	@Override
	public String getGridChildCode(Room loc)
	{
		return CMLib.map().getExtendedRoomID(loc);
	}

	@Override
	public Room getRandomGridChild()
	{
		return super.getRandomProperRoom();
	}

	@Override
	public Room getGridChild(String childCode)
	{
		return CMLib.map().getRoom(childCode);
	}

	@Override
	public boolean isMyGridChild(Room loc)
	{
		return super.isRoom(loc);
	}

	@Override
	public int xGridSize()
	{
		return xSize;
	}

	@Override
	public int yGridSize()
	{
		return ySize;
	}

	@Override
	public void setXGridSize(int x)
	{
		xSize=x;
	}

	@Override
	public void setYGridSize(int y)
	{
		ySize=y;
		yLength=Integer.toString(ySize).length();
	}

	@Override
	public Room getGridChild(XYVector xy)
	{
		if(xy==null)
			return null;
		return getGridChild(xy.x,xy.y);
	}
}
