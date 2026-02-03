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
public class PropHidden extends PropTest
{
	@Override
	public String ID()
	{
		return "PropHidden";
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
		final Ability Hidden = CMClass.getAbility("Prop_Hidden");

		// Test 1: Item without Prop_Hidden should not be hidden
		final Item I1 = CMClass.getBasicItem("GenItem");
		I1.setName("Test Item");
		R1.addItem(I1);
		R1.recoverRoomStats();
		if (CMLib.flags().isHidden(I1))
		{
			return ("Error#1: Item should not be hidden without Prop_Hidden");
		}

		// Test 2: Item with Prop_Hidden in room should be hidden
		resetTest();
		final Item I2 = CMClass.getBasicItem("GenItem");
		I2.setName("Hidden Item");
		giveAbility(I2, Hidden);
		R1.addItem(I2);
		R1.recoverRoomStats();
		if (!CMLib.flags().isHidden(I2))
		{
			return ("Error#2: Item with Prop_Hidden in room should be hidden");
		}

		// Test 3: Item with Prop_Hidden UNLOCATABLE should have unlocatable sense
		resetTest();
		final Ability HiddenUnloc = CMClass.getAbility("Prop_Hidden");
		HiddenUnloc.setMiscText("UNLOCATABLE");
		final Item I3 = CMClass.getBasicItem("GenItem");
		I3.setName("Unlocatable Item");
		giveAbility(I3, HiddenUnloc);
		R1.addItem(I3);
		R1.recoverRoomStats();
		if (!CMLib.flags().isHidden(I3))
		{
			return ("Error#3: Unlocatable item should still be hidden");
		}
		if ((I3.phyStats().sensesMask() & PhyStats.SENSE_UNLOCATABLE) == 0)
		{
			return ("Error#4: Item with UNLOCATABLE should have SENSE_UNLOCATABLE");
		}

		// Test 4: MOB with Prop_Hidden should be able to see hidden
		resetTest();
		final int origSenses = mobs[0].phyStats().sensesMask();
		final boolean couldSeeHidden = (origSenses & PhyStats.CAN_SEE_HIDDEN) != 0;
		giveAbility(mobs[0], Hidden);
		R1.recoverRoomStats();
		final boolean canSeeHiddenNow = (mobs[0].phyStats().sensesMask() & PhyStats.CAN_SEE_HIDDEN) != 0;
		if (!canSeeHiddenNow && !couldSeeHidden)
		{
			return ("Error#5: MOB with Prop_Hidden should be able to see hidden");
		}

		// Test 5: MOB with Prop_Hidden should have increased detection save
		resetTest();
		final int origDetectSave = mobs[0].charStats().getStat(CharStats.STAT_SAVE_DETECTION);
		giveAbility(mobs[0], Hidden);
		mobs[0].recoverCharStats();
		final int newDetectSave = mobs[0].charStats().getStat(CharStats.STAT_SAVE_DETECTION);
		if (newDetectSave < origDetectSave + 100)
		{
			return ("Error#6: MOB with Prop_Hidden should have +100 detection save");
		}

		return null;
	}
}
