package com.planet_ink.coffee_mud.Items.Weapons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;

public class StdSpear extends StdWeapon
{
	public StdSpear()
	{
		super();
		name="a spear";
		displayText="a spear has been left here.";
		miscText="";
		description="It looks like it might sail far!";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(8);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(5);
		baseGoldValue=10;
		recoverEnvStats();
		minRange=0;
		maxRange=3;
		weaponType=Weapon.TYPE_PIERCING;
		material=EnvResource.RESOURCE_WOOD;
		weaponClassification=Weapon.CLASS_THROWN;
		setRawLogicalAnd(false);
	}

	public Environmental newInstance()
	{
		return new StdSpear();
	}
}
