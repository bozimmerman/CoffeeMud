package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class SteelGauntlets extends StdArmor
{
	public String ID(){	return "SteelGauntlets";}
	public SteelGauntlets()
	{
		super();

		setName("some steel gauntlets");
		setDisplayText("a pair of steel gauntlets sit here.");
		setDescription("They look like they're made of steel.");
		properWornBitmap=Item.ON_HANDS | Item.ON_LEFT_WRIST | Item.ON_RIGHT_WRIST;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(3); // = $$$$ =
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(5);
		baseGoldValue=20;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}

}
