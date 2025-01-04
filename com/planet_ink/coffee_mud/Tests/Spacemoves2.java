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
import java.math.BigDecimal;
import java.net.*;
import java.text.*;
import java.util.*;

/*
Copyright 2024-2025 Bo Zimmerman

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
public class Spacemoves2 extends StdTest
{
	@Override
	public String ID()
	{
		return "Spacemoves2";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "spaceall"};
	}

/*
			360
		330		30
	300				60
270						90
	240				120
		210		150
			 180
 */
	
	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final boolean shortDebug = !what.equalsIgnoreCase(ID());
		final SpaceShip o = (SpaceShip)CMClass.getItem("GenSpaceShip");
		final Random ra = new Random(System.nanoTime());
		final Coord3D startCoords = new Coord3D(new long[] {ra.nextLong()/2,ra.nextLong()/2,ra.nextLong()/2});
		long startDistance = 10000000L;
		int testNum = 0;
		final int colWidth = 19;
		final long distance = startDistance;
		final long startSpeed = startDistance / 1000L;
		startDistance = startDistance / 2;
		for(int t=0;t<10;t++)
		{
			testNum++;
			final Dir3D setupTargetDir = new Dir3D(new double[] {Math.PI*2*ra.nextDouble(), Math.PI*ra.nextDouble()});
			final Coord3D destCoords = CMLib.space().moveSpaceObject(startCoords, setupTargetDir, distance).copyOf();
			final Dir3D wrongTargetDir = new Dir3D(new double[] {Math.PI*2*ra.nextDouble(), Math.PI*ra.nextDouble()});
			if(!shortDebug)
			{
				Log.debugOut(CMStrings.padRight("Dir", colWidth)+"|"+
						 CMStrings.padRight("Target", colWidth)+"|"+
						 CMStrings.padRight("Accel", colWidth)+"|"+
						 CMStrings.padRight("NewDir", colWidth)+"|"+
						 "Delta");
			}
			o.setCoords(startCoords.copyOf());
			o.setDirection(wrongTargetDir);
			o.setSpeed(startSpeed);
			double finalWrong = 999;
			int tries = 0;
			final List<Dir3D> lastTwo = new ArrayList<Dir3D>(2);
			for(tries=0;tries<100 && (finalWrong>1);tries++)
			{
				final Dir3D rightDir = CMLib.space().getDirection(o.coordinates(), destCoords);
				final Dir3D oldDir = o.direction().copyOf();
				Dir3D deltaDir = rightDir.copyOf();
				o.setFacing(deltaDir.copyOf());
				o.setSpeed(CMLib.space().accelSpaceObject(o.direction(), o.speed(), o.facing(), 300.0));
				CMLib.space().moveSpaceObject(o);
				finalWrong = Math.round(10000*Math.toDegrees(CMLib.space().getAngleDelta(o.direction(), rightDir)))/10000.0;
				if(!shortDebug)
				{
					Log.debugOut(CMStrings.padRight(CMLib.english().directionDescShort(oldDir.toDoubles()), colWidth)+"|"+
							 CMStrings.padRight(CMLib.english().directionDescShort(rightDir.toDoubles()), colWidth)+"|"+
							 CMStrings.padRight(CMLib.english().directionDescShort(deltaDir.toDoubles()), colWidth)+"|"+
							 CMStrings.padRight(CMLib.english().directionDescShort(o.direction().toDoubles()), colWidth)+"|"+
							 finalWrong
					);
				}
				if(lastTwo.size()<2)
					lastTwo.add(o.direction().copyOf());
				else
				if(lastTwo.contains(o.direction()))
					break;
				else
				{
					lastTwo.set(0, lastTwo.get(1));
					lastTwo.set(1, o.direction().copyOf());
				}
			}
			if(finalWrong>1)
			{
				return "Fail: #"+(testNum)+": "+(finalWrong)
						+": SD="+CMLib.english().directionDescShortest(setupTargetDir.toDoubles())
						+", WD="+CMLib.english().directionDescShortest(wrongTargetDir.toDoubles())
						+", SI="+startDistance;
			}
		}
		return null;
	}

	public String spaceMoveError(final String pos, final SpaceShip o, final Dir3D dir,
								 final double speedDiff, final double speed,
								 final double distDiff, final double traveledDistance, final double distanceTravelled,
								 final Coord3D oldCoords)
	{
		String complaint="";
		if(!dir.equals(o.direction()))
		{
			complaint += " angles: "+
				Math.round(Math.toDegrees(dir.xyd()))+"mk"+Math.round(Math.toDegrees(dir.zd()))
			 +" vs "+Math.round(Math.toDegrees(o.direction().xyd()))+"mk"+Math.round(Math.toDegrees(o.direction().zd()))
			 +"\n\r";
		}
		if(speedDiff > 1)
			complaint +=  "speed: "+o.speed()+" vs "+speed+"\n\r";
		if(distDiff > 1)
			complaint += "dist: "+traveledDistance+" vs "+distanceTravelled+"\n\r";
		if(!o.coordinates().equals(oldCoords))
		{
			complaint += "coords: "+CMParms.toListString(oldCoords.toLongs())+" vs "+CMParms.toListString(o.coordinates().toLongs());
			complaint += "\n\r";
		}
		return "Error: Space move2 "+pos+", test failed: "+"\n\r"+complaint;
	}
}
