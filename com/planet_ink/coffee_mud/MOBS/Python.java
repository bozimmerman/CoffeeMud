package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Python extends StdMOB
{

	public Python()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a python";
		setDescription("A humungous snake that is known for squeezing you to DEATH.");
		setDisplayText("A python wants to give you a hug.");
		setAlignment(500);
		setMoney(0);

		baseEnvStats().setDamage(7);

		baseCharStats().setStat(CharStats.INTELLIGENCE,1);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(10);

		baseCharStats().setMyRace(CMClass.getRace("Snake"));
		baseCharStats().getMyRace().startRacing(this,false);
		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Python();
	}
}