package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
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
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/*
Copyright 2024-2025 Bo Zimmerman

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
public class PropFightspellcast extends PropTest
{
	@Override
	public String ID()
	{
		return "PropFightspellcast";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "all_properties"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		resetTest();
		final Ability FightSpellCast = CMClass.getAbility("Prop_FightSpellCast");
		FightSpellCast.setMiscText(maliciousSemiSpellList());
		//mob.tell(("Test:@x2-1: @x1", FightSpellCast.accountForYourself(),what));
		IS = giveTo(CMClass.getWeapon("Sword"), FightSpellCast, mobs[0], null, 1);
		if (effectCheck(maliciousspells, mobs[1]))
		{
			return (("Error#1"));
		}
		if (effectCheck(maliciousspells, mobs[0]))
		{
			return (("Error#2"));
		}
		for (int i = 0; i < 100; i++)
		{
			mobs[1].curState().setHitPoints(1000);
			mobs[0].curState().setHitPoints(1000);
			CMLib.combat().postAttack(mobs[0], mobs[1], mobs[0].fetchWieldedItem());
			if (effectCheck(maliciousspells, mobs[1]))
				break;
		}
		if (!effectCheck(maliciousspells, mobs[1]))
		{
			return (("Error#3"));
		}
		R1.recoverRoomStats();
		resetTest();
		FightSpellCast.setMiscText(maliciousSemiSpellList() + "MASK=-RACE +Human");
		//mob.tell(("Test:@x2-2: @x1", FightSpellCast.accountForYourself(),what));
		IS = giveTo(CMClass.getWeapon("Sword"), FightSpellCast, mobs[1], null, 1);
		if (effectCheck(maliciousspells, mobs[1]))
		{
			return (("Error#4"));
		}
		if (effectCheck(maliciousspells, mobs[0]))
		{
			return (("Error#5"));
		}
		for (int i = 0; i < 100; i++)
		{
			mobs[1].curState().setHitPoints(1000);
			mobs[0].curState().setHitPoints(1000);
			CMLib.combat().postAttack(mobs[1], mobs[0], mobs[1].fetchWieldedItem());
			if (effectCheck(maliciousspells, mobs[1]))
				break;
		}
		if (effectCheck(maliciousspells, mobs[1]))
		{
			return (("Error#6"));
		}
		R1.recoverRoomStats();
		resetTest();
		FightSpellCast.setMiscText(maliciousSemiSpellList() + "MASK=-RACE +Human");
		//mob.tell(("Test:@x2-3: @x1", FightSpellCast.accountForYourself(),what));
		IS = giveTo(CMClass.getWeapon("Sword"), FightSpellCast, mobs[0], null, 1);
		if (effectCheck(maliciousspells, mobs[1]))
		{
			return (("Error#7"));
		}
		if (effectCheck(maliciousspells, mobs[0]))
		{
			return (("Error#8"));
		}
		for (int i = 0; i < 100; i++)
		{
			mobs[1].curState().setHitPoints(1000);
			mobs[0].curState().setHitPoints(1000);
			CMLib.combat().postAttack(mobs[0], mobs[1], mobs[0].fetchWieldedItem());
			if (effectCheck(maliciousspells, mobs[1]))
				break;
		}
		if (!effectCheck(maliciousspells, mobs[1]))
		{
			return (("Error#9"));
		}
		R1.recoverRoomStats();
		return null;
	}
}
