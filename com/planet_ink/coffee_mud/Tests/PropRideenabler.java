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
public class PropRideenabler extends PropTest
{
	@Override
	public String ID()
	{
		return "PropRideenabler";
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
		final Ability RideEnabler = CMClass.getAbility("Prop_RideEnabler");
		RideEnabler.setMiscText(semiSpellList());
		//mob.tell(L("Test:"+what+"-1: @x1", RideEnabler.accountForYourself()));
		IS = giveTo(CMClass.getItem("Boat"), RideEnabler, mobs[0], null, 2);
		if (!spellCheck(spells, mobs[0]))
		{
			return (L("Error#1"));
		}
		mobs[0].setRiding(null);
		R1.recoverRoomStats();
		if (spellCheck(spells, mobs[0]))
		{
			return (L("Error#2"));
		}
		resetTest();
		RideEnabler.setMiscText(semiSpellList() + "MASK=-RACE +Dwarf");
		//mob.tell(L("Test:"+what+"-2: @x1", RideEnabler.accountForYourself()));
		IS = giveTo(CMClass.getItem("Boat"), RideEnabler, mobs[0], mobs[1], 2);
		if (!spellCheck(spells, mobs[0]))
		{
			return (L("Error#3"));
		}
		if (spellCheck(spells, mobs[1]))
		{
			return (L("Error#4"));
		}
		mobs[0].setRiding(null);
		mobs[1].setRiding(null);
		R1.recoverRoomStats();
		if (spellCheck(spells, mobs[0]))
		{
			return (L("Error#5"));
		}
		if (spellCheck(spells, mobs[1]))
		{
			return (L("Error#6"));
		}
		return null;
	}
}
