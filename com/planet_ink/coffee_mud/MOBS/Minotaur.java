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
public class Minotaur extends StdMOB
{
	
	public Minotaur()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());
		
		Username="a minotaur";
		setDescription("A tall humanoid with the head of a bull, and the body of a very muscular man.  It\\`s covered in red fur.");
		setDisplayText("A minotaur glares at you.");
		setAlignment(0);
		setMoney(0);
		baseEnvStats.setWeight(350 + Math.abs(randomizer.nextInt() % 55));
		
		
		baseCharStats().setIntelligence(4 + Math.abs(randomizer.nextInt() % 5));
		baseCharStats().setStrength(18);
		baseCharStats().setDexterity(15);
		
		BattleAxe mainWeapon = new BattleAxe();
		mainWeapon.wear(Item.WIELD);
		this.addInventory(mainWeapon);

		baseEnvStats().setDamage(12);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(6);
		baseEnvStats().setArmor(60);
		
		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 20)*baseEnvStats().level()) + 10);
		
		addBehavior(new Aggressive());
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Minotaur();
	}
}
