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
   Copyright 2001-2018 Bo Zimmerman

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
public class BlackBear extends StdMOB
{
	@Override
	public String ID()
	{
		return "BlackBear";
	}

	public BlackBear()
	{
		super();
		final Random randomizer = new Random(System.currentTimeMillis());

		username="a Black Bear";
		setDescription("A bear, husky with black fur.");
		setDisplayText("A black bear ambles around.");
		CMLib.factions().setAlignment(this,Faction.Align.NEUTRAL);
		setMoney(0);
		basePhyStats.setWeight(20 + Math.abs(randomizer.nextInt() % 45));
		setWimpHitPoint(2);

		basePhyStats.setWeight(250 + Math.abs(randomizer.nextInt() % 55));

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,1);
		baseCharStats().setStat(CharStats.STAT_STRENGTH,16);
		baseCharStats().setStat(CharStats.STAT_DEXTERITY,18);
		baseCharStats().setMyRace(CMClass.getRace("Bear"));
		baseCharStats().getMyRace().startRacing(this,false);

		basePhyStats().setDamage(6);
		basePhyStats().setSpeed(2.0);
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(3);
		basePhyStats().setArmor(80);

		baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(),20,basePhyStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

}
