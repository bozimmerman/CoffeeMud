package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Bugbear extends StdMOB
{
	public String ID(){return "Bugbear";}
	public Bugbear()
	{
		super();
		Username="a Bugbear";
		setDescription("a 7 foot tall, hairy, yellow-brown, muscular creature with sharp teeth and recessed eyes.");
		setDisplayText("A large Bugbear stands here.");
		setAlignment(0);
		setMoney(20);
		baseEnvStats.setWeight(300);
		setWimpHitPoint(0);

		Weapon h=(Weapon)CMClass.getWeapon("Halberd");
		if(h!=null)
		{
			h.wearAt(Item.WIELD);
			addInventory(h);
		}

		baseCharStats().setStat(CharStats.INTELLIGENCE,6);
		baseCharStats().setStat(CharStats.CHARISMA,2);
		baseCharStats().setStat(CharStats.STRENGTH,22);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(3);
		baseEnvStats().setArmor(40);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Bugbear();
	}
}
