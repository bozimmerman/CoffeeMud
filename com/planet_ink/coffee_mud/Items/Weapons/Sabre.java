package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Sabre extends StdWeapon
{
	public String ID(){	return "Sabre";}
	public Sabre()
	{
		super();

		name="a sabre";
		displayText="a sabre has been dropped by someone.";
		miscText="";
		description="A slender piece of metal with a fancy silver basket-hilt.";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(2);
		baseEnvStats.setWeight(5);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(6);
		baseGoldValue=15;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
		weaponType=TYPE_SLASHING;//?????????
		weaponClassification=Weapon.CLASS_SWORD;
	}

	public Environmental newInstance()
	{
		return new Sabre();
	}
}
