package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class StdShield extends StdArmor implements Armor, Shield
{
	public String ID(){	return "StdShield";}
	public StdShield()
	{
		super();

		name="a shield";
		displayText="a sturdy round shield sits here.";
		description="Its made of steel, and looks in good shape.";
		properWornBitmap=Item.HELD;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(10);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(15);
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}

	public Environmental newInstance()
	{
		return new StdShield();
	}
}
