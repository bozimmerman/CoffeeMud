package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.db.*;
public class Orc extends StdMOB
{
	
	public Orc()
	{
		super();
		Username="an Orc";
		setDescription("He\\`s dirty, cranky, and very mean.");
		setDisplayText("An angry Orc marches around.");
		setAlignment(0);
		setMoney(10);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);
		
		Dagger d=new Dagger();
		d.wear(Item.WIELD);
		addInventory(d);
		
		baseCharStats().setIntelligence(6);
		baseCharStats().setCharisma(2);
		
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(50);
		
		maxState.setHitPoints(10+(int)Math.round(Math.random()*10.0));
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Orc();
	}



}
