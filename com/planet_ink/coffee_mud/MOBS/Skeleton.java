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
public class Skeleton extends Undead
{
	@Override
	public String ID()
	{
		return "Skeleton";
	}

	public Skeleton()
	{

		super();
		username="a skeleton";
		setDescription("A walking pile of bones...");
		setDisplayText("a skeleton rattles as it walks.");
		setMoney(0);
		basePhyStats.setWeight(30);

		final Weapon sword=CMClass.getWeapon("Longsword");
		if(sword!=null)
		{
			sword.wearAt(Wearable.WORN_WIELD);
			addItem(sword);
		}

		basePhyStats().setDamage(5);
		basePhyStats().setLevel(1);
		basePhyStats().setArmor(90);
		basePhyStats().setSpeed(1.0);

		baseCharStats().setMyRace(CMClass.getRace("Skeleton"));
		baseCharStats().getMyRace().startRacing(this,false);
		baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(),20,basePhyStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

}
