package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class SmallDagger extends Dagger
{
	public String ID(){	return "SmallDagger";}
	public SmallDagger()
	{
		super();

		baseEnvStats().setDamage(3);
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}

	public Environmental newInstance()
	{
		return new SmallDagger();
	}

}
