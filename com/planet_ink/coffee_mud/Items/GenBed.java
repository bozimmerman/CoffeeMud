package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenBed extends GenRideable
{
	public String ID(){	return "GenBed";}
	protected String	readableText="";
	public GenBed()
	{
		super();

		setName("a generic bed");
		baseEnvStats.setWeight(150);
		setDisplayText("a generic bed sits here.");
		setDescription("");
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		setMaterial(EnvResource.RESOURCE_COTTON);
		setRiderCapacity(2);
		setRideBasis(Rideable.RIDEABLE_SLEEP);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenBed();
	}
}
