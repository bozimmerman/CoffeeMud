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
public class Spacemovereport1 extends StdTest
{
	@Override
	public String ID()
	{
		return "Spacemovereport1";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"info_all"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		//List<double[]> results = new ArrayList<double[]>();
		for(double dir0 = 0; dir0 <=Math.PI*2; dir0 += (Math.PI/12.0))
		{
			for(double dir1 = 0; dir1 <(Math.PI+(Math.PI/12)); dir1 += (Math.PI/12.0))
			{
				for(double adir0 = 0; adir0 <=Math.PI*2; adir0 += (Math.PI/12.0))
				{
					for(double adir1 = 0; adir1 <(Math.PI+(Math.PI/12)); adir1 += (Math.PI/12.0))
					{
						if(dir1 > Math.PI)
							dir1=Math.PI;
						if(adir1 > Math.PI)
							adir1=Math.PI;
						final double[] curDir = new double[] {dir0, dir1};
						final double[] accelDir = new double[] {adir0, adir1};
						double curSpeed = 1000;
						final long newAcceleration = 200;
						int steps = 0;
						final double totDirDiff = CMLib.space().getAngleDelta(curDir, accelDir);
						mob.tell("Interesting: ");
						mob.tell("Andgle diff between "+Math.round(Math.toDegrees(curDir[0]))+"mk"+Math.round(Math.toDegrees(curDir[1]))
						+"   and   "+Math.round(Math.toDegrees(accelDir[0]))+"mk"+Math.round(Math.toDegrees(accelDir[1]))
						+"       is: "+Math.round(Math.toDegrees(totDirDiff)));
						final double halfPI = Math.PI/2.0;
						while(!Arrays.equals(curDir, accelDir))
						{
							final double oldCurSpeed = curSpeed;
							final double curDirDiff = CMLib.space().getAngleDelta(curDir, accelDir);
							final double[] oldCurDir=new double[]{curDir[0],curDir[1]};
							curSpeed = CMLib.space().accelSpaceObject(curDir,curSpeed,accelDir, newAcceleration);
							final double newDirDiff = CMLib.space().getAngleDelta(curDir, accelDir);
							if((curDirDiff > halfPI)
							&&(newDirDiff > halfPI))
							{
								if(curSpeed > oldCurSpeed)
								{
									Log.debugOut("Step "+steps+" of "+
											Math.round(Math.toDegrees(oldCurDir[0]))+"@"+Math.round(Math.toDegrees(oldCurDir[1]))
											+" -> "
											+Math.round(Math.toDegrees(accelDir[0]))+"@"+Math.round(Math.toDegrees(accelDir[1]))
											+" (angle Diff "+curDirDiff+") went from speed "+oldCurSpeed+" to "+curSpeed);
									//CMLib.space().moveSpaceObject(oldCurDir,oldCurSpeed,accelDir, newAcceleration);
									//curDirDiff = CMLib.space().getAngleDelta(oldCurDir, accelDir);
								}
							}
							else
							if((curDirDiff < halfPI)
							&&(newDirDiff < halfPI))
							{
								if(curSpeed < oldCurSpeed)
								{
									mob.tell("Step "+steps+" of "+
											Math.round(Math.toDegrees(oldCurDir[0]))+"@"+Math.round(Math.toDegrees(oldCurDir[1]))
											+" -> "
											+Math.round(Math.toDegrees(accelDir[0]))+"@"+Math.round(Math.toDegrees(accelDir[1]))
											+" (angle Diff "+curDirDiff+") went from speed "+oldCurSpeed+" to "+curSpeed);
								}
							}
							steps++;
						}
						// Test Ideas
						// test whether smaller angle diffs result in fewer steps.
						mob.tell(Math.round(Math.toDegrees(totDirDiff))+", ="+steps+"                      fspeed="+curSpeed);
						//results.add(new double[]{Math.round(Math.toDegrees(totDirDiff)),steps});
					}
				}
			}
		}
		return null;
	}
}
