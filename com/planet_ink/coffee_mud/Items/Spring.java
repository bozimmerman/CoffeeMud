package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spring extends Drink
{
	public Spring()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a spring";
		amountOfThirstQuenched=100;
		amountOfLiquidHeld=999999;
		amountOfLiquidRemaining=999999;
		baseEnvStats().setWeight(5);
		capacity=0;
		displayText="a little magical spring flows here.";
		description="The spring is coming magically from the ground.  The water looks pure and clean.";
		baseGoldValue=10;
		isGettable=false;
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new Spring();
	}
	
	public boolean okAffect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetCode())
			{
				case Affect.HANDS_FILL:
					mob.tell("You can't fill the magical spring.");
					return false;
				default:
					break;
			}
		}
		return super.okAffect(affect);
	}
	
}
