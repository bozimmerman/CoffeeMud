package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class PaddedArmor extends StdArmor
{
	public String ID(){	return "PaddedArmor";}
	public PaddedArmor()
	{
		super();

		setName("a suit of padded armor");
		setDisplayText("a suit of padded armor including everything needed to protect the torso, legs, and arms");
		setDescription("This is a fairly decent looking suit of padded armor");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(12);
		baseEnvStats().setWeight(30);
		baseEnvStats().setAbility(0);
		baseGoldValue=8;
		recoverEnvStats();
		material=EnvResource.RESOURCE_LEATHER;
	}
	public Environmental newInstance()
	{
		return new PaddedArmor();
	}
}
