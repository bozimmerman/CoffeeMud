package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class MetalBracers extends StdArmor
{
	public String ID(){	return "MetalBracers";}
	public MetalBracers()
	{
		super();

		setName("a pair of metal bracers");
		setDisplayText("a pair of metal bracers lie here.");
		setDescription("Good and solid protection for your arms.");
		properWornBitmap=Item.ON_LEFT_WRIST | Item.ON_RIGHT_WRIST | Item.ON_ARMS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(4);
		baseEnvStats().setWeight(10);
		baseEnvStats().setAbility(0);
		baseGoldValue=10;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}

}
