package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenLiquidResource extends GenWater implements EnvResource, Drink
{
	public String ID(){	return "GenLiquidResource";}
	public GenLiquidResource()
	{
		super();
		setName("a puddle of resource thing");
		setDisplayText("a puddle of resource sits here.");
		setDescription("");
		setMaterial(EnvResource.RESOURCE_FRESHWATER);
		disappearsAfterDrinking=true;
		baseEnvStats().setWeight(0);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenLiquidResource();
	}
}
