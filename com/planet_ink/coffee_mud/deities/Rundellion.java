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

public class Rundellion extends StdMOB 
{
	
	public String influence = "War";
	public Weapon[] priestWeapons = {new Mace(), new Sword()};

	public Rundellion()
	{
		super();
		Username="Rundellion";
		setDescription("Rundellion is the god of War.");
		setDisplayText("Rundellion\\`s avatar appears as a powerful young man wielding a jewel-encrusted sword and dressed in full battle regalia.");
		setAlignment(0);
		setMoney(1000000);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);
		
		baseCharStats().setStrength(30);
		baseCharStats().setCharisma(30);
		baseCharStats().setIntelligence(30);
		baseCharStats().setDexterity(30);
		baseCharStats().setConstitution(30);
		baseCharStats().setWisdom(30);
		
		baseEnvStats().setArmor(0);
		
		maxState.setHitPoints(10000);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	
	public Environmental newInstance()
	{
		return new Rundellion();
	}
	
}
