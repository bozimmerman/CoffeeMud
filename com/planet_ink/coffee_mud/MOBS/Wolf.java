package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Wolf extends StdMOB
{

	public Wolf()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a wolf";
		setDescription("A powerful wolf with grey fur and amber eyes.");
		setDisplayText("A wolf growls and stares at you.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);

		baseEnvStats.setWeight(50 + Math.abs(randomizer.nextInt() % 55));


		baseCharStats().setStat(CharStats.INTELLIGENCE,1);
		baseCharStats().setStat(CharStats.STRENGTH,10);
		baseCharStats().setStat(CharStats.DEXTERITY,14);
		baseCharStats().setMyRace(CMClass.getRace("Wolf"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setDamage(6);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(70);

		baseState.setHitPoints((Math.abs(randomizer.nextInt() % 10)*baseEnvStats().level()) + 9);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Wolf();
	}
}
