package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Tiger extends StdMOB
{

	public Tiger()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a tiger";
		setDescription("Tigers have reddish-orange fur and dark vertical stripes.");
		setDisplayText("A tiger prowls here.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(300 + Math.abs(randomizer.nextInt() % 55));


		baseCharStats().setStat(CharStats.INTELLIGENCE,1);
		baseCharStats().setStat(CharStats.STRENGTH,13);
		baseCharStats().setStat(CharStats.DEXTERITY,17);
		baseCharStats().setMyRace(CMClass.getRace("GreatCat"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setDamage(10);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(5);
		baseEnvStats().setArmor(60);

		baseState.setHitPoints((Math.abs(randomizer.nextInt() % 18)*baseEnvStats().level()) + 10);

		addBehavior(CMClass.getBehavior("Aggressive"));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Tiger();
	}
}
