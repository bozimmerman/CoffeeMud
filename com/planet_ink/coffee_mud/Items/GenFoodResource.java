package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenFoodResource extends GenFood implements EnvResource, Food
{
	public String ID(){	return "GenFoodResource";}
	public GenFoodResource()
	{
		super();
		setName("an edible resource");
		setDisplayText("a pile of edible resource sits here.");
		setDescription("");
		isReadable=false;
		setMaterial(EnvResource.RESOURCE_BERRIES);
		setNourishment(200);
		baseEnvStats().setWeight(0);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenFoodResource();
	}
}
