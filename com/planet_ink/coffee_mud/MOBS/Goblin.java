package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.db.*;


public class Goblin extends StdMOB
{
	Random randomizer = new Random(System.currentTimeMillis());
	int birthType=0;
	
	public Goblin()
	{
		super();
		int goblinType = Math.abs(randomizer.nextInt() % 1000);

		setMOBSpecifics(goblinType);

		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Goblin();
	}
	
	public boolean tick(int tickID)
	{
		if(tickID==ServiceEngine.MOB_TICK)
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
			this.delInventory(this.fetchInventory(0));

		birthType=goblinType;
		setMoney(randomizer.nextInt() % 15);
		setWimpHitPoint(0);
		baseEnvStats.setWeight(40 + Math.abs(randomizer.nextInt() % 30));
		setAlignment(0);
		baseCharStats().setIntelligence(5 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setCharisma(2 + Math.abs(randomizer.nextInt() % 3));
		baseEnvStats().setArmor(25 + Math.abs(randomizer.nextInt() % 20));
		baseEnvStats().setLevel(1 + Math.abs(randomizer.nextInt() % 3));
		baseEnvStats().setAbility(goblinType);
		maxState.setHitPoints(10+(int)Math.round(Math.random()*10.0));

		if (goblinType > 0   && goblinType <=  99)
		{
			Username="a nasty Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A nasty goblin marches around.");
			Mace m=new Mace();
			m.wear(Item.WIELD);
			addInventory(m);
			return;
		}
		if (goblinType > 100 && goblinType <= 199)
		{
			Username="a Goblin";
			setDescription("He\\`s smelly and has red skin.");
			setDisplayText("A nasty goblin scuttles about.");
			Mace m=new Mace();
			m.wear(Item.WIELD);
			addInventory(m);
			return;
		}
		if (goblinType > 200 && goblinType <= 299)
		{
			Username="an ugly Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A nasty goblin scurries nearby.");
			Mace m=new Mace();
			m.wear(Item.WIELD);
			addInventory(m);
			return;
		}
		if (goblinType > 300 && goblinType <= 399)
		{
			Username="a Goblin female";
			setDescription("She\\`s ugly and very smelly.");
			setDisplayText("A female goblin sits nearby.");
			return;
		}
		if (goblinType > 400 && goblinType <= 499)
		{
			Username="a mean Goblin";
			setDescription("He appears to be bigger...and smellier than most goblins.");
			setDisplayText("A mean goblin glares at you.");
			Shortsword m=new Shortsword();
			m.wear(Item.WIELD);
			addInventory(m);
			ChainMailArmor c = new ChainMailArmor();
			c.wear(Item.ON_TORSO);
			addInventory(c);
			return;
		}
		if (goblinType > 500 && goblinType <= 599)
		{
			Username="a smelly Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A nasty goblin sits nearby.");
			Mace m=new Mace();
			m.wear(Item.WIELD);
			addInventory(m);
			return;
		}
		if (goblinType > 600 && goblinType <= 699)
		{
			Username="a Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A very smelly goblin stands near you.");
			Mace m=new Mace();
			m.wear(Item.WIELD);
			addInventory(m);
			return;
		}
		if (goblinType > 700 && goblinType <= 799)
		{
			Username="a Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A nasty goblin glares are you with lemon colored eyes.");
			Mace m=new Mace();
			m.wear(Item.WIELD);
			addInventory(m);
			return;
		}
		if (goblinType > 800 && goblinType <= 899)
		{
			Username="a Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A goblin stares are you with red eyes.");
			Mace m=new Mace();
			m.wear(Item.WIELD);
			addInventory(m);
			return;
		}
		if (goblinType > 900 && goblinType <= 999)
		{
			Username="an armed Goblin";
			setDescription("He\\`s wielding a sword.");
			setDisplayText("A nasty goblin marches around.");
			Shortsword m=new Shortsword();
			m.wear(Item.WIELD);
			addInventory(m);

			return;
		}

	}
}

