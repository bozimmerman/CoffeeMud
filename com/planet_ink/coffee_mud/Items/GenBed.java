package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenBed extends GenRideable
{
	protected String	readableText="";
	public GenBed()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic bed";
		baseEnvStats.setWeight(150);
		displayText="a generic bed sits here.";
		description="Looks like a bed";
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		setMaterial(EnvResource.RESOURCE_COTTON);
		setMobCapacity(2);
		setRideBasis(Rideable.RIDEABLE_SLEEP);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenBed();
	}
}
