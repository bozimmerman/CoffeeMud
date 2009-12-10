package com.planet_ink.coffee_mud.core.interfaces;
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
/**
 * This interface is still in development.  It will some day represent an object in space
 * @author Bo Zimmerman
 */
public interface SpaceObject extends Environmental
{
    /**
     * The current absolute coordinates of  the object
     * @return 2 dimensional array of the coordinates
     */
	public long[] coordinates();
    /**
     * Sets the current absolute coordinates of the object
     * @param coords 2  dimensional array of the coordinates in space
     */
	public void setCoords(long[] coords);
    /**
     * The direction of travel of this object in radians. 
     * @return 2 dimensional array for the direction of movement
     */
	public double[] direction();
    /**
     * Sets the direction of travel of this object in radians.
     * @param dir 2 dimensional array for the direction of movement
     */
	public void setDirection(double[] dir);
    /**
     * The velocity of the object through space
     * @return the velocity
     */
	public long velocity();
    /**
     * Sets the velocify of the object through space
     * @param v the velocity
     */
	public void setVelocity(long v);
    /**
     * Gets the current accelleration rate of the object,  per tick
     * @return the current accelleration rate
     */
	public long accelleration();
    /**
     * Sets the current accelleration rate of the object,  per tick
     * @param v current accelleration rate
     */
	public void setAccelleration(long v);

    /**
     * If this object is targeting another space object as a destination, this will return it
     * @return the target destination
     */
	public SpaceObject knownTarget();
    /**
     * If this object is targeting another space object as a destination, this will set it
     * @param O the target destination
     */
	public void setKnownTarget(SpaceObject O);
    /**
     * The source object from which this space object is travelling from
     * @return the source of  this object
     */
	public SpaceObject knownSource();
    /**
     * Sets the source object from which this space object is travelling from
     * @param O the source of  this object
     */
	public void setKnownSource(SpaceObject O);
    /**
     * If this object is in orbit about another,  what is that other
     * @return the object about which this one is orbiting
     */
	public SpaceObject orbiting();
    /**
     * Sets this object in orbit about another
     * @param O the object about which this one is orbiting
     */
	public void setOrbiting(SpaceObject O);
	
    /** distance constant useful for coordinates, is 1 mile*/
	public static final long DISTANCE_MILE=1;
    /** distance constant useful for coordinates, is 1 lightyear*/
	public static final long DISTANCE_LIGHTYEAR=2000000000;
    /** distance constant useful for coordinates, is 1 galaxy*/
	public static final long DISTANCE_AROUNDGALAXY=DISTANCE_LIGHTYEAR*100;
    /** distance constant useful for coordinates, is 1 planet*/
	public static final long DISTANCE_PLANETRADIUS=3976;
    /** distance constant useful for coordinates, is 1 orbit*/
	public static final long DISTANCE_ORBITING=3252;

    /** velocity constant for the speed of light */
	public static final long VELOCITY_LIGHT=833333;
    /** velocity constant for the speed of sublight */
	public static final long VELOCITY_SUBLIGHT=750000;
    /** velocity constant for the speed of sound */
	public static final long VELOCITY_SOUND=1;
    /** velocity constant for the speed of orbiting */
	public static final long VELOCITY_ORBITING=311;
    /** velocity constant for the speed required to escape 1g */
	public static final long VELOCITY_ESCAPE=442;
    /** velocity constant for the speed warp 1 */
	public static final long VELOCITY_WARP1=VELOCITY_LIGHT;
    /** velocity constant for the speed warp 2 */
	public static final long VELOCITY_WARP2=VELOCITY_LIGHT*4;
    /** velocity constant for the speed warp 3 */
	public static final long VELOCITY_WARP3=VELOCITY_LIGHT*9;
    /** velocity constant for the speed warp 4 */
	public static final long VELOCITY_WARP4=VELOCITY_LIGHT*16;
    /** velocity constant for the speed warp 5 */
	public static final long VELOCITY_WARP5=VELOCITY_LIGHT*25;
    /** velocity constant for the speed warp 6 */
	public static final long VELOCITY_WARP6=VELOCITY_LIGHT*36;
    /** velocity constant for the speed warp 7 */
	public static final long VELOCITY_WARP7=VELOCITY_LIGHT*49;
    /** velocity constant for the speed warp 8 */
	public static final long VELOCITY_WARP8=VELOCITY_LIGHT*64;
    /** velocity constant for the speed warp 9 */
	public static final long VELOCITY_WARP9=VELOCITY_LIGHT*81;
    /** velocity constant for the speed warp 10 */
	public static final long VELOCITY_WARP10=VELOCITY_LIGHT*100;
    /** velocity constant for the speed transwarp 1 */
	public static final long VELOCITY_TRANSWARP1=VELOCITY_LIGHT;
    /** velocity constant for the speed transwarp 2 */
	public static final long VELOCITY_TRANSWARP2=VELOCITY_LIGHT*8;
    /** velocity constant for the speed transwarp 3 */
	public static final long VELOCITY_TRANSWARP3=VELOCITY_LIGHT*27;
    /** velocity constant for the speed transwarp 4 */
	public static final long VELOCITY_TRANSWARP4=VELOCITY_LIGHT*64;
    /** velocity constant for the speed transwarp 5 */
	public static final long VELOCITY_TRANSWARP5=VELOCITY_LIGHT*125;
    /** velocity constant for the speed transwarp 6 */
	public static final long VELOCITY_TRANSWARP6=VELOCITY_LIGHT*216;
    /** velocity constant for the speed transwarp 7 */
	public static final long VELOCITY_TRANSWARP7=VELOCITY_LIGHT*343;
    /** velocity constant for the speed transwarp 8 */
	public static final long VELOCITY_TRANSWARP8=VELOCITY_LIGHT*512;
    /** velocity constant for the speed transwarp 9 */
	public static final long VELOCITY_TRANSWARP9=VELOCITY_LIGHT*729;
    /** velocity constant for the speed transwarp 10 */
	public static final long VELOCITY_TRANSWARP10=VELOCITY_LIGHT*1000;
}
