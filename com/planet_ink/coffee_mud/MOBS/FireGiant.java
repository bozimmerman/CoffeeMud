package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class FireGiant extends StdMOB
{

	public FireGiant()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a Fire Giant";
		setDescription("A tall humanoid standing about 18 feet tall, 12 foot chest, coal black skin and fire red-orange hair.");
		setDisplayText("A Fire Giant ponders killing you.");
		setAlignment(0);
		setMoney(0);
		baseEnvStats.setWeight(6500 + Math.abs(randomizer.nextInt() % 1001));


		baseCharStats().setStat(CharStats.INTELLIGENCE,8 + Math.abs(randomizer.nextInt() % 3));
		baseCharStats().setStat(CharStats.STRENGTH,20);
		baseCharStats().setStat(CharStats.DEXTERITY,13);
		baseCharStats().setMyRace(CMClass.getRace("Giant"));

		baseEnvStats().setDamage(20);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(15);
		baseEnvStats().setArmor(-10);

		baseState.setHitPoints((Math.abs(randomizer.nextInt() % 12)*baseEnvStats().level()) + 17);

		addBehavior(CMClass.getBehavior("Aggressive"));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new FireGiant();
	}
	public void recoverCharStats()
	{
		super.recoverCharStats();
		charStats().setStat(CharStats.SAVE_FIRE,charStats().getStat(CharStats.SAVE_FIRE)+100);
	}


}
