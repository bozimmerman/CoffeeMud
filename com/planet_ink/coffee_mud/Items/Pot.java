package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class Pot extends StdDrink
{
	public String ID(){	return "Pot";}
	public Pot()
	{
		super();
		setName("a pot");
		setDisplayText("a cooking pot sits here.");
		setDescription("A sturdy iron pot for cooking in.");
		capacity=25;
		baseGoldValue=5;
		setLiquidHeld(20);
		setThirstQuenched(1);
		setLiquidRemaining(0);
		setLiquidType(EnvResource.RESOURCE_FRESHWATER);
		setMaterial(EnvResource.RESOURCE_IRON);
		baseEnvStats().setWeight(5);
		recoverEnvStats();
	}



}
