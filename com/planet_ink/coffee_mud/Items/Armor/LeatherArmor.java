package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class LeatherArmor extends StdArmor
{
	public String ID(){	return "LeatherArmor";}
	public LeatherArmor()
	{
		super();

		name="a suit of leather armor";
		displayText="a suit of leather armor including a breastplate, arms, and legs.";
		description="This is a fairly decent looking suit of leather armor.";
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(12);
		baseEnvStats().setWeight(15);
		baseEnvStats().setAbility(0);
		baseGoldValue=10;
		recoverEnvStats();
		material=EnvResource.RESOURCE_LEATHER;
	}
	public Environmental newInstance()
	{
		return new LeatherArmor();
	}
}
