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

public class Bugbear extends StdMOB
{
	
	public Bugbear()
	{
		super();
		Username="a Bugbear";
		setDescription("a 7 foot tall, hairy, yellow-brown, muscular creature with sharp teeth and recessed eyes.");
		setDisplayText("A large Bugbear stands here.");
		setAlignment(0);
		setMoney(20);
		baseEnvStats.setWeight(300);
		setWimpHitPoint(0);
		
		Halberd h=new Halberd();
		h.wear(Item.WIELD);
		addInventory(h);
		
		baseCharStats().setIntelligence(6);
		baseCharStats().setCharisma(2);
		baseCharStats().setStrength(22);
		
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(3);
		baseEnvStats().setArmor(40);
		
		maxState.setHitPoints(((int)Math.round((Math.random()*12)+1))*3+1);
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Bugbear();
	}
}
