package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class StoneGolem extends StdMOB
{
	public String ID(){return "StoneGolem";}
	public StoneGolem()
	{
		super();
		Username="a stone golem";
		setDescription("Looke like an abomination of arcane magic.");
		setDisplayText("A stone golem stares at you coldly");
		setAlignment(500);
		setMoney(0);

		baseEnvStats().setDamage(4);

		baseCharStats().setStat(CharStats.INTELLIGENCE,1);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(25);
		baseEnvStats().setArmor(-100);

		baseCharStats().setMyRace(CMClass.getRace("StoneGolem"));
		baseCharStats().getMyRace().startRacing(this,false);
		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new StoneGolem();
	}
}