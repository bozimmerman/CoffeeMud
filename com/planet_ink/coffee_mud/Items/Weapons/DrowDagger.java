package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class DrowDagger extends Dagger
{
	public String ID(){	return "DrowDagger";}
	public DrowDagger()
	{
		super();

		name="a dagger";
		displayText="a dagger with a dark metallic blade.";
		miscText="";
		description="A dagger made out of a very dark material metal.";
		secretIdentity="A Drow dagger";
		baseEnvStats().setAbility(Dice.roll(1,6,0));
		baseEnvStats().setLevel(1);
		baseEnvStats().setWeight(4);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(4);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		baseGoldValue=2500;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
		weaponType=TYPE_BASHING;
	}

	public Environmental newInstance()
	{
		return new DrowDagger();
	}

}
