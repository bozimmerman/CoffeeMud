package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Mace extends StdWeapon
{
	public String ID(){	return "Mace";}
	public Mace()
	{
		super();

		setName("a rather large mace");
		setDisplayText("a heavy mace is found in the center of the room.");
		setDescription("It`s metallic and quite hard..");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(10);
		material=EnvResource.RESOURCE_STEEL;
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(7);
		baseGoldValue=8;
		recoverEnvStats();
		weaponType=TYPE_BASHING;
		weaponClassification=Weapon.CLASS_BLUNT;
	}


}
