package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class LeatherCap extends StdArmor
{
	public String ID(){	return "LeatherCap";}
	public LeatherCap()
	{
		super();

		setName("a leather cap");
		setDisplayText("a round leather cap sits here.");
		setDescription("It looks like its made of cured leather hide, with metal bindings.");
		properWornBitmap=Item.ON_HEAD;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(4);
		baseEnvStats().setWeight(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=5;
		recoverEnvStats();
		material=EnvResource.RESOURCE_LEATHER;
	}
	public Environmental newInstance()
	{
		return new LeatherCap();
	}
}
