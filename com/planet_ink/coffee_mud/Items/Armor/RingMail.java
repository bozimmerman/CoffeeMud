package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class RingMail extends StdArmor
{
	public String ID(){	return "RingMail";}
	public RingMail()
	{
		super();

		setName("suit of ring mail");
		setDisplayText("a suit of armor made with large metal rings fastened to leather");
		setDescription("A suit of ring mail including everything to protect the body, legs and arms.");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(22);
		baseEnvStats().setWeight(50);
		baseEnvStats().setAbility(0);
		baseGoldValue=200;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}

}
