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
public class HeavenlyServent extends StdMOB
{
	
	public HeavenlyServent()
	{
		super();

		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="an archon servant";
		setDescription("An angelic form in gowns of white, with golden hair, and an ever present smile.");
		setDisplayText("A servant of the Archons is running errands.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 55));
		setWimpHitPoint(2);
		
		addBehavior(new Mobile());
		addBehavior(new MudChat());
		
		baseEnvStats().setDamage(25);
		
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(10);
		baseEnvStats().setArmor(0);
		
		maxState.setHitPoints(200);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new HeavenlyServent();
	}
}
