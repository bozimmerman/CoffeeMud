package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Bed extends GenRideable
{
	protected String	readableText="";
	public Bed()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a bed";
		baseEnvStats.setWeight(150);
		displayText="a bed is here.";
		description="Looks like a nice comfortable bed";
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		setMaterial(EnvResource.RESOURCE_COTTON);
		setRideBasis(Rideable.RIDEABLE_SLEEP);
		setMobCapacity(2);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new Bed();
	}
}
