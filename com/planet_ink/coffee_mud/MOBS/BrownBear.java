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
public class BrownBear extends StdMOB
{
	
	public BrownBear()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a Brown Bear";
		setDescription("A bear, large and husky with brown fur.");
		setDisplayText("A brown bear hunts here.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 45));
		setWimpHitPoint(2);
		
		baseEnvStats.setWeight(450 + Math.abs(randomizer.nextInt() % 55));
		
		
		baseCharStats().setIntelligence(4);
		baseCharStats().setStrength(18);
		baseCharStats().setDexterity(16);
		
		baseEnvStats().setDamage(8);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(5);
		baseEnvStats().setArmor(60);
		
		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 18)*baseEnvStats().level()) + 5);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new BrownBear();
	}
}
