package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Rabbit extends StdMOB
{

	public Rabbit()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a rabbit";
		setDescription("It\\`s small, cute, and fluffy with a cute cotton-ball tail.");
		setDisplayText("A rabbit hops by.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 55));
		setWimpHitPoint(2);

		baseEnvStats().setDamage(2);

		baseCharStats().setStat(CharStats.INTELLIGENCE,1);
		baseCharStats().setMyRace(CMClass.getRace("Rabbit"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(30);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Rabbit();
	}
}
