package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Ogre extends StdMOB
{

	public Ogre()
	{

		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="an Ogre";
		setDescription("Nine foot tall and with skin that is a covered in bumps and dead yellow in color..");
		setDisplayText("An ogre stares at you while he clenches his fists.");
		setAlignment(0);
		setMoney(10);
		baseEnvStats.setWeight(350);
		setWimpHitPoint(0);
		baseEnvStats().setDamage(12);

		baseCharStats().setStat(CharStats.INTELLIGENCE,8);
		baseCharStats().setStat(CharStats.CHARISMA,2);
		baseCharStats().setStat(CharStats.STRENGTH,22);
		baseCharStats().setMyRace(CMClass.getRace("Giant"));

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(4);
		baseEnvStats().setArmor(50);
		baseEnvStats().setSpeed(3.0);

		int hitPoints = 0;
		for(int i = 0; i < 4; i++)
			hitPoints += Math.abs(randomizer.nextInt()) % 16 + 1;
		hitPoints++;

		baseState.setHitPoints(hitPoints);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Ogre();
	}
}
