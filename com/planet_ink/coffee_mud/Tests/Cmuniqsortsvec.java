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
public class Cmuniqsortsvec extends StdTest
{
	@Override
	public String ID()
	{
		return "Cmuniqsortsvec";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final String[] tests=new String[]{
			"Elvish",
			"Fighter_FastSlinging",
			"Common",
			"Proficiency_Sling",
			"Skill_Befriend",
			"Skill_Haggle",
			"Skill_Recall",
			"Skill_Write",
			"Song_Detection",
			"Song_Nothing",
			"Specialization_EdgedWeapon",
			"SignLanguage",
			"Song_Seeing",
			"Specialization_EdgedWeapon",
			"FireBuilding",
			"Song_Valor",
			"Specialization_EdgedWeapon",
			"Fighter_FastSlinging",
			"FireBuilding",
			"Proficiency_Sling",
			"FireBuilding",
			"Song_Charm",
			"Fighter_FastSlinging",
			"FireBuilding",
			"Proficiency_Sling",
			"Specialization_Sword",
			"Butchering",
			"Skill_Befriend",
			"Skill_Haggle",
			"Song_Armor",
			"Song_Babble",
			"Song_Charm",
			"Song_Seeing",
			"FireBuilding",
			"Play_Break",
			"Play_Tempo",
			"Skill_Befriend",
			"Skill_Recall",
			"Skill_Write",
			"Song_Nothing",
			"Specialization_Ranged",
			"Fighter_FastSlinging",
		};
		for(int y=0;y<100;y++)
		{
			for(int x=0;x<100;x++)
			{
				final java.util.concurrent.atomic.AtomicInteger counter=new java.util.concurrent.atomic.AtomicInteger(0);
				final CMUniqSortSVec<Ability> vec = new CMUniqSortSVec<Ability>();
				final int delayType = x/30;
				for(int i=0;i<tests.length;i++)
				{
					final Ability A1=CMClass.getAbility(tests[i]);
					if(delayType == 0)
					{
						final Ability A=A1;
						if(vec.find(A.ID())==null)
							vec.addElement(A);
						counter.incrementAndGet();
					}
					else
					{
						CMLib.threads().executeRunnable(new Runnable()
						{
							final Ability A=A1;
							@Override
							public void run()
							{
								if(delayType == 2)
									CMLib.s_sleep(CMLib.dice().roll(1, 10, -1));
								if(vec.find(A.ID())==null)
									vec.addElement(A);
								counter.incrementAndGet();
							}
						});
					}
				}
				while(counter.get() < tests.length)
					CMLib.s_sleep(10);
				final Set<String> found=new TreeSet<String>();
				for(int i=0;i<vec.size();i++)
					if(found.contains(vec.get(i).ID()))
					{
						return (L("Error#"+i+"("+vec.get(i).ID()+")"));
					}
					else
					{
						found.add(vec.get(i).ID());
					}
				if(vec.size() != found.size())
				{
					return (L("Error#"));
				}
			}
		}
		return null;
	}
}
