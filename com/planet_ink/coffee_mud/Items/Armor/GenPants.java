package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class GenPants extends GenArmor
{
	public String ID(){	return "GenPants";}
	public GenPants()
	{
		super();

		name="a pair of pants";
		displayText="a pair of pants lies here";
		description="a well tailored pair of travellors pants.";
		properWornBitmap=Item.ON_LEGS;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(1);
		baseEnvStats().setWeight(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=1;
		recoverEnvStats();
		material=EnvResource.RESOURCE_COTTON;
	}
	public Environmental newInstance()
	{
		return new GenPants();
	}
}
