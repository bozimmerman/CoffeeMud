package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Food extends StdItem
{
	
	public int amountOfNourishment=300;
	
	public Food()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a bit of food";
		baseEnvStats.setWeight(2);
		displayText="a bit of food is here.";
		description="Looks like some mystery meat";
		baseGoldValue=5;
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new Food();
	}
	
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetCode())
			{
			case Affect.TASTE_FOOD:
				if(mob.isMine(this))
					return true;
				else
				{
					mob.tell("You don't have that.");
					return false;
				}
			}
		}
		return true;
	}
	
	public void affect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetCode())
			{
			case Affect.TASTE_FOOD:
				if(mob.curState().adjHunger(amountOfNourishment,mob.maxState()))
					mob.tell("You are full.");
				this.destroyThis();
				mob.location().recoverRoomStats();
				break;
			default:
				break;
			}
		}
		super.affect(affect);
	}
}
