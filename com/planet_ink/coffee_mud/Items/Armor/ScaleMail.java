package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class ScaleMail extends StdArmor
{
	public String ID(){	return "ScaleMail";}
	public ScaleMail()
	{
		super();

		setName("a suit of Scalemail");
		setDisplayText("a suit of armor made of overlapping leather scales.");
		setDescription("This suit of armor is made of overlapping leather scales and will provide protection for the torso, arms, and legs.");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(29);
		baseEnvStats().setWeight(60);
		baseEnvStats().setAbility(0);
		baseGoldValue=240;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}

}
