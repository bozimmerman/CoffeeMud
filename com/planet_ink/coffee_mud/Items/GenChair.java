package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenChair extends GenRideable
{
	protected String	readableText="";
	public GenChair()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic chair";
		baseEnvStats.setWeight(150);
		displayText="a generic chair is here.";
		description="Looks like a chair";
		setMaterial(Item.WOODEN);
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		setMobCapacity(1);
		setRideBasis(Rideable.RIDEABLE_SIT);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenChair();
	}
}
