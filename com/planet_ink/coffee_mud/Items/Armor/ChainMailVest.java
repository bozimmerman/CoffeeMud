package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class ChainMailVest extends StdArmor
{
	public String ID(){	return "ChainMailVest";}
	public ChainMailVest()
	{
		super();

		setName("a chain mail vest");
		setDisplayText("a chain mail vest sits here.");
		setDescription("This is fairly solid looking vest made of chain mail.");
		properWornBitmap=Item.ON_TORSO;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(25);
		baseEnvStats().setWeight(30);
		baseEnvStats().setAbility(0);
		baseGoldValue=75;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}

}
