package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Wineskin extends Drink
{
	public Wineskin()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a wineskin";
		amountOfThirstQuenched=50;
		amountOfLiquidHeld=1000;
		amountOfLiquidRemaining=1000;
		baseEnvStats.setWeight(10);
		capacity=5;
		displayText="a tough little waterskin sits here.";
		description="Looks like it could hold quite a bit of drink.";
		baseGoldValue=10;
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new Wineskin();
	}
	
}
