package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Shortsword extends Sword
{
	public String ID(){	return "Shortsword";}
	public Shortsword()
	{
		super();

		name="a short sword";
		displayText="a short sword has been dropped on the ground.";
		miscText="";
		description="A sword with a not-too-long blade.";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(3);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(5);
		baseGoldValue=10;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
		weaponType=TYPE_PIERCING;
	}

	public Environmental newInstance()
	{
		return new Shortsword();
	}
}
