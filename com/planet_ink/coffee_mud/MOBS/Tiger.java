package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Behaviors.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.db.*;
public class Tiger extends StdMOB
{
	
	public Tiger()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a tiger";
		setDescription("Tigers have reddish-orange fur and dark vertical stripes.");
		setDisplayText("A tiger prowls here.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(300 + Math.abs(randomizer.nextInt() % 55));
		
		
		baseCharStats().setIntelligence(1 + Math.abs(randomizer.nextInt() % 4));
		baseCharStats().setStrength(13);
		baseCharStats().setDexterity(17);
		
		baseEnvStats().setDamage(10);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(5);
		baseEnvStats().setArmor(60);
		
		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 18)*baseEnvStats().level()) + 10);
		
		addBehavior(new Aggressive());
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Tiger();
	}
}
