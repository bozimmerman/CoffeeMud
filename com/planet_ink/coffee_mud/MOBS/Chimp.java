package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Chimp extends StdMOB
{

	public Chimp()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a chimp";
		setDescription("The chimp is reddish brown with a big pink butt.");
		setDisplayText("A chimp sits here watching you.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(2);

		baseEnvStats().setDamage(1);

		baseCharStats().setMyRace(CMClass.getRace("Chimp"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(4);
		baseEnvStats().setArmor(60);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Chimp();
	}
}
