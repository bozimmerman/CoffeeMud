package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class GiantBat extends StdMOB
{

	public GiantBat()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a giant bat";
		setDescription("It is a giant version of your common bat.");
		setDisplayText("A giant bat flies nearby.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);

		baseEnvStats.setWeight(1 + Math.abs(randomizer.nextInt() % 100));


		baseCharStats().setStat(CharStats.INTELLIGENCE,7);
		baseCharStats().setStat(CharStats.STRENGTH,16);
		baseCharStats().setStat(CharStats.DEXTERITY,17);

		baseEnvStats().setDamage(8);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(4);
		baseEnvStats().setArmor(80);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_FLYING);

		baseState.setHitPoints((Math.abs(randomizer.nextInt() % 8)*baseEnvStats().level()) + 4);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new GiantBat();
	}
}
