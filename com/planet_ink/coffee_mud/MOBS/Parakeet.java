package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Parakeet extends StdMOB
{

	public Parakeet()
	{
		super();
		Username="a parakeet";
		setDescription("a small colorful bird.");
		setDisplayText("A parakeet flies nearby.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.INTELLIGENCE,1);

		baseEnvStats().setDamage(1);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(50);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_FLYING);
		baseCharStats().setMyRace(CMClass.getRace("SongBird"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Parakeet();
	}
}
