package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class FieldPlate extends StdArmor
{
	public String ID(){	return "FieldPlate";}
	public FieldPlate()
	{
		super();

		setName("suit of Field Plate");
		setDisplayText("a suit of field plate Armor.");
		setDescription("A suit of field plate Armor including everything to protect the body, legs and arms.");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(59);
		baseEnvStats().setWeight(80);
		baseEnvStats().setAbility(0);
		baseGoldValue=4000;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}
	public Environmental newInstance()
	{
		return new FieldPlate();
	}
}
