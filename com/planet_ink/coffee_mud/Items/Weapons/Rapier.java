package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Rapier extends Sword
{
	public Rapier()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="an sleek rapier";
		displayText="a sleek rapier sits on the ground.";
		miscText="";
		description="It has a long, thin metal blade.";
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
