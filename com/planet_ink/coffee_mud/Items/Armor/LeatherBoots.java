package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class LeatherBoots extends StdArmor
{
	public String ID(){	return "LeatherBoots";}
	public LeatherBoots()
	{
		super();

		name="a pair of leather boots";
		displayText="a pair of leather boots sits here.";
		description="They look like a rather nice pair of footwear.";
		properWornBitmap=Item.ON_FEET;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(1);
		baseEnvStats().setWeight(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=5;
		recoverEnvStats();
		material=EnvResource.RESOURCE_LEATHER;
	}
	public Environmental newInstance()
	{
		return new LeatherBoots();
	}

}
