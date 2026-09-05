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
Copyright 2024-2026 Bo Zimmerman

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
public class MonkeyGrip extends PropTest
{
	@Override
	public String ID()
	{
		return "MonkeyGrip";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all"};
	}

	protected boolean monkeyGrip(final MOB M, final Item weapon)
	{
		final Ability A = CMClass.getAbility("Fighter_MonkeyGrip");
		if(A == null)
			return false;
		return A.invoke(M, new XArrayList<String>("MonkeyGrip","$"+weapon.Name()+"$"), weapon, true, 0);
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		super.resetTest();
		final MOB M = mobs[0];

		final Item sword = CMClass.getWeapon("Claymore");
		M.addItem(sword);
		final Item polearm = CMClass.getWeapon("Glaive");
		M.addItem(polearm);

		if(!monkeyGrip(M, sword))
			return (("Error#1: Couldn't monkey grip+wield the sword"));
		if((M.fetchWieldedItem()!=sword)
		||(sword.fetchEffect("Fighter_MonkeyGrip")==null)
		||(sword.amWearingAt(Wearable.IN_INVENTORY)))
			return (("Error#2: Sword not properly monkey gripped/wielded"));

		if(!monkeyGrip(M, polearm))
			return (("Error#3: Couldn't monkey grip+hold the polearm"));
		if((M.fetchHeldItem()!=polearm)
		||(polearm.fetchEffect("Fighter_MonkeyGrip")==null)
		||(polearm.amWearingAt(Wearable.IN_INVENTORY)))
			return (("Error#4: Polearm not properly monkey gripped/held"));

		// Simulates an automatic re-equip cycle (like an unarmed melee check or
		// a piece of armor being re-worn): both weapons cycle through the
		// inventory and the mob re-renders its phystats in between.
		M.recoverCharStats();
		sword.unWear();
		polearm.unWear();
		M.recoverPhyStats();

		// The monkey grip must survive the transient inventory state, otherwise
		// the weapons revert to two-handed and get dropped ("removed without command").
		if((sword.fetchEffect("Fighter_MonkeyGrip")==null)
		||(polearm.fetchEffect("Fighter_MonkeyGrip")==null))
			return (("Error#5: Monkey grip cleaned off an inventory weapon during a transient re-equip"));
		if((sword.rawLogicalAnd())
		||(polearm.rawLogicalAnd()))
			return (("Error#6: Monkey gripped weapon reverted to two-handed"));

		// Re-equip both, as the automatic logic would after the transient.
		CMLib.commands().postWear(M, sword, true);
		CMLib.commands().forceStandardCommand(M, "Hold", new XArrayList<String>("Hold","$"+polearm.Name()+"$"));
		if((M.fetchWieldedItem()!=sword)
		||(M.fetchHeldItem()!=polearm)
		||(sword.fetchEffect("Fighter_MonkeyGrip")==null)
		||(polearm.fetchEffect("Fighter_MonkeyGrip")==null)
		||(sword.rawLogicalAnd())
		||(polearm.rawLogicalAnd()))
			return (("Error#7: Main weapon lost its monkey grip when re-equipped"));

		// A genuine removal must still clean off the monkey grip.
		CMLib.commands().postRemove(M, sword, true);
		M.recoverPhyStats();
		if((sword.fetchEffect("Fighter_MonkeyGrip")!=null)
		||(!sword.rawLogicalAnd()))
			return (("Error#8: Monkey grip not cleaned off a genuinely removed weapon"));
		return null;
	}
}