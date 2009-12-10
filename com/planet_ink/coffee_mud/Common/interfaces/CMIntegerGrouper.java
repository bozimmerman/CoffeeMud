package com.planet_ink.coffee_mud.Common.interfaces;
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
/**
 * This class represents an efficient representation of a group
 * of room numbers with optional xy grid coords.  It maintains 
 * a compact list by ordering the digits and then tagging the 
 * groups without missing members.
 */
public interface CMIntegerGrouper extends CMCommon
{
	/** 
	 * This set of integers as a savable string.
	 * @return this group as a string.
	 */
    public String text();
    
	/** 
	 * Return a random coded room number from this set.
	 * @return the random room number as coded long
	 */
    public long random();
    
	/** 
	 * Return all raw room numbers, with grid locales
	 * encoded.
	 * @return all of the room numbers
	 */
    public long[] allRoomNums();
    
	/** 
	 * Return all primary raw room numbers.
	 * @return all of the room numbers
	 */
    public int[] allPrimaryRoomNums();
    
	/** 
	 * Return all packed grid room numbers, with grid locales
	 * encoded.
	 * @return all of the packed grid room numbers
	 */
    public long[] packedGridRoomNums();
    
	/** 
	 * Return all packed grid room numbers, with grid locales
	 * encoded.
	 * @return all of the packed grid room numbers
	 */
    public int[] packedRoomNums();
    
    /**
     * Creates a nice storage of integers from a
     * saved string.
     * @param txt the list as a string
     * @return a populated grouping object
     */
    public CMIntegerGrouper parseText(String txt);
    /**
     * Returns whether this list contains the given coded room number.
     * @param x the number to look for, coded as a room number
     * @return whether x is in this grouping.
     */
    public boolean contains(long x);
    /**
     * The total number of digits represented by this grouping.
     * @return the number of room numbers herein.
     */
    public int roomCount();
    /**
     * Removes the room number
     * @param x the coded digit pair to remove.
     * @return the integrouper with the pair removed
     */
    public CMIntegerGrouper remove(long x);
    /**
     * Adds the room number
     * @param x the coded digit pair to add.
     * @return the integrouper with the pair added
     */
    public CMIntegerGrouper add(long x);
    /**
     * Removes the room number
     * @param grp the coded digit pair to remove.
     * @return the integrouper with the pair removed
     */
    public CMIntegerGrouper remove(CMIntegerGrouper grp);
    /**
     * Adds a group of room numbers
     * @param grp the coded digit pairs to add.
     * @return the integrouper with the pair added
     */
    public CMIntegerGrouper add(CMIntegerGrouper grp);
    /**
     * Adds the single digit
     * @param x the single digit to add.
     */
    public void addy(long x);
    /**
     * Adds the single digit
     * @param x the single digit to add.
     */
    public void addx(int x);
    /**  Whether this number denotes the beginning of a grouping.*/
    public static final int NEXT_FLAG=(Integer.MAX_VALUE/2)+1;
    /**  a mask for room number values */
    public static final int NEXT_BITS=NEXT_FLAG-1;
    /**  Whether this number denotes the beginning of a grouping.*/
    public static final long NEXT_FLAGL=(Long.MAX_VALUE/2)+1;
    /**  Whether this number denotes the beginning of a grouping.*/
    public static final long GRID_FLAGL=NEXT_FLAGL/2;
    /**  a mask for room number values */
    public static final long NEXT_BITSL=NEXT_FLAGL-1;
}
