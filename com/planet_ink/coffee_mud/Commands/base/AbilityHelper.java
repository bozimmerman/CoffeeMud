package com.planet_ink.coffee_mud.Commands.base;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;


public class AbilityHelper
{
	protected final static int TRACK_ATTEMPTS=25;
	protected final static int TRACK_DEPTH=500;
	
	public static boolean findTheRoom(Room location, 
									  Room destRoom, 
									  int tryCode, 
									  Vector dirVec,
									  Vector theTrail,
									  Hashtable lookedIn,
									  int depth,
									  boolean noWater)
	{
		if(lookedIn==null) return false;
		if(lookedIn.get(location)!=null) return false;
		if(depth>TRACK_DEPTH) return false;
		
		lookedIn.put(location,location);
		for(int x=0;x<dirVec.size();x++)
		{
			int i=((Integer)dirVec.elementAt(x)).intValue();
			Room nextRoom=location.getRoomInDir(i);
			Exit nextExit=location.getExitInDir(i);
			if((nextRoom!=null)
			&&(nextExit!=null)
			&&((!noWater)||(
			  (nextRoom.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
			&&(nextRoom.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
			&&(nextRoom.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
			&&(nextRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE))))
			{
				if((nextRoom==destRoom)
				||(findTheRoom(nextRoom,destRoom,tryCode,dirVec,theTrail,lookedIn,depth+1,noWater)))
				{
					theTrail.addElement(nextRoom);
					return true;
				}
			}
		}
		return false;
	}
	
	public static Vector findBastardTheBestWay(Room location, 
											   Vector destRooms,
											   boolean noWater)
	{
		
		Vector trailArray[] = new Vector[TRACK_ATTEMPTS];
		Room trackArray[] = new Room[TRACK_ATTEMPTS];
		
		for(int t=0;t<TRACK_ATTEMPTS;t++)
		{
			Vector dirVec=new Vector();
			while(dirVec.size()<Directions.NUM_DIRECTIONS)
			{
				int direction=Dice.roll(1,Directions.NUM_DIRECTIONS,-1);
				for(int x=0;x<dirVec.size();x++)
					if(((Integer)dirVec.elementAt(x)).intValue()==direction)
						continue;
				dirVec.addElement(new Integer(direction));
			}
			Room roomToTry=(Room)destRooms.elementAt(Dice.roll(1,destRooms.size(),-1));
			Hashtable lookedIn=new Hashtable();
			Vector theTrail=new Vector();
			if(findTheRoom(location,roomToTry,2,dirVec,theTrail,lookedIn,0,noWater))
			{
				trailArray[t]=theTrail;
				trackArray[t]=roomToTry;
			}
		}
		int winner=-1;
		int winningTotal=Integer.MAX_VALUE;
		for(int t=0;t<TRACK_ATTEMPTS;t++)
		{
			Vector V=trailArray[t];
			Room which=trackArray[t];
			if((V!=null)&&(which!=null)&&(V.size()<winningTotal))
			{
				winningTotal=V.size();
				winner=t;
			}
		}
		
		if(winner<0) 
			return null;
		else
			return trailArray[winner];
	}
	
	public static int trackNextDirectionFromHere(Vector theTrail, 
												 Room location,
												 boolean noWater)
	{
		if((theTrail==null)||(location==null))
			return -1;
		if(location==theTrail.elementAt(0))
			return 999;

		Room nextRoom=null;
		int bestDirection=-1;
		int trailLength=Integer.MAX_VALUE;
		for(int dirs=0;dirs<Directions.NUM_DIRECTIONS;dirs++)
		{
			Room thisRoom=location.getRoomInDir(dirs);
			Exit thisExit=location.getExitInDir(dirs);
			if((thisRoom!=null)
			   &&(thisExit!=null)
			   &&((!noWater)||(
					  (thisRoom.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
					&&(thisRoom.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
					&&(thisRoom.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
					&&(thisRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE))))
			{
				for(int trail=0;trail<theTrail.size();trail++)
				{
					if((theTrail.elementAt(trail)==thisRoom)
					&&(trail<trailLength))
					{
						bestDirection=dirs;
						trailLength=trail;
						nextRoom=thisRoom;
					}
				}
			}
		}
		return bestDirection;
	}
}
