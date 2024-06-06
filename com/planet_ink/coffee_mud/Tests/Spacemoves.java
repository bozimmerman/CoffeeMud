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
public class Spacemoves extends StdTest
{
	@Override
	public String ID()
	{
		return "Spacemoves";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "spaceall"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final SpaceShip o = (SpaceShip)CMClass.getItem("GenSpaceShip");
		final boolean shortDebug = !what.equalsIgnoreCase(ID());
		// timtest1
		{
			// pitches 45 & 135
			final long[] startCoords = new long[] {0,0,0};
			o.setCoords(Arrays.copyOf(startCoords, 3));
			o.setDirection(new double[] {Math.PI/8.0, Math.PI*3.0/8.0});
			o.setSpeed(100.0);
			o.setFacing(o.direction());
			CMLib.space().moveSpaceObject(o);
			final long[] midCoord = Arrays.copyOf(o.coordinates(), 3);
			//mob.tell(CMParms.toListString(o.coordinates())+", dist="+CMLib.space().getDistanceFrom(startCoords, midCoord)+"");
			o.setDirection(new double[] {Math.PI/8.0, Math.PI*5.0/8.0});
			o.setSpeed(100.0);
			o.setFacing(o.direction());
			CMLib.space().moveSpaceObject(o);
			//mob.tell(CMParms.toListString(o.coordinates())+", dist="+CMLib.space().getDistanceFrom(midCoord, o.coordinates())+"");
			if(!Arrays.equals(o.coordinates(), new long[] {170,70,0}))
			{
				return ("Error space move: TT1: "+CMParms.toListString(o.coordinates())
				+", dist="+CMLib.space().getDistanceFrom(midCoord, o.coordinates())+"");
			}
		}
		final int accelMoves = 110;
		final int decelMoves = accelMoves - 1;
		final double accel = 3.0;
		int passes=0;
		int fails=0;
		String errMsg=null;
		for(int i=0;i<100;i++)
		{
			try
			{
				final double[] dir = new double[] { Math.random() * Math.PI*2, Math.random() * Math.PI};
				o.setSpeed(0.0);
				o.setDirection(dir);
				o.setFacing(dir);
				final double[] opDir = CMLib.space().getOppositeDir(dir);
				final double[] opOpDir = CMLib.space().getOppositeDir(opDir);
				if(CMLib.space().getAngleDelta(dir, opOpDir)>0.01)
				{
					final String angles = Math.round(Math.toDegrees(dir[0]))+"mk"+Math.round(Math.toDegrees(dir[1]))
					+" vs "+Math.round(Math.toDegrees(opOpDir[0]))+"mk"+Math.round(Math.toDegrees(opOpDir[1]));
					final String s=(L("Error: Space move OpDir Fail: "+angles));
					throw new CMException(s);
				}
				double predictedMidDistance=0;
				for(int a=1;a<=accelMoves;a++)
					predictedMidDistance += (accel * a);
				final double predictedMidSpeed = accelMoves * accel;
				double predictedDistance = predictedMidDistance;
				for(int a=accelMoves;a>0;a--)
					predictedDistance += (accel * a);
				final long[] startCoords = Arrays.copyOf(o.coordinates(),3);
				double speed=0.0;
				double distanceTravelled = 0.0;
				for(int a=0;a<accelMoves;a++)
				{
					CMLib.space().accelSpaceObject(o,dir,accel);
					speed += accel;
					final long[] oldCoords = Arrays.copyOf(o.coordinates(), 3);
					CMLib.space().moveSpaceObject(o);
					distanceTravelled+=speed;
					final double traveledDistance = CMLib.space().getDistanceFrom(startCoords, o.coordinates());
					final double distDiff = Math.abs(traveledDistance - distanceTravelled);
					final double speedDiff = Math.abs(o.speed() - speed);
					if((speedDiff>1)
					||(!Arrays.equals(dir, o.direction()))
					||(distDiff > 2))
					{
						final String s;
						if(shortDebug)
						{
							final String complaint = (speedDiff>1)+"/"+(!Arrays.equals(dir, o.direction()))+"/"+(distDiff > 2);
							s="Error: Space move mid-mid"+i+"."+a+", test failed: "+complaint;
						}
						else
						{
							s = spaceMoveError("mid-mid"+i+"."+a, o, dir, speedDiff, speed, distDiff,
														traveledDistance, distanceTravelled, oldCoords);
						}
						throw new CMException(s);
					}
					if(Math.abs(traveledDistance - distanceTravelled)>1)
					{
						return("Move Fail: "+Math.abs(traveledDistance-distanceTravelled));
					}
					distanceTravelled=traveledDistance;
				}
				final double midTraveledDistance = CMLib.space().getDistanceFrom(startCoords, o.coordinates());
				final double midDistDiff = Math.abs(midTraveledDistance - predictedMidDistance);
				final double midSpeedDiff = Math.abs(o.speed() - predictedMidSpeed);
				if((midSpeedDiff>accel+1)
				||(!Arrays.equals(dir, o.direction()))
				||(midDistDiff > accel))
				{
					final String s;
					if(shortDebug)
					{
						final String complaint = (midSpeedDiff>accel+1)+"/"+(!Arrays.equals(dir, o.direction()))+"/"+(midDistDiff > accel);
						s="Error: Space move mid-"+i+", test failed: "+complaint;
					}
					else
					{
						s=spaceMoveError("mid-"+i, o, dir, midSpeedDiff, predictedMidSpeed, midDistDiff,
										midTraveledDistance, predictedMidDistance, o.coordinates());
					}
					throw new CMException(s);
				}

				for(int a=0;a<decelMoves;a++)
				{
					CMLib.space().accelSpaceObject(o,CMLib.space().getOppositeDir(o.direction()),accel);
					speed -= accel;
					final long[] oldCoords = Arrays.copyOf(o.coordinates(), 3);
					final double otraveledDistance = CMLib.space().getDistanceFrom(startCoords, o.coordinates());
					CMLib.space().moveSpaceObject(o);
					distanceTravelled+=speed;
					final double traveledDistance = CMLib.space().getDistanceFrom(startCoords, o.coordinates());
					final double distDiff = Math.abs(traveledDistance - distanceTravelled);
					final double speedDiff = Math.abs(o.speed() - speed);
					if((speedDiff>1)
					||(!Arrays.equals(dir, o.direction()))
					||(distDiff > (accel*10*(a+1))))
					{
						String s;
						if(shortDebug)
						{
							final String complaint = (speedDiff>1)+"/"+(!Arrays.equals(dir, o.direction()))+"/"+(distDiff > accel+(2*speed));
							s="Error: Space move mid-end"+i+"."+a+", test failed: "+complaint;
						}
						else
						{
							s = spaceMoveError("mid-end"+i+"."+a, o, dir, speedDiff, speed, distDiff,
												traveledDistance, distanceTravelled, oldCoords);
							distanceTravelled-=speed;
							s+=("dist:"+otraveledDistance+"+"+speed+"="+traveledDistance);
						}
						CMLib.space().moveSpaceObject(o);
						throw new CMException(s);
					}
					distanceTravelled=traveledDistance;
				}
				final double traveledDistance = CMLib.space().getDistanceFrom(startCoords, o.coordinates());
				final double distDiff = Math.abs(traveledDistance - predictedDistance);
				final double speedDiff = Math.abs(o.speed() - accel);
				if((speedDiff>accel+1)
				||(!Arrays.equals(dir, o.direction()))
				||(distDiff > 3000))
				{
					String s;
					if(shortDebug)
					{
						final String complaint = (speedDiff>accel+1)+"/"+(!Arrays.equals(dir, o.direction()))+"/"+(distDiff > decelMoves);
						s="Error: Space move test"+i+", test failed: "+complaint;
					}
					else
					{
						s = spaceMoveError("overall "+i, o, dir, speedDiff, speed, distDiff,
											traveledDistance, predictedDistance, startCoords);
					}
					throw new CMException(s);
				}
				passes++;
			}
			catch(final CMException e)
			{
				fails++;
				if(errMsg == null)
					errMsg=e.getMessage();
			}
		}
		if(passes>fails/2)
			return null;
		return errMsg;
	}

	public String spaceMoveError(final String pos, final SpaceShip o, final double[] dir,
								 final double speedDiff, final double speed,
								 final double distDiff, final double traveledDistance, final double distanceTravelled,
								 final long[] oldCoords)
	{
		String complaint="";
		if((!Arrays.equals(dir, o.direction())))
		{
			complaint += " angles: "+
				Math.round(Math.toDegrees(dir[0]))+"mk"+Math.round(Math.toDegrees(dir[1]))
			 +" vs "+Math.round(Math.toDegrees(o.direction()[0]))+"mk"+Math.round(Math.toDegrees(o.direction()[1]))
			 +"\n\r";
		}
		if(speedDiff > 1)
			complaint +=  "speed: "+o.speed()+" vs "+speed+"\n\r";
		if(distDiff > 1)
			complaint += "dist: "+traveledDistance+" vs "+distanceTravelled+"\n\r";
		if(!Arrays.equals(o.coordinates(), oldCoords))
		{
			complaint += "coords: "+CMParms.toListString(oldCoords)+" vs "+CMParms.toListString(o.coordinates());
			complaint += "\n\r";
		}
		return "Error: Space move "+pos+", test failed: "+"\n\r"+complaint;
	}
}
