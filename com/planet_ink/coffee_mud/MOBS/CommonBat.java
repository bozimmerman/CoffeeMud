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
public class CommonBat extends StdMOB
{
	
	public CommonBat()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a common bat";
		setDescription("It seemingly has the body of a rat with wings.");
		setDisplayText("A bat flies nearby.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);
		
		baseEnvStats.setWeight(1 + Math.abs(randomizer.nextInt() % 2));
		
		
		baseCharStats().setIntelligence(1);
		baseCharStats().setStrength(12);
		baseCharStats().setDexterity(17);
		
		baseEnvStats().setDamage(1);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(80);
		baseEnvStats().setDisposition(Sense.IS_FLYING);
		
		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 2)*baseEnvStats().level()) + 1);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new CommonBat();
	}
}
