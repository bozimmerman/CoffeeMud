package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Centaur extends StdMOB
{

	public Centaur()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a centaur";
		setDescription("A creature whose upper body is that of a man, and lower body that of a horse.");
		setDisplayText("A centaur gallops around...");
		setAlignment(750);
		setMoney(200);
		baseEnvStats.setWeight(600 + Math.abs(randomizer.nextInt() % 101));


		baseCharStats().setStat(CharStats.INTELLIGENCE,5 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.STRENGTH,12 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.DEXTERITY,9 + Math.abs(randomizer.nextInt() % 6));

		baseEnvStats().setDamage(7);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(4);
		baseEnvStats().setArmor(0);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		addBehavior(CMClass.getBehavior("Mobile"));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Centaur();
	}
}
