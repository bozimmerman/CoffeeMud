package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class MobileGoodGuardian extends Mobile
{
	public MobileGoodGuardian()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{
		return new MobileGoodGuardian();
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public boolean okAffect(Environmental oking, Affect affect)
	{
		if(!super.okAffect(oking,affect)) return false;
		return GoodGuardian.GoodGuardianOkAffect(oking,affect);
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.MOB_TICK) return;
		if(!canFreelyBehaveNormal(ticking)) return;
		MOB mob=(MOB)ticking;
		
		// ridden things dont wander!
		if(ticking instanceof Rideable)
			if(((Rideable)ticking).numRiders()>0)
				return;
		
		Room thisRoom=mob.location();
		GoodGuardian.keepPeace(mob);
		int dirCode=-1;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=thisRoom.doors()[d];
			Exit exit=thisRoom.exits()[d];
			if((room!=null)&&(exit!=null)&&(room.getArea().name().equals(thisRoom.getArea().name())))
			{
				if(exit.isOpen())
				{
					for(int i=0;i<room.numInhabitants();i++)
					{
						MOB inhab=room.fetchInhabitant(i);
						if((inhab!=null)&&(inhab.isInCombat())&&(inhab.getAlignment()>650))
						{
							dirCode=d;
							break;
						}
					}
				}
			}
			if(dirCode>=0) break;
		}
		if(dirCode>=0)
		{
			ExternalPlay.move(mob,dirCode,false);
			GoodGuardian.keepPeace(mob);
		}
	}

}
