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
		baseEnvStats.setWeight(1);
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
	
	/*
	A failed aborted attempt to link endless skys together.
	public static synchronized void buildSky(EndlessSky me)
	{
		me.clearGrid();
		me.subMap=new Room[me.size][me.size];
		for(int x=0;x<me.size;x++)
			for(int y=0;y<me.size;y++)
			{
				Room newRoom=me.getGridRoom(x,y);
				if(newRoom!=null)
				{
					me.subMap[x][y]=newRoom;
					if((y>0)&&(me.subMap[x][y-1]!=null))
					{
						linkRoom(newRoom,me.subMap[x][y-1],Directions.NORTH);
						linkRoom(newRoom,me.subMap[x][y-1],Directions.UP);
					}

					if((x>0)&&(me.subMap[x-1][y]!=null))
						linkRoom(newRoom,me.subMap[x-1][y],Directions.WEST);
					CMMap.addRoom(newRoom);
				}
			}
		me.buildFinalLinks();
		if((me.subMap[0][0]!=null)&&(me.subMap[0][0].rawDoors()[Directions.UP]==null))
			linkRoom(me.subMap[0][0],me.subMap[1][0],Directions.UP);
		for(int x=1;x<me.subMap.length;x++)
			linkRoom(me.subMap[x][0],me.subMap[x-1][me.subMap.length-1],Directions.UP);
		
		Room below=me.rawDoors()[Directions.DOWN];
		if(below!=null)
		for(int i=0;i<4;i++)
		{
			int dir=-1;
			switch(i){
			case 0: dir=Directions.NORTH; 
					break;
			case 1: dir=Directions.SOUTH; 
					break;
			case 2: dir=Directions.EAST; 
					break;
			case 3: dir=Directions.WEST; 
					break;
			}
			int opDir=Directions.getOpDirectionCode(dir);
			Room nextBelow=below.rawDoors()[dir];
			
			if((nextBelow!=null)
			&&(nextBelow.rawDoors()[Directions.UP] instanceof EndlessSky)
			&&(((EndlessSky)nextBelow.rawDoors()[Directions.UP]).done))
			{
				EndlessSky friend=(EndlessSky)nextBelow.rawDoors()[Directions.UP];
				switch(dir){
				case Directions.WEST:
					for(int y=0;y<me.subMap[0].length;y++)
					{
						Room friendWay=friend.subMap[me.subMap.length-1][y];
						Room friendDoor=friendWay.rawDoors()[Directions.EAST];
						if((friendDoor!=null)&&(friend.isMyChild(friendDoor)))
							friendWay.rawDoors()[Directions.EAST]=null;
						linkRoom(me.subMap[0][y],friendWay,Directions.WEST);
					}
					break;
				case Directions.NORTH:
					for(int x=0;x<me.subMap.length;x++)
					{
						Room friendWay=friend.subMap[x][friend.subMap[0].length-1];
						Room friendDoor=friendWay.rawDoors()[Directions.SOUTH];
						if((friendDoor!=null)&&(friend.isMyChild(friendDoor)))
							friendWay.rawDoors()[Directions.SOUTH]=null;
						linkRoom(me.subMap[x][0],friendWay,Directions.NORTH);
					}
					break;
				case Directions.EAST:
					for(int y=0;y<me.subMap[0].length;y++)
					{
						Room friendWay=friend.subMap[0][y];
						Room friendDoor=friendWay.rawDoors()[Directions.EAST];
						if((friendDoor!=null)&&(friend.isMyChild(friendDoor)))
							friendWay.rawDoors()[Directions.EAST]=null;
						linkRoom(me.subMap[me.subMap.length-1][y],friendWay,Directions.WEST);
					}
					break;
				case Directions.SOUTH:
					for(int x=0;x<me.subMap.length;x++)
					{
						Room friendWay=friend.subMap[x][0];
						Room friendDoor=friendWay.rawDoors()[Directions.SOUTH];
						if((friendDoor!=null)&&(friend.isMyChild(friendDoor)))
							friendWay.rawDoors()[Directions.SOUTH]=null;
						linkRoom(me.subMap[x][me.subMap[0].length-1],friendWay,Directions.NORTH);
					}
					break;
				}
			}
			else
			{
				switch(dir){
				case Directions.WEST:
					for(int y=0;y<me.subMap[0].length;y++)
						halfLink(me.subMap[0][y],me.subMap[me.subMap.length-1][y],Directions.WEST);
					break;
				case Directions.NORTH:
					for(int x=0;x<me.subMap.length;x++)
						halfLink(me.subMap[x][0],me.subMap[x][me.subMap.length-1],Directions.NORTH);
					break;
				case Directions.EAST:
					for(int y=0;y<me.subMap[0].length;y++)
						halfLink(me.subMap[me.subMap.length-1][y],me.subMap[0][y],Directions.EAST);
					break;
				case Directions.SOUTH:
					for(int x=0;x<me.subMap.length;x++)
						halfLink(me.subMap[x][me.subMap.length-1],me.subMap[x][0],Directions.SOUTH);
					break;
				}
			}
		}
	}*/
}
