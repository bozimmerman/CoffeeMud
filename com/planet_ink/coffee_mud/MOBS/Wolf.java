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
public class Wolf extends StdMOB
{
	
	public Wolf()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a wolf";
		setDescription("A powerful wolf with grey fur and amber eyes.");
		setDisplayText("A wolf growls and stares at you.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);
		
		baseEnvStats.setWeight(50 + Math.abs(randomizer.nextInt() % 55));
		
		
		baseCharStats().setIntelligence(1 + Math.abs(randomizer.nextInt() % 4));
		baseCharStats().setStrength(10);
		baseCharStats().setDexterity(14);
		
		baseEnvStats().setDamage(6);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(3);
		baseEnvStats().setArmor(70);
		
		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 10)*baseEnvStats().level()) + 9);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Wolf();
	}
}
