package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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
	
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		
		if(!canBehave(affecting)) return;
		MOB mob=affect.source();
		Aggressive.pickAFight((MOB)affecting);
	}
	
	public static void tickVeryAggressively(Environmental ticking, int tickID)
	{
		if(tickID!=ServiceEngine.MOB_TICK) return;
		if(!canBehave(ticking)) return;
		MOB mob=(MOB)ticking;
		Room thisRoom=mob.location();
		int dirCode=-1;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=thisRoom.getRoom(d);
			Exit exit=thisRoom.getExit(d);
			if((room!=null)&&(exit!=null)&&(room.getAreaID().equals(thisRoom.getAreaID())))
			{
				if(exit.isOpen())
				{
					for(int i=0;i<room.numInhabitants();i++)
					{
						MOB inhab=room.fetchInhabitant(i);
						if((!inhab.isMonster()))
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
			Movement.move(mob,dirCode,false);
			pickAFight(mob);
		}
	}
	
	public void tick(Environmental ticking, int tickID)
	{
		tickVeryAggressively(ticking,tickID);
	}
}
