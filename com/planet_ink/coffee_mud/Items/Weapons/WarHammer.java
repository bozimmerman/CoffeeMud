package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class WarHammer extends StdWeapon
{
	public WarHammer()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a warhammer";
		displayText="a brutal warhammer sits here";
		miscText="";
		description="It has a large wooden handle with a brutal blunt double-head.";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(10);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(6);
		baseGoldValue=25;
		wornLogicalAnd=true;
		properWornBitmap=Item.HELD|Item.WIELD;
		recoverEnvStats();
		weaponType=Weapon.TYPE_BASHING;
		material=EnvResource.RESOURCE_STEEL;
		weaponClassification=Weapon.CLASS_HAMMER;
	}

	public Environmental newInstance()
	{
		return new WarHammer();
	}
}
