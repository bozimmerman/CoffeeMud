package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Zombie extends Undead
{

	public Zombie()
	{

		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a zombie";
		setDescription("decayed and rotting, a dead body has been brought back to life...");
		setDisplayText("a skeleton slowly moves about.");
		setMoney(10);
		baseEnvStats.setWeight(30);

		baseEnvStats().setDamage(8);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(80);
		baseEnvStats().setSpeed(1.0);

		int hitPoints = 0;
		hitPoints += Math.abs(randomizer.nextInt()) % 18 + 1;
		hitPoints += Math.abs(randomizer.nextInt()) % 18 + 1;

		baseState.setHitPoints(hitPoints);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Zombie();
	}
}
