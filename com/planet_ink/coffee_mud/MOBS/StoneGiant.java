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
public class StoneGiant extends StdMOB
{
	
	public StoneGiant()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a Stone Giant";
		setDescription("A tall humanoid standing about 18 feet tall with gray, hairless flesh.");
		setDisplayText("A Stone Giant glares at you.");
		setAlignment(0);
		setMoney(0);
		baseEnvStats.setWeight(8000 + Math.abs(randomizer.nextInt() % 1001));
		
		
		baseCharStats().setIntelligence(8 + Math.abs(randomizer.nextInt() % 3));
		baseCharStats().setStrength(20);
		baseCharStats().setDexterity(13);
		
		baseEnvStats().setDamage(20);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(14);
		baseEnvStats().setArmor(0);
		
		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 12)*baseEnvStats().level()) + 16);
		
		addBehavior(new Aggressive());
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new StoneGiant();
	}
}
