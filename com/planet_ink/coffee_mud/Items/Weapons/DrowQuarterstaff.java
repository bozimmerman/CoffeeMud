package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class DrowQuarterstaff extends Mace
{
	public String ID(){	return "DrowQuarterstaff";}
	public DrowQuarterstaff()
	{
		super();

		name="a quarterstaff";
		displayText="a quarterstaff is on the ground.";
		miscText="";
		description="A quarterstaff made out of a very dark material metal.";
		secretIdentity="A Drow quarterstaff";
		baseEnvStats().setAbility(Dice.roll(1,6,0));
		baseEnvStats().setLevel(1);
		baseEnvStats().setWeight(4);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(6);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		baseGoldValue=2500;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
		weaponType=TYPE_BASHING;
	}

	public Environmental newInstance()
	{
		return new DrowQuarterstaff();
	}

}
