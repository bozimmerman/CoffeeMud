package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class StuddedLeatherArmor extends StdArmor
{
	public String ID(){	return "StuddedLeatherArmor";}
	public StuddedLeatherArmor()
	{
		super();

		setName("suit of studded leather armor");
		setDisplayText("a suit of leather armor reinforced with decorative studs");
		setDescription("A suit of studded leather armor including everything to protect the body, legs and arms.");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(22);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(35);
		baseGoldValue=40;
		recoverEnvStats();
		material=EnvResource.RESOURCE_LEATHER;
	}
	public Environmental newInstance()
	{
		return new StuddedLeatherArmor();
	}
}
