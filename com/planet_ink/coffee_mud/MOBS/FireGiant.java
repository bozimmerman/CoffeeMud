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
public class FireGiant extends StdMOB
{
	@Override
	public String ID()
	{
		return "FireGiant";
	}

	public FireGiant()
	{
		super();
		final Random randomizer = new Random(System.currentTimeMillis());

		username="a Fire Giant";
		setDescription("A tall humanoid standing about 18 feet tall, 12 foot chest, coal black skin and fire red-orange hair.");
		setDisplayText("A Fire Giant ponders killing you.");
		CMLib.factions().setAlignment(this,Faction.Align.EVIL);
		setMoney(0);
		basePhyStats.setWeight(6500 + Math.abs(randomizer.nextInt() % 1001));

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,8 + Math.abs(randomizer.nextInt() % 3));
		baseCharStats().setStat(CharStats.STAT_STRENGTH,20);
		baseCharStats().setStat(CharStats.STAT_DEXTERITY,13);
		baseCharStats().setMyRace(CMClass.getRace("Giant"));
		baseCharStats().getMyRace().startRacing(this,false);

		basePhyStats().setDamage(20);
		basePhyStats().setSpeed(1.0);
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(15);
		basePhyStats().setArmor(-10);

		baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(),20,basePhyStats().level()));

		addBehavior(CMClass.getBehavior("Aggressive"));

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	@Override
	public void recoverCharStats()
	{
		super.recoverCharStats();
		charStats().setStat(CharStats.STAT_SAVE_FIRE,charStats().getStat(CharStats.STAT_SAVE_FIRE)+100);
	}

}
