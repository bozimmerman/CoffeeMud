package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Gnoll extends StdMOB
{

	public Gnoll()
	{
		super();
		Username="a Gnoll";
		setDescription("a 7 foot tall creature with a body resembling a large human and the head of a hyena.");
		setDisplayText("A nasty Gnoll stands here.");
		setAlignment(0);
		setMoney(20);
		baseEnvStats.setWeight(300);
		setWimpHitPoint(0);

		Weapon h=(Weapon)CMClass.getWeapon("MorningStar");
		Random randomizer = new Random(System.currentTimeMillis());
		int percentage = randomizer.nextInt() % 100;
		if((percentage & 1) == 0)
		{
		   h = (Weapon) CMClass.getWeapon("Longsword");
		}
		if(h!=null)
		{
			h.wearAt(Item.WIELD);
			addInventory(h);
		}

		baseCharStats().setStat(CharStats.INTELLIGENCE,6);
		baseCharStats().setStat(CharStats.CHARISMA,2);
		baseCharStats().setStat(CharStats.STRENGTH,22);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(2);
		baseEnvStats().setArmor(50);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Gnoll();
	}
}
