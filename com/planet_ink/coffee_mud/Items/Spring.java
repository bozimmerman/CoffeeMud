package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spring extends StdDrink
{
	public String ID(){	return "Spring";}
	public Spring()
	{
		super();
		setName("a spring");
		amountOfThirstQuenched=250;
		amountOfLiquidHeld=999999;
		amountOfLiquidRemaining=999999;
		baseEnvStats().setWeight(5);
		capacity=0;
		setDisplayText("a little magical spring flows here.");
		setDescription("The spring is coming magically from the ground.  The water looks pure and clean.");
		baseGoldValue=10;
		baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMNOTGET);
		material=EnvResource.RESOURCE_FRESHWATER;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spring();
	}
}
