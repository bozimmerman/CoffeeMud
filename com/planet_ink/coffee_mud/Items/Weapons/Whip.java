package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Whip extends StdWeapon
{
	public String ID(){	return "Whip";}
	public Whip()
	{
		super();

		setName("a long leather whip");
		setDisplayText("a long leather whip has been dropped by someone.");
		setMiscText("");
		setDescription("Weaved of leather with a nasty little barb at the end.");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(2);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(2);
		baseGoldValue=1;
		recoverEnvStats();
		material=EnvResource.RESOURCE_LEATHER;
		weaponType=Weapon.TYPE_SLASHING;//?????????
		weaponClassification=Weapon.CLASS_FLAILED;
	}

	public Environmental newInstance()
	{
		return new Whip();
	}
}
