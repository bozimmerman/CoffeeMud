package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chair extends StdRideable
{
	protected String	readableText="";
	public Chair()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a chair";
		baseEnvStats.setWeight(150);
		displayText="a chair is here.";
		description="Looks like a nice comfortable wooden chair";
		setMaterial(Item.WOODEN);
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
