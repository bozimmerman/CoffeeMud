package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenWater extends GenDrink
{
	public String ID(){	return "GenWater";}
	protected String	readableText="";
	public GenWater()
	{
		super();
		setName("a generic puddle of water");
		baseEnvStats.setWeight(2);
		setDisplayText("a generic puddle of water sits here.");
		setDescription("");
		baseGoldValue=0;
		capacity=0;
		amountOfThirstQuenched=250;
		amountOfLiquidHeld=10000;
		amountOfLiquidRemaining=10000;
		setMaterial(EnvResource.RESOURCE_FRESHWATER);
		baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMNOTGET);
		recoverEnvStats();
	}


}
