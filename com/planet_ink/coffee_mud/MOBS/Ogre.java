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
public class Ogre extends StdMOB
{
	
	public Ogre()
	{

		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="an Ogre";
		setDescription("Nine foot tall and with skin that is a covered in bumps and dead yellow in color..");
		setDisplayText("An ogre stares at you while he clenches his fists.");
		setAlignment(0);
		setMoney(10);
		baseEnvStats.setWeight(350);
		setWimpHitPoint(0);
		
		Natural fists = new Natural();
		fists.baseEnvStats().setDamage(12);
		fists.wear(Item.WIELD);
		addInventory(fists);
		
		baseCharStats().setIntelligence(8);
		baseCharStats().setCharisma(2);
		baseCharStats().setStrength(22);
		
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(4);
		baseEnvStats().setArmor(50);
		baseEnvStats().setSpeed(3.0);
		
		int hitPoints = 0;
		for(int i = 0; i < 4; i++)
			hitPoints += Math.abs(randomizer.nextInt()) % 16 + 1;
		hitPoints++;

		maxState.setHitPoints(hitPoints);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Ogre();
	}
}
