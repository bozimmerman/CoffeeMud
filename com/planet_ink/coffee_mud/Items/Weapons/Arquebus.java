package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Arquebus extends StdWeapon
{
	public Arquebus()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="an arquebus";
		displayText="an arquebus is on the ground.";
		miscText="";
		description="It\\`s got a metal barrel and wooden stock.";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(15);

		baseEnvStats().setAttackAdjustment(-1);
		baseEnvStats().setDamage(10);

		setAmmunitionType("bullets");
		setAmmoCapacity(1);
		setAmmoRemaining(1);
		minRange=0;
		maxRange=5;
		baseGoldValue=500;
		recoverEnvStats();
		wornLogicalAnd=true;
		material=EnvResource.RESOURCE_IRON;
		properWornBitmap=Item.HELD|Item.WIELD;
		weaponClassification=Weapon.CLASS_RANGED;
		weaponType=Weapon.TYPE_PIERCING;
	}

	public Environmental newInstance()
	{
		return new Arquebus();
	}

//	private boolean isBackfire()
//	{
//
//	}

}
