package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
/*
   Copyright 2005-2022 Bo Zimmerman

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
public interface GalacticMap extends CMLibrary
{
	public Enumeration<Area> spaceAreas();
	public long getRelativeSpeed(SpaceObject O1, SpaceObject O2);
	public int numSpaceObjects();
	public boolean isObjectInSpace(SpaceObject O);
	public void delObjectInSpace(SpaceObject O);
	public void addObjectToSpace(SpaceObject O, long[] coords);
	public void addObjectToSpace(final SpaceObject O);
	public long getDistanceFrom(SpaceObject O1, SpaceObject O2);
	public long getDistanceFrom(final long[] coord1, final long[] coord2);
	public double getAngleDelta(final double[] fromAngle, final double[] toAngle);
	public double[] getFacingAngleDiff(final double[] fromAngle, final double[] toAngle);
	public double getMinDistanceFrom(final long[] prevPos, final long[] curPosition, final long[] objPos);
	public double[] getDirection(SpaceObject fromObj, SpaceObject toObj);
	public double[] getDirection(final long[] fromCoords, final long[] toCoords);
	public ShipDirComponent.ShipDir getDirectionFromDir(double[] facing, double roll, double[] direction);
	public double[] getOppositeDir(final double[] dir);
	public long[] getLocation(long[] oldLocation, double[] direction, long distance);
	public void moveSpaceObject(SpaceObject O);
	public void moveSpaceObject(SpaceObject O, long[] coords);
	public void moveSpaceObject(final SpaceObject O, final double[] accelDirection, final double newAcceleration);
	public double moveSpaceObject(final double[] curDirection, final double curSpeed, final double[] accelDirection, final double newAcceleration);
	public long[] moveSpaceObject(final long[] coordinates, final double[] direction, long speed);
	public SpaceObject getSpaceObject(CMObject o, boolean ignoreMobs);
	public Enumeration<SpaceObject> getSpaceObjects();
	public Enumeration<Entry<SpaceObject, List<WeakReference<TrackingVector<SpaceObject>>>>>  getSpaceObjectEntries();
	public List<SpaceObject> getSpaceObjectsWithin(SpaceObject ofObj, long minDistance, long maxDistance);
	public List<SpaceObject> getSpaceObjectsByCenterpointWithin(final long[] centerCoordinates, long minDistance, long maxDistance);
	public SpaceObject findSpaceObject(String s, boolean exactOnly);
	public String getSectorName(long[] coordinates);
	public long[] getInSectorCoords(long[] coordinates);
	public List<LocationRoom> getLandingPoints(final SpaceObject ship, final Environmental O);

}
