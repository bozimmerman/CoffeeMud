package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Buffalo extends StdMOB
{
	public String ID(){return "Buffalo";}
	public Buffalo()
	{
		super();
		Username="a buffalo";
		setDescription("A large lumbering beast that looks too slow to get out of your way.");
		setDisplayText("A huge buffalo grazes here.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);

		baseEnvStats().setDamage(1);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(3);
		baseEnvStats().setArmor(90);
		baseCharStats().setMyRace(CMClass.getRace("Buffalo"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}