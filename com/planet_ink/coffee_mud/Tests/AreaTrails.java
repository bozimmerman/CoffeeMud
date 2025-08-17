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
		final PairList<Room,Room> failPairs = new PairVector<Room,Room>();
		long totalNewMillis = 0;
		long totalOldMillis = 0;
		int shortestFail = Integer.MAX_VALUE;
		Pair<Room,Room> shortestFailPair = null;
		for(final Area fromA : elligibleAreas)
		{
			//for(final Area toA : elligibleAreas)
			for(int a=0;a<10;a++)
			{
				final Area toA = CMLib.map().getRandomArea();

				if((fromA == toA)||(fromA.getTimeObj() != toA.getTimeObj()))
					continue;
				tries++;
				Room fromRoom = fromA.getRandomProperRoom();
				if(fromRoom instanceof GridLocale)
					fromRoom = ((GridLocale)fromRoom).getRandomGridChild();
				Room toRoom = toA.getRandomProperRoom();
				if(toRoom instanceof GridLocale)
					toRoom = ((GridLocale)toRoom).getRandomGridChild();
				if((fromRoom==null)||(toRoom==null))
				{
					mob.tell("No proper rooms in " + fromA.name() + " or " + toA.name());
					continue;
				}
				long MilliStart = System.currentTimeMillis();
				final List<Room> oldTrail = CMLib.tracking().findTrailToRoom(fromRoom, toRoom, flags, 150);
				final long oldMillis = System.currentTimeMillis() - MilliStart;
				totalOldMillis += oldMillis;
				MilliStart = System.currentTimeMillis();
				final List<Room> trail = CMLib.tracking().findTrailToRoom(fromRoom, toRoom, flags);
				final long newMillis = System.currentTimeMillis() - MilliStart;
				totalNewMillis += newMillis;
				final int otSize = (oldTrail == null) ? 0 : oldTrail.size();
				if ((trail == null) || (trail.size() == 0) || (trail.get(0)!=toRoom))
				{
					if((oldTrail == null)||(oldTrail.size()==0))
						mob.tell(fromRoom.roomID()+"->"+toRoom.roomID()+" is an invalid trail, no steps found.");
					else
					{
						failPairs.add(new Pair<Room,Room>(fromRoom,toRoom));
						if (oldTrail.size() < shortestFail)
						{
							shortestFail = oldTrail.size();
							shortestFailPair = new Pair<Room, Room>(fromRoom, toRoom);
						}
						fails++;
						if((trail != null) && (trail.size()>0))
							mob.tell(CMLib.map().getExtendedRoomID(fromRoom)+"->"+CMLib.map().getExtendedRoomID(toRoom)+" failed because last room is "+trail.get(0).roomID());
						else
							mob.tell(CMLib.map().getExtendedRoomID(fromRoom)+"->"+CMLib.map().getExtendedRoomID(toRoom)+" failed (old method steps = "+oldTrail.size()+")");
					}
				}
				else
				{
					mob.tell(CMLib.map().getExtendedRoomID(fromRoom)+"->"+CMLib.map().getExtendedRoomID(toRoom)+" has "+trail.size() + "/"+otSize+" steps");
					Room R = trail.get(trail.size()-1);
					final StringBuilder steps = new StringBuilder(CMLib.map().getExtendedRoomID(R));
					success++;
					for(int r=trail.size()-2; r>=0;r--)
					{
						final int dir = CMLib.map().getRoomDir(R, trail.get(r));
						if(dir < 0)
						{
							success--;
							fails++;
							failPairs.add(new Pair<Room,Room>(fromRoom,toRoom));
							//mob.tell(CMLib.map().getExtendedRoomID(fromRoom)+"->"+CMLib.map().getExtendedRoomID(toRoom)+" has "+trail.size() + "/"+otSize+" steps");
							mob.tell(".. but failed viability check from "+CMLib.map().getExtendedRoomID(R)+" to "+CMLib.map().getExtendedRoomID(trail.get(r)));
							break;
						}
						else
						{
							R = trail.get(r);
							steps.append(",").append(CMLib.map().getExtendedRoomID(R));
						}
					}
					if(trail.size()<otSize/2)
						mob.tell("=="+steps.toString()+"==");
				}
				mob.tell("   ^^ Times: Old: "+oldMillis+", New: "+newMillis);
			}
		}
		mob.tell("Success "+success+"/"+tries);
		mob.tell("Fail "+fails+"/"+tries);
		if (shortestFailPair != null)
			mob.tell("Shortest fail "+shortestFail+"/"+tries+" from "+CMLib.map().getExtendedRoomID(shortestFailPair.first)+" to "+CMLib.map().getExtendedRoomID(shortestFailPair.second));
		final long avgNewMillis = (totalNewMillis / tries);
		final long avgOldMillis = (totalOldMillis / tries);
		mob.tell("Average new Millis: "+avgNewMillis+", old Millis: "+avgOldMillis);
		return null;
	}
}
