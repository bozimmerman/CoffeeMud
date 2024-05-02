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
public class Cmparms extends StdTest
{
	@Override
	public String ID()
	{
		return "Cmparms";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		List<String> V = CMParms.parseAny("blah~BLAH~BLAH!", '~', true);
		if (V.size() != 3)
		{
			return (L("Error#1"));
		}
		if (!CMParms.combineWithX(V, "~", 0).equals("blah~BLAH~BLAH!~"))
		{
			return (L("Error#2"));
		}
		V = CMParms.parseAny("blah~~", '~', true);
		if (V.size() != 1)
		{
			return (L("Error#3"));
		}
		if (!V.get(0).equals("blah"))
		{
			return (L("Error#4"));
		}
		V = CMParms.parseAny("blah~~", '~', false);
		if (V.size() != 3)
		{
			return (L("Error#5"));
		}
		if (!CMParms.combineWithX(V, "~", 0).equals("blah~~~"))
		{
			return (L("Error#6"));
		}
		V = CMParms.parseAny("blah~~BLAH~~BLAH!", "~~", true);
		if (V.size() != 3)
		{
			return (L("Error#7"));
		}
		if (!CMParms.combineWithX(V, "~~", 0).equals("blah~~BLAH~~BLAH!~~"))
		{
			return (L("Error#8"));
		}
		V = CMParms.parseAny("blah~~~~", "~~", true);
		if (V.size() != 1)
		{
			return (L("Error#9"));
		}
		if (!V.get(0).equals("blah"))
		{
			return (L("Error#10"));
		}
		V = CMParms.parseAny("blah~~~~", "~~", false);
		if (V.size() != 3)
		{
			return (L("Error#11"));
		}
		if (!CMParms.combineWithX(V, "~~", 0).equals("blah~~~~~~"))
		{
			return (L("Error#12"));
		}
		V = CMParms.parseSentences("blah. blahblah. poo");
		if (V.size() != 3)
		{
			return (L("Error#13"));
		}
		if (!V.get(0).equals("blah."))
		{
			return (L("Error#14:@x1", V.get(0)));
		}
		if (!V.get(1).equals("blahblah."))
		{
			return (L("Error#15:@x1", V.get(1)));
		}
		if (!V.get(2).equals("poo"))
		{
			return (L("Error#16:@x1", V.get(2)));
		}
		V = CMParms.parseAny("blah~BLAH~BLAH!~", '~', true);
		if (V.size() != 3)
		{
			return (L("Error#17"));
		}
		if (!CMParms.combineWithX(V, "~", 0).equals("blah~BLAH~BLAH!~"))
		{
			return (L("Error#18"));
		}
		V = CMParms.parseAny("blah~~BLAH~~BLAH!~~", "~~", true);
		if (V.size() != 3)
		{
			return (L("Error#19"));
		}
		if (!CMParms.combineWithX(V, "~~", 0).equals("blah~~BLAH~~BLAH!~~"))
		{
			return (L("Error#20"));
		}
		V = CMParms.parseAny("blah~BLAH~BLAH!~", '~', false);
		if (V.size() != 4)
		{
			return (L("Error#21"));
		}
		if (!CMParms.combineWithX(V, "~", 0).equals("blah~BLAH~BLAH!~~"))
		{
			return (L("Error#22"));
		}
		V = CMParms.parseAny("blah~~BLAH~~BLAH!~~", "~~", false);
		if (V.size() != 4)
		{
			return (L("Error#23"));
		}
		if (!CMParms.combineWithX(V, "~~", 0).equals("blah~~BLAH~~BLAH!~~~~"))
		{
			return (L("Error#24"));
		}
		return null;
	}
}
