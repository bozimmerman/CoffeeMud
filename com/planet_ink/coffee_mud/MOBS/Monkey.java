package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Monkey extends StdMOB
{

	public Monkey()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a monkey";
		setDescription("The monkey is brown with a big pink butt.");
		setDisplayText("A silly monkey lops around here.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(2);

		baseEnvStats().setDamage(1);

		baseCharStats().setMyRace(CMClass.getRace("Monkey"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(2);
		baseEnvStats().setArmor(30);

		baseState.setHitPoints((Math.abs(randomizer.nextInt() % 12)*baseEnvStats().level()) + 5);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Monkey();
	}
}
