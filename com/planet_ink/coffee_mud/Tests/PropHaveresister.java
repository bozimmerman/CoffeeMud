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
public class PropHaveresister extends PropTest
{
	@Override
	public String ID()
	{
		return "PropHaveresister";
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
		final Ability HaveResister = CMClass.getAbility("Prop_HaveResister");
		HaveResister.setMiscText("pierce 100% holy 100% acid 30%");
		//mob.tell(L("Test:"+what+"-1: @x1", HaveResister.accountForYourself()));
		if (testResistance(mobs[0]))
		{
			return (L("Error#1"));
		}
		IS = giveTo(CMClass.getItem("SmallSack"), HaveResister, mobs[0], null, 0);
		R1.recoverRoomStats();
		if (!testResistance(mobs[0]))
		{
			return (L("Error#2"));
		}
		IS[0].unWear();
		R1.moveItemTo(IS[0], ItemPossessor.Expire.Never, ItemPossessor.Move.Followers);
		R1.recoverRoomStats();
		if (testResistance(mobs[0]))
		{
			return (L("Error#3"));
		}
		resetTest();
		HaveResister.setMiscText("pierce 100% holy 100% acid 30% MASK=-RACE +DWARF");
		//mob.tell(L("Test:"+what+"-2: @x1", HaveResister.accountForYourself()));
		if (testResistance(mobs[0]))
		{
			return (L("Error#4"));
		}
		if (testResistance(mobs[1]))
		{
			return (L("Error#5"));
		}
		IS = giveTo(CMClass.getItem("SmallSack"), HaveResister, mobs[0], mobs[1], 0);
		R1.recoverRoomStats();
		if (!testResistance(mobs[0]))
		{
			return (L("Error#6"));
		}
		if (testResistance(mobs[1]))
		{
			return (L("Error#7"));
		}
		IS[0].unWear();
		IS[1].unWear();
		R1.moveItemTo(IS[0], ItemPossessor.Expire.Never, ItemPossessor.Move.Followers);
		R1.moveItemTo(IS[1], ItemPossessor.Expire.Never, ItemPossessor.Move.Followers);
		R1.recoverRoomStats();
		if (testResistance(mobs[0]))
		{
			return (L("Error#8"));
		}
		if (testResistance(mobs[1]))
		{
			return (L("Error#9"));
		}
		return null;
	}
}
