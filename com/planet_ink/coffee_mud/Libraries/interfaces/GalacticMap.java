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
/**
 * The Galactic Map of All Space Objects is more than a simple object
 * cache of things in space, but a sophisticated system for tracking
 * where those things are in relation to each other, and finding
 * things around a particular part of space quickly.
 *
 * This Library is also the place to go for ALL space calculations
 * related to movement.
 *
 * @author Bo Zimmerman
 *
 */
public interface GalacticMap extends CMLibrary
{
	/**
	 * Returns an enumeration of all Areas that are also
	 * SpaceObjects, typically planets.
	 *
	 * @return an enumeration of all space Areas
	 */
	public Enumeration<Area> spaceAreas();

	/**
	 * Returns the number of objects in the galactic map
	 * cache.
	 * @return the number of objects in the galactic map
	 */
	public int numSpaceObjects();

	/**
	 * Returns whether the given object is actually in
	 * the galactic map cache at the moment -- it could
	 * be landed somewhere.
	 *
	 * @see GalacticMap#numSpaceObjects()
	 * @see GalacticMap#isObjectInSpace(SpaceObject)
	 * @see GalacticMap#delObjectInSpace(SpaceObject)
	 * @see GalacticMap#addObjectToSpace(SpaceObject, long[])
	 * @see GalacticMap#addObjectToSpace(SpaceObject)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 *
	 * @param O the space object to look for
	 * @return true if it's in space
	 */
	public boolean isObjectInSpace(SpaceObject O);

	/**
	 * Removes a SpaceObject from the galactic map cache.
	 * This is normally required if it is moving.
	 *
	 * @see GalacticMap#numSpaceObjects()
	 * @see GalacticMap#isObjectInSpace(SpaceObject)
	 * @see GalacticMap#delObjectInSpace(SpaceObject)
	 * @see GalacticMap#addObjectToSpace(SpaceObject, long[])
	 * @see GalacticMap#addObjectToSpace(SpaceObject)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 *
	 * @param O the object to remove
	 */
	public void delObjectInSpace(SpaceObject O);

	/**
	 * Adds the given SpaceObject to the galactic map cache
	 * at the given coordinates.
	 *
	 * @see GalacticMap#numSpaceObjects()
	 * @see GalacticMap#isObjectInSpace(SpaceObject)
	 * @see GalacticMap#delObjectInSpace(SpaceObject)
	 * @see GalacticMap#addObjectToSpace(SpaceObject, long[])
	 * @see GalacticMap#addObjectToSpace(SpaceObject)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 *
	 * @param O the SpaceObject to add
	 * @param coords the coordinates to add it at
	 */
	public void addObjectToSpace(SpaceObject O, long[] coords);

	/**
	 * Adds the given SpaceObject to the galactic map cache.
	 *
	 * @see GalacticMap#numSpaceObjects()
	 * @see GalacticMap#isObjectInSpace(SpaceObject)
	 * @see GalacticMap#delObjectInSpace(SpaceObject)
	 * @see GalacticMap#addObjectToSpace(SpaceObject, long[])
	 * @see GalacticMap#addObjectToSpace(SpaceObject)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 *
	 * @param O the object to put in space
	 */
	public void addObjectToSpace(final SpaceObject O);

	/**
	 * Given two space objects, this returns the actual
	 * distance between the two.
	 *
	 * @see GalacticMap#getDistanceFrom(long[], long[])
	 * @see GalacticMap#getDistanceFrom(SpaceObject, SpaceObject)
	 * @see GalacticMap#getMinDistanceFrom(long[], long[], long[])
	 *
	 * @param O1 the first space object
	 * @param O2 the second space object
	 * @return the distance between the two
	 */
	public long getDistanceFrom(SpaceObject O1, SpaceObject O2);

	/**
	 * Given two galactic coordinates, this returns the actual
	 * distance between the two.
	 *
	 * @see GalacticMap#getDistanceFrom(long[], long[])
	 * @see GalacticMap#getDistanceFrom(SpaceObject, SpaceObject)
	 * @see GalacticMap#getMinDistanceFrom(long[], long[], long[])
	 *
	 * @param coord1 first galactic coords
	 * @param coord2 second galactic coords
	 * @return distance between the two
	 */
	public long getDistanceFrom(final long[] coord1, final long[] coord2);

	/**
	 * Given a previous position and a current position, this will return the minimum
	 * distance approached to the given object position.
	 *
	 * @see GalacticMap#getDistanceFrom(long[], long[])
	 * @see GalacticMap#getDistanceFrom(SpaceObject, SpaceObject)
	 * @see GalacticMap#getMinDistanceFrom(long[], long[], long[])
	 *
	 * @param prevPos previous position on galactic chart
	 * @param curPosition current position on galactic chart
	 * @param objPos the object position curious about minimum distance to
	 * @return the minimum distance in high precision
	 */
	public double getMinDistanceFrom(final long[] prevPos, final long[] curPosition, final long[] objPos);

	/**
	 * Given two angles, this returns the difference between them as a single angle.
	 *
	 * @see GalacticMap#getFacingAngleDiff(double[], double[])
	 *
	 * @param fromAngle the first angle
	 * @param toAngle the second angle
	 * @return the angle delta
	 */

	public double getAngleDelta(final double[] fromAngle, final double[] toAngle);

	/**
	 * Given two angles, this returns the difference between them in pitch and yaw.
	 *
	 * @see GalacticMap#getAngleDelta(double[], double[])
	 *
	 * @param fromAngle the first angle
	 * @param toAngle the second angle
	 * @return the angle delta
	 */
	public double[] getFacingAngleDiff(final double[] fromAngle, final double[] toAngle);

	/**
	 * Given two space objects, this will return the direction in radians
	 * from the first to the second.
	 *
	 * @see GalacticMap#getDirection(long[], long[])
	 * @see GalacticMap#getDirection(SpaceObject, SpaceObject)
	 * @see GalacticMap#getDirectionFromDir(double[], double, double[])
	 * @see GalacticMap#getOppositeDir(double[])
	 *
	 * @param fromObj the first SpaceObject
	 * @param toObj the second SpaceObject
	 * @return the direction in radians from the first to the second
	 */
	public double[] getDirection(SpaceObject fromObj, SpaceObject toObj);

	/**
	 * Given two galactic coordinates, this will return the direction in radians
	 * from the first to the second.
	 *
	 * @see GalacticMap#getDirection(long[], long[])
	 * @see GalacticMap#getDirection(SpaceObject, SpaceObject)
	 * @see GalacticMap#getDirectionFromDir(double[], double, double[])
	 * @see GalacticMap#getOppositeDir(double[])
	 *
	 * @param fromCoords the first coordinates
	 * @param toCoords the second coordinates
	 * @return the angle of direction from the first to the second
	 */
	public double[] getDirection(final long[] fromCoords, final long[] toCoords);

	/**
	 * Given a facing direction (NOT direction of travel), and a roll angle (belly/axis), and a
	 * direction to some other object, this will return a relative direction object describing
	 * the ships relationship to the other object.  This is mostly for combat, where you need to
	 * know when a missile his the belly or ass, or whether you can fire a forward missile at it.
	 *
	 * @see com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir
	 *
	 * @see GalacticMap#getDirection(long[], long[])
	 * @see GalacticMap#getDirection(SpaceObject, SpaceObject)
	 * @see GalacticMap#getDirectionFromDir(double[], double, double[])
	 * @see GalacticMap#getOppositeDir(double[])
	 * @see GalacticMap#getAbsoluteDirectionalFromDir(double[])
	 *
	 * @param facing the direction of facing
	 * @param roll the roll angle
	 * @param direction the direction to the other object
	 * @return the relative direction code
	 */
	public ShipDirectional.ShipDir getDirectionFromDir(double[] facing, double roll, double[] direction);

	/**
	 * Given a direction this will return a relative direction object describing
	 * the directions absolute facing.  This is arbitrary, and just allows zones
	 * of direction to be described more easily.
	 *
	 * @see com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir
	 *
	 * @see GalacticMap#getDirection(long[], long[])
	 * @see GalacticMap#getDirection(SpaceObject, SpaceObject)
	 * @see GalacticMap#getDirectionFromDir(double[], double, double[])
	 * @see GalacticMap#getOppositeDir(double[])
	 * @see GalacticMap#getDirectionFromDir(double[], double, double[])
	 *
	 * @param direction the direction to the other object
	 * @return the absolute direction code
	 */
	public ShipDirectional.ShipDir getAbsoluteDirectionalFromDir(final double[] direction);

	/**
	 * Given a direction, this will return its opposite
	 *
	 * @see GalacticMap#getDirection(long[], long[])
	 * @see GalacticMap#getDirection(SpaceObject, SpaceObject)
	 * @see GalacticMap#getDirectionFromDir(double[], double, double[])
	 * @see GalacticMap#getOppositeDir(double[])
	 *
	 * @param dir the direction
	 * @return the opposite direction
	 */
	public double[] getOppositeDir(final double[] dir);

	/**
	 * Calculates the relative speed of two SpaceObjects to each other.
	 * The math is based entirely on coordinates and speed, not direction,
	 * so take it for what you will.  Do they both need to be going the
	 * same direction for this to work?
	 *
	 * @param O1 the first Space Object
	 * @param O2 the second Space Object
	 * @return the relative speed of the two objects
	 */
	public long getRelativeSpeed(SpaceObject O1, SpaceObject O2);

	/**
	 * This method does not actually move anything, but given a space object, and an
	 * acceleration direction and an acceleration speed, this will alter the space objects
	 * direction and speed based on the new acceleration.
	 *
	 * @see GalacticMap#accelSpaceObject(double[], double, double[], double)
	 *
	 * @param O the space object to modify
	 * @param accelDirection the acceleration direction
	 * @param newAcceleration the acceleration amount
	 */
	public void accelSpaceObject(final SpaceObject O, final double[] accelDirection, final double newAcceleration);

	/**
	 * This method does not actually move anything, but returns a change in direction, and
	 * a change in speed caused by an acceleration in a new direction.
	 *
	 * @see GalacticMap#accelSpaceObject(double[], double, double[], double)
	 *
	 * @param curDirection the current direction, *AND* the new direction
	 * @param curSpeed the current speed
	 * @param accelDirection the direction of acceleration
	 * @param newAcceleration the amount of acceleration
	 * @return the new speed
	 */
	public double accelSpaceObject(final double[] curDirection, final double curSpeed, final double[] accelDirection, final double newAcceleration);

	/**
	 * Given some galactic coordinates, a direction of travel, and a distance, this will
	 * return the new coordinates.
	 *
	 * @see GalacticMap#moveSpaceObject(SpaceObject)
	 * @see GalacticMap#moveSpaceObject(SpaceObject, long[])
	 * @see GalacticMap#moveSpaceObject(long[], double[], long)
	 *
	 * @param oldLocation the previous location
	 * @param direction the direction of travel
	 * @param distance the distance of travel
	 * @return the new location
	 */
	public long[] getLocation(long[] oldLocation, double[] direction, long distance);

	/**
	 * Given a SpaceObject that is moving, this will alter the given objects
	 * coordinates based on its speed and direction.
	 *
	 * @see GalacticMap#moveSpaceObject(SpaceObject, long[])
	 * @see GalacticMap#moveSpaceObject(long[], double[], long)
	 * @see GalacticMap#getLocation(long[], double[], long)
	 *
	 * @param O the SpaceObject to move
	 */
	public void moveSpaceObject(SpaceObject O);

	/**
	 * Given a SpaceObject and its coordinates, this will alter the given coordinates
	 * based on the speed and direction of the given space object.
	 *
	 * @see GalacticMap#moveSpaceObject(SpaceObject)
	 * @see GalacticMap#moveSpaceObject(long[], double[], long)
	 * @see GalacticMap#getLocation(long[], double[], long)
	 *
	 * @param O the space object that is moving
	 * @param coords the coordinates to modify based on speed/dir of the object
	 */
	public void moveSpaceObject(SpaceObject O, long[] coords);

	/**
	 * Given a set of galactic coordinates, and a direction angle, and a speed, this will return
	 * the new coordinates after applying all the inputs.
	 *
	 * @see GalacticMap#moveSpaceObject(SpaceObject)
	 * @see GalacticMap#moveSpaceObject(SpaceObject, long[])
	 * @see GalacticMap#getLocation(long[], double[], long)
	 *
	 * @param coordinates the galactic coordinates
	 * @param direction the direction angle
	 * @param speed the speed
	 * @return the new coordinates
	 */
	public long[] moveSpaceObject(final long[] coordinates, final double[] direction, long speed);

	/**
	 * Returns an enumeration of all object in the galactic space map.
	 *
	 * @see GalacticMap#getSpaceObjectEntries()
	 * @see GalacticMap#getSpaceObjectsWithin(SpaceObject, long, long)
	 * @see GalacticMap#getSpaceObjectsByCenterpointWithin(long[], long, long)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 * @see GalacticMap#getSpaceObject(CMObject, boolean)
	 *
	 * @return an enumeration of all object in the galactic space map
	 */
	public Enumeration<SpaceObject> getSpaceObjects();

	/**
	 * Returns an enumeration of all objects, along with a list of their tracking vectors?
	 * I have no idea why anyone would want this, so I won't explain any more until someone
	 * does try to use this.
	 *
	 * @see GalacticMap#getSpaceObjects()
	 * @see GalacticMap#getSpaceObjectsWithin(SpaceObject, long, long)
	 * @see GalacticMap#getSpaceObjectsByCenterpointWithin(long[], long, long)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 * @see GalacticMap#getSpaceObject(CMObject, boolean)
	 *
	 * @return returns all of the objects in space
	 */
	public Enumeration<Entry<SpaceObject, List<WeakReference<TrackingVector<SpaceObject>>>>>  getSpaceObjectEntries();

	/**
	 * Given a space object to use as a center point on the galactic map, this will
	 * return all objects in the caches space map that is within the "donut" defined by those things.
	 *
	 * @see GalacticMap#getSpaceObjects()
	 * @see GalacticMap#getSpaceObjectEntries()
	 * @see GalacticMap#getSpaceObjectsByCenterpointWithin(long[], long, long)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 * @see GalacticMap#getSpaceObject(CMObject, boolean)
	 *
	 * @param ofObj the space object to use as a center point
	 * @param minDistance the minimum distance to return
	 * @param maxDistance the maximum distance to return
	 * @return all objects matching the distance scan
	 */
	public List<SpaceObject> getSpaceObjectsWithin(SpaceObject ofObj, long minDistance, long maxDistance);

	/**
	 * Given a center galactic coordinates, and a minimum and maximum distance from that coordinate, this will
	 * return all objects in the caches space map that is within the "donut" defined by those things.
	 *
	 * @see GalacticMap#getSpaceObjects()
	 * @see GalacticMap#getSpaceObjectEntries()
	 * @see GalacticMap#getSpaceObjectsWithin(SpaceObject, long, long)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 * @see GalacticMap#getSpaceObject(CMObject, boolean)
	 *
	 * @param centerCoordinates the full galactic coordinates of the center point
	 * @param minDistance the minimum distance to return
	 * @param maxDistance the maximum distance to return
	 * @return all objects matching the distance scan
	 */
	public List<SpaceObject> getSpaceObjectsByCenterpointWithin(final long[] centerCoordinates, long minDistance, long maxDistance);

	/**
	 * Given a random object, this will return null, or the "nearest" space object,
	 * by crawling up an item container tree, or however it needs to.
	 *
	 * @see GalacticMap#getSpaceObjects()
	 * @see GalacticMap#getSpaceObjectEntries()
	 * @see GalacticMap#getSpaceObjectsWithin(SpaceObject, long, long)
	 * @see GalacticMap#getSpaceObjectsByCenterpointWithin(long[], long, long)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 *
	 * @param o the game object, of almost any sort (item, area, room, mob)
	 * @param ignoreMobs true to return null on mobs, false otherwise
	 * @return the nearest SpaceObject the object IS, or is inside
	 */
	public SpaceObject getSpaceObject(CMObject o, boolean ignoreMobs);

	/**
	 * Given the name of an object in space, preferably a unique name, this will attempt to find and return it
	 * using standard search methods.  That means class IDs, Names, and display texts are all game.  Preference
	 * to the first hit, but supporting the .1, .2, etc syntax.
	 *
	 * @see GalacticMap#getSpaceObjects()
	 * @see GalacticMap#getSpaceObjectEntries()
	 * @see GalacticMap#getSpaceObjectsWithin(SpaceObject, long, long)
	 * @see GalacticMap#getSpaceObjectsByCenterpointWithin(long[], long, long)
	 * @see GalacticMap#getSpaceObject(CMObject, boolean)
	 *
	 * @param s the search string
	 * @param exactOnly true to only match full terms, false for partial matches
	 * @return the SpaceObject found, or null
	 */
	public SpaceObject findSpaceObject(String s, boolean exactOnly);

	/**
	 * Given some absolute space coordinates, this will return
	 * the name of the sector the coordinates are in.
	 *
	 * @see GalacticMap#getInSectorCoords(long[])
	 *
	 * @param coordinates the space coordinates
	 * @return the name of the sector the coordinates are in
	 */
	public String getSectorName(long[] coordinates);

	/**
	 * Given absolute space coordinates, from -Long.MAX to Long.MAX,
	 * this will return the relative coordinates INSIDE the
	 * sector, whose bounds are determined by lots of math.
	 *
	 * @see GalacticMap#getSectorName(long[])
	 *
	 * @param coordinates the space coordinates
	 * @return the inner bounds
	 */
	public long[] getInSectorCoords(long[] coordinates);

	/**
	 * Given a space ship, and an object that can be reduced to an Area, this will
	 * return a list of all valid LocationRoom objects, sorted by their present
	 * distance from the ship, from each other, and whether its a space port.
	 * Best closest match at the top of the list.
	 *
	 * @param ship the ship looking for a landing spot
	 * @param O an area, room, boardable, whatever
	 * @return a list of LocationRoom objects, best on top
	 */
	public List<LocationRoom> getLandingPoints(final SpaceObject ship, final Environmental O);
}
