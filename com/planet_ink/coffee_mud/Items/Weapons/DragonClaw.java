package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class DragonClaw extends Natural
{
	public String ID(){	return "DragonClaw";}
	public DragonClaw()
	{
		super();

		setName("a vicious dragons claw.");
		setDisplayText("a Dragons Claw");
		setDescription("the claw of a dragon");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(7);
		baseEnvStats().setAttackAdjustment(2);
		baseEnvStats().setDamage(8);
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
		weaponType=TYPE_SLASHING;
		weaponClassification=Weapon.CLASS_NATURAL;
	}

	public Environmental newInstance()
	{
		return new DragonClaw();
	}
}
