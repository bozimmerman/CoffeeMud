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
public class InvisibleStalker extends StdMOB
{
	
	public InvisibleStalker()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="an Invisible Stalker";
		setDescription("A shimmering blob of energy.");
		setDisplayText("An invisible stalker hunts here.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(10 + Math.abs(randomizer.nextInt() % 10));
		
		
		baseCharStats().setIntelligence(12 + Math.abs(randomizer.nextInt() % 3));
		baseCharStats().setStrength(20);
		baseCharStats().setDexterity(13);
		
		baseEnvStats().setDamage(16);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(4);
		baseEnvStats().setArmor(0);
		baseEnvStats().setDisposition(Sense.IS_INVISIBLE);
		
		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 12)*baseEnvStats().level()) + 4);
		
		addBehavior(new Aggressive());
		addBehavior(new Mobile());
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new InvisibleStalker();
	}
}
