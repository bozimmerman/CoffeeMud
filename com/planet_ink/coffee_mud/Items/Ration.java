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
		setName("a ration pack");
		baseEnvStats.setWeight(10);
		amountOfNourishment=500;
		setDisplayText("a standard ration pack sits here.");
		setDescription("Bits of salt dried meat, dried fruit, and hard bread.");
		baseGoldValue=15;
		setMaterial(EnvResource.RESOURCE_MEAT);
		recoverEnvStats();
	}


}
