package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class LeatherBracers extends StdArmor
{
	public String ID(){	return "LeatherBracers";}
	public LeatherBracers()
	{
		super();

		setName("a pair of leather bracers");
		setDisplayText("a pair of leather bracers are here.");
		setDescription("Strong enough to protect your forearms against the strongest of feathers...");
		properWornBitmap=Item.ON_LEFT_WRIST | Item.ON_RIGHT_WRIST | Item.ON_ARMS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(1);
		baseEnvStats().setWeight(5);
		baseEnvStats().setAbility(0);
		baseGoldValue=5;
		recoverEnvStats();
		material=EnvResource.RESOURCE_LEATHER;
	}


}
