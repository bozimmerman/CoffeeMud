package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class DrowSword extends Longsword
{
	public String ID(){	return "DrowSword";}
	public DrowSword()
	{
		super();

		setName("a longsword");
		setDisplayText("a fancy longsword has been dropped on the ground.");
		setMiscText("");
		setDescription("A one-handed sword with a very dark blade.");
		secretIdentity="A Drow Sword";
		baseEnvStats().setAbility(Dice.roll(1,6,0));
		baseEnvStats().setLevel(1);
		baseEnvStats().setWeight(4);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(8);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		baseGoldValue=2500;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
		weaponType=TYPE_SLASHING;
	}

	public Environmental newInstance()
	{
		return new DrowSword();
	}

}
