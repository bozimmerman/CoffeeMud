package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class Pan extends StdDrink
{
	public String ID(){	return "Pan";}
	public Pan()
	{
		super();
		setName("a pan");
		setDisplayText("an iron pan sits here.");
		setDescription("A sturdy iron pan for cooking in.");
		capacity=25;
		baseGoldValue=5;
		setMaterial(EnvResource.RESOURCE_IRON);
		baseEnvStats().setWeight(5);
		recoverEnvStats();
	}



}
