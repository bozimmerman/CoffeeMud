package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Dirk extends Dagger
{
	public String ID(){	return "Dirk";}
	public Dirk()
	{
		super();

		setName("a dirk");
		setDisplayText("a pointy dirk is on the ground.");
		setMiscText("");
		setDescription("The dirk is a single-edged, grooved weapon with a back edge near the point. ");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(1);
		baseGoldValue=2;
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(4);
		weaponType=TYPE_PIERCING;
		material=EnvResource.RESOURCE_STEEL;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Dirk();
	}
}
