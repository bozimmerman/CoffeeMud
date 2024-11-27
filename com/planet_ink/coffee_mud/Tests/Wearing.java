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
Copyright 2024-2024 Bo Zimmerman

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
public class Wearing extends PropTest
{
	@Override
	public String ID()
	{
		return "Wearing";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		super.resetTest();
		final MOB M = mobs[0];
		final Item w1 = CMClass.getWeapon("Sword");
		M.addItem(w1);
		final Item w2 = CMClass.getWeapon("Sword");
		M.addItem(w2);
		final Item w3 = CMClass.getWeapon("TwoHandedSword");
		M.addItem(w3);
		/*

		CMLib.commands().postWear(M, w1, true);
		if((!w1.amBeingWornProperly())||(M.fetchWieldedItem()!=w1))
			return (L("Error#1"));

		CMLib.commands().forceStandardCommand(M, "Hold", new XArrayList<String>("Hold","$"+w2.Name()+"$"));
		if((!w2.amBeingWornProperly())||(M.fetchHeldItem()!=w2))
			return (L("Error#2"));

		CMLib.commands().postWear(M, w3, true);
		if((!w3.amBeingWornProperly())
		||(M.fetchWieldedItem()!=w3)
		||(M.fetchHeldItem()!=w3))
			return (L("Error#3"));
		if((w1.amBeingWornProperly())
		||(w2.amBeingWornProperly()))
			return (L("Error#4"));

		CMLib.commands().postWear(M, w1, true);
		if((!w1.amBeingWornProperly())
		||(M.fetchWieldedItem()!=w1)
		||(w3.amBeingWornProperly()))
			return (L("Error#5"));

		CMLib.commands().forceStandardCommand(M, "Hold", new XArrayList<String>("Hold","$"+w2.Name()+"$"));
		if((!w2.amBeingWornProperly())
		||(M.fetchHeldItem()!=w2))
			return (L("Error#6"));

		*/

		final Item a1 = CMClass.getArmor("Hat");
		M.addItem(a1);
		final Item a2 = CMClass.getArmor("Helmet");
		M.addItem(a2);

		CMLib.commands().postWear(M, a1, true);
		if((!a1.amBeingWornProperly())
		||(!M.fetchWornItems(Wearable.WORN_HEAD, (short)-1, (short)0).contains(a1)))
			return (L("Error#7"));

		CMLib.commands().postWear(M, a2, true);
		if((!a2.amBeingWornProperly())
		||(!M.fetchWornItems(Wearable.WORN_HEAD, (short)-1, (short)0).contains(a2))
		||(a1.amBeingWornProperly()))
			return (L("Error#8"));
		return null;
	}
}
