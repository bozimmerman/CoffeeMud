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
public class Spaceturns extends StdTest
{
	@Override
	public String ID()
	{
		return "Spaceturns";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "spaceall"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final double[][][] tests = new double[][][] {
			{ {1000.0}, {Math.PI, Math.PI-.2}, {Math.PI/2.0, Math.PI-.2} },
			{ {1000.0}, {Math.PI, Math.PI/2.0}, {Math.PI/2.0, Math.PI/2.0} },
			{ {1000.0}, {Math.PI/2.0, Math.PI/2.0}, {Math.PI+Math.PI/2.0, Math.PI/2.0}},
			{ {100.0}, {Math.PI, Math.PI-.2}, {Math.PI/2.0, Math.PI-.2} },
			{ {100.0}, {Math.PI, Math.PI/2.0}, {Math.PI/2.0, Math.PI/2.0} },
			{ {100.0}, {Math.PI/2.0, Math.PI/2.0}, {Math.PI+Math.PI/2.0, Math.PI/2.0}},
		};
		final SpaceShip o = (SpaceShip)CMClass.getItem("GenSpaceShip");
		for(int t=0;t<tests.length;t++)
		{
			final double[][] test = tests[t];
			final double speed = test[0][0];
			final Dir3D startDir = new Dir3D(test[1]);
			final Dir3D accelDir = new Dir3D(test[2]);
			final Coord3D startCoords = new Coord3D();
			o.setCoords(startCoords.copyOf());
			o.setDirection(startDir);
			o.setSpeed(speed);
			o.setFacing(accelDir);
			int i=0;
			for(i=0;i<1000;i++)
			{
				CMLib.space().accelSpaceObject(o,accelDir,3.0);
				final double d = CMLib.space().getAngleDelta(o.direction(), accelDir);
				/*
				mob.tell(Math.toDegrees(
						 o.direction()[0])+","+Math.toDegrees(o.direction()[1])
						+"  -->  "
						+Math.toDegrees(accelDir[0])+","+Math.toDegrees(accelDir[1])
						+" === "+d);
				*/
				if(d<0.1)
					break;
			}
			if((test.length>3)&&(i!=test[3][0]))
			{
				return ("Error: Space turn test "+t+", test failed: "+i+"!="+test[2][0]);
			}
			/*
			else
				mob.tell("Info: Space turn test "+t+", test result: "+i);
			*/
		}
		return null;
	}
}
