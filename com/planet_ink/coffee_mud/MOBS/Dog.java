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
public class Dog extends StdMOB
{
	
	public Dog()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a dog";
		setDescription("It\\`s furry with four legs, just like a dog ought to be.");
		setDisplayText("A dog scurries nearby.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 55));
		setWimpHitPoint(2);
		
		addBehavior(new Follower());
		addBehavior(new MudChat());
		
		baseEnvStats().setDamage(4);
		
		baseCharStats().setIntelligence(1 + Math.abs(randomizer.nextInt() % 4));
		
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(30);
		
		maxState.setHitPoints(Math.abs(randomizer.nextInt() % 12) + 2);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Dog();
	}
}
