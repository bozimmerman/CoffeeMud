package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Dirk extends Dagger
{
	public Dirk()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a dirk";
		displayText="a pointy dirk is on the ground.";
		miscText="";
		description="The dirk is a single-edged, grooved weapon with a back edge near the point. ";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(1);
		baseGoldValue=2;
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(4);
		weaponType=TYPE_PIERCING;
		material=EnvResource.RESOURCE_STEEL;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Dirk();
	}
}
