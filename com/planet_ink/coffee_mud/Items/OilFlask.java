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
		name="an oil flask";
		baseEnvStats.setWeight(3);
		capacity=0;
		setMaterial(EnvResource.RESOURCE_GLASS);
		displayText="an oil flask sits here.";
		description="A small glass flask containing lamp oil, with a lid.";
		baseGoldValue=5;
		amountOfLiquidHeld=5;
		amountOfLiquidRemaining=5;
		liquidType=EnvResource.RESOURCE_LAMPOIL;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new OilFlask();
	}
}