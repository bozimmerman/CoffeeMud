package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.db.*;
public class Skeleton extends Undead
{
	
	public Skeleton()
	{

		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a skeleton";
		setDescription("A walking pile of bones...");
		setDisplayText("a skeleton rattles as it walks.");
		setMoney(0);
		baseEnvStats.setWeight(30);
		
		Longsword sword=new Longsword();
		sword.wear(Item.WIELD);
		addInventory(sword);
		
		baseEnvStats().setDamage(5);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(70);
		baseEnvStats().setSpeed(1.0);
		
		int hitPoints = 0;
		hitPoints += Math.abs(randomizer.nextInt()) % 18 + 1;
		maxState.setHitPoints(hitPoints);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Skeleton();
	}
}
