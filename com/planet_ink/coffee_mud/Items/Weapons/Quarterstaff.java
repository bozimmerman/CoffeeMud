package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Quarterstaff extends StdWeapon
{
	public String ID(){	return "Quarterstaff";}
	public Quarterstaff()
	{
		super();

		setName("a wooden quarterstaff");
		setDisplayText("a wooden quarterstaff lies in the corner of the room.");
		setDescription("It`s long and wooden, just like a quarterstaff ought to be.");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(4);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(3);
		baseGoldValue=1;
		recoverEnvStats();
		wornLogicalAnd=true;
		material=EnvResource.RESOURCE_OAK;
		properWornBitmap=Item.HELD|Item.WIELD;
		weaponType=TYPE_BASHING;
		weaponClassification=Weapon.CLASS_STAFF;
	}

	public Environmental newInstance()
	{
		return new Quarterstaff();
	}
}
