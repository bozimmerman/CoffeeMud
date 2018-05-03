package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2018 Lee H. Fox

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Goblin extends StdMOB
{
	@Override
	public String ID()
	{
		return "Goblin";
	}
	Random randomizer = new Random(System.currentTimeMillis());
	int birthType=0;

	public Goblin()
	{
		super();
		final int goblinType = Math.abs(randomizer.nextInt() % 1000);

		setMOBSpecifics(goblinType);
		baseCharStats().setMyRace(CMClass.getRace("Goblin"));
		baseCharStats().getMyRace().startRacing(this,false);

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if(birthType!=basePhyStats().ability())
				setMOBSpecifics(basePhyStats().ability());
		}
		return super.tick(ticking,tickID);
	}

	public void setMOBSpecifics(int goblinType)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return;

		if (goblinType < 0)
			goblinType *= -1;

		delAllItems(false);

		birthType=goblinType;
		setMoney(randomizer.nextInt() % 15);
		setWimpHitPoint(0);
		basePhyStats.setWeight(40 + Math.abs(randomizer.nextInt() % 30));
		CMLib.factions().setAlignment(this,Faction.Align.EVIL);
		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,5 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.STAT_CHARISMA,2 + Math.abs(randomizer.nextInt() % 3));
		basePhyStats().setArmor(75 + Math.abs(randomizer.nextInt() % 20));
		basePhyStats().setLevel(1 + Math.abs(randomizer.nextInt() % 3));
		basePhyStats().setAbility(goblinType);
		baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(),20,basePhyStats().level()));

		Weapon m=null;
		Armor c=null;
		if (goblinType >= 0   && goblinType <=  99)
		{
			username="a nasty Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A nasty goblin marches around.");
			m=CMClass.getWeapon("Mace");
		}
		else
		if (goblinType >= 100 && goblinType <= 199)
		{
			username="a Goblin";
			setDescription("He\\`s smelly and has red skin.");
			setDisplayText("A nasty goblin scuttles about.");
			m=CMClass.getWeapon("Mace");
		}
		else
		if (goblinType >= 200 && goblinType <= 299)
		{
			username="an ugly Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A nasty goblin scurries nearby.");
			m=CMClass.getWeapon("Mace");
		}
		else
		if (goblinType >= 300 && goblinType <= 399)
		{
			username="a Goblin female";
			setDescription("She\\`s ugly and very smelly.");
			setDisplayText("A female goblin sits nearby.");
		}
		else
		if (goblinType >= 400 && goblinType <= 499)
		{
			username="a mean Goblin";
			setDescription("He appears to be bigger...and smellier than most goblins.");
			setDisplayText("A mean goblin glares at you.");
			m=CMClass.getWeapon("Shortsword");
			c = CMClass.getArmor("ChainMailArmor");
		}
		else
		if (goblinType >= 500 && goblinType <= 599)
		{
			username="a smelly Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A nasty goblin sits nearby.");
			m=CMClass.getWeapon("Mace");
		}
		else
		if (goblinType >= 600 && goblinType <= 699)
		{
			username="a Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A very smelly goblin stands near you.");
			m=CMClass.getWeapon("Mace");
		}
		else
		if (goblinType >= 700 && goblinType <= 799)
		{
			username="a Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A nasty goblin glares are you with lemon colored eyes.");
			m=CMClass.getWeapon("Mace");
		}
		else
		if (goblinType >= 800 && goblinType <= 899)
		{
			username="a Goblin";
			setDescription("He\\`s dirty, cranky, and very smelly.");
			setDisplayText("A goblin stares are you with red eyes.");
			m=CMClass.getWeapon("Mace");
		}
		else
		{
			username="an armed Goblin";
			setDescription("He\\`s wielding a sword.");
			setDisplayText("A nasty goblin marches around.");
			m=CMClass.getWeapon("Shortsword");
		}
		if(m!=null)
		{
			m.wearAt(Wearable.WORN_WIELD);
			addItem(m);
		}
		if(c!=null)
		{
			c.wearAt(Wearable.WORN_TORSO);
			addItem(c);
		}
	}
}

