package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Parchment extends GenReadable
{
	public String ID(){	return "Parchment";}
	public Parchment()
	{
		super();
		name="a piece of parchment";
		displayText="a piece of parchment here.";
		description="looks kinda like a piece of paper";
		baseEnvStats().setWeight(1);
		recoverEnvStats();
		setMaterial(EnvResource.RESOURCE_PAPER);
	}

	public Environmental newInstance()
	{
		return new Parchment();
	}

}
