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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
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
Copyright 2025-2025 Bo Zimmerman

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
public class AreaTrails extends StdTest
{
	@Override
	public String ID()
	{
		return "AreaTrails";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all"};
	}

	final PairList<Room,Room> failPairs = new PairVector<Room,Room>();

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final List<Area> elligibleAreas = new ArrayList<Area>();
		for (final Enumeration<Area> a = CMLib.map().areas(); a.hasMoreElements();)
		{
			final Area fromA = a.nextElement();
			if((fromA!=null)
			&&(!CMLib.flags().isHidden(fromA))
			&&(!CMath.bset(fromA.flags(),Area.FLAG_INSTANCE_CHILD))
			&&(fromA.numberOfProperIDedRooms()>5)
			&&((!fromA.Name().equals("New Area"))))
				elligibleAreas.add(fromA);
		}
		final TrackingFlags flags=
				CMLib.tracking().newFlags()
				.plus(TrackingFlag.PASSABLE)
				.plus(TrackingFlag.NOEMPTYGRIDS)
				.plus(TrackingFlag.NOHIDDENAREAS)
				.plus(TrackingFlag.NOHOMES);
		int tries = 0;
		int fails = 0;
		int success = 0;
		//final int shortestFail = Integer.MAX_VALUE;
		//Pair<Room,Room> shortestFailPair = null;
		final PairList<Room,Room> tests = new PairArrayList<Room,Room>();
		int testType = 1;
		if((commands.size()>0)&&(CMath.isInteger(commands.get(commands.size()-1))))
			testType = CMath.s_int(commands.get(commands.size()-1));
		if(testType >0)
		{
			for(final Area fromA : elligibleAreas)
				for(int a=0;a<testType;a++)
				{
					final Area toA = CMLib.map().getRandomArea();
					if((fromA == toA)||(fromA.getTimeObj() != toA.getTimeObj()))
						continue;
					Room fromRoom = fromA.getRandomProperRoom();
					if(fromRoom instanceof GridLocale)
						fromRoom = ((GridLocale)fromRoom).getRandomGridChild();
					Room toRoom = toA.getRandomProperRoom();
					if(toRoom instanceof GridLocale)
						toRoom = ((GridLocale)toRoom).getRandomGridChild();
					if((fromRoom != null) && (toRoom != null))
						tests.add(new Pair<Room, Room>(fromRoom, toRoom));
				}

		}
		else
		if((failPairs.size()>0)&&(testType==0))
		{
			tests.addAll(failPairs);
		}
		else
		{
			for(final Area fromA : elligibleAreas)
				for(final Area toA : elligibleAreas)
				{
					if((fromA == toA)||(fromA.getTimeObj() != toA.getTimeObj()))
						continue;
					Room fromRoom = fromA.getRandomProperRoom();
					if(fromRoom instanceof GridLocale)
						fromRoom = ((GridLocale)fromRoom).getRandomGridChild();
					Room toRoom = toA.getRandomProperRoom();
					if(toRoom instanceof GridLocale)
						toRoom = ((GridLocale)toRoom).getRandomGridChild();
					if((fromRoom != null) && (toRoom != null))
						tests.add(new Pair<Room, Room>(fromRoom, toRoom));
				}
		}

		failPairs.clear();
		for(final Pair<Room, Room> test : tests)
		{
			//long MilliStart;
			final Room fromRoom = test.first;
			final Room toRoom = test.second;

			//long oldMillis;
			//MilliStart = System.currentTimeMillis();
			final List<Room> trail = CMLib.tracking().findTrailToRoom(fromRoom, toRoom, flags, 150);
			//oldMillis = System.currentTimeMillis() - MilliStart;
			//totalOldMillis += oldMillis;
			tries++;

			//final String testTrailName = CMLib.map().getExtendedRoomID(fromRoom)+"->"+CMLib.map().getExtendedRoomID(toRoom);
			if ((trail == null) || (trail.size() == 0) || (trail.get(0)!=toRoom))
			{
				failPairs.add(new Pair<Room,Room>(fromRoom,toRoom));
				/*
				if (trail.size() < shortestFail)
				{
					shortestFail = trail.size();
					//shortestFailPair = new Pair<Room, Room>(fromRoom, toRoom);
				}
				*/
				fails++;
				/*
				if((trail != null) && (trail.size()>0))
					mob.tell(testTrailName+" failed because last room is "+trail.get(0).roomID());
				else
					mob.tell(testTrailName+" failed.");
				mob.tell("   ^^ Time:"+oldMillis+"ms");
				*/
			}
			else
			{
				//mob.tell(testTrailName+" trail steps="+trail.size());
				success++;
				Room R = trail.get(trail.size()-1);
				final StringBuilder steps = new StringBuilder(CMLib.map().getExtendedRoomID(R));
				for(int r=trail.size()-2; r>=0;r--)
				{
					final int dir = CMLib.map().getRoomDir(R, trail.get(r));
					if(dir < 0)
					{
						success--;
						fails++;
						failPairs.add(new Pair<Room,Room>(fromRoom,toRoom));
						//mob.tell(testTrailName+" has "+trail.size() + "/"+otSize+" steps");
						//mob.tell(".. but failed viability check from "+CMLib.map().getExtendedRoomID(R)+" to "+CMLib.map().getExtendedRoomID(trail.get(r)));
						break;
					}
					else
					{
						R = trail.get(r);
						steps.append(",").append(CMLib.map().getExtendedRoomID(R));
					}
				}
				//mob.tell("   ^^ Time: "+oldMillis+"ms");
			}
		}
		/*
		mob.tell("Success: "+success);
		mob.tell("Fail: "+fails);
		mob.tell("Total: "+(success + fails));
		mob.tell("Invalid tests skipped: "+(tries - success - fails));
		if (shortestFailPair != null)
			mob.tell("(Shortest fail "+shortestFail+"/"+tries+" from "+CMLib.map().getExtendedRoomID(shortestFailPair.first)+" to "+CMLib.map().getExtendedRoomID(shortestFailPair.second)+")");
		*/
		//mob.tell("final String[][] failPairs = new String[][] {");
		/*
		for (final Pair<Room, Room> failPair : failPairs)
		{
			//mob.tell("{\"" + CMLib.map().getExtendedRoomID(failPair.first) + "\", \"" + CMLib.map().getExtendedRoomID(failPair.second)+"\",");
			final List<Room> trail = CMLib.tracking().findTrailToRoom(failPair.first, failPair.second, flags);
			if ((trail == null) || (trail.size() == 0))
				mob.tell("   No trail found.");
			else
				mob.tell("   Retry succeeded tho.");
		}
		//mob.tell("};");
		*/
		if(tries > 0)
		{
			//final long avgNewMillis = (totalNewMillis / tries);
			//final long avgOldMillis = (totalOldMillis / tries);
			//mob.tell("Average times per trail: "+avgNewMillis+"ms, old Millis: "+avgOldMillis+"ms");
		}
		if(fails == 0)
			return null;
		return fails+" out of "+(success + fails);
	}
}
