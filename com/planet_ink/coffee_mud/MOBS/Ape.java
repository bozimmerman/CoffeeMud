package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Ape extends StdMOB
{

	public Ape()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="an ape";
		setDescription("The ape is big, the ape is black, the ape means business..");
		setDisplayText("An ape sits here watching you.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(2);

		baseEnvStats().setDamage(1);

		baseCharStats().setMyRace(CMClass.getRace("Ape"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(6);
		baseEnvStats().setArmor(50);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Ape();
	}
}
