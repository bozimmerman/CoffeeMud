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
public class Troll extends StdMOB
{
	@Override
	public String ID()
	{
		return "Troll";
	}
	Random randomizer = new Random();
	int regDown=3;

	public Troll()
	{
		super();
		username="a troll";
		setDescription("Nine foot tall and reeking of rotten meat..");
		setDisplayText("A mean looking troll glares at you.");
		CMLib.factions().setAlignment(this,Faction.Align.EVIL);
		setMoney(10);
		basePhyStats.setWeight(350);
		setWimpHitPoint(0);

		final int addedInt = (int)Math.round(Math.random() * 3.0);
		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,8 + addedInt);
		baseCharStats().setStat(CharStats.STAT_CHARISMA,2);
		baseCharStats().setMyRace(CMClass.getRace("Troll"));
		baseCharStats().getMyRace().startRacing(this,false);

		basePhyStats().setAbility(0);
		basePhyStats().setLevel(8);
		basePhyStats().setAttackAdjustment(basePhyStats().attackAdjustment()+20);
		basePhyStats().setDamage(basePhyStats().damage()+5);
		basePhyStats().setArmor(60);
		basePhyStats().setSpeed(3.0);
		basePhyStats().setSensesMask(PhyStats.CAN_SEE_DARK | PhyStats.CAN_SEE_INFRARED);

		baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(),20,basePhyStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}
}
