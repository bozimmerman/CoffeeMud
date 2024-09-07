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
public class Spacecollisions extends StdTest
{
	@Override
	public String ID()
	{
		return "Spacecollisions";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "spaceall"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		// {ship coord}, {speed}, {deg.diff,r},{target},{hit=1}
		final long[][][] tests={
			{{0,0,0},{1000},{},{500,0,0},{1}}, // 0
			{{0,0,0},{1000},{},{0,500,0},{1}},
			{{0,0,0},{1000},{},{0,0,500},{1}},
			{{0,0,0},{1000},{},{-500,0,0},{1}},
			{{0,0,0},{1000},{},{0,-500,0},{1}},
			{{0,0,0},{1000},{},{0,0,-500},{1}},
			{{0,0,0},{1000},{},{500,0,500},{1}},
			{{0,0,0},{1000},{},{0,500,500},{1}},
			{{0,0,0},{1000},{},{500,0,500},{1}},
			{{0,0,0},{1000},{},{-500,0,500},{1}},
			{{0,0,0},{1000},{},{0,-500,500},{1}},
			{{0,0,0},{1000},{},{500,0,-500},{1}}, //11
			{{0,0,0},{480},{},{500,0,0},{0}}, // 12
			{{0,0,0},{480},{},{0,500,0},{0}},
			{{0,0,0},{480},{},{0,0,500},{0}},
			{{0,0,0},{480},{},{-500,0,0},{0}},
			{{0,0,0},{480},{},{0,-500,0},{0}},
			{{0,0,0},{480},{},{0,0,-500},{0}},
			{{0,0,0},{480},{},{500,0,500},{0}},
			{{0,0,0},{480},{},{0,500,500},{0}},
			{{0,0,0},{480},{},{500,0,500},{0}},
			{{0,0,0},{480},{},{-500,0,500},{0}},
			{{0,0,0},{480},{},{0,-500,500},{0}},
			{{0,0,0},{480},{},{500,0,-500},{0}}, // 23
			{{0,0,0},{495},{},{500,0,0},{1}}, // 24
			{{0,0,0},{495},{},{0,500,0},{1}},
			{{0,0,0},{495},{},{0,0,500},{1}},
			{{0,0,0},{495},{},{-500,0,0},{1}},
			{{0,0,0},{495},{},{0,-500,0},{1}},
			{{0,0,0},{495},{},{0,0,-500},{1}},
			{{0,0,0},{700},{},{500,0,500},{1}},
			{{0,0,0},{700},{},{0,500,500},{1}},
			{{0,0,0},{700},{},{500,0,500},{1}},
			{{0,0,0},{700},{},{-500,0,500},{1}},
			{{0,0,0},{700},{},{0,-500,500},{1}},
			{{0,0,0},{700},{},{500,0,-500},{1}}, // 35
			{{0,0,0},{10000},{1,0},{5000,0,0},{0}}, // 36
			{{0,0,0},{10000},{0,1},{0,5000,0},{0}},
			{{0,0,0},{10000},{0,1},{0,0,5000},{0}},
			{{0,0,0},{10000},{1,0},{-5000,0,0},{0}},
			{{0,0,0},{10000},{0,1},{0,-5000,0},{0}},
			{{0,0,0},{10000},{0,1},{0,0,-5000},{0}},
			{{0,0,0},{10000},{1,1},{5000,0,5000},{0}},
			{{0,0,0},{10000},{0,1},{0,5000,5000},{0}},
			{{0,0,0},{10000},{1,0},{5000,0,5000},{0}},
			{{0,0,0},{10000},{1,0},{-5000,0,5000},{0}},
			{{0,0,0},{10000},{1,1},{0,-5000,5000},{0}},
			{{0,0,0},{10000},{1,0},{5000,0,-5000},{0}}, // 47
			{{0,0,0},{10000},{1,0},{5000,0,0},{0}}, // 48
			{{0,0,0},{10000},{0,1},{0,5000,0},{0}},
			{{0,0,0},{10000},{0,1},{0,0,5000},{0}},
			{{0,0,0},{10000},{1,0},{-5000,0,0},{0}},
			{{0,0,0},{10000},{0,1},{0,-5000,0},{0}},
			{{0,0,0},{10000},{0,1},{0,0,-5000},{0}},
			{{0,0,0},{10000},{1,1},{5000,0,5000},{0}},
			{{0,0,0},{10000},{0,1},{0,5000,5000},{0}},
			{{0,0,0},{10000},{1,0},{5000,0,5000},{0}},
			{{0,0,0},{10000},{1,0},{-5000,0,5000},{0}},
			{{0,0,0},{10000},{1,1},{0,-5000,5000},{0}},
			{{0,0,0},{10000},{1,0},{5000,0,-5000},{0}}, // 59
			{{0,0,0},{10000},{179,0},{5000,0,0},{0}}, // 60
			{{0,0,0},{10000},{0,89},{0,5000,0},{0}},
			{{0,0,0},{10000},{0,89},{0,0,5000},{0}},
			{{0,0,0},{10000},{179,0},{-5000,0,0},{0}},
			{{0,0,0},{10000},{0,89},{0,-5000,0},{0}},
			{{0,0,0},{10000},{0,89},{0,0,-5000},{0}},
			{{0,0,0},{10000},{179,89},{5000,0,5000},{0}},
			{{0,0,0},{10000},{0,89},{0,5000,5000},{0}},
			{{0,0,0},{10000},{179,0},{5000,0,5000},{0}},
			{{0,0,0},{10000},{179,0},{-5000,0,5000},{0}},
			{{0,0,0},{10000},{179,89},{0,-5000,5000},{0}},
			{{0,0,0},{10000},{179,0},{5000,0,-5000},{0}}, // 71
			{{0,0,0},{42},{179,1},{620000,0,0},{0}}, // 72
			{{9735, -1357, 707161},{29979245},{5,0},{2000, 1000, 0},{0}}, // 73
			{{9735, -1357, 707161},{29979245},{0,0},{20000, 10000, 5030},{1}}, // 74
		};
		final Coord3D lt1= new Coord3D(new long[]{5, 2, 1});
		final Coord3D lt2= new Coord3D(new long[]{3, 1, -1});
		final Coord3D lt3= new Coord3D(new long[]{0, 2, 3});
		if(CMLib.space().getMinDistanceFrom(lt1,lt2,lt3)<1)
		{
			return (L("Error: Straight line test failed: "+CMLib.space().getMinDistanceFrom(lt1,lt2,lt3)));
		}
		final Coord3D ld1 = new Coord3D(new long[] {175, 193, 117});
		final Coord3D ld2 = new Coord3D(new long[] {197, 218, 134});
		final Coord3D ld3 = new Coord3D(new long[] {0, 0, 0});
		if(CMLib.space().getMinDistanceFrom(ld1,ld2,ld3)<285)
		{
			return (L("Error: Short line test failed: "+CMLib.space().getMinDistanceFrom(ld1,ld2,ld3)));
		}
		final Coord3D l1= new Coord3D(new long[]{3515255, 3877051, -239069815});
		final Coord3D l2= new Coord3D(new long[]{3953445, 4361852, -269041937});
		final Coord3D l3= new Coord3D(new long[]{9734, -1358, 707222});
		if(CMLib.space().getMinDistanceFrom(l1,l2,l3)<239834022)
		{
			return (L("Error: Straight line test failed: "+CMLib.space().getMinDistanceFrom(l1,l2,l3)));
		}
		for(int li=0;li<tests.length;li++)
		{
			final long[][] l=tests[li];
			// l->r
			{
				final Coord3D shipCoord1 = new Coord3D(l[0]);
				final long speed = l[1][0];
				final Coord3D targetCoord=new Coord3D(l[3]);
				final double[] dir=CMLib.space().getDirection(shipCoord1, targetCoord);
				if(l[2].length==2)
				{
					CMLib.space().changeDirection(dir, Math.toRadians(l[2][0]), Math.toRadians(l[2][1]));
				}
				//Log.debugOut(dir[0]+","+dir[1]);
				final boolean expectHit=l[4][0]>0;
				final Coord3D shipCoord2=CMLib.space().moveSpaceObject(shipCoord1, dir, speed);
				final double swish=CMLib.space().getMinDistanceFrom(shipCoord1, shipCoord2, targetCoord);
				if(expectHit != (swish < 10))
				{
					/*
					mob.tell(li+"A) orig coords="+shipCoord1[0]+","+shipCoord1[1]+","+shipCoord1[2]);
					mob.tell(li+"A) collider coords="+targetCoord[0]+","+targetCoord[1]+","+targetCoord[2]);
					mob.tell(li+"A) final coords="+shipCoord2[0]+","+shipCoord2[1]+","+shipCoord2[2]);
					mob.tell(li+"A) direction to target="+dir[0]+","+dir[1]);
					mob.tell(li+"A) speed="+speed);
					mob.tell(li+"A) original distance="+CMLib.space().getDistanceFrom(shipCoord1, targetCoord));
					mob.tell(li+"A) current distance="+CMLib.space().getDistanceFrom(shipCoord2, targetCoord));
					mob.tell(li+"A) min distance during move="+swish);
					*/
					return (L("Error:"+expectHit+"!="+li+"A: minDist="+swish+"/"+(CMLib.space().getDistanceFrom(shipCoord1, targetCoord))));
				}
			}
			// r->l
			{
				final Coord3D shipCoord1 = new Coord3D(l[3]);
				final long speed = l[1][0];
				final Coord3D targetCoord=new Coord3D(l[0]);
				final double[] dir=CMLib.space().getDirection(shipCoord1, targetCoord);
				if(l[2].length==2)
				{
					CMLib.space().changeDirection(dir, Math.toRadians(l[2][0]), Math.toRadians(l[2][1]));
				}
				//Log.debugOut(dir[0]+","+dir[1]);
				final boolean expectHit=l[4][0]>0;
				final Coord3D shipCoord2=CMLib.space().moveSpaceObject(shipCoord1, dir, speed);
				final double swish=CMLib.space().getMinDistanceFrom(shipCoord1, shipCoord2, targetCoord);
				if(expectHit != (swish < 10))
				{
					/*
					mob.tell(li+"B) orig coords="+shipCoord1[0]+","+shipCoord1[1]+","+shipCoord1[2]);
					mob.tell(li+"B) target coords="+targetCoord[0]+","+targetCoord[1]+","+targetCoord[2]);
					mob.tell(li+"B) final coords="+shipCoord2[0]+","+shipCoord2[1]+","+shipCoord2[2]);
					mob.tell(li+"B) original direction to target="+dir[0]+","+dir[1]);
					mob.tell(li+"B) adjusted direction to target="+dir[0]+","+dir[1]);
					mob.tell(li+"B) speed="+speed);
					mob.tell(li+"B) min distance during move="+swish);
					*/
					return (L("Error:"+expectHit+"!="+li+"B: minDist="+swish+"/"+(CMLib.space().getDistanceFrom(shipCoord1, targetCoord))));
				}
			}
		}
		return null;
	}
}
