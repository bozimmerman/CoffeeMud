package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class UnderSaltWaterGrid extends StdGrid
{
	public UnderSaltWaterGrid()
	{
		super();
		baseEnvStats().setSensesMask(baseEnvStats().sensesMask()|EnvStats.CAN_NOT_BREATHE);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_SWIMMING);
		baseEnvStats.setWeight(3);
		setDisplayText("Under the water");
		setDescription("");
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_UNDERWATER;
		domainCondition=Room.CONDITION_WET;
		baseThirst=0;
	}

	public Environmental newInstance()
	{
		return new UnderSaltWaterGrid();
	}
	public String getChildLocaleID(){return "UnderSaltWater";}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SWIMMING);
	}
	public Vector resourceChoices(){return UnderSaltWater.roomResources;}
	
	protected void buildFinalLinks()
	{
		Exit ox=CMClass.getExit("Open");
		for(int d=0;d<Directions.NUM_DIRECTIONS-1;d++)
		{
			Room dirRoom=rawDoors()[d];
			Exit dirExit=rawExits()[d];
			if((dirExit==null)||(dirExit.hasADoor()))
				dirExit=ox;
			if(dirRoom!=null)
			{
				if(d!=Directions.DOWN)
					alts[d]=findCenterRoom(d);
				else
					alts[d]=subMap[subMap.length-1][subMap[0].length-1];
				Exit altExit=dirRoom.rawExits()[Directions.getOpDirectionCode(d)];
				if(altExit==null) altExit=ox;
				switch(d)
				{
					case Directions.NORTH:
						for(int x=0;x<subMap.length;x++)
							linkRoom(subMap[x][0],dirRoom,d,dirExit,altExit);
						break;
					case Directions.SOUTH:
						for(int x=0;x<subMap.length;x++)
							linkRoom(subMap[x][subMap[0].length-1],dirRoom,d,dirExit,altExit);
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
				}
			}
		}
	}

	public void buildGrid()
	{
		clearGrid();
		subMap=new Room[xsize][ysize];
		Exit ox=CMClass.getExit("Open");
		for(int x=0;x<subMap.length;x++)
			for(int y=0;y<subMap[x].length;y++)
			{
				Room newRoom=(Room)getGridRoom(x,y);
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
					CMMap.addRoom(newRoom);
				}
			}
		buildFinalLinks();
		if((subMap[0][0]!=null)
		&&(subMap[0][0].rawDoors()[Directions.UP]==null)
		&&(xsize>1))
			linkRoom(subMap[0][0],subMap[1][0],Directions.UP,ox,ox);
		for(int y=0;y<subMap[0].length;y++)
			linkRoom(subMap[0][y],subMap[subMap.length-1][y],Directions.WEST,ox,ox);
		for(int x=0;x<subMap.length;x++)
			linkRoom(subMap[x][0],subMap[x][subMap.length-1],Directions.NORTH,ox,ox);
		for(int x=1;x<subMap.length;x++)
			linkRoom(subMap[x][0],subMap[x-1][subMap.length-1],Directions.UP,ox,ox);
	}
}
