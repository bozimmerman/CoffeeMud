package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Ration extends StdFood
{
	public String ID(){	return "Ration";}
	public Ration()
	{
		super();
		name="a ration pack";
		baseEnvStats.setWeight(10);
		amountOfNourishment=500;
		displayText="a standard ration pack sits here.";
		description="Bits of salt dried meat, dried fruit, and hard bread.";
		baseGoldValue=15;
		setMaterial(EnvResource.RESOURCE_MEAT);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Ration();
	}
}
