package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class GenShirt extends GenArmor
{
	public String ID(){	return "GenShirt";}
	public GenShirt()
	{
		super();

		name="a nice tunic";
		displayText="a plain tunic is folded neatly here.";
		description="It is a plain buttoned tunic.";
		properWornBitmap=Item.ON_TORSO;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(2);
		baseEnvStats().setWeight(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=1;
		recoverEnvStats();
		material=EnvResource.RESOURCE_COTTON;
	}
	public Environmental newInstance()
	{
		return new GenShirt();
	}
}
