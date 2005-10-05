package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
/* 
   Copyright 2000-2005 Bo Zimmerman

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
	public String ID(){return "StdMaze";}
	public StdMaze()
	{
		super();
	}


    protected Room getGridRoom(int x, int y)
    {
        Room R=super.getGridRoom(x,y);
        if((R!=null)&&(!Util.bset(R.envStats().sensesMask(),EnvStats.SENSE_ROOMUNEXPLORABLE)))
        {
            R.baseEnvStats().setSensesMask(R.baseEnvStats().sensesMask()|EnvStats.SENSE_ROOMUNEXPLORABLE);
            R.envStats().setSensesMask(R.envStats().sensesMask()|EnvStats.SENSE_ROOMUNEXPLORABLE);
        }
        return R;
    }
    protected Room findCenterRoom(int dirCode)
	{
		Room dirRoom=rawDoors()[dirCode];
		if(dirRoom!=null)
		{
			Room altR=super.findCenterRoom(dirCode);
			if(altR!=null)
			{
				Exit ox=CMClass.getExit("Open");
				linkRoom(altR,dirRoom,dirCode,ox,ox);
				return altR;
			}
		}
		return null;
	}

	protected boolean goodDir(int x, int y, int dirCode)
	{
		if(dirCode==Directions.UP) return false;
		if(dirCode==Directions.DOWN) return false;
		if(dirCode>=Directions.GATE) return false;
		if((x==0)&&(dirCode==Directions.WEST)) return false;
		if((y==0)&&(dirCode==Directions.NORTH)) return false;
		if((x>=(subMap.length-1))&&(dirCode==Directions.EAST)) return false;
		if((y>=(subMap[0].length-1))&&(dirCode==Directions.SOUTH)) return false;
		return true;
	}

	protected Room roomDir(int x, int y, int dirCode)
	{
		if(!goodDir(x,y,dirCode)) return null;
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

	protected void mazify(Hashtable visited, int x, int y)
	{

		if(visited.get(subMap[x][y])!=null) return;
		Room room=subMap[x][y];
		visited.put(room,room);
		Exit ox=CMClass.getExit("Open");

		boolean okRoom=true;
		while(okRoom)
		{
			okRoom=false;
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
			    if(d==Directions.GATE) continue;
				Room possRoom=roomDir(x,y,d);
				if(possRoom!=null)
					if(visited.get(possRoom)==null)
					{
						okRoom=true;
						break;
					}
			}
			if(okRoom)
			{
				Room goRoom=null;
				int dirCode=-1;
				while(goRoom==null)
				{
					int d=Dice.roll(1,Directions.NUM_DIRECTIONS,0)-1;
					Room possRoom=roomDir(x,y,d);
					if(possRoom!=null)
						if(visited.get(possRoom)==null)
						{
							goRoom=possRoom;
							dirCode=d;
						}
				}
				linkRoom(room,goRoom,dirCode,ox,ox);
				mazify(visited,getX(x,dirCode),getY(y,dirCode));
			}
		}
	}

	protected void buildMaze()
	{
		Hashtable visited=new Hashtable();
		int x=xsize/2;
		int y=ysize/2;
		mazify(visited,x,y);
	}

	public void buildGrid()
	{
		clearGrid(null);
		try
		{
			subMap=new Room[xsize][ysize];
			for(int x=0;x<subMap.length;x++)
				for(int y=0;y<subMap[x].length;y++)
				{
					Room newRoom=getGridRoom(x,y);
					if(newRoom!=null)
					{
						subMap[x][y]=newRoom;
						CMMap.addRoom(newRoom);
					}
				}
			buildMaze();
			buildFinalLinks();
		}
		catch(Exception e)
		{
			clearGrid(null);
		}
	}
}
