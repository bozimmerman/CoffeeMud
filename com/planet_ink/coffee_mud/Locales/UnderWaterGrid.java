package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class UnderWaterGrid extends StdGrid
{
	public UnderWaterGrid()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		baseEnvStats().setSensesMask(baseEnvStats().sensesMask()|EnvStats.CAN_BREATHE);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_SWIMMING);
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_UNDERWATER;
		domainCondition=Room.CONDITION_WET;
		setDisplayText("Under the water");
		setDescription("");
		baseThirst=0;
	}

	public Environmental newInstance()
	{
		return new UnderWaterGrid();
	}
	public String getChildLocaleID(){return "UnderWater";}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SWIMMING);
	}
	public Vector resourceChoices(){return UnderWater.roomResources;}
	
	protected void buildFinalLinks()
	{
		for(int d=0;d<Directions.NUM_DIRECTIONS-1;d++)
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
				Room newRoom=(Room)getGridRoom(x,y);
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
					CMMap.addRoom(newRoom);
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
