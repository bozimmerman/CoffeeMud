package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Jaguar extends StdMOB
{

	public Jaguar()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a jaguar";
		setDescription("A powerful cat with a deep chest and muscular limbs.  It\\`s covered in light yellow fur with black spots.");
		setDisplayText("A jaguar prowls quietly.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 45));
		setWimpHitPoint(2);

		baseEnvStats.setWeight(200 + Math.abs(randomizer.nextInt() % 55));


		baseCharStats().setStat(CharStats.INTELLIGENCE,1 + Math.abs(randomizer.nextInt() % 4));
		baseCharStats().setStat(CharStats.STRENGTH,12);
		baseCharStats().setStat(CharStats.DEXTERITY,17);
		baseCharStats().setMyRace(CMClass.getRace("GreatCat"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setDamage(8);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(4);
		baseEnvStats().setArmor(60);

		baseState.setHitPoints((Math.abs(randomizer.nextInt() % 18)*baseEnvStats().level()) + 9);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Jaguar();
	}
}
