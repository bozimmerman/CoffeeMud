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
			{ {180,0},  	 {180,180}, {360,0}, {0,0}, {0,180} },
			{ {180,5},  	 {0,175}, {360,175} },
			{ {90,15},  	 {270,165} },
			{ {15,90},  	 {195,90} }
		};
		final int[][][] offsets = new int[][][] {
			{ {90,45},{85,45},   {95,45} },
		};
		final int[][][] midsets = new int[][][] {
			{ {90,45},{80,45},   {85,45} },
		};
		for(int i=0;i<diffsets.length;i++)
		{
			final int[] anglei1 = diffsets[i][0];
			final double[] angle1 = new double[] {Math.toRadians(anglei1[0]), Math.toRadians(anglei1[1])};
			final int[] anglei2 = diffsets[i][1];
			final double[] angle2 = new double[] {Math.toRadians(anglei2[0]), Math.toRadians(anglei2[1])};
			final double diff = CMLib.space().getAngleDelta(angle1, angle2);
			final int[] an = diffsets[i][2];
			final double and = Math.toRadians(an[0]);
			final boolean found = Math.abs(diff - and) < 0.001;
			if(!found)
				return ("DIFF Test #"+(i+1)+" failed: "+Math.toDegrees(diff));
			/*
			else
				mob.tell("DIFF Test #"+(i+1)+": "+
						CMLib.english().directionDescShort(angle1)+"-"+CMLib.english().directionDescShort(angle2)
							+"="+Math.toDegrees(diff));
			*/
		}
		for(int i=0;i<opps.length;i++)
		{
			final int[] anglei = opps[i][0];
			final double[] angle = new double[] {Math.toRadians(anglei[0]), Math.toRadians(anglei[1])};
			final double[] op = CMLib.space().getOppositeDir(angle);
			boolean found=false;
			for(int x=1;x<opps[i].length;x++)
			{
				final int[] compi = opps[i][x];
				final double[] comp = new double[] {Math.toRadians(compi[0]), Math.toRadians(compi[1])};
				if((Math.abs(comp[0]-op[0])<0.001)&&(Math.abs(comp[1]-op[1])<0.001))
					found=true;
			}
			if(!found)
				return ("OP Test #"+(i+1)+" failed: "+Math.toDegrees(op[0])+","+Math.toDegrees(op[1]));
		}
		for(int i=0;i<offsets.length;i++)
		{
			final int[] anglei1 = offsets[i][0];
			final double[] angle1 = new double[] {Math.toRadians(anglei1[0]), Math.toRadians(anglei1[1])};
			final int[] anglei2 = offsets[i][1];
			final double[] angle2 = new double[] {Math.toRadians(anglei2[0]), Math.toRadians(anglei2[1])};
			final double[] off = CMLib.space().getOffsetAngle(angle1, angle2);
			final int[] an = offsets[i][2];
			final double[] and = new double[] {Math.toRadians(an[0]), Math.toRadians(an[1])};
			final boolean found = (Math.abs(off[0] - and[0])<0.001) && (Math.abs(off[1] - and[1])<0.001);
			if(!found)
				return ("OF Test #"+(i+1)+" failed: "+Math.toDegrees(off[0])+","+Math.toDegrees(off[1]));
		}
		for(int i=0;i<midsets.length;i++)
		{
			final int[] anglei1 = midsets[i][0];
			final double[] angle1 = new double[] {Math.toRadians(anglei1[0]), Math.toRadians(anglei1[1])};
			final int[] anglei2 = midsets[i][1];
			final double[] angle2 = new double[] {Math.toRadians(anglei2[0]), Math.toRadians(anglei2[1])};
			final double[] mid = CMLib.space().getMiddleAngle(angle1, angle2);
			final int[] an = midsets[i][2];
			final double[] and = new double[] {Math.toRadians(an[0]), Math.toRadians(an[1])};
			final boolean found = (Math.abs(mid[0] - and[0])<0.001) && (Math.abs(mid[1] - and[1])<0.001);
			if(!found)
				return ("MID Test #"+(i+1)+" failed: "+Math.toDegrees(mid[0])+","+Math.toDegrees(mid[1]));
		}
		return null;
	}
}
