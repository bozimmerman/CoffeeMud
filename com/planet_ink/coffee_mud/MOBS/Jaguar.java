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
public class Jaguar extends StdMOB
{
	
	public Jaguar()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a jaguar";
		setDescription("A powerful cat with a deep chest and muscular limbs.  It\\`s covered in light yellow fur with black spots.");
		setDisplayText("A jaguar prowls quietly.");
		setAlignment(500);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 45));
		setWimpHitPoint(2);
		
		baseEnvStats.setWeight(200 + Math.abs(randomizer.nextInt() % 55));
		
		
		baseCharStats().setIntelligence(1 + Math.abs(randomizer.nextInt() % 4));
		baseCharStats().setStrength(12);
		baseCharStats().setDexterity(17);
		
		baseEnvStats().setDamage(8);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(4);
		baseEnvStats().setArmor(60);
		
		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 18)*baseEnvStats().level()) + 9);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Jaguar();
	}
}
