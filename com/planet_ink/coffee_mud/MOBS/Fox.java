package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Fox extends StdMOB
{
	public String ID(){return "Fox";}
	public Fox()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a fox";
		setDescription("It\\'s got a red coat and a tail.  Never has there been a more majestic animal.");
		setDisplayText("A fox growls.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 55));
		setWimpHitPoint(2);

		baseCharStats().setStat(CharStats.INTELLIGENCE,1);
		baseCharStats().setMyRace(CMClass.getRace("Fox"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setDamage(6);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(5);
		baseEnvStats().setArmor(50);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Fox();
	}
}
