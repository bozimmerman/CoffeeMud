package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class BattleAxe extends Sword
{
	public String ID(){	return "BattleAxe";}
	public BattleAxe()
	{
		super();

		setName("a battle axe");
		setDisplayText("a heavy battle axe sits here");
		setDescription("It has a stout pole, about 4 feet in length with a trumpet shaped blade.");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(15);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(8);
		baseGoldValue=35;
		wornLogicalAnd=true;
		material=EnvResource.RESOURCE_STEEL;
		properWornBitmap=Item.HELD|Item.WIELD;
		recoverEnvStats();
		weaponType=Weapon.TYPE_SLASHING;
		weaponClassification=Weapon.CLASS_AXE;
	}

	public Environmental newInstance()
	{
		return new BattleAxe();
	}
}
