package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Falcon extends StdMOB
{

	public Falcon()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a falcon";
		setDescription("a small hunting bird.");
		setDisplayText("A falcon flies nearby.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);

		baseEnvStats.setWeight(1 + Math.abs(randomizer.nextInt() % 6));


		baseCharStats().setStat(CharStats.INTELLIGENCE,4);
		baseCharStats().setStat(CharStats.STRENGTH,10);
		baseCharStats().setStat(CharStats.DEXTERITY,17);

		baseEnvStats().setDamage(1);
		baseEnvStats().setSpeed(3.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(2);
		baseEnvStats().setArmor(50);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_FLYING);

		baseState.setHitPoints((Math.abs(randomizer.nextInt() % 4)*baseEnvStats().level()) + 1);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Falcon();
	}
}
