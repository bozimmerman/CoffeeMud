package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Puppy extends StdMOB
{

	public Puppy()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a puppy";
		setDescription("It\\`s small, cute, and furry with four legs, just like a puppy ought to be.");
		setDisplayText("A puppy scurries nearby.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 55));
		setWimpHitPoint(2);

		addBehavior(CMClass.getBehavior("Follower"));
		addBehavior(CMClass.getBehavior("MudChat"));

		baseEnvStats().setDamage(4);

		baseCharStats().setStat(CharStats.INTELLIGENCE,1);
		baseCharStats().setMyRace(CMClass.getRace("Puppy"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(30);

		baseState.setHitPoints(Math.abs(randomizer.nextInt() % 12) + 2);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Puppy();
	}
}
