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
public class Falcon extends StdMOB
{
	
	public Falcon()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a falcon";
		setDescription("a small hunting bird.");
		setDisplayText("A falcon flies nearby.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);
		
		baseEnvStats.setWeight(1 + Math.abs(randomizer.nextInt() % 6));
		
		
		baseCharStats().setIntelligence(4);
		baseCharStats().setStrength(10);
		baseCharStats().setDexterity(17);
		
		baseEnvStats().setDamage(1);
		baseEnvStats().setSpeed(3.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(2);
		baseEnvStats().setArmor(50);
		baseEnvStats().setDisposition(Sense.IS_FLYING);
		
		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 4)*baseEnvStats().level()) + 1);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Falcon();
	}
}
