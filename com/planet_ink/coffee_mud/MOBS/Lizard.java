package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Lizard extends StdMOB
{

	public Lizard()
	{
		super();
		Username="a lizard";
		setDescription("A small unobtrusize reptile with rough green skin.");
		setDisplayText("A lizard scurries by.");
		setAlignment(500);
		setMoney(0);

		baseEnvStats().setDamage(1);

		baseCharStats().setStat(CharStats.INTELLIGENCE,1);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(90);

		baseCharStats().setMyRace(CMClass.getRace("Lizard"));
		baseCharStats().getMyRace().startRacing(this,false);
		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Lizard();
	}
}