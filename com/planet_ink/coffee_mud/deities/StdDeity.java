package com.planet_ink.coffee_mud.deities;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.db.*;

public class StdDeity extends StdMOB 
{
	
	public String influence = "anything";
	public Weapon[] priestWeapons = {new Weapon()};

	public StdDeity()
	{
		super();
		Username="a god";
		setDescription("He\\`s all-mighty.");
		setDisplayText(".");
		setAlignment(1000);
		setMoney(1000000);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);
		
		baseCharStats().setIntelligence(16);
		baseCharStats().setCharisma(25);
		
		baseEnvStats().setArmor(0);
		
		maxState.setHitPoints(10000);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	
	public Environmental newInstance()
	{
		return new StdDeity();
	}
	
}
