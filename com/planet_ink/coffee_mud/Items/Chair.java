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
		setName("a chair");
		baseEnvStats.setWeight(150);
		setDisplayText("a chair is here.");
		setDescription("Looks like a nice comfortable wooden chair");
		material=EnvResource.RESOURCE_OAK;
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		setRideBasis(Rideable.RIDEABLE_SIT);
		setRiderCapacity(1);
		recoverEnvStats();
	}

}
