package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Rapier extends Sword
{
	public String ID(){	return "Rapier";}
	public Rapier()
	{
		super();

		setName("an sleek rapier");
		setDisplayText("a sleek rapier sits on the ground.");
		setDescription("It has a long, thin metal blade.");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(4);
		material=EnvResource.RESOURCE_STEEL;
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(7);
		baseGoldValue=15;
		recoverEnvStats();
		weaponType=TYPE_PIERCING;
	}

	public Environmental newInstance()
	{
		return new Rapier();
	}
}
