package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Mouse extends StdMOB
{

	public Mouse()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a mouse";
		setDescription("The little mouse is a small white rodent.");
		setDisplayText("A little mouse tries to scurry out of your way.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(2);

		baseEnvStats().setDamage(1);

		baseCharStats().setMyRace(CMClass.getRace("Mouse"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(60);

		baseState.setHitPoints((Math.abs(randomizer.nextInt() % 10)*baseEnvStats().level()) + 1);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Mouse();
	}
}
