package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Katana extends Sword
{
	public String ID(){	return "Katana";}
	public Katana()
	{
		super();

		setName("a katana");
		setDisplayText("a very ornate katana rests in the room.");
		setDescription("Just your typical, run-of-the-mill ninja sword--wrapped handle, steel blade, etc.");
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
		return new Katana();
	}
}
