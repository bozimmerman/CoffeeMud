package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class Shoes extends StdArmor
{
	public String ID(){	return "Shoes";}
	public Shoes()
	{
		super();

		setName("a pair of shoes");
		setDisplayText("a pair of shoes lies here");
		setDescription("a well tailored pair of walking shoes.");
		properWornBitmap=Item.ON_FEET;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(1);
		baseEnvStats().setWeight(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=1;
		recoverEnvStats();
		material=EnvResource.RESOURCE_COTTON;
	}

}
