package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Cheetah extends StdMOB
{
	public String ID(){return "Cheetah";}
	public Cheetah()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a cheetah";
		setDescription("A medium-sized, lightly built cat with sand covered fur and black spot.");
		setDisplayText("A cheetah stalks its prey.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 45));
		setWimpHitPoint(2);

		baseEnvStats.setWeight(150 + Math.abs(randomizer.nextInt() % 55));


		baseCharStats().setStat(CharStats.INTELLIGENCE,1);
		baseCharStats().setStat(CharStats.STRENGTH,12);
		baseCharStats().setStat(CharStats.DEXTERITY,18);
		baseCharStats().setMyRace(CMClass.getRace("GreatCat"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setDamage(8);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(3);
		baseEnvStats().setArmor(60);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
