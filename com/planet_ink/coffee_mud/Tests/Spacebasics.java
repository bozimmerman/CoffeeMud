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
public class Spacebasics extends StdTest
{
	@Override
	public String ID()
	{
		return "Spacebasics";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "spaceall"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		// test angle integrity
		final Random r = new Random(System.currentTimeMillis());
		for(final int distance : new int[] { 1000, 10000, 100000, 1000000 })
		{
			for(int i=0;i<1000;i++)
			{
				final long[] opos = new long[] { r.nextLong(),r.nextLong(),r.nextLong() };
				final double[] angle = new double[] {
					(Math.PI * 2.0) * r.nextDouble(),
					Math.PI  * r.nextDouble()
				};
				final long[] npos = CMLib.space().moveSpaceObject(opos, angle, distance);
				final double[] nangle = CMLib.space().getDirection(opos, npos);
				final double delta = CMLib.space().getAngleDelta(angle, nangle);
				if(delta > 0.1)
				{
					return ("Fail: "+CMLib.english().coordDescShort(opos)+" @ "+CMLib.english().directionDescShort(angle) + " -> "
							+CMLib.english().coordDescShort(npos)+" @ "+CMLib.english().directionDescShort(nangle) + " : " + delta );
				}
			}
			for(int i=0;i<1000;i++)
			{
				final long[] opos = new long[] { r.nextLong(),r.nextLong(),r.nextLong() };
				final long[] npos = new long[] {
					opos[0] + r.nextInt(distance/3),opos[1] + r.nextInt(distance/3),opos[2] + r.nextInt(distance/3)
				};
				final long actualDistance = CMLib.space().getDistanceFrom(opos, npos);
				final double[] angle = CMLib.space().getDirection(opos, npos);
				final long[] cpos = CMLib.space().moveSpaceObject(opos, angle, actualDistance);
				final long delta = Math.abs(npos[0]-cpos[0])+Math.abs(npos[1]-cpos[1])+Math.abs(npos[2]-cpos[2]);
				if(delta > actualDistance/20)
				{
					return ("Fail: "+CMLib.english().coordDescShort(opos)+" @ "+CMLib.english().directionDescShort(angle) + " -> "
							+CMLib.english().coordDescShort(npos)+" = " + CMLib.english().coordDescShort(cpos)+" : "+delta );
				}
			}
			for(int i=0;i<100;i++)
			{
				final long[] opos = new long[] { r.nextLong(),r.nextLong(),r.nextLong() };
				final double[] angle = new double[] {
					Math.PI * 2.0 * r.nextDouble(),
					Math.PI  * r.nextDouble()
				};
				final long[] npos = CMLib.space().moveSpaceObject(opos, angle, distance);
				final long dist = CMLib.space().getDistanceFrom(opos, npos);
				final long delta = Math.abs(dist-distance);
				if(delta > distance/20)
				{
					return ("Fail: "+CMLib.english().coordDescShort(opos)+" @ "+CMLib.english().directionDescShort(angle) + " -> "
							+CMLib.english().coordDescShort(npos)+" = " + distance+" : "+dist );
				}
			}
			boolean overlap=false;
			for(int i=0;i<1000;i++)
			{
				overlap = !overlap;
				final long[] pos1 = new long[] {
									1000000L + (Math.abs(r.nextLong()) % 100),
									1000000L + (Math.abs(r.nextLong()) % 100),
									1000000L + (Math.abs(r.nextLong()) % 100) };
				final long r1 = Math.abs(r.nextLong() % distance/100)+1;
				final long r2 = Math.abs(r.nextLong() % distance/10)+1;
				final long localDist;
				if(overlap)
					localDist = (distance % (r1 + r2 - 1)) + 1;
				else
				if(i<10)
					localDist = (r1 + r2) + 23;
				else
					localDist = r1 + r2 + r.nextInt((int)(distance-r1-r2));
				final double[] angle = new double[] {
					(Math.PI * 2.0) * r.nextDouble(),
					Math.PI  * r.nextDouble()
				};
				final long[] pos2 = CMLib.space().moveSpaceObject(pos1, angle, localDist);
				if(Arrays.equals(pos1, pos2))
				{
					return ("Fail-moveObj: "+CMLib.english().coordDescShort(pos1)+": "+CMLib.english().directionDescShort(angle)+"="+localDist);
				}
				final BoundedSphere cube1 = new BoundedSphere(pos1,r1);
				final BoundedSphere cube2 = new BoundedSphere(pos2,r2);
				if(cube1.intersects(cube2) != overlap)
				{
					/*
					System.out.println("cube1, radius="+r1+", coordinates=");
					System.out.println("x: "+cube1.lx +","+cube1.rx);
					System.out.println("y: "+cube1.ty +","+cube1.by);
					System.out.println("z: "+cube1.iz +","+cube1.oz);
					System.out.println("cube2, radius="+r2+", coordinates=");
					System.out.println("x: "+cube2.lx +","+cube2.rx);
					System.out.println("y: "+cube2.ty +","+cube2.by);
					System.out.println("z: "+cube2.iz +","+cube2.oz);
					System.out.println("distance between centers = "+localDist);
					cube1.intersects(cube2);
					*/
					if(overlap)
					{
						if((localDist-r1-r2) != -1) // that's just too close to call
							return ("Fail-noverlap: "+CMLib.english().coordDescShort(pos1)+" # "+ r1 + " -> "
									+CMLib.english().coordDescShort(pos2)+" # "+r2+" = " + localDist+"("+(localDist-r1-r2)+")" );
					}
					else
					if((localDist-r1-r2) != 1) // that's just too close to call
						return ("Fail-overlaps: "+CMLib.english().coordDescShort(pos1)+" # "+ r1 + " -> "
								+CMLib.english().coordDescShort(pos2)+" # "+r2+" = " + localDist+"("+(localDist-r1-r2)+")" );
				}
			}
		}
		return null;
	}
}
