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

public class Foxheart extends StdMOB 
{
	
	public String influence = "Hunt and Nature";
	public Weapon[] priestWeapons = {new Scimitar(), new Sickle(), new Javelin()};

	public Foxheart()
	{
		super();
		Username="Foxheart";
		setDescription("Foxheart is the god of the Hunt and Nature.");
		setDisplayText("Foxheart shows himself to his followers as a young man, dressed in finely crafted leather armor.  His face is hidden beneath the deep hood of his green cloak.  He carries a longbow with his ever-full quiver slung across his shoulders.");
		setAlignment(500);
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
		return new Foxheart();
	}
	
}
