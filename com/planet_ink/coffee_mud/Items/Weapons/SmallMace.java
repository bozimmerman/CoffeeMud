package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class SmallMace extends StdWeapon
{
	public String ID(){	return "SmallMace";}
	public SmallMace()
	{
		super();

		name="a small mace";
		displayText="a small mace has been left here.";
		miscText="";
		description="It's metallic and quite hard..";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(10);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(4);
		baseGoldValue=8;
		recoverEnvStats();
		weaponType=TYPE_BASHING;
		material=EnvResource.RESOURCE_STEEL;
		weaponClassification=Weapon.CLASS_BLUNT;
	}

	public Environmental newInstance()
	{
		return new SmallMace();
	}
}
