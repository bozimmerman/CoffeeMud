package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chair extends StdRideable
{
	public String ID(){	return "Chair";}
	protected String	readableText="";
	public Chair()
	{
		super();
		name="a chair";
		baseEnvStats.setWeight(150);
		displayText="a chair is here.";
		description="Looks like a nice comfortable wooden chair";
		material=EnvResource.RESOURCE_OAK;
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		setRideBasis(Rideable.RIDEABLE_SIT);
		setMobCapacity(1);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new Chair();
	}
}
