package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Cobra extends StdMOB
{

	public Cobra()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a cobra";
		setDescription("A fearsome creature with long fangs and a menacing head.");
		setDisplayText("A cobra is hissing at you.");
		setAlignment(500);
		setMoney(0);

		addBehavior(CMClass.getBehavior("CombatAbilities"));
		addAbility(CMClass.getAbility("Poison"));

		baseEnvStats().setDamage(4);

		baseCharStats().setStat(CharStats.INTELLIGENCE,1);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(10);

		baseCharStats().setMyRace(CMClass.getRace("Cobra"));
		baseCharStats().getMyRace().startRacing(this,false);
		baseState.setHitPoints(Math.abs(randomizer.nextInt() % 6) + 2);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Cobra();
	}
}