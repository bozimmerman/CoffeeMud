package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.db.*;
public class Panther extends StdMOB
{
	
	public Panther()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a panther";
		setDescription("A powerful cat with a deep chest and muscular limbs, covered in midnight black fur.");
		setDisplayText("A panther slowly stalks prey.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 45));
		setWimpHitPoint(2);
		
		baseEnvStats.setWeight(200 + Math.abs(randomizer.nextInt() % 55));
		
		
		baseCharStats().setIntelligence(1 + Math.abs(randomizer.nextInt() % 4));
		baseCharStats().setStrength(12);
		baseCharStats().setDexterity(17);
		
		baseEnvStats().setDamage(8);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(4);
		baseEnvStats().setArmor(60);
		
		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 18)*baseEnvStats().level()) + 9);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Panther();
	}
}
