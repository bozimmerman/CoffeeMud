package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Follower extends StdBehavior
{
	public String ID(){return "Follower";}
	public Follower()
	{
		direction=-1;
	}

	public Behavior newInstance()
	{
		return new Follower();
	}

	int direction=-1;

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);

		if(!canFreelyBehaveNormal(affecting)) return;

		MOB mob=affect.source();
		if(mob.amDead()) return;
		if(mob.location()==null) return;


		if((direction<0)
		&&(affect.amITarget(((MOB)affecting).location()))
		&&(Sense.canBeSeenBy(mob,affecting))
		&&(affect.othersMessage()!=null)
		&&((affect.targetMinor()==Affect.TYP_LEAVE)
		 ||(affect.targetMinor()==Affect.TYP_FLEE)))
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
		super.tick(ticking,tickID);
		if(tickID!=Host.MOB_TICK) return;
		if((direction>=0)&&(ticking instanceof MOB))
		{
			if(!canFreelyBehaveNormal(ticking)) return;
			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			Room otherRoom=(Room)thisRoom.getRoomInDir(direction);

			if(otherRoom!=null)
			{
				if(!otherRoom.getArea().name().equals(thisRoom.getArea().name()))
					direction=-1;
			}
			else
				direction=-1;

			if(direction<0)
				return;


			boolean move=true;
			for(int m=0;m<thisRoom.numInhabitants();m++)
			{
				MOB inhab=thisRoom.fetchInhabitant(m);
				if((inhab!=null)&&(inhab.isASysOp(thisRoom)))
					move=false;
			}
			if(move)
				ExternalPlay.move(mob,direction,false,false);
			direction=-1;
		}
		return;
	}
}
