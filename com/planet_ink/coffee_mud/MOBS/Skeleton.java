package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Skeleton extends Undead
{
	public String ID(){return "Skeleton";}
	public Skeleton()
	{

		super();
		Username="a skeleton";
		setDescription("A walking pile of bones...");
		setDisplayText("a skeleton rattles as it walks.");
		setMoney(0);
		baseEnvStats.setWeight(30);

		Weapon sword=(Weapon)CMClass.getWeapon("Longsword");
		if(sword!=null)
		{
			sword.wearAt(Item.WIELD);
			addInventory(sword);
		}

		baseEnvStats().setDamage(5);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(70);
		baseEnvStats().setSpeed(1.0);

		baseCharStats().setMyRace(CMClass.getRace("Skeleton"));
		baseCharStats().getMyRace().startRacing(this,false);
		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
