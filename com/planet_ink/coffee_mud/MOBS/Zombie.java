package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Zombie extends Undead
{
	public String ID(){return "Zombie";}
	public Zombie()
	{

		super();
		Username="a zombie";
		setDescription("decayed and rotting, a dead body has been brought back to life...");
		setDisplayText("a skeleton slowly moves about.");
		setMoney(10);
		baseEnvStats.setWeight(30);

		baseEnvStats().setDamage(8);
		baseEnvStats().setLevel(2);
		baseEnvStats().setArmor(80);
		baseEnvStats().setSpeed(1.0);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
