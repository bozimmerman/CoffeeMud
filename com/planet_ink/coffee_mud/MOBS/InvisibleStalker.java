package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class InvisibleStalker extends StdMOB
{
	public String ID(){return "InvisibleStalker";}
	public InvisibleStalker()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="an Invisible Stalker";
		setDescription("A shimmering blob of energy.");
		setDisplayText("An invisible stalker hunts here.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(10 + Math.abs(randomizer.nextInt() % 10));


		baseCharStats().setStat(CharStats.INTELLIGENCE,12 + Math.abs(randomizer.nextInt() % 3));
		baseCharStats().setStat(CharStats.STRENGTH,20);
		baseCharStats().setStat(CharStats.DEXTERITY,13);

		baseEnvStats().setDamage(16);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(4);
		baseEnvStats().setArmor(0);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_INVISIBLE);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		addBehavior(CMClass.getBehavior("Aggressive"));
		addBehavior(CMClass.getBehavior("Mobile"));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new InvisibleStalker();
	}
}
