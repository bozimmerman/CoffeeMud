package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class SplintMail extends StdArmor
{
	public String ID(){	return "SplintMail";}
	public SplintMail()
	{
		super();

		name="suit of splint mail";
		displayText="a suit of splint mail.";
		description="A suit of splint mail armor including everything to protect the body, legs and arms.";
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(44);
		baseEnvStats().setWeight(60);
		baseEnvStats().setAbility(0);
		baseGoldValue=160;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}
	public Environmental newInstance()
	{
		return new SplintMail();
	}
}
