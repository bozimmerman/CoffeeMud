package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class GreatHelm extends StdArmor
{
	public String ID(){	return "GreatHelm";}
	public GreatHelm()
	{
		super();

		setName("a steel Great Helm.");
		setDisplayText("a steel great helm sits here.");
		setDescription("This is a steel helmet that completely encloses the head.");
		properWornBitmap=Item.ON_HEAD;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(18);
		baseEnvStats().setWeight(10);
		baseEnvStats().setAbility(0);
		baseGoldValue=60;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}

}
