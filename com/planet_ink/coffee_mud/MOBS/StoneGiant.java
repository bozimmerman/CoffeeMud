package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class StoneGiant extends StdMOB
{
	public String ID(){return "StoneGiant";}
	public StoneGiant()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a Stone Giant";
		setDescription("A tall humanoid standing about 18 feet tall with gray, hairless flesh.");
		setDisplayText("A Stone Giant glares at you.");
		setAlignment(0);
		setMoney(0);
		baseEnvStats.setWeight(8000 + Math.abs(randomizer.nextInt() % 1001));


		baseCharStats().setStat(CharStats.INTELLIGENCE,8 + Math.abs(randomizer.nextInt() % 3));
		baseCharStats().setStat(CharStats.STRENGTH,20);
		baseCharStats().setStat(CharStats.DEXTERITY,13);

		baseEnvStats().setDamage(20);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(14);
		baseEnvStats().setArmor(0);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		addBehavior(CMClass.getBehavior("Aggressive"));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new StoneGiant();
	}
}
