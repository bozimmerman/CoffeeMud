package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;

public class StdSling extends StdWeapon
{
	public StdSling()
	{
		super();
		name="a sling";
		displayText="a sling has been left here.";
		miscText="";
		description="It looks like it might shoot bullets!";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(8);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(2);
		setAmmunitionType("bullets");
		setAmmoCapacity(50);
		setAmmoRemaining(10);
		baseGoldValue=150;
		recoverEnvStats();
		minRange=1;
		maxRange=2;
		weaponType=Weapon.TYPE_BASHING;
		material=EnvResource.RESOURCE_LEATHER;
		weaponClassification=Weapon.CLASS_RANGED;
		setRawLogicalAnd(false);
	}

	public Environmental newInstance()
	{
		return new StdSling();
	}
}
