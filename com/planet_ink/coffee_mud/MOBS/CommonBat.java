package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class CommonBat extends StdMOB
{

	public CommonBat()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a common bat";
		setDescription("It seemingly has the body of a rat with wings.");
		setDisplayText("A bat flies nearby.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);

		baseEnvStats.setWeight(1 + Math.abs(randomizer.nextInt() % 2));


		baseCharStats().setStat(CharStats.INTELLIGENCE,1);
		baseCharStats().setStat(CharStats.STRENGTH,12);
		baseCharStats().setStat(CharStats.DEXTERITY,17);

		baseEnvStats().setDamage(1);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(80);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_FLYING);
		baseCharStats().setMyRace(CMClass.getRace("Bat"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new CommonBat();
	}
}
