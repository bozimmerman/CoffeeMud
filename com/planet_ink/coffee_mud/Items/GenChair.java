package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenChair extends GenRideable
{
	public String ID(){	return "GenChair";}
	protected String	readableText="";
	public GenChair()
	{
		super();
		setName("a generic chair");
		baseEnvStats.setWeight(150);
		setDisplayText("a generic chair is here.");
		setDescription("");
		material=EnvResource.RESOURCE_OAK;
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		setRiderCapacity(1);
		setRideBasis(Rideable.RIDEABLE_SIT);
		recoverEnvStats();
	}
}
