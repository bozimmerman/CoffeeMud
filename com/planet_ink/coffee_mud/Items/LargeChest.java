package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class LargeChest extends LockableContainer
{
	public String ID(){	return "LargeChest";}
	public LargeChest()
	{
		super();
		setName("a large chest");
		setDisplayText("a large wooden chest sits here.");
		setDescription("It\\`s of solid wood construction with metal bracings.  The lid has a key hole.");
		capacity=150;
		setMaterial(EnvResource.RESOURCE_OAK);
		baseGoldValue=50;
		baseEnvStats().setWeight(50);
		recoverEnvStats();
	}



}
