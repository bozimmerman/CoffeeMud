package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class ShieldWeapon extends StdWeapon
{
	public String ID(){	return "ShieldWeapon";}
	public ShieldWeapon()
	{
		super();

		setName("a bashing shield");
		setDisplayText("A bashing shield has been left here.");
		setMiscText("");
		setDescription("Looks like natural fighting ability.");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats().setWeight(0);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(1);
		weaponType=Weapon.TYPE_BASHING;
		material=EnvResource.RESOURCE_STEEL;
		weaponClassification=Weapon.CLASS_BLUNT;
		recoverEnvStats();
	}

	public void setShield(Item shield)
	{
		name=shield.name();
		displayText=shield.displayText();
		miscText="";
		setDescription(shield.description());
		baseEnvStats().setDamage(shield.envStats().level());
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats().setWeight(0);
		baseEnvStats().setAttackAdjustment(0);
		weaponType=Weapon.TYPE_BASHING;
		recoverEnvStats();
	}
	public ShieldWeapon(Item shield)
	{
		super();

		setShield(shield);
	}

	public Environmental newInstance()
	{
		return new ShieldWeapon();
	}
}
