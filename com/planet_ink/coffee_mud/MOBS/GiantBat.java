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
public class GiantBat extends StdMOB
{
	
	public GiantBat()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a giant bat";
		setDescription("It is a giant version of your common bat.");
		setDisplayText("A giant bat flies nearby.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);
		
		baseEnvStats.setWeight(1 + Math.abs(randomizer.nextInt() % 100));
		
		
		baseCharStats().setIntelligence(7);
		baseCharStats().setStrength(16);
		baseCharStats().setDexterity(17);
		
		baseEnvStats().setDamage(8);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(4);
		baseEnvStats().setArmor(80);
		baseEnvStats().setDisposition(Sense.IS_FLYING);
		
		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 8)*baseEnvStats().level()) + 4);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new GiantBat();
	}
}
