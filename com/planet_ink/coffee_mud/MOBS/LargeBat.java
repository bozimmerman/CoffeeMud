package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class LargeBat extends StdMOB
{

	public LargeBat()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a large bat";
		setDescription("It looks like a bat, just larger.");
		setDisplayText("A large bat flies nearby.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);

		baseEnvStats.setWeight(1 + Math.abs(randomizer.nextInt() % 10));


		baseCharStats().setIntelligence(1);
		baseCharStats().setStrength(12);
		baseCharStats().setDexterity(17);
		baseCharStats().setMyRace(CMClass.getRace("Bat"));

		baseEnvStats().setDamage(5);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(80);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|Sense.IS_FLYING);

		baseState.setHitPoints((Math.abs(randomizer.nextInt() % 4)*baseEnvStats().level()) + 1);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new LargeBat();
	}
}
