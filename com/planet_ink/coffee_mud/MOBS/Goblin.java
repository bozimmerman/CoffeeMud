package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;


public class Goblin extends StdMOB
{
	Random randomizer = new Random(System.currentTimeMillis());
	int birthType=0;

	public Goblin()
	{
		super();
		int goblinType = Math.abs(randomizer.nextInt() % 1000);

		setMOBSpecifics(goblinType);
		baseCharStats().setMyRace(CMClass.getRace("Goblin"));
		baseCharStats().getMyRace().setHeightWeight(baseEnvStats(),(char)baseCharStats().getStat(CharStats.GENDER));


		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Goblin();
	}

	public boolean tick(int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if(birthType!=baseEnvStats().ability())
				setMOBSpecifics(baseEnvStats().ability());
		}
		return super.tick(tickID);
	}

	public void setMOBSpecifics(int goblinType)
	{
		if (goblinType < 0)
			goblinType *= -1;

		while(this.inventorySize()>0)
		{
			Item I=fetchInventory(0);
			if(I!=null)
				this.delInventory(I);
		}

		birthType=goblinType;
		setMoney(randomizer.nextInt() % 15);
		setWimpHitPoint(0);
		baseEnvStats.setWeight(40 + Math.abs(randomizer.nextInt() % 30));
		setAlignment(0);
		baseCharStats().setStat(CharStats.INTELLIGENCE,5 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.CHARISMA,2 + Math.abs(randomizer.nextInt() % 3));
		baseEnvStats().setArmor(25 + Math.abs(randomizer.nextInt() % 20));
		baseEnvStats().setLevel(1 + Math.abs(randomizer.nextInt() % 3));
		baseEnvStats().setAbility(goblinType);
		baseState.setHitPoints(10+(int)Math.round(Math.random()*10.0));

		Weapon m=null;
		Armor c=null;
		if (goblinType > 0   && goblinType <=  99)
		{
			Username="a nasty Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A nasty goblin marches around.");
			m=(Weapon)CMClass.getWeapon("Mace");
		}
		if (goblinType > 100 && goblinType <= 199)
		{
			Username="a Goblin";
			setDescription("He\\`s smelly and has red skin.");
			setDisplayText("A nasty goblin scuttles about.");
			m=(Weapon)CMClass.getWeapon("Mace");
		}
		if (goblinType > 200 && goblinType <= 299)
		{
			Username="an ugly Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A nasty goblin scurries nearby.");
			m=(Weapon)CMClass.getWeapon("Mace");
		}
		if (goblinType > 300 && goblinType <= 399)
		{
			Username="a Goblin female";
			setDescription("She\\`s ugly and very smelly.");
			setDisplayText("A female goblin sits nearby.");
		}
		if (goblinType > 400 && goblinType <= 499)
		{
			Username="a mean Goblin";
			setDescription("He appears to be bigger...and smellier than most goblins.");
			setDisplayText("A mean goblin glares at you.");
			m=(Weapon)CMClass.getWeapon("Shortsword");
			c = (Armor)CMClass.getArmor("ChainMailArmor");
		}
		if (goblinType > 500 && goblinType <= 599)
		{
			Username="a smelly Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A nasty goblin sits nearby.");
			m=(Weapon)CMClass.getWeapon("Mace");
		}
		if (goblinType > 600 && goblinType <= 699)
		{
			Username="a Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A very smelly goblin stands near you.");
			m=(Weapon)CMClass.getWeapon("Mace");
		}
		if (goblinType > 700 && goblinType <= 799)
		{
			Username="a Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A nasty goblin glares are you with lemon colored eyes.");
			m=(Weapon)CMClass.getWeapon("Mace");
		}
		if (goblinType > 800 && goblinType <= 899)
		{
			Username="a Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A goblin stares are you with red eyes.");
			m=(Weapon)CMClass.getWeapon("Mace");
		}
		if (goblinType > 900 && goblinType <= 999)
		{
			Username="an armed Goblin";
			setDescription("He\\`s wielding a sword.");
			setDisplayText("A nasty goblin marches around.");
			m=(Weapon)CMClass.getWeapon("Shortsword");
		}
		if(m!=null)
		{
			m.wearAt(Item.WIELD);
			addInventory(m);
		}
		if(c!=null)
		{
			c.wearAt(Item.ON_TORSO);
			addInventory(c);
		}
	}
}

