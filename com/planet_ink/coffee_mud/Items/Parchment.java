package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Parchment extends GenReadable
{
	public String ID(){	return "Parchment";}
	public Parchment()
	{
		super();
		setName("a piece of parchment");
		setDisplayText("a piece of parchment here.");
		setDescription("looks kinda like a piece of paper");
		baseEnvStats().setWeight(1);
		recoverEnvStats();
		setMaterial(EnvResource.RESOURCE_PAPER);
	}



}
