package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.CompiledFormula;
import com.planet_ink.coffee_mud.core.CMath.CompiledOperation;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.Color;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.PlayerCode;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Tests.interfaces.CMTest;
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
* <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
* <p>Portions Copyright (c) 2004-2024 Bo Zimmerman</p>

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
public class Test extends StdCommand
{
	public Test()
	{
	}

	private final String[]	access	= I(new String[] { "TEST" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected Map<String,List<CMTest>> getMapped()
	{
		final Map<String,List<CMTest>> map = new HashMap<String,List<CMTest>>();
		for(final Enumeration<CMTest> e=CMClass.tests();e.hasMoreElements();)
		{
			final CMTest T = e.nextElement();
			for(final String cat : T.getTestGroups())
			{
				if(!map.containsKey(cat))
					map.put(cat, new ArrayList<CMTest>());
				map.get(cat).add(T);
			}
		}
		return map;
	}

	protected String getCategoriesList()
	{
		final StringBuilder str = new StringBuilder("");
		str.append(L("^HCategories: ^N"));
		final Map<String,List<CMTest>> all = this.getMapped();
		for(final String s : all.keySet())
			str.append(s).append(", ");
		return str.toString().substring(0,str.length()-2);
	}

	protected String getTestsList()
	{
		final StringBuilder str = new StringBuilder("");
		str.append(L("^HTests: ^N"));
		for(final Enumeration<CMTest> e=CMClass.tests();e.hasMoreElements();)
		{
			final CMTest T = e.nextElement();
			str.append(T.name().toLowerCase()).append(", ");
		}
		return str.toString().substring(0,str.length()-2);
	}

	protected String finalTestOrCategory(final String s)
	{
		final Map<String,List<CMTest>> all = this.getMapped();
		if(all.containsKey(s))
			return s;
		for(final String str : all.keySet())
		{
			if(str.equalsIgnoreCase(s))
				return str;
		}
		for(final String str : all.keySet())
		{
			if(str.startsWith(s.toLowerCase()))
				return str;
		}
		CMTest T = CMClass.getTest(s);
		if(T != null)
			return T.ID();
		for(final Enumeration<CMTest> e=CMClass.tests();e.hasMoreElements();)
		{
			T = e.nextElement();
			if(s.equalsIgnoreCase(T.ID()))
				return T.ID();
		}
		for(final Enumeration<CMTest> e=CMClass.tests();e.hasMoreElements();)
		{
			T = e.nextElement();
			if(T.ID().toLowerCase().startsWith(s.toLowerCase()))
				return T.ID();
		}
		for(final Enumeration<CMTest> e=CMClass.tests();e.hasMoreElements();)
		{
			T = e.nextElement();
			if(T.ID().toLowerCase().endsWith(s.toLowerCase()))
				return T.ID();
		}
		for(final String str : all.keySet())
		{
			if(str.indexOf(s.toLowerCase())>=0)
				return str;
		}
		for(final Enumeration<CMTest> e=CMClass.tests();e.hasMoreElements();)
		{
			T = e.nextElement();
			if(T.ID().toLowerCase().indexOf(s.toLowerCase())>=0)
				return T.ID();
		}
		return null;
	}

	protected String getTestsList(String category)
	{
		final Map<String,List<CMTest>> all = this.getMapped();
		if(!all.containsKey(category))
		{
			for(final String str : all.keySet())
			{
				if(str.equalsIgnoreCase(category))
					category = str;
			}
		}
		if(!all.containsKey(category))
		{
			for(final String str : all.keySet())
			{
				if(str.startsWith(category.toLowerCase()))
					category = str;
			}
		}
		if(!all.containsKey(category))
			return L("Unknown category '"+category+"'");
		final StringBuilder str = new StringBuilder("");
		str.append(L("^HTests in @x1: ^N",category));
		final List<CMTest> tests = all.get(category);
		for(final CMTest T : tests)
		{
			str.append(T.name().toLowerCase()).append(", ");
		}
		return str.toString().substring(0,str.length()-2);
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Session S = mob.session();
		if(S == null)
			return false;
		if((commands.size()>1)
		&&(commands.get(1).equalsIgnoreCase("list")))
		{
			if(commands.size()==2)
			{
				mob.tell(getCategoriesList());
				mob.tell(getTestsList());
				return false;
			}
			mob.tell(getTestsList(commands.get(2)));
		}
		else
		if(commands.size()>1)
		{
			final String what=commands.get(1).toUpperCase().trim();
			final String testID = finalTestOrCategory(what);
			if(testID == null)
			{
				mob.tell(L("'@x1' is an unknown test or category.  Try Test LIST",what));
				mob.tell(getCategoriesList());
				return false;
			}
			List<CMTest> tests;
			final Map<String,List<CMTest>> all = this.getMapped();
			if(all.containsKey(testID))
				tests = all.get(testID);
			else
				tests = new XVector<CMTest>(CMClass.getTest(testID));
			int longest = 0;
			for(final CMTest T : tests)
			{
				if(T.name().length()>longest)
					longest = T.name().length();
			}
			commands.remove(0); // remove 'test'
			commands.remove(0); // remove 'id'
			for(final CMTest T : tests)
			{
				S.print(CMStrings.padRight(T.name(), longest)+": ");
				T.cleanupTest();
				try
				{
					final String result = T.doTest(mob, metaFlags, what, commands);
					if(result == null)
						S.println("Passed");
					else
						S.println(result);
				}
				catch(final Throwable t)
				{
					Log.errOut(t);
					S.println(t.getMessage());
				}
				T.cleanupTest();
			}
		}
		else
		{
			mob.tell(L("Test what?  Try Test [CATAGORY] or Test LIST"));
			mob.tell(getCategoriesList());
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isASysOp(mob);
	}
}
