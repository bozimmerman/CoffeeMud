package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Bed extends GenRideable
{
	public String ID(){	return "Bed";}
	protected String	readableText="";
	public Bed()
	{
		super();
		setName("a bed");
		baseEnvStats.setWeight(150);
		setDisplayText("a bed is here.");
		setDescription("Looks like a nice comfortable bed");
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		setMaterial(EnvResource.RESOURCE_COTTON);
		setRideBasis(Rideable.RIDEABLE_SLEEP);
		setRiderCapacity(2);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new Bed();
	}
}
