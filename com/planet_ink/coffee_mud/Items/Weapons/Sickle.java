package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Sickle extends StdWeapon
{
	public String ID(){	return "Sickle";}
	public Sickle()
	{
		super();

		name="a sickle";
		displayText="a sickle lies on the ground.";
		miscText="";
		description="A long and very curvy blade attached to a wooden handle.";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(3);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(5);
		baseGoldValue=1;
		recoverEnvStats();
		material=EnvResource.RESOURCE_OAK;
		weaponType=TYPE_PIERCING;
		weaponClassification=Weapon.CLASS_EDGED;
	}

	public Environmental newInstance()
	{
		return new Sickle();
	}

}
