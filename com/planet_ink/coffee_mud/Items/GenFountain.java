package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenFountain extends GenWater
{
	public String ID(){	return "GenFountain";}
	public GenFountain()
	{
		super();
		name="a fountain";
		amountOfThirstQuenched=250;
		amountOfLiquidHeld=999999;
		amountOfLiquidRemaining=999999;
		baseEnvStats().setWeight(5);
		capacity=0;
		displayText="a little fountain flows here.";
		description="The water looks pure and clean.";
		baseGoldValue=10;
		isGettable=false;
		material=EnvResource.RESOURCE_FRESHWATER;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new GenFountain();
	}

	public boolean okAffect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
				case Affect.TYP_FILL:
					mob.tell("There's no need to fill the fountain.");
					return false;
				default:
					break;
			}
		}
		return super.okAffect(affect);
	}

}
