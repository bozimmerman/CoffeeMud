package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Bee extends StdMOB
{
	public String ID(){return "Bee";}
	public Bee()
	{
		super();

		Username="a bee";
		setDescription("It\\`s a small buzzing insect with a nasty stinger on its butt.");
		setDisplayText("A bee buzzes around here.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(1);
		setWimpHitPoint(2);

		addBehavior(CMClass.getBehavior("Follower"));
		addBehavior(CMClass.getBehavior("CombatAbilities"));
		baseEnvStats().setDamage(1);

		baseCharStats().setStat(CharStats.INTELLIGENCE,1);
		baseEnvStats().setDisposition(EnvStats.IS_FLYING);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(10);

		baseCharStats().setMyRace(CMClass.getRace("Insect"));
		baseCharStats().getMyRace().startRacing(this,false);
		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));
		Ability A=CMClass.getAbility("Poison_BeeSting");
		if(A!=null) {
			A.setProfficiency(100);
			addAbility(A);
		}

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
