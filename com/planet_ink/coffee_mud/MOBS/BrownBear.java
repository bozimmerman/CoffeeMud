package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class BrownBear extends StdMOB
{

	public BrownBear()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a Brown Bear";
		setDescription("A bear, large and husky with brown fur.");
		setDisplayText("A brown bear hunts here.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 45));
		setWimpHitPoint(2);

		baseEnvStats.setWeight(450 + Math.abs(randomizer.nextInt() % 55));


		baseCharStats().setStat(CharStats.INTELLIGENCE,1);
		baseCharStats().setStat(CharStats.STRENGTH,18);
		baseCharStats().setStat(CharStats.DEXTERITY,16);
		baseCharStats().setMyRace(CMClass.getRace("Bear"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setDamage(8);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(5);
		baseEnvStats().setArmor(60);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new BrownBear();
	}
}
