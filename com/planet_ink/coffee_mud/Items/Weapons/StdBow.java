package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;

public class StdBow extends StdWeapon
{
	public String ID(){	return "StdBow";}
	public StdBow()
	{
		super();
		setName("a short bow");
		setDisplayText("a short bow has been left here.");
		setDescription("It looks like it might shoot arrows!");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(8);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(5);
		setAmmunitionType("arrows");
		setAmmoCapacity(20);
		setAmmoRemaining(20);
		baseGoldValue=150;
		recoverEnvStats();
		minRange=1;
		maxRange=3;
		weaponType=Weapon.TYPE_PIERCING;
		material=EnvResource.RESOURCE_WOOD;
		weaponClassification=Weapon.CLASS_RANGED;
		setRawLogicalAnd(true);
	}

	public Environmental newInstance()
	{
		return new StdBow();
	}
}
