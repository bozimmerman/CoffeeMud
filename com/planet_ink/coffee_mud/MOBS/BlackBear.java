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
public class BlackBear extends StdMOB
{
	
	public BlackBear()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a Black Bear";
		setDescription("A bear, husky with black fur.");
		setDisplayText("A black bear ambles around.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 45));
		setWimpHitPoint(2);
		
		baseEnvStats.setWeight(250 + Math.abs(randomizer.nextInt() % 55));
		
		
		baseCharStats().setIntelligence(4);
		baseCharStats().setStrength(16);
		baseCharStats().setDexterity(18);
		
		baseEnvStats().setDamage(6);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(3);
		baseEnvStats().setArmor(70);
		
		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 18)*baseEnvStats().level()) + 3);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new BlackBear();
	}
}
