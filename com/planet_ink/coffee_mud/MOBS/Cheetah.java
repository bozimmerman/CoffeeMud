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
public class Cheetah extends StdMOB
{
	
	public Cheetah()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a cheetah";
		setDescription("A medium-sized, lightly built cat with sand covered fur and black spot.");
		setDisplayText("A cheetah stalks its prey.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 45));
		setWimpHitPoint(2);
		
		baseEnvStats.setWeight(150 + Math.abs(randomizer.nextInt() % 55));
		
		
		baseCharStats().setIntelligence(1);
		baseCharStats().setStrength(12);
		baseCharStats().setDexterity(18);
		
		baseEnvStats().setDamage(8);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(3);
		baseEnvStats().setArmor(60);
		
		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 18)*baseEnvStats().level()) + 3);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Cheetah();
	}
}
