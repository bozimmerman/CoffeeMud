package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;

public class Dagger extends Weapon
{
	public Dagger()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a small dagger";
		displayText="a sharp little dagger lies here.";
		miscText="";
		description="It has a wooden handle and a metal blade.";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(1);
		baseGoldValue=2;
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(4);
		weaponType=TYPE_PIERCING;
		weaponClassification=Weapon.CLASS_EDGED;
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new Dagger();
	}

	public void strike(MOB source, MOB target, boolean success)
	{
		super.strike(source, target, success);
	}

}
