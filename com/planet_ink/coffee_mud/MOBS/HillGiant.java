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
public class HillGiant extends StdMOB
{
	
	public HillGiant()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a Hill Giant";
		setDescription("A tall humanoid standing about 16 feet tall and very smelly.");
		setDisplayText("A Hill Giant glares at you.");
		setAlignment(0);
		setMoney(0);
		baseEnvStats.setWeight(3500 + Dice.roll(1, 1000, 0));
		
		
		baseCharStats().setIntelligence(6 + Dice.roll(1, 2, 0));
		baseCharStats().setStrength(20);
		baseCharStats().setDexterity(13);
		
		baseEnvStats().setDamage(19);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(12);
		baseEnvStats().setArmor(0);
		
		maxState.setHitPoints(Dice.roll(baseEnvStats().level(), 12, 1));
		
		addBehavior(new Aggressive());
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new HillGiant();
	}
}
