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
public class Spaceintersect extends StdTest
{
	@Override
	public String ID()
	{
		return "Spaceintersect";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "spaceall"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		for(final Enumeration<SpaceObject> o1 = CMLib.space().getSpaceObjects(); o1.hasMoreElements();)
		{
			final SpaceObject O1 = o1.nextElement();
			final long O1radius = Math.round(CMath.mul(O1.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS));
			final BoundedSphere O1Cube = new BoundedSphere(O1.coordinates(), O1radius);
			for(final Enumeration<SpaceObject> o2 = CMLib.space().getSpaceObjects(); o2.hasMoreElements();)
			{
				final SpaceObject O2 = o2.nextElement();
				if(O1 != O2)
				{
					final long O2radius = Math.round(CMath.mul(O2.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS));
					final BoundedSphere O2Cube = new BoundedSphere(O2.coordinates(), O2radius);
					if(O1Cube.intersects(O2Cube))
					{
						return (O1.Name()+" intersects "+O2.Name()+" right now: "
										+(CMLib.space().getDistanceFrom(O1,O2)-O1radius-O2radius));
						/*
						for(int i=1;i<100000;i++)
						{
							final double[] moondir = CMLib.space().getOppositeDir(CMLib.space().getDirection(O2, O1));
							final long[] newCoord = CMLib.space().moveSpaceObject(O2.coordinates(), moondir, i);
							final BoundedSphere O3Cube = new BoundedSphere(newCoord, O2radius);
							if(!O1Cube.intersects(O3Cube))
							{
								mob.tell(O1.Name()+" stops intersecting "+O2.Name()+": "
												+(CMLib.space().getDistanceFrom(O1.coordinates(),newCoord)-O1radius-O2radius) + ": "+i);
								break;
							}
						}
						*/
					}
				}
			}
		}
		return null;
	}
}
