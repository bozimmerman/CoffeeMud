package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Javelin extends StdWeapon
{
	public Javelin()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a steel javelin";
		displayText="a steel javelin sticks out from the wall.";
		miscText="";
		description="It's metallic and quite sharp..";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(2);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(6);
		baseGoldValue=1;
		setUsesRemaining(1);
		recoverEnvStats();
		material=EnvResource.RESOURCE_OAK;
		weaponType=TYPE_PIERCING;
		weaponClassification=Weapon.CLASS_RANGED;
	}

	public Environmental newInstance()
	{
		return new Javelin();
	}
}
