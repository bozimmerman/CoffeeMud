package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class WalkingBoots extends StdArmor
{
	public String ID(){	return "WalkingBoots";}
	public WalkingBoots()
	{
		super();

		name="a pair of nice hide walking boots";
		displayText="a pair of hide walking boots sits here.";
		description="They look like a rather nice pair of footwear.";
		properWornBitmap=Item.ON_FEET;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(1);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(5);
		baseGoldValue=5;
		recoverEnvStats();
		material=EnvResource.RESOURCE_LEATHER;
	}
	public Environmental newInstance()
	{
		return new WalkingBoots();
	}

}
