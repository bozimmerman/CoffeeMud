package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Minotaur extends StdMOB
{
	public String ID(){return "Minotaur";}
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


		baseCharStats().setStat(CharStats.INTELLIGENCE,4 + Math.abs(randomizer.nextInt() % 5));
		baseCharStats().setStat(CharStats.STRENGTH,18);
		baseCharStats().setStat(CharStats.DEXTERITY,15);
		baseCharStats().setMyRace(CMClass.getRace("Minotaur"));
		baseCharStats().getMyRace().startRacing(this,false);

		Weapon mainWeapon=(Weapon)CMClass.getWeapon("BattleAxe");
		if(mainWeapon!=null)
		{
			mainWeapon.wearAt(Item.WIELD);
			this.addInventory(mainWeapon);
		}

		baseEnvStats().setDamage(12);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(6);
		baseEnvStats().setArmor(60);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		addBehavior(CMClass.getBehavior("Aggressive"));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Minotaur();
	}
}
