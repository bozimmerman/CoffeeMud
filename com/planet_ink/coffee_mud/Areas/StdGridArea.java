package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

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

	public String ID(){	return "StdGridArea";}
	public final static String ZEROES="0000000000";
	protected int xSize=100;
	protected int ySize=100;
	protected int yLength=3;

	public CMObject newInstance()
	{
		if(CMSecurity.isDisabled("FATAREAS")
		&&(ID().equals("StdGridArea")))
		{
			Area A=CMClass.getAreaType("StdThinGridArea");
			if(A!=null) return A;
		}
		return super.newInstance();
	}

	public String getNewRoomID(Room startRoom, int direction)
	{
		int[] xy=posFromRoomID(startRoom);
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
			if((xy!=null)&&(xy[0]>=0))
			{
				String newID=roomIDFromPos(xy[0]+xChange,xy[1]+yChange);
				if((newID!=null)&&(newID.length()>0))
				{
					newID=Name()+"#"+newID;
					Room duplicateRoom=getRoom(newID);
					if((duplicateRoom==null)
					||(duplicateRoom==startRoom))
						return newID;
				}
			}
		}
		// next in line
		for(int x=0;x<xGridSize();x++)
			for(int y=0;y<yGridSize();y++)
			{
				Room R=getRoom(Name()+"#"+roomIDFromPos(x,y));
				if(R==null) return Name()+"#"+roomIDFromPos(x,y);
			}
		// not even a next in line exists!
		return "";
	}

	protected String roomIDFromPos(int x, int y)
	{
		if((x<0)||(y<0)||(y>=yGridSize())||(x>=xGridSize())) return null;
		String s=Integer.toString(y);
		if(x>0) return x+(ZEROES.substring(ZEROES.length()-(yLength-s.length())))+s;
		return s;
	}
	public Room getGridChild(int x, int y)
	{
		String roomID=roomIDFromPos(x,y);
		if(roomID==null) return null;
		return getRoom(Name()+"#"+roomID);
	}
	public int[] getRoomXY(String roomID)
	{
		int[] xy={-1,-1};
		if(roomID.length()==0) return null;
		if(roomID.endsWith(")"))
		{
			int y=roomID.lastIndexOf("#(");
			if(y>0) roomID=roomID.substring(0,y);
		}
		int x=roomID.indexOf("#");
		if(x<0) return null;
		if(!roomID.substring(0,x).equalsIgnoreCase(Name())) return null;
		roomID=roomID.substring(x+1);
		if(!CMath.isNumber(roomID)) return null;
		int len=(""+ySize).length();
		if(roomID.length()<=len)
		{
			xy[0]=0;
			xy[1]=CMath.s_int(roomID);
			return xy;
		}
		String xStr=roomID.substring(0,roomID.length()-len);
		String yStr=roomID.substring(roomID.length()-len);
		while(yStr.startsWith("0")) yStr=yStr.substring(1);
		xy[0]=CMath.s_int(xStr);
		xy[1]=CMath.s_int(yStr);
		if((xy[0]<0)||(xy[1]<0)||(xy[1]>=xGridSize())||(xy[0]>=yGridSize()))
			return null;
		return xy;
	}
	protected int[] posFromRoomID(Room loc)
	{
		if(loc==null) return null;
		String roomID=loc.roomID();
		if(roomID.length()==0) roomID=getGridChildCode(loc);
		return getRoomXY(roomID);
	}
	public int getGridChildX(Room loc){return posFromRoomID(loc)[0];}
	public int getGridChildY(Room loc){return posFromRoomID(loc)[1];}
	public String getGridChildCode(Room loc){ return CMLib.map().getExtendedRoomID(loc);}
	public Room getRandomGridChild(){ return super.getRandomProperRoom();}
	public Room getGridChild(String childCode){return CMLib.map().getRoom(childCode);}
	public boolean isMyGridChild(Room loc){ return super.isRoom(loc);}
	public int xGridSize(){ return xSize;}
	public int yGridSize(){ return ySize;}
	public void setXGridSize(int x){xSize=x;}
	public void setYGridSize(int y){ySize=y; yLength=Integer.toString(ySize).length();}
}
