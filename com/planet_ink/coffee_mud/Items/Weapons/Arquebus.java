package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;

public class Arquebus extends Weapon
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
		
		baseGoldValue=500;
		recoverEnvStats();
		wornLogicalAnd=true;
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
