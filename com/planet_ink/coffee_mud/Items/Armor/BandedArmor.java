package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class BandedArmor extends StdArmor
{
	public String ID(){	return "BandedArmor";}
	public BandedArmor()
	{
		super();
		name="a suit of banded armor";
		displayText="a suit of armor made from metal bands fastened to leather";
		description="This suit of armor is made from metal bands fastened to leather and will provide protection for the torso, arms, and legs.";
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(44);
		baseEnvStats().setWeight(55);
		baseEnvStats().setAbility(0);
		baseGoldValue=400;
		material=EnvResource.RESOURCE_IRON;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new BandedArmor();
	}
}
