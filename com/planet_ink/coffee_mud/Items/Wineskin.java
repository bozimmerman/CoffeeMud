package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Wineskin extends StdDrink
{
	public Wineskin()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a wineskin";
		amountOfThirstQuenched=200;
		amountOfLiquidHeld=1000;
		amountOfLiquidRemaining=1000;
		baseEnvStats.setWeight(10);
		capacity=5;
		displayText="a tough little wineskin sits here.";
		description="Looks like it could hold quite a bit of drink.";
		baseGoldValue=10;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Wineskin();
	}

}
