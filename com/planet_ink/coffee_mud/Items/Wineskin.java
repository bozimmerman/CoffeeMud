package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Wineskin extends StdDrink
{
	public String ID(){	return "Wineskin";}
	public Wineskin()
	{
		super();

		setName("a wineskin");
		amountOfThirstQuenched=200;
		amountOfLiquidHeld=1000;
		amountOfLiquidRemaining=1000;
		baseEnvStats.setWeight(10);
		capacity=5;
		setDisplayText("a tough little wineskin sits here.");
		setDescription("Looks like it could hold quite a bit of drink.");
		baseGoldValue=10;
		material=EnvResource.RESOURCE_LEATHER;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Wineskin();
	}

}
