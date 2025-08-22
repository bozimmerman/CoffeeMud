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
Copyright 2025-2025 Bo Zimmerman

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
public class LongSet extends StdTest
{
	@Override
	public String ID()
	{
		return "LongSet";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"info_all"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		long memUse = 0;
		final com.planet_ink.coffee_mud.core.collections.LongSet set1 = new com.planet_ink.coffee_mud.core.collections.LongSet();
		memUse = CMLib.utensils().memoryUsage(set1, new ArrayList<String>(0),new HashSet<Object>());
		if(memUse > 64)
			return "Failed 1";
		for(int i=0;i<1000;i++)
			set1.add(i);
		for(int i=0;i<1000;i++)
		{
			final int x = CMLib.dice().roll(1,1000,-1);
			if (!set1.contains(x))
				return "Failed 1.5: " + x;
		}
		memUse = CMLib.utensils().memoryUsage(set1, new ArrayList<String>(0),new HashSet<Object>());
		if(memUse > 64)
			return "Failed 2";
		set1.clear();
		for(int i=1000;i>=0;i--)
			set1.add(i);
		memUse = CMLib.utensils().memoryUsage(set1, new ArrayList<String>(0),new HashSet<Object>());
		if(memUse > 64)
			return "Failed 2.2";
		for(int i=0;i<1000;i++)
		{
			final int x = CMLib.dice().roll(1,1000,-1);
			if (!set1.contains(x))
				return "Failed 2.7: " + x;
		}
		set1.clear();
		for(int i=0;i<1000;i+=2)
			set1.add(i);
		memUse = CMLib.utensils().memoryUsage(set1, new ArrayList<String>(0),new HashSet<Object>());
		if(memUse <= 2048)
			return "Failed 3";
		for(int i=0;i<1000;i++)
		{
			final int x = CMLib.dice().roll(1,1000,-1);
			if(x%2 == 0)
			{
				if (!set1.contains(x))
					return "Failed 3.5: " + x;
			}
			else
			if (set1.contains(x))
				return "Failed 3.7: " + x;
		}
		for(int i=1;i<1000;i+=2)
			set1.add(i);
		memUse = CMLib.utensils().memoryUsage(set1, new ArrayList<String>(0),new HashSet<Object>());
		if(memUse > 64)
			return "Failed 4";
		for(int i=0;i<1000;i++)
		{
			final int x = CMLib.dice().roll(1,1000,-1);
			if (!set1.contains(x))
				return "Failed 4.5: " + x;
		}
		set1.clear();
		for(int i=1;i<1000;i+=2)
			set1.add(i);
		for(int i=0;i<1000;i++)
		{
			final int x = CMLib.dice().roll(1,1000,-1);
			if(x%2 == 0)
			{
				if (set1.contains(x))
					return "Failed 4.6: " + x;
			}
			else
			if (!set1.contains(x))
				return "Failed 4.7: " + x;
		}
		for(int i=0;i<1000;i+=2)
			set1.add(i);
		memUse = CMLib.utensils().memoryUsage(set1, new ArrayList<String>(0),new HashSet<Object>());
		if(memUse > 64)
			return "Failed 5";
		for(int i=0;i<1000;i++)
		{
			final int x = CMLib.dice().roll(1,1000,-1);
			if (!set1.contains(x))
				return "Failed 5.5: " + x;
		}
		return null;
	}
}
