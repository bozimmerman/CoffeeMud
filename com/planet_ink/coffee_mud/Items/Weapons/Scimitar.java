package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Scimitar extends Sword
{
	public String ID(){	return "Scimitar";}
	public Scimitar()
	{
		super();

		setName("an ornate scimitar");
		setDisplayText("a rather ornate looking Scimitar leans against the wall.");
		setDescription("It has a metallic pommel, and a long curved blade.");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats().setWeight(4);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(8);
		baseGoldValue=15;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
		weaponType=TYPE_SLASHING;
	}


}
