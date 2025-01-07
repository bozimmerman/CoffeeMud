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
public class Spaceangles extends StdTest
{
	@Override
	public String ID()
	{
		return "Spaceangles";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "spaceall"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final int[][][] diffsets = new int[][][] {
			{ {90,90}, {80,90},  {10} },
			{ {90,90}, {180,90},  {90} },
			{ {0,90}, {80,90},  {80} },
			{ {90,10}, {90,100},  {90} },
		};
		final int[][][] opps = new int[][][] {
			{ {180,0},  	 {180,180}, {360,0}, {0,0}, {0,180}, {360,180} },
			{ {180,5},  	 {0,175}, {360,175} },
			{ {90,15},  	 {270,165} },
			{ {15,90},  	 {195,90} }
		};
		final int[][][] offsets = new int[][][] {
			{ {90,40},{85,50},   {95,30} },
			{ {85,50},{90,40},   {80,60} },
			{ {350,10},{10,20},   {330,0} },
			{ {10,20},{350,10},   {30,30} },
			{ {350,10},{340,20},   {0,0} },
			{ {340,10},{350,20},   {330,0} },
			{ {350,10},{80,20},   {260,0} },
			{ {80,10},{350,20},   {170,0} },
		};
		final int[][][] midsets = new int[][][] {
			{ {90,40},{80,50},   {85,45} },
			{ {80,50},{90,40},   {85,45} },
			{ {350,10},{10,170},   {360,90} },
			{ {10,170},{350,10},   {360,90} },
		};
		final int[][][] exaggsets = new int[][][] {
			{ {90,45},{80,50},   {70,55} },
			{ {80,50},{90,45},   {100,40} },
			{ {10,10},{350,5},   {330,0} },
			{ {350,5},{10,10},   {30,15} },
		};
		for(int i=0;i<diffsets.length;i++)
		{
			final int[] anglei1 = diffsets[i][0];
			final Dir3D angle1 = new Dir3D(new double[] {Math.toRadians(anglei1[0]), Math.toRadians(anglei1[1])});
			final int[] anglei2 = diffsets[i][1];
			final Dir3D angle2 = new Dir3D(new double[] {Math.toRadians(anglei2[0]), Math.toRadians(anglei2[1])});
			final double diff = CMLib.space().getAngleDelta(angle1, angle2);
			final int[] an = diffsets[i][2];
			final double and = Math.toRadians(an[0]);
			final boolean pass = Math.abs(diff - and) < 0.001;
			if(!pass)
			{
				return ("DIFF Test #"+(i+1)+": "+
						CMLib.english().directionDescShort(angle1.toDoubles())+"-"+CMLib.english().directionDescShort(angle2.toDoubles())
							+"="+Math.toDegrees(diff));
			}
		}
		for(int i=0;i<opps.length;i++)
		{
			final int[] anglei = opps[i][0];
			final Dir3D angle = new Dir3D(new double[] {Math.toRadians(anglei[0]), Math.toRadians(anglei[1])});
			final Dir3D op = CMLib.space().getOppositeDir(angle);
			boolean pass=false;
			for(int x=1;x<opps[i].length;x++)
			{
				final int[] compi = opps[i][x];
				final double[] comp = new double[] {Math.toRadians(compi[0]), Math.toRadians(compi[1])};
				if((Math.abs(comp[0]-op.xyd())<0.001)&&(Math.abs(comp[1]-op.zd())<0.001))
					pass=true;
			}
			if(!pass)
				return ("OP Test #"+(i+1)+" failed: "+Math.toDegrees(op.xyd())+","+Math.toDegrees(op.zd()));
		}
		for(int i=0;i<offsets.length;i++)
		{
			final int[] anglei1 = offsets[i][0];
			final Dir3D angle1 = new Dir3D(new double[] {Math.toRadians(anglei1[0]), Math.toRadians(anglei1[1])});
			final int[] anglei2 = offsets[i][1];
			final Dir3D angle2 = new Dir3D(new double[] {Math.toRadians(anglei2[0]), Math.toRadians(anglei2[1])});
			final Dir3D off = CMLib.space().getOffsetAngle(angle1, angle2);
			final int[] an = offsets[i][2];
			final Dir3D and = new Dir3D(new double[] {Math.toRadians(an[0]), Math.toRadians(an[1])});
			final boolean pass = (Math.abs(off.xyd() - and.xyd())<0.001) && (Math.abs(off.zd() - and.zd())<0.001);
			if(!pass)
				return ("OF Test #"+(i+1)+" failed: "+Math.toDegrees(off.xyd())+","+Math.toDegrees(off.zd()));
		}
		for(int i=0;i<midsets.length;i++)
		{
			final int[] anglei1 = midsets[i][0];
			final Dir3D angle1 = new Dir3D(new double[] {Math.toRadians(anglei1[0]), Math.toRadians(anglei1[1])});
			final int[] anglei2 = midsets[i][1];
			final Dir3D angle2 = new Dir3D(new double[] {Math.toRadians(anglei2[0]), Math.toRadians(anglei2[1])});
			final Dir3D mid = CMLib.space().getMiddleAngle(angle1, angle2);
			final int[] an = midsets[i][2];
			final Dir3D and = new Dir3D(new double[] {Math.toRadians(an[0]), Math.toRadians(an[1])});
			final boolean pass = (Math.abs(mid.xyd() - and.xyd())<0.001) && (Math.abs(mid.zd() - and.zd())<0.001);
			if(!pass)
				return ("MID Test #"+(i+1)+" failed: "+Math.toDegrees(mid.xyd())+","+Math.toDegrees(mid.zd()));
		}
		for(int i=0;i<exaggsets.length;i++)
		{
			final int[] anglei1 = exaggsets[i][0];
			final Dir3D angle1 = new Dir3D(new double[] {Math.toRadians(anglei1[0]), Math.toRadians(anglei1[1])});
			final int[] anglei2 = exaggsets[i][1];
			final Dir3D angle2 = new Dir3D(new double[] {Math.toRadians(anglei2[0]), Math.toRadians(anglei2[1])});
			final Dir3D mid = CMLib.space().getExaggeratedAngle(angle1, angle2);
			final int[] an = exaggsets[i][2];
			final Dir3D and = new Dir3D(new double[] {Math.toRadians(an[0]), Math.toRadians(an[1])});
			final boolean pass = (Math.abs(mid.xyd() - and.xyd())<0.001) && (Math.abs(mid.zd() - and.zd())<0.001);
			if(!pass)
				return ("EXX Test #"+(i+1)+" failed: "+Math.toDegrees(mid.xyd())+","+Math.toDegrees(mid.zd()));
		}
		return null;
	}
}
