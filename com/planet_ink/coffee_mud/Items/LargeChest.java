package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;


public class LargeChest extends LockableContainer
{
	public LargeChest()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a large chest";
		displayText="a large wooden chest sits here.";
		description="It\\`s of solid wood construction with metal bracings.  The lid has a key hole.";
		capacity=100;
		material=Item.WOODEN;
		baseGoldValue=50;
		baseEnvStats().setWeight(50);
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new LargeChest();
	}

}
