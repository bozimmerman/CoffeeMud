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

public class Follower extends StdBehavior
{
	int direction=-1;
	
	public Follower()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		direction=-1;
	}
	
	public Behavior newInstance()
	{ 
		return new Follower();
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
		if(mob.amDead()) return;
		if(mob.location()==null) return;
		
			
		if((direction<0)
		&&(affect.target()==((MOB)affecting).location())
		&&(Sense.canBeSeenBy(mob,affecting))
		&&(affect.othersMessage()!=null)
		&&((affect.targetCode()==Affect.MOVE_LEAVE)
		 ||(affect.targetCode()==Affect.MOVE_FLEE)))
		{
			String directionWent=affect.othersMessage();
			int x=directionWent.lastIndexOf(" ");
			if((x>=0)&&((Dice.rollPercentage()*10)<affect.source().getAlignment()))
			{
				directionWent=directionWent.substring(x+1);
				direction=Directions.getDirectionCode(directionWent);
			}
			else
				direction=-1;
		}
		
	}
	
	public void tick(Environmental ticking, int tickID)
	{
		if(tickID!=ServiceEngine.MOB_TICK) return;
		if((direction>=0)&&(ticking instanceof MOB))
		{
			if(!canBehave(ticking)) return;
			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			Room otherRoom=(Room)thisRoom.getRoom(direction);
			
			if(otherRoom!=null)
			{
				if(!otherRoom.getAreaID().equals(thisRoom.getAreaID()))
					direction=-1;
			}
			else
				direction=-1;
			
			if(direction<0)
				return;
			
			
			boolean move=true;
			for(int m=0;m<thisRoom.numInhabitants();m++)
				if(thisRoom.fetchInhabitant(m).isASysOp())
					move=false;
			if(move)
				Movement.move(mob,direction,false);
			direction=-1;
		}
		return;
	}
}
