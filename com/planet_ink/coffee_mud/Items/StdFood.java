package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdFood extends StdItem implements Food
{

	protected int amountOfNourishment=300;

	public StdFood()
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
		return new StdFood();
	}

	public int nourishment()
	{
		return amountOfNourishment;
	}
	public void setNourishment(int amount)
	{
		amountOfNourishment=amount;
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_DRINK:
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
			switch(affect.targetMinor())
			{
			case Affect.TYP_EAT:
				boolean hungry=mob.curState().getHunger()==0;
				boolean full=!mob.curState().adjHunger(amountOfNourishment,mob.maxState());
				if(hungry)
					mob.tell("You are no longer hungry.");
				else
				if(full)
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
