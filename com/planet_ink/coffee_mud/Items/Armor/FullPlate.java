package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class FullPlate extends StdArmor
{
	public String ID(){	return "FullPlate";}
	public FullPlate()
	{
		super();

		name="suit of Full Plate";
		displayText="a suit of Full Plate Armor.";
		description="A suit of Full Plate Armor including everything from head to toe.  Fine workmanship make this both very decorative and functional.";
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS | Item.ON_FEET | Item.ON_HEAD | Item.ON_HANDS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(90);
		baseEnvStats().setWeight(90);
		baseEnvStats().setAbility(0);
		baseGoldValue=20000;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}
	public Environmental newInstance()
	{
		return new FullPlate();
	}
}
