package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class VeryAggressive extends Aggressive
{

	public VeryAggressive()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{
		return new VeryAggressive();
	}

	public static void tickVeryAggressively(Environmental ticking, int tickID)
	{
		if(tickID!=Host.MOB_TICK) return;
		if(!canFreelyBehaveNormal(ticking)) return;
		MOB mob=(MOB)ticking;
		
		// ridden things dont wander!
		if(ticking instanceof Rideable)
			if(((Rideable)ticking).numRiders()>0)
				return;

		// let's not do this 100%
		if(Dice.rollPercentage()>15) return;

		Room thisRoom=mob.location();
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
						if((inhab!=null)
						&&(!inhab.isMonster())
						&&(inhab.envStats().level()<(mob.envStats().level()+11))
						&&(inhab.envStats().level()>(mob.envStats().level()-11)))
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
			pickAFight(mob);
		}
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		tickVeryAggressively(ticking,tickID);
	}
}
