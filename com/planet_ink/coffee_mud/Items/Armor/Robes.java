package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class Robes extends StdArmor
{
	public String ID(){	return "Robes";}
	public Robes()
	{
		super();

		setName("a set of robes");
		setDisplayText("a set of robes is folded nice and neatly here.");
		setDescription("It is a finely crafted set of robes.");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(8);
		baseEnvStats().setWeight(10);
		baseEnvStats().setAbility(0);
		baseGoldValue=5;
		recoverEnvStats();
		material=EnvResource.RESOURCE_COTTON;
	}
	public Environmental newInstance()
	{
		return new Robes();
	}

}
