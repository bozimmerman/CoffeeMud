package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class Dress extends StdArmor
{
	public String ID(){	return "Dress";}
	public Dress()
	{
		super();

		setName("a nice dress");
		setDisplayText("a nice dress has been left here.");
		setDescription("Well and neatly made, this plain dress would look fine on just about anyone.");
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
		return new Dress();
	}

}
