package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Glaive extends StdWeapon
{
	public String ID(){	return "Glaive";}
	public Glaive()
	{
		super();

		setName("a heavy glaive");
		setDisplayText("a glaive leans against the wall.");
		setDescription("A long blade on a pole.");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(8);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(6);
		weaponType=TYPE_SLASHING;
		baseGoldValue=6;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
		wornLogicalAnd=true;
		properWornBitmap=Item.HELD|Item.WIELD;
		weaponClassification=Weapon.CLASS_POLEARM;
	}




}
