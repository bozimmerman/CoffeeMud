package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class MobileGoodGuardian extends Mobile
{
	public String ID(){return "MobileGoodGuardian";}
	public Behavior newInstance()
	{
		return new MobileGoodGuardian();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=MudHost.TICK_MOB) return true;
		if(!canFreelyBehaveNormal(ticking)) return true;
		MOB mob=(MOB)ticking;

		// ridden things dont wander!
		if(ticking instanceof Rideable)
			if(((Rideable)ticking).numRiders()>0)
				return true;
		if(((mob.amFollowing()!=null)&&(mob.location()==mob.amFollowing().location()))
		||(!Sense.canTaste(mob)))
		   return true;

		Room thisRoom=mob.location();
		MOB victim=GoodGuardian.anyPeaceToMake(mob.location(),mob);
		GoodGuardian.keepPeace(mob,victim);
		victim=null;
		int dirCode=-1;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=thisRoom.getRoomInDir(d);
			Exit exit=thisRoom.getExitInDir(d);
			if((room!=null)&&(exit!=null)&&(okRoomForMe(thisRoom,room)))
			{
				if(exit.isOpen())
				{
					victim=GoodGuardian.anyPeaceToMake(room,mob);
					if(victim!=null)
					{
						dirCode=d;
						break;
					}
				}
			}
			if(dirCode>=0) break;
		}
		if(dirCode>=0)
		{
			MUDTracker.move(mob,dirCode,false,false);
			GoodGuardian.keepPeace(mob,victim);
		}
		return true;
	}
}
