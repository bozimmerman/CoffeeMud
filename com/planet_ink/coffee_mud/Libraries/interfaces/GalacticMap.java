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
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
/*
   Copyright 2005-2025 Bo Zimmerman

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
	 * Returns the name of all the sectors, and the cube
	 * that covers its domain.
	 *
	 * @return the sector map
	 */
	public Map<String,BoundedCube> getSectorMap();

	/**
	 * Returns whether the given object is actually in
	 * the galactic map cache at the moment -- it could
	 * be landed somewhere.
	 *
	 * @see GalacticMap#numSpaceObjects()
	 * @see GalacticMap#isObjectInSpace(SpaceObject)
	 * @see GalacticMap#delObjectInSpace(SpaceObject)
	 * @see GalacticMap#addObjectToSpace(SpaceObject, Coord3D)
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
	 * @see GalacticMap#addObjectToSpace(SpaceObject, Coord3D)
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
	 * @see GalacticMap#addObjectToSpace(SpaceObject, Coord3D)
	 * @see GalacticMap#addObjectToSpace(SpaceObject)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 *
	 * @param O the SpaceObject to add
	 * @param coords the coordinates to add it at
	 */
	public void addObjectToSpace(SpaceObject O, Coord3D coords);

	/**
	 * Adds the given SpaceObject to the galactic map cache.
	 *
	 * @see GalacticMap#numSpaceObjects()
	 * @see GalacticMap#isObjectInSpace(SpaceObject)
	 * @see GalacticMap#delObjectInSpace(SpaceObject)
	 * @see GalacticMap#addObjectToSpace(SpaceObject, Coord3D)
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
	 * @see GalacticMap#getDistanceFrom(Coord3D, Coord3D)
	 * @see GalacticMap#getDistanceFrom(SpaceObject, SpaceObject)
	 * @see GalacticMap#getMinDistanceFrom(Coord3D, Coord3D, Coord3D)
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
	 * @see GalacticMap#getDistanceFrom(Coord3D, Coord3D)
	 * @see GalacticMap#getDistanceFrom(SpaceObject, SpaceObject)
	 * @see GalacticMap#getMinDistanceFrom(Coord3D, Coord3D, Coord3D)
	 *
	 * @param coord1 first galactic coords
	 * @param coord2 second galactic coords
	 * @return distance between the two
	 */
	public long getDistanceFrom(final Coord3D coord1, final Coord3D coord2);

	/**
	 * Given a previous position and a current position, this will return the minimum
	 * distance approached to the given object position.
	 *
	 * @see GalacticMap#getDistanceFrom(Coord3D, Coord3D)
	 * @see GalacticMap#getDistanceFrom(SpaceObject, SpaceObject)
	 * @see GalacticMap#getMinDistanceFrom(Coord3D, Coord3D, Coord3D)
	 *
	 * @param prevPos previous position on galactic chart
	 * @param curPosition current position on galactic chart
	 * @param objPos the object position curious about minimum distance to
	 * @return the minimum distance in high precision
	 */
	public double getMinDistanceFrom(final Coord3D prevPos, final Coord3D curPosition, final Coord3D objPos);

	/**
	 * Calculates the best available orbit.
	 *
	 * @param o the space ship that wants to orbit
	 * @param p the planet to orbit
	 * @return the final direction and speed to be in orbit
	 */
	public Pair<Dir3D, Double> calculateOrbit(final SpaceObject o, final SpaceObject p);

	/**
	 * Given two vectors, this will return the minimum
	 * distance between the two vectors.
	 *
	 * @see GalacticMap#getDistanceFrom(Coord3D, Coord3D)
	 * @see GalacticMap#getDistanceFrom(SpaceObject, SpaceObject)
	 * @see GalacticMap#getMinDistanceFrom(Coord3D, Coord3D, Coord3D)
	 * @see GalacticMap#getMinDistanceFrom(Coord3D, Coord3D, Coord3D, Coord3D)
	 *
	 * @param vec1s start point of the first vector
	 * @param vec1e end point of the first vector
	 * @param vec2s start point of the second vector
	 * @param vec2e end point of the second vector
	 * @return the minimum distance in low precision
	 */
	public double getMinDistanceFrom(final Coord3D vec1s, final Coord3D vec1e, final Coord3D vec2s, final Coord3D vec2e);

	/**
	 * Given two angles, this returns the difference between them as a single angle.
	 *
	 * @see GalacticMap#getAngleDiff(Dir3D, Dir3D)
	 *
	 * @param fromAngle the first angle
	 * @param toAngle the second angle
	 * @return the angle delta
	 */

	public double getAngleDelta(final Dir3D fromAngle, final Dir3D toAngle);

	/**
	 * Given two angles, this returns the angle between them.
	 * @param angle1 the first angle
	 * @param angle2 the second angle
	 * @return the middle angle
	 */
	public Dir3D getMiddleAngle(final Dir3D angle1, final Dir3D angle2);

	/**
	 * Given a base 'correct' angle and another 'wrong' angle, this will return
	 * the angle that it opposite to, but equal in distance.
	 *
	 * @param correctAngle the base angle
	 * @param wrongAngle the wrong angle
	 * @return another wrong angle, on the other 'side' of the base
	 */
	public Dir3D getOffsetAngle(final Dir3D correctAngle, final Dir3D wrongAngle);

	/**
	 * Given a base 'correct' angle and another 'wrong' angle, this will return
	 * the angle that is more incorrect, being equal in distance, in the same
	 * direction as the incorrect angle
	 *
	 * @param correctAngle the base angle
	 * @param wrongAngle the wrong angle
	 * @return a MORE wrong angle, on the same 'side' of the base
	 */
	public Dir3D getExaggeratedAngle(final Dir3D correctAngle, final Dir3D wrongAngle);

	/**
	 * Given two angles, this returns the difference between them in pitch and yaw.
	 *
	 * @see GalacticMap#getAngleDelta(Dir3D, Dir3D)
	 *
	 * @param fromAngle the first angle
	 * @param toAngle the second angle
	 * @return the angle delta
	 */
	public Dir3D getAngleDiff(final Dir3D fromAngle, final Dir3D toAngle);

	/**
	 * Given an angle and a simple delta, with positive and negative values,
	 * this will add the delta to the angle and normalize the angle.
	 *
	 * @param angle the angle to modify
	 * @param delta the delta
	 */
	public void applyAngleDiff(final Dir3D angle, final Dir3D delta);

	/**
	 * Given two space objects, this will return the direction in radians
	 * from the first to the second.
	 *
	 * @see GalacticMap#getDirection(Coord3D, Coord3D)
	 * @see GalacticMap#getDirection(SpaceObject, SpaceObject)
	 * @see GalacticMap#getDirectionFromDir(Dir3D, double, Dir3D)
	 * @see GalacticMap#getOppositeDir(Dir3D)
	 *
	 * @param fromObj the first SpaceObject
	 * @param toObj the second SpaceObject
	 * @return the direction in radians from the first to the second
	 */
	public Dir3D getDirection(SpaceObject fromObj, SpaceObject toObj);

	/**
	 * Given two galactic coordinates, this will return the direction in radians
	 * from the first to the second.
	 *
	 * @see GalacticMap#getDirection(Coord3D, Coord3D)
	 * @see GalacticMap#getDirection(SpaceObject, SpaceObject)
	 * @see GalacticMap#getDirectionFromDir(Dir3D, double, Dir3D)
	 * @see GalacticMap#getOppositeDir(Dir3D)
	 *
	 * @param fromCoords the first coordinates
	 * @param toCoords the second coordinates
	 * @return the angle of direction from the first to the second
	 */
	public Dir3D getDirection(final Coord3D fromCoords, final Coord3D toCoords);

	/**
	 * Given a facing direction (NOT direction of travel), and a roll angle (belly/axis), and a
	 * direction to some other object, this will return a relative direction object describing
	 * the ships relationship to the other object.  This is mostly for combat, where you need to
	 * know when a missile his the belly or ass, or whether you can fire a forward missile at it.
	 *
	 * @see com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir
	 *
	 * @see GalacticMap#getDirection(Coord3D, Coord3D)
	 * @see GalacticMap#getDirection(SpaceObject, SpaceObject)
	 * @see GalacticMap#getDirectionFromDir(Dir3D, double, Dir3D)
	 * @see GalacticMap#getOppositeDir(Dir3D)
	 * @see GalacticMap#getAbsoluteDirectionalFromDir(Dir3D)
	 *
	 * @param facing the direction of facing
	 * @param roll the roll angle
	 * @param direction the direction to the other object
	 * @return the relative direction code
	 */
	public ShipDirectional.ShipDir getDirectionFromDir(Dir3D facing, double roll, Dir3D direction);

	/**
	 * Given a direction this will return a relative direction object describing
	 * the directions absolute facing.  This is arbitrary, and just allows zones
	 * of direction to be described more easily.
	 *
	 * @see com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir
	 *
	 * @see GalacticMap#getDirection(Coord3D, Coord3D)
	 * @see GalacticMap#getDirection(SpaceObject, SpaceObject)
	 * @see GalacticMap#getDirectionFromDir(Dir3D, double, Dir3D)
	 * @see GalacticMap#getOppositeDir(Dir3D)
	 * @see GalacticMap#getDirectionFromDir(Dir3D, double, Dir3D)
	 *
	 * @param direction the direction to the other object
	 * @return the absolute direction code
	 */
	public ShipDirectional.ShipDir getAbsoluteDirectionalFromDir(final Dir3D direction);

	/**
	 * Given a direction, this will return its opposite
	 *
	 * @see GalacticMap#getDirection(Coord3D, Coord3D)
	 * @see GalacticMap#getDirection(SpaceObject, SpaceObject)
	 * @see GalacticMap#getDirectionFromDir(Dir3D, double, Dir3D)
	 * @see GalacticMap#getOppositeDir(Dir3D)
	 *
	 * @param dir the direction
	 * @return the opposite direction
	 */
	public Dir3D getOppositeDir(final Dir3D dir);

	/**
	 * Given an angle from origin, this will return the other
	 * trivial angles that are 90 degrees from the given one.
	 * Will NOT include the given one.
	 *
	 * @param angle the angle from origin
	 * @return the set of angles (usually 4 of them).
	 */
	public Dir3D[] getPerpendicularAngles(final Dir3D angle);

	/**
	 * Given the origin point and an angle from the origin, this
	 * will return the other points that are 90 degrees from the
	 * given one, at the given distance.
	 *
	 * @see GalacticMap#getPerpendicularAngles(Dir3D)
	 *
	 * @param origin the origin points
	 * @param angle the angle from origin
	 * @param distance the distance from origin of the points to return
	 * @return the set of points (usually 4 of them)
	 */
	public Coord3D[] getPerpendicularPoints(final Coord3D origin, final Dir3D angle, final long distance);

	/**
	 * Changes the given direction by the given delta variables.  Corrects any
	 * crossover bounds, and checks the delta bounds.
	 *
	 * @param dir the current direction to change
	 * @param delta0 the port/starboard delta
	 * @param delta1 the ventral/dorsel delta
	 */
	public void changeDirection(final Dir3D dir, final double delta0, final double delta1);

	/**
	 * Changes the given direction by the given delta variable.  Corrects any
	 * crossover bounds, and checks the delta bounds.
	 *
	 * @param dir the current direction to change
	 * @param delta the delta to change it by, + or -
	 */
	public void changeDirection(final Dir3D dir, final Dir3D delta);

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
	 * @see GalacticMap#accelSpaceObject(Dir3D, double, Dir3D, double)
	 *
	 * @param O the space object to modify
	 * @param accelDirection the acceleration direction
	 * @param newAcceleration the acceleration amount
	 */
	public void accelSpaceObject(final SpaceObject O, final Dir3D accelDirection, final double newAcceleration);

	/**
	 * This method does not actually move anything, but returns a change in direction, and
	 * a change in speed caused by an acceleration in a new direction.
	 *
	 * @see GalacticMap#accelSpaceObject(Dir3D, double, Dir3D, double)
	 *
	 * @param curDirection the current direction, *AND* the new direction
	 * @param curSpeed the current speed
	 * @param accelDirection the direction of acceleration
	 * @param newAcceleration the amount of acceleration
	 * @return the new speed
	 */
	public double accelSpaceObject(final Dir3D curDirection, final double curSpeed, final Dir3D accelDirection, final double newAcceleration);

	/**
	 * Given some galactic coordinates, a direction of travel, and a distance, this will
	 * return the new coordinates.
	 *
	 * @see GalacticMap#moveSpaceObject(SpaceObject)
	 * @see GalacticMap#moveSpaceObject(SpaceObject, Coord3D)
	 * @see GalacticMap#moveSpaceObject(Coord3D, Dir3D, long)
	 *
	 * @param oldLocation the previous location
	 * @param direction the direction of travel
	 * @param distance the distance of travel
	 * @return the new location
	 */
	public Coord3D getLocation(Coord3D oldLocation, Dir3D direction, long distance);

	/**
	 * Given a SpaceObject that is moving, this will alter the given objects
	 * coordinates based on its speed and direction.
	 *
	 * @see GalacticMap#moveSpaceObject(SpaceObject, Coord3D)
	 * @see GalacticMap#moveSpaceObject(Coord3D, Dir3D, long)
	 * @see GalacticMap#getLocation(Coord3D, Dir3D, long)
	 *
	 * @param O the SpaceObject to move
	 */
	public void moveSpaceObject(SpaceObject O);

	/**
	 * Given a SpaceObject and its coordinates, this will alter the given coordinates
	 * based on the speed and direction of the given space object.
	 *
	 * @see GalacticMap#moveSpaceObject(SpaceObject)
	 * @see GalacticMap#moveSpaceObject(Coord3D, Dir3D, long)
	 * @see GalacticMap#getLocation(Coord3D, Dir3D, long)
	 *
	 * @param O the space object that is moving
	 * @param coords the coordinates to modify based on speed/dir of the object
	 */
	public void moveSpaceObject(SpaceObject O, Coord3D coords);

	/**
	 * Given a set of galactic coordinates, and a direction angle, and a speed, this will return
	 * the new coordinates after applying all the inputs.
	 *
	 * @see GalacticMap#moveSpaceObject(SpaceObject)
	 * @see GalacticMap#moveSpaceObject(SpaceObject, Coord3D)
	 * @see GalacticMap#getLocation(Coord3D, Dir3D, long)
	 *
	 * @param coordinates the galactic coordinates
	 * @param direction the direction angle
	 * @param speed the speed
	 * @return the new coordinates
	 */
	public Coord3D moveSpaceObject(final Coord3D coordinates, final Dir3D direction, long speed);

	/**
	 * Returns the proper direction and speed to allow the given chaser to intercept the given runner.
	 *
	 * @see GalacticMap#canMaybeIntercept(SpaceObject, SpaceObject, int, double)
	 *
	 * @param chaserO the chasing object
	 * @param runnerO the running object
	 * @param maxChaserSpeed the max chasing speed
	 * @param maxTicks the maximum number of movements
	 * @return null if no intercept possible, or the new dir and speed
	 */
	public Pair<Dir3D,Long> calculateIntercept(final SpaceObject chaserO, final SpaceObject runnerO, final long maxChaserSpeed, final int maxTicks);

	/**
	 * Returns whether the vectors described by the chaser and runners, their speeds, and the amount of time in
	 * movement can even POSSIBLY intercept, because the two vectors overlap
	 *
	 * @see GalacticMap#calculateIntercept(SpaceObject, SpaceObject, long, int)
	 *
	 * @param chaserO the chasing object
	 * @param runnerO the running object
	 * @param maxTicks the maximum number of movements
	 * @param maxSpeed the max speed of the chaser
	 * @return true if there is a possible intercept, false otherwise
	 */
	public boolean canMaybeIntercept(final SpaceObject chaserO, final SpaceObject runnerO, final int maxTicks, double maxSpeed);

	/**
	 * Returns an enumeration of all object in the galactic space map.
	 *
	 * @see GalacticMap#getSpaceObjectEntries()
	 * @see GalacticMap#getSpaceObjectsWithin(SpaceObject, long, long)
	 * @see GalacticMap#getSpaceObjectsByCenterpointWithin(Coord3D, long, long)
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
	 * @see GalacticMap#getSpaceObjectsByCenterpointWithin(Coord3D, long, long)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 * @see GalacticMap#getSpaceObject(CMObject, boolean)
	 * @see GalacticMap#getSpaceObjectsInBound(BoundedCube)
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
	 * @see GalacticMap#getSpaceObjectsByCenterpointWithin(Coord3D, long, long)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 * @see GalacticMap#getSpaceObject(CMObject, boolean)
	 * @see GalacticMap#getSpaceObjectsInBound(BoundedCube)
	 * @see GalacticMap#getSpaceObjectsInBound(BoundedTube)
	 *
	 * @param ofObj the space object to use as a center point
	 * @param minDistance the minimum distance to return
	 * @param maxDistance the maximum distance to return
	 * @return all objects matching the distance scan
	 */
	public List<SpaceObject> getSpaceObjectsWithin(SpaceObject ofObj, long minDistance, long maxDistance);

	/**
	 * Given a bounded cube, this will return all space objects within that cube,
	 * and by within, i mean even just intersecting it.
	 *
	 * @see GalacticMap#getSpaceObjects()
	 * @see GalacticMap#getSpaceObjectEntries()
	 * @see GalacticMap#getSpaceObjectsByCenterpointWithin(Coord3D, long, long)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 * @see GalacticMap#getSpaceObject(CMObject, boolean)
	 * @see GalacticMap#getSpaceObjectsInBound(BoundedTube)
	 *
	 * @param cube the cube to look within
	 * @return the objects in space in that cube
	 */
	public List<SpaceObject> getSpaceObjectsInBound(final BoundedCube cube);


	/**
	 * Given a bounded tube, this will return all space objects within that tube,
	 * with exceptions
	 *
	 * @see GalacticMap#getSpaceObjects()
	 * @see GalacticMap#getSpaceObjectEntries()
	 * @see GalacticMap#getSpaceObjectsByCenterpointWithin(Coord3D, long, long)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 * @see GalacticMap#getSpaceObject(CMObject, boolean)
	 * @see GalacticMap#getSpaceObjectsInBound(BoundedCube)
	 *
	 * @param tube the tube to look within
	 * @param except don't return any objects centered on the given coordinates
	 * @return the objects in space in that tube
	 */
	public List<SpaceObject> getSpaceObjectsInBound(final BoundedTube tube, final Set<Coord3D> except);

	/**
	 * Given a center galactic coordinates, and a minimum and maximum distance from that coordinate, this will
	 * return all objects in the caches space map that is within the "donut" defined by those things.
	 *
	 * @see GalacticMap#getSpaceObjects()
	 * @see GalacticMap#getSpaceObjectEntries()
	 * @see GalacticMap#getSpaceObjectsWithin(SpaceObject, long, long)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 * @see GalacticMap#getSpaceObject(CMObject, boolean)
	 * @see GalacticMap#getSpaceObjectsInBound(BoundedCube)
	 * @see GalacticMap#getSpaceObjectsInBound(BoundedTube)
	 *
	 * @param centerCoordinates the full galactic coordinates of the center point
	 * @param minDistance the minimum distance to return
	 * @param maxDistance the maximum distance to return
	 * @return all objects matching the distance scan
	 */
	public List<SpaceObject> getSpaceObjectsByCenterpointWithin(final Coord3D centerCoordinates, long minDistance, long maxDistance);

	/**
	 * Given a random object, this will return null, or the "nearest" space object,
	 * by crawling up an item container tree, or however it needs to.
	 *
	 * @see GalacticMap#getSpaceObjects()
	 * @see GalacticMap#getSpaceObjectEntries()
	 * @see GalacticMap#getSpaceObjectsWithin(SpaceObject, long, long)
	 * @see GalacticMap#getSpaceObjectsByCenterpointWithin(Coord3D, long, long)
	 * @see GalacticMap#findSpaceObject(String, boolean)
	 * @see GalacticMap#getSpaceObjectsInBound(BoundedCube)
	 * @see GalacticMap#getSpaceObjectsInBound(BoundedTube)
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
	 * @see GalacticMap#getSpaceObjectsByCenterpointWithin(Coord3D, long, long)
	 * @see GalacticMap#getSpaceObject(CMObject, boolean)
	 * @see GalacticMap#getSpaceObjectsInBound(BoundedCube)
	 * @see GalacticMap#getSpaceObjectsInBound(BoundedTube)
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
	 * @see GalacticMap#getInSectorCoords(Coord3D)
	 *
	 * @param coordinates the space coordinates
	 * @return the name of the sector the coordinates are in
	 */
	public String getSectorName(Coord3D coordinates);

	/**
	 * Given absolute space coordinates, from -Long.MAX to Long.MAX,
	 * this will return the relative coordinates INSIDE the
	 * sector, whose bounds are determined by lots of math.
	 *
	 * @see GalacticMap#getSectorName(Coord3D)
	 *
	 * @param coordinates the space coordinates
	 * @return the inner bounds
	 */
	public Coord3D getInSectorCoords(Coord3D coordinates);

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

	/**
	 * If the two given objects are within an appropriate distance
	 * from each other, this will return the correct amount of
	 * acceleration g-force applied by the second object to the
	 * first. typically &lt;= 1G
	 *
	 * @param S the object being pulled
	 * @param cO the object pulling
	 * @return the amount of gravity force, or 0
	 */
	public double getGravityForce(SpaceObject S, SpaceObject cO);

	/**
	 * Given a ship war component, returns the directions in which it is
	 * currently covering.
	 *
	 * @param comp the war component
	 * @return the directions being covered
	 */
	public ShipDir[] getCurrentBattleCoveredDirections(final ShipDirectional comp);

	/**
	 * Generates an sends a message representing an emission in space,
	 * which might be picked up by various sensors.  Includes broadcast messages.
	 * @param srcP the generator of the event/the center
	 * @param tool the means by which the event was generated
	 * @param emissionType the CMMsg type of the event
	 * @param msgStr a description of the sensory message
	 * @return true if the event was propogated
	 */
	public boolean sendSpaceEmissionEvent(final SpaceObject srcP, final Environmental tool, final int emissionType, final String msgStr);

	/**
	 * Generates an sends a message representing an emission in space,
	 * which might be picked up by various sensors.  Includes broadcast messages.
	 * @param srcP the generator of the event/the center
	 * @param tool the means by which the event was generated
	 * @param range the range at which the emission can be detected
	 * @param emissionType the CMMsg type of the event
	 * @param msgStr a description of the sensory message
	 * @return true if the event was propogated
	 */
	public boolean sendSpaceEmissionEvent(final SpaceObject srcP, final Environmental tool, final long range, final int emissionType, final String msgStr);

	/**
	 * Plots a course from the source to the target, with a maximum number of steps/ticks.
	 * Will return a list of coordinates for the steps of the course.  If the list contains the
	 * given otarget, then the plotting is complete.  The list will never contain the source,
	 * as it is implied.  If the course list comes back empty, then either something terrible
	 * went wrong, or the source is already close enough to the target for a course to be
	 * impossible.
	 *
	 * @param osrc the source coordinates
	 * @param sradius the source object radius
	 * @param otarget the target coordinates
	 * @param tradius the target object radius
	 * @param maxTicks maximum number of direction changes .. always send something gt 0
	 * @return the step coordinates in the course, with the source implied
	 */
	public List<Coord3D> plotCourse(final Coord3D osrc, final long sradius, final Coord3D otarget, final long tradius, int maxTicks);
}
