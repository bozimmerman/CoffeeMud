package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class ChainMailArmor extends StdArmor
{
	public String ID(){	return "ChainMailArmor";}
	public ChainMailArmor()
	{
		super();

		setName("a suit of chain mail armor");
		setDisplayText("a suit of chain mail armor sits here.");
		setDescription("This suit includes a fairly solid looking hauberk with leggings and a coif.");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(37);
		baseEnvStats().setWeight(60);
		baseEnvStats().setAbility(0);
		baseGoldValue=150;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}
	public Environmental newInstance()
	{
		return new ChainMailArmor();
	}
}
