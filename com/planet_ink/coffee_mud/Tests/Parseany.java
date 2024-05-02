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
public class Parseany extends StdTest
{
	@Override
	public String ID()
	{
		return "Parseany";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final String t1="";
		final String t2="]blah";
		final String t3="]blah]";
		final String t4="boo]blah]";
		final String t5="boo]blah]poo";
		if (CMParms.parseAny(t1, "]", true).size() != 0)
		{
			return (L("Error#0"));
		}
		if (CMParms.parseAny(t1, "]", false).size() != 0)
		{
			return (L("Error#1"));
		}
		if (!Arrays.deepEquals(CMParms.parseAny(t2, "]", true).toArray(), new Object[] { "blah" }))
		{
			return (L("Error#2"));
		}
		if (!Arrays.deepEquals(CMParms.parseAny(t2, "]", false).toArray(), new Object[] { "", "blah" }))
		{
			return (L("Error#3"));
		}
		if (!Arrays.deepEquals(CMParms.parseAny(t3, "]", true).toArray(), new Object[] { "blah" }))
		{
			return (L("Error#4"));
		}
		if (!Arrays.deepEquals(CMParms.parseAny(t3, "]", false).toArray(), new Object[] { "", "blah", "" }))
		{
			return (L("Error#5"));
		}
		if (!Arrays.deepEquals(CMParms.parseAny(t4, "]", true).toArray(), new Object[] { "boo", "blah" }))
		{
			return (L("Error#6"));
		}
		if (!Arrays.deepEquals(CMParms.parseAny(t4, "]", false).toArray(), new Object[] { "boo", "blah", "" }))
		{
			return (L("Error#7"));
		}
		if (!Arrays.deepEquals(CMParms.parseAny(t5, "]", true).toArray(), new Object[] { "boo", "blah", "poo" }))
		{
			return (L("Error#8"));
		}
		if (!Arrays.deepEquals(CMParms.parseAny(t5, "]", false).toArray(), new Object[] { "boo", "blah", "poo" }))
		{
			return (L("Error#9"));
		}
		return null;
	}
}
