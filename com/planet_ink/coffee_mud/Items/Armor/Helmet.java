package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class Helmet extends StdArmor
{
	public String ID(){	return "Helmet";}
	public Helmet()
	{
		super();

		setName("a helmet");
		setDisplayText("a helmet sits here.");
		setDescription("This is fairly solid looking helmet.");
		properWornBitmap=Item.ON_HEAD;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(10);
		baseEnvStats().setWeight(10);
		baseEnvStats().setAbility(0);
		baseGoldValue=16;
		material=EnvResource.RESOURCE_IRON;
		recoverEnvStats();
	}

}
