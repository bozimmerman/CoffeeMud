package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class GenUndead extends GenMob
{
	public String ID(){return "GenUndead";}
	public GenUndead()
	{
		super();
		Username="a generic undead being";
		setDescription("He looks dead to me.");
		setDisplayText("A generic undead mob stands here.");
		setAlignment(0);
		setMoney(10);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setMyRace(CMClass.getRace("Undead"));
		baseCharStats().getMyRace().startRacing(this,false);
		baseCharStats().setStat(CharStats.CHARISMA,2);

		baseEnvStats().setAbility(10);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(50);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

	public boolean isGeneric(){return true;}
}
