package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class GenShield extends GenArmor implements Shield
{
	public String ID(){	return "GenShield";}
	public GenShield()
	{
		super();

		name="a shield";
		displayText="a sturdy round shield sits here.";
		description="";
		properWornBitmap=Item.HELD;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(10);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(15);
		recoverEnvStats();
		material=EnvResource.RESOURCE_OAK;
	}
	public boolean isGeneric(){return true;}

	public Environmental newInstance()
	{
		return new GenShield();
	}
}
