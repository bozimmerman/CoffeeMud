package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Bull extends StdMOB
{
	public String ID(){return "Bull";}
	public Bull()
	{
		super();
		Username="a bull";
		setDescription("A large lumbering beast that looks too slow to get out of your way.");
		setDisplayText("An old bull doesn`t look happy to see you.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);

		baseEnvStats().setDamage(10);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(7);
		baseEnvStats().setArmor(90);
		baseCharStats().setMyRace(CMClass.getRace("Cow"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}