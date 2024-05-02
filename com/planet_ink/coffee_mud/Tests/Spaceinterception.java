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
public class Spaceinterception extends StdTest
{
	@Override
	public String ID()
	{
		return "Spaceinterception";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "spaceall"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final Random rand=new Random(System.currentTimeMillis());
		final GalacticMap space=CMLib.space();
		int passed=0;
		int fails=0;
		final int numTests=100;
		final int maxTicks=10000;
		String errMsg=null;
		for(int tests=0;tests<numTests;tests++)
		{
			try
			{
				final SpaceObject obj1 = (SpaceObject)CMClass.getItem("StdSpaceBody");
				obj1.setRadius(100);
				final SpaceObject obj2 = (SpaceObject)CMClass.getItem("StdSpaceBody");
				obj2.setRadius(100);
				for(int i=0;i<3;i++)
				{
					obj1.coordinates()[i]=rand.nextInt(50000);
					obj2.coordinates()[i]=rand.nextInt(50000);
				}
				obj2.direction()[0]=Math.abs(rand.nextDouble() * Math.PI * 2.0);
				obj2.direction()[1]=Math.abs(rand.nextDouble() * Math.PI);
				obj2.setSpeed(300+rand.nextInt(300));
				final Pair<double[], Long> pair = space.calculateIntercept(obj1, obj2, 1000, maxTicks);
				if(pair == null)
				{
					throw new CMException("FAILED! #"+tests+": "+CMLib.english().coordDescShort(obj2.coordinates())
							+": "+CMLib.english().directionDescShort(obj2.direction())
							+": "+obj2.speed());
				}
				else
				{
					//final SpaceObject orig2=(SpaceObject)obj2.copyOf();
					obj1.setDirection(pair.first);
					obj1.setSpeed(pair.second.longValue());
					if(!space.canMaybeIntercept(obj1, obj2, maxTicks, pair.second.longValue()))
					{
						mob.tell("Stupid #"+tests+": "+CMLib.english().coordDescShort(obj2.coordinates())
								+": "+CMLib.english().directionDescShort(obj2.direction())
								+": "+obj2.speed());
						continue;
					}
					int atti=0;
					final long radius = obj1.radius()+obj2.radius();
					//final double orig = space.getDistanceFrom(obj1, obj2);
					for(;atti<maxTicks;atti++)
					{
						final long[] oldCoords1=obj1.coordinates().clone();
						obj1.setCoords(space.moveSpaceObject(oldCoords1, obj1.direction(), Math.round(obj1.speed())));
						final double x=space.getMinDistanceFrom(oldCoords1, obj1.coordinates(), obj2.coordinates());
						if(x<radius)
							break;
						obj2.setCoords(space.moveSpaceObject(obj2.coordinates(), obj2.direction(), Math.round(obj2.speed())));
					}
					if(atti>=maxTicks)
					{
						throw new CMException("Failed #"+tests+": "+CMLib.english().coordDescShort(obj2.coordinates())
								+": "+CMLib.english().directionDescShort(obj2.direction())
								+": "+obj2.speed());
					}
					else
						passed++;
				}
			}
			catch(final CMException e)
			{
				fails++;
				if(errMsg == null)
					errMsg=e.getMessage();
			}
		}
		if(fails *2 > passed)
			return errMsg;
		return null;
	}
}
