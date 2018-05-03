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
   Copyright 2003-2018 Bo Zimmerman

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
public class Assassin extends GenMob
{
	@Override
	public String ID()
	{
		return "Assassin";
	}

	public Assassin()
	{
		super();
		username="an assassin";
		setDescription("He`s all dressed in black, and has eyes as cold as ice.");
		setDisplayText("An assassin stands here.");
		final Race R=CMClass.getRace("Human");
		if(R!=null)
		{
			baseCharStats().setMyRace(R);
			R.startRacing(this,false);
		}
		baseCharStats().setStat(CharStats.STAT_DEXTERITY,18);
		baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		baseCharStats().setStat(CharStats.STAT_WISDOM,18);
		basePhyStats().setSensesMask(basePhyStats().sensesMask()|PhyStats.CAN_SEE_DARK);

		Ability A=CMClass.getAbility("Thief_Hide");
		if(A!=null)
		{
			A.setProficiency(100);
			addAbility(A);
		}
		A=CMClass.getAbility("Thief_Sneak");
		if(A!=null)
		{
			A.setProficiency(100);
			addAbility(A);
		}
		A=CMClass.getAbility("Thief_BackStab");
		if(A!=null)
		{
			A.setProficiency(100);
			addAbility(A);
		}
		A=CMClass.getAbility("Thief_Assassinate");
		if(A!=null)
		{
			A.setProficiency(100);
			addAbility(A);
		}
		Item I=CMClass.getWeapon("Longsword");
		if(I!=null)
		{
			addItem(I);
			I.wearAt(Wearable.WORN_WIELD);
		}
		I=CMClass.getArmor("LeatherArmor");
		if(I!=null)
		{
			addItem(I);
			I.wearIfPossible(this);
		}
		final Weapon d=CMClass.getWeapon("Dagger");
		if(d!=null)
		{
			d.wearAt(Wearable.WORN_HELD);
			addItem(d);
		}

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
		addBehavior(CMClass.getBehavior("CombatAbilities"));
	}

}
