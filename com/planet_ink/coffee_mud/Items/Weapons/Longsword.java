package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Longsword extends Sword
{
	public String ID(){	return "Longsword";}
	public Longsword()
	{
		super();

		name="a fancy longsword";
		displayText="a fancy longsword has been dropped on the ground.";
		miscText="";
		description="A standard one-handed sword.";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(4);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(8);
		baseGoldValue=15;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
		weaponType=TYPE_SLASHING;
	}

	public Environmental newInstance()
	{
		return new Longsword();
	}
}
