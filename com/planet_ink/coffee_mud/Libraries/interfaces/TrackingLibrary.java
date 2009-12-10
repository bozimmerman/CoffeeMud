package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public interface TrackingLibrary extends CMLibrary
{
    public Vector findBastardTheBestWay(Room location, Room destRoom, TrackingFlags flags, int maxRadius);
    public Vector findBastardTheBestWay(Room location, Vector<Room> destRooms, TrackingFlags flags, int maxRadius);
    public int trackNextDirectionFromHere(Vector<Room> theTrail, Room location, boolean openOnly);
    public void stopTracking(MOB mob);
    public int radiatesFromDir(Room room, Vector<Room> rooms);
    public void getRadiantRooms(Room room, Vector<Room> rooms, TrackingFlags flags, Room radiateTo, int maxDepth, HashSet<Room> ignoreRooms);
	public Vector getRadiantRooms(Room room, TrackingFlags flags, int maxDepth);
    public boolean beMobile(MOB mob,
                            boolean dooropen,
                            boolean wander,
                            boolean roomprefer, 
                            boolean roomobject,
                            long[] status,
                            Vector<Room> rooms);
    public void wanderAway(MOB M, boolean mindPCs, boolean andGoHome);
    public void wanderFromTo(MOB M, Room toHere, boolean mindPCs);
    public void wanderIn(MOB M, Room toHere);
    public boolean move(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders);
    public boolean move(MOB mob, int directionCode, boolean flee, boolean nolook);
    public int findExitDir(MOB mob, Room R, String desc);
    public int findRoomDir(MOB mob, Room R);
	public Vector findAllTrails(Room from, Room to, Vector<Room> radiantTrail);
	public Vector findAllTrails(Room from, Vector<Room> tos, Vector<Room> radiantTrail);
	public String getTrailToDescription(Room R1, Vector<Room> set, String where, boolean areaNames, boolean confirm, int radius, HashSet<Room> ignoreRooms, int maxMins);
	
	public static enum TrackingFlag {NOHOMES,OPENONLY,AREAONLY,NOEMPTYGRIDS,NOAIR,NOWATER};
	
	public static class TrackingFlags extends HashSet {
		private static final long serialVersionUID = 1L;
		public TrackingFlags add(TrackingFlag flag) { super.add(flag); return this;}
	}
}
