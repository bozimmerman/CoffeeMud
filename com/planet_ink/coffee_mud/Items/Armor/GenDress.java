package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class GenDress extends GenArmor
{
	public String ID(){	return "GenDress";}
	public GenDress()
	{
		super();

		setName("a nice dress");
		setDisplayText("a nice dress lies here");
		setDescription("a well tailored dress.");
		properWornBitmap=Item.ON_LEGS|Item.ON_WAIST|Item.ON_TORSO;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(2);
		baseEnvStats().setWeight(3);
		baseEnvStats().setAbility(0);
		baseGoldValue=1;
		recoverEnvStats();
		material=EnvResource.RESOURCE_COTTON;
	}

}
