package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class GardenSnake extends StdMOB
{
	public String ID(){return "GardenSnake";}
	public GardenSnake()
	{
		super();
		Username="a garden snake";
		setDescription("A harmless little green string.");
		setDisplayText("A little garden snake slithers around looking for bugs.");
		setAlignment(500);
		setMoney(0);

		baseEnvStats().setDamage(1);

		baseCharStats().setStat(CharStats.INTELLIGENCE,1);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(50);

		baseCharStats().setMyRace(CMClass.getRace("GardenSnake"));
		baseCharStats().getMyRace().startRacing(this,false);
		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}