package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class OilFlask extends StdDrink
{
	public String ID(){	return "OilFlask";}


	public OilFlask()
	{
		super();
		setName("an oil flask");
		baseEnvStats.setWeight(3);
		capacity=0;
		setMaterial(EnvResource.RESOURCE_GLASS);
		setDisplayText("an oil flask sits here.");
		setDescription("A small glass flask containing lamp oil, with a lid.");
		baseGoldValue=5;
		amountOfLiquidHeld=5;
		amountOfLiquidRemaining=5;
		liquidType=EnvResource.RESOURCE_LAMPOIL;
		recoverEnvStats();
	}


}