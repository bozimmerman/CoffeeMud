package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Sense;
import com.planet_ink.coffee_mud.utils.Directions;
import java.util.*;

public class EndlessSky extends StdGrid
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

		return InTheAir.isOkAffect(this,affect);
	}
	public String getChildLocaleID(){return "InTheAir";}

	protected void buildFinalLinks()
	{
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room dirRoom=rawDoors()[d];
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
					CMMap.map.addElement(newRoom);
				}
			}
		buildFinalLinks();
		if((subMap[0][0]!=null)&&(subMap[0][0].rawDoors()[Directions.UP]==null))
			linkRoom(subMap[0][0],subMap[1][0],Directions.UP);
		for(int y=0;y<subMap[0].length;y++)
			linkRoom(subMap[0][y],subMap[subMap.length-1][y],Directions.WEST);
		for(int x=0;x<subMap.length;x++)
			linkRoom(subMap[x][0],subMap[x][subMap.length-1],Directions.NORTH);
		for(int x=1;x<subMap.length;x++)
			linkRoom(subMap[x][0],subMap[x-1][subMap.length-1],Directions.UP);
	}
}
