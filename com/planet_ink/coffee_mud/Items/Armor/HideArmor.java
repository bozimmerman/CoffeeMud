package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class HideArmor extends StdArmor
{
	public String ID(){	return "HideArmor";}
	public HideArmor()
	{
		super();

		setName("suit of Hide Armor");
		setDisplayText("a suit of armor made from animal hides");
		setDescription("A suit of armor made from animal hides including everything to protect the body, legs and arms.");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(29);
		baseEnvStats().setWeight(15);
		baseEnvStats().setAbility(0);
		baseGoldValue=30;
		recoverEnvStats();
		material=EnvResource.RESOURCE_LEATHER;
	}
	public Environmental newInstance()
	{
		return new HideArmor();
	}
}
