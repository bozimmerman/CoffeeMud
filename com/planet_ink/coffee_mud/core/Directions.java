package com.planet_ink.coffee_mud.core;

import java.util.Arrays;

import com.planet_ink.coffee_mud.Items.interfaces.SpaceShip;
import com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;

/*
   Copyright 2001-2016 Bo Zimmerman

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
public class Directions
{
	/**
	 * Constructs a new Directions object for the current thread group.
	 */
	public Directions()
	{
		super();
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(dirs[c]==null)
			dirs[c]=this;
	}
	
	/**
	 * Returns the Directions object for the current threadgroup, or null if unassigned.
	 * @return the Directions object, or null
	 */
	private static Directions d()
	{ 
		return dirs[Thread.currentThread().getThreadGroup().getName().charAt(0)];
	}
	
	/**
	 * Returns the Directions instance tied to the given thread group, or null if not yet created.
	 * @param c the code for the thread group to return (0-255)
	 * @return the Directions instance tied to the given thread group, or null if not yet created.
	 */
	public static Directions d(char c)
	{
		return dirs[c];
	}
	
	/**
	 * Returns the Directions object that applies to the callers thread group.
	 * @return the Directions object that applies to the callers thread group.
	 */
	public static Directions instance()
	{
		final Directions d=d();
		if(d==null)
			return new Directions();
		return d;
	}

	private static final Directions[] dirs=new Directions[256];

	public static final int	NORTH		= 0;
	public static final int	SOUTH		= 1;
	public static final int	EAST		= 2;
	public static final int	WEST		= 3;
	public static final int	UP			= 4;
	public static final int	DOWN		= 5;

	public static final int	GATE		= 6;

	public static final int	NORTHEAST	= 7;
	public static final int	NORTHWEST	= 8;
	public static final int	SOUTHEAST	= 9;
	public static final int	SOUTHWEST	= 10;

	/* Display order directions.  Include up, down, and gate.  Include -1 to insert the other 4/8 */
	private final static int[]	DIRECTIONS_DISP_ORDER	= { UP, -1, DOWN, GATE };
	private final static int[]	DIRECTIONS_7_BASE		= { NORTH, SOUTH, EAST, WEST };
	private final static int[]	DIRECTIONS_11_BASE		= { NORTH, SOUTH, EAST, WEST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST };
	private final static String	DIRECTION_7_LETTERS		= "N, S, E, W, U, D, or V";
	private final static String	DIRECTION_11_LETTERS	= "N, S, E, W, NE, NW, SE, SW, U, D, or V";
	private final static String	DIRECTION_7_NAMES		= "North, South, East, West, Up, or Down";
	private final static String	DIRECTION_11_NAMES		= "North, South, East, West, Northeast, Northwest, Southeast, Southwest, Up, or Down";
	private final static String	DIRECTION_7_SHIPNAMES	= "Foreward, Aft, Starboard, Port, Above, or Below";
	private final static String	DIRECTION_11_SHIPNAMES	= "Foreward, Aft, Starboard, Port, Foreward Starboard, Foreward Port, "
														+ "Aft Starboard, Aft Port, Above, or Below";

	private int[]	DIRECTIONS_CODES	= { NORTH, SOUTH, EAST, WEST };
	private int[]	DIRECTIONS_DISPLAY	= { UP, DOWN, NORTH, SOUTH, EAST, WEST, GATE };
	private String	DIRECTION_LETTERS	= DIRECTION_7_LETTERS;
	private String	DIRECTION_NAMES		= DIRECTION_7_NAMES;
	private String	DIRECTION_SHIPNAMES	= DIRECTION_7_SHIPNAMES;

	private int NUM_DIRECTIONS=7;

	private static final String[] DIRECTION_CHARS= { "N","S","E","W","U","D","V","NE","NW","SE","SW" };

	private static final String[] DIRECTIONS_COMPASS_UPPER_INDEXED=
	{
		"NORTH","SOUTH","EAST","WEST","UP","DOWN","THERE","NORTHEAST","NORTHWEST","SOUTHEAST","SOUTHWEST"
	};


	private static final String[] DIRECTIONS_SHIP_UPPER_INDEXED=
	{
		"FOREWARD","AFT","STARBOARD","PORT","ABOVE","BELOW","THERE","FOREWARD STARBOARD","FOREWARD PORT","AFT STARBOARD","AFT PORT"
	};
	
	
	private static final Object[][] DIRECTIONS_COMPASS_CHART=
	{
		{"UP",Integer.valueOf(UP)},
		{"ABOVE",Integer.valueOf(UP)},
		{"NORTH",Integer.valueOf(NORTH)},
		{"SOUTH",Integer.valueOf(SOUTH)},
		{"EAST",Integer.valueOf(EAST)},
		{"WEST",Integer.valueOf(WEST)},
		{"NORTHEAST",Integer.valueOf(NORTHEAST)},
		{"NORTHWEST",Integer.valueOf(NORTHWEST)},
		{"SOUTHWEST",Integer.valueOf(SOUTHWEST)},
		{"SOUTHEAST",Integer.valueOf(SOUTHEAST)},
		{"NW",Integer.valueOf(NORTHWEST)},
		{"NE",Integer.valueOf(NORTHEAST)},
		{"SW",Integer.valueOf(SOUTHWEST)},
		{"SE",Integer.valueOf(SOUTHEAST)},
		{"DOWN",Integer.valueOf(DOWN)},
		{"BELOW",Integer.valueOf(DOWN)},
		{"NOWHERE",Integer.valueOf(GATE)},
		{"HERE",Integer.valueOf(GATE)},
		{"THERE",Integer.valueOf(GATE)},
		{"VORTEX",Integer.valueOf(GATE)},
	};

	private static final Object[][] DIRECTIONS_SHIP_CHART=
	{
		{"ABOVE",Integer.valueOf(UP)},
		{"FOREWARD",Integer.valueOf(NORTH)},
		{"STARBOARD",Integer.valueOf(EAST)},
		{"PORTSIDE",Integer.valueOf(WEST)},
		{"AFT",Integer.valueOf(SOUTH)},
		{"FORESTARBOARD",Integer.valueOf(NORTHEAST)},
		{"FOREPORT",Integer.valueOf(NORTHWEST)},
		{"AFTPORT",Integer.valueOf(SOUTHWEST)},
		{"AFTSTARBOARD",Integer.valueOf(SOUTHEAST)},
		{"FP",Integer.valueOf(NORTHWEST)},
		{"FS",Integer.valueOf(NORTHEAST)},
		{"AP",Integer.valueOf(SOUTHWEST)},
		{"AS",Integer.valueOf(SOUTHEAST)},
		{"BELOW",Integer.valueOf(DOWN)},
		{"NOWHERE",Integer.valueOf(GATE)},
		{"HERE",Integer.valueOf(GATE)},
		{"THERE",Integer.valueOf(GATE)},
		{"VORTEX",Integer.valueOf(GATE)}
	};

	private static <T> T[] concat(T[] first, T[] second)
	{
		final T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	private static final Object[][] DIRECTIONS_FULL_CHART=concat(DIRECTIONS_COMPASS_CHART, DIRECTIONS_SHIP_CHART);

	/**
	 * Returns the total number of permitted exit directions, either 7 or 11
	 * @return the total number of permitted exit directions, either 7 or 11
	 */
	public static final int NUM_DIRECTIONS()
	{
		return d().NUM_DIRECTIONS;
	}

	/**
	 * The BASE direction code numbers, in numeric order.  Typically 4 or 8
	 * @return BASE direction code numbers, in numeric order
	 */
	public static final int[] CODES()
	{
		return d().DIRECTIONS_CODES;
	}

	/**
	 * The direction code numbers in preferred display order.  Typically 7 or 11.
	 * @return direction code numbers in preferred display order.  Typically 7 or 11.
	 */
	public static final int[] DISPLAY_CODES()
	{
		return d().DIRECTIONS_DISPLAY;
	}

	/**
	 * Returns a string list of all of the permitted direction letters. Either 7 or 11.
	 * @return a string list of all of the permitted direction letters. Either 7 or 11.
	 */
	public static final String LETTERS()
	{
		return L(d().DIRECTION_LETTERS);
	}

	/**
	 * Returns a string list of all of the permitted direction names. Either 6 or 10.
	 * @return a string list of all of the permitted direction names. Either 6 or 10.
	 */
	public static final String NAMES_LIST()
	{
		return L(d().DIRECTION_NAMES);
	}

	/**
	 * Returns a string list of all of the permitted direction names, in ship-talk. Either 6 or 10.
	 * @return a string list of all of the permitted direction names, in ship-talk. Either 6 or 10.
	 */
	public static final String SHIP_NAMES_LIST()
	{
		return L(d().DIRECTION_SHIPNAMES);
	}

	/**
	 * Returns the formal direction name of the partial direction given.
	 * @param theDir the partial direction name, case insensitive
	 * @return the formal direction name
	 */
	public static final String getDirectionName(final String theDir)
	{
		return getDirectionName(getDirectionCode(theDir));
	}

	/**
	 * Reinitializes this direction object with a new number-of-directions.
	 * @param dirs the number of directions, either 7 or 11
	 */
	public final void reInitialize(final int dirs)
	{
		NUM_DIRECTIONS=dirs;
		if(dirs<11)
		{
			DIRECTIONS_CODES=DIRECTIONS_7_BASE;
			DIRECTION_LETTERS=DIRECTION_7_LETTERS;
			DIRECTION_NAMES=DIRECTION_7_NAMES;
			DIRECTION_SHIPNAMES=DIRECTION_7_SHIPNAMES;
		}
		else
		{
			DIRECTIONS_CODES=DIRECTIONS_11_BASE;
			DIRECTION_LETTERS=DIRECTION_11_LETTERS;
			DIRECTION_NAMES=DIRECTION_11_NAMES;
			DIRECTION_SHIPNAMES=DIRECTION_11_SHIPNAMES;
		}
		DIRECTIONS_DISPLAY = new int[100];
		int index=0;
		for(int i=0;i<Directions.DIRECTIONS_DISP_ORDER.length;i++)
		{
			if(Directions.DIRECTIONS_DISP_ORDER[i] >= 0)
			{
				DIRECTIONS_DISPLAY[index++] = Directions.DIRECTIONS_DISP_ORDER[i];
			}
			else
			{
				for(int dir : DIRECTIONS_CODES)
				{
					DIRECTIONS_DISPLAY[index++]= dir;
				}
			}
		}
		DIRECTIONS_DISPLAY = Arrays.copyOf(DIRECTIONS_DISPLAY, index);
	}

	/**
	 * Given the direction code, returns the formal name of that direction, capitalized.
	 * @param code the direction code
	 * @return the name of that direction, capitalized
	 */
	public static final String getDirectionName(final int code)
	{
		switch(code)
		{
			case NORTH:
				return L("North");
			case SOUTH:
				return L("South");
			case EAST:
				return L("East");
			case WEST:
				return L("West");
			case UP:
				return L("Up");
			case DOWN:
				return L("Down");
			case GATE:
				return L("There");
			case NORTHEAST:
				return L("Northeast");
			case NORTHWEST:
				return L("Northwest");
			case SOUTHEAST:
				return L("Southeast");
			case SOUTHWEST:
				return L("Southwest");
		}
		return "";
	}


	/**
	 * Given the direction code, returns the formal name of that direction, capitalized.
	 * @param code the direction code
	 * @return the name of that direction, capitalized
	 */
	public static final String getUpperDirectionName(final int code, boolean useShip)
	{
		if((code>=0)&&(code<NUM_DIRECTIONS()))
		{
			if(useShip)
				return DIRECTIONS_COMPASS_UPPER_INDEXED[code];
			else
				return DIRECTIONS_SHIP_UPPER_INDEXED[code];
		}
		return "";
	}
	
	/**
	 * Given the direction code, returns the ship-talk name of that direction, capitalized.
	 * @param code the direction code
	 * @return the ship-talk name of that direction, capitalized
	 */
	public static final String getShipDirectionName(final int code)
	{
		switch(code)
		{
			case NORTH:
				return L("Foreward");
			case SOUTH:
				return L("Aft");
			case EAST:
				return L("Starboard");
			case WEST:
				return L("Portside");
			case UP:
				return L("Above");
			case DOWN:
				return L("Below");
			case GATE:
				return L("There");
			case NORTHEAST:
				return L("Fore Starboard");
			case NORTHWEST:
				return L("Fore Portside");
			case SOUTHEAST:
				return L("Aft Starboard");
			case SOUTHWEST:
				return L("Aft Portside");
		}
		return "";
	}

	/**
	 * Given the numeric direction code, this method returns the 1 or 2 char
	 * "code" that represents that direction, such as N, S, E, W, NW, SW, etc..
	 * @param code the direction code
	 * @return the direction character or characters
	 */
	public static final String getDirectionChar(final int code)
	{
		if(code<NUM_DIRECTIONS())
			return DIRECTION_CHARS[code];
		return " ";
	}

	/**
	 * Given a string which is supposed to be a direction name of any type,
	 * this method will make a case-insensitive check against the given
	 * string and return the direction it probably represents.  It gives
	 * preference to actual direction names, such as north, aft, below, up, but
	 * if all fail, it will try prefixes, such as nor, por, or just N, S, etc.
	 * @param theDir the direction search string
	 * @return the direction code it represents, or -1 if no match at ALL
	 */
	public static final int getDirectionCode(final String theDir)
	{
		final int code=getGoodDirectionCode(theDir);
		if(code<0)
		{
			final String upDir=theDir.toUpperCase();
			for(int i=0;i<NUM_DIRECTIONS();i++)
			{
				if(upDir.startsWith(DIRECTION_CHARS[i]))
					return i;
			}
		}
		return code;
	}

	/**
	 * Given a string which is technically supposed to be a ship-talk direction name,
	 * this method will make a case-insensitive check against the given
	 * string and return the direction it probably represents.  It gives
	 * preference to actual direction names, such as port and aft, but
	 * if all fail, it will try prefixes, such as por, for, etc.
	 * @param theDir the ship-talk direction search string
	 * @return the direction code it represents, or -1 if no match at ALL
	 */
	public static final int getShipDirectionCode(final String theDir)
	{
		return getGoodShipDirectionCode(theDir);
	}

	/**
	 * Given a string which is supposed to be a compass direction name,
	 * this method will make a case-insensitive check against the given
	 * string and return the direction it probably represents.  It gives
	 * preference to actual direction names, such as north, south, etc, but
	 * if all fail, it will try prefixes, such as nor, sou, or just N, S, etc.
	 * @param theDir the direction search string
	 * @return the direction code it represents, or -1 if no match at ALL
	 */
	public static final int getCompassDirectionCode(final String theDir)
	{
		final int code=getGoodCompassDirectionCode(theDir);
		if(code<0)
		{
			final String upDir=theDir.toUpperCase();
			for(int i=0;i<NUM_DIRECTIONS();i++)
			{
				if(upDir.startsWith(DIRECTION_CHARS[i]))
					return i;
			}
		}
		return code;
	}

	/**
	 * Given a string which is supposed to be a direction name of any type,
	 * this method will make a case-insensitive check against the given
	 * string and return the direction it probably represents.  It gives
	 * preference to actual direction names, such as north, aft, below, up, but
	 * if all fail, it will try prefixes, such as nor, por, etc.
	 * @param theDir the direction search string
	 * @return the direction code it represents, or -1 if no match at ALL
	 */
	public static final int getGoodDirectionCode(final String theDir)
	{
		if(theDir.length()==0)
			return -1;
		final String upDir=theDir.toUpperCase();
		for (final Object[] element : DIRECTIONS_FULL_CHART)
		{
			if((element[0].toString().startsWith(upDir))
			&&(((Integer)element[1]).intValue()<NUM_DIRECTIONS()))
				return ((Integer)element[1]).intValue();
		}
		return -1;
	}

	/**
	 * Given a string which is supposed to be a compass direction name,
	 * this method will make a case-insensitive check against the given
	 * string and return the direction it probably represents.  It gives
	 * preference to actual direction names, such as north, south, etc, but
	 * if all fail, it will try prefixes, such as nor, sou, etc.
	 * @param theDir the direction search string
	 * @return the direction code it represents, or -1 if no match at ALL
	 */
	public static final int getGoodCompassDirectionCode(final String theDir)
	{
		if(theDir.length()==0)
			return -1;
		final String upDir=theDir.toUpperCase();
		for (final Object[] element : DIRECTIONS_COMPASS_CHART)
		{
			if((element[0].toString().startsWith(upDir))
			&&(((Integer)element[1]).intValue()<NUM_DIRECTIONS()))
				return ((Integer)element[1]).intValue();
		}
		return -1;
	}

	/**
	 * Given a string which is technically supposed to be a ship-talk direction name,
	 * this method will make a case-insensitive check against the given
	 * string and return the direction it probably represents.  It gives
	 * preference to actual direction names, such as port and aft, but
	 * if all fail, it will try prefixes, such as por, for, etc.
	 * @param theDir the ship-talk direction search string
	 * @return the direction code it represents, or -1 if no match at ALL
	 */
	public static final int getGoodShipDirectionCode(final String theDir)
	{
		if(theDir.length()==0)
			return -1;
		final String upDir=theDir.toUpperCase();
		for (final Object[] element : DIRECTIONS_SHIP_CHART)
		{
			if((element[0].toString().startsWith(upDir))
			&&(((Integer)element[1]).intValue()<NUM_DIRECTIONS()))
				return ((Integer)element[1]).intValue();
		}
		return -1;
	}

	/**
	 * Given an x and y coordinate, and a direction code, this method will return
	 * an int array with 2 entries x, and y, representing the changes to the given
	 * x and y after moving in that direction.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param direction the direction code being travelled
	 * @return the 2 dimensional int array with the new x and y
	 */
	public static final int[] adjustXYByDirections(int x, int y, final int direction)
	{
		switch(direction)
		{
		case Directions.NORTH:
			y--;
			break;
		case Directions.SOUTH:
			y++;
			break;
		case Directions.EAST:
			x++;
			break;
		case Directions.WEST:
			x--;
			break;
		case Directions.NORTHEAST:
			x++;
			y--;
			break;
		case Directions.NORTHWEST:
			x--;
			y--;
			break;
		case Directions.SOUTHEAST:
			x++;
			y++;
			break;
		case Directions.SOUTHWEST:
			x--;
			y++;
			break;
		}
		final int[] xy=new int[2];
		xy[0]=x;
		xy[1]=y;
		return xy;
	}

	/**
	 * Returns the proper english compass direction name to follow the preposition 
	 * "from" when talking about something or someone coming FROM the given direction 
	 * code.  Completes the following sentence: "Joe arrived from ..."
	 * @param code the direction code
	 * @return the name of the direction phrase
	 */
	public static final String getFromCompassDirectionName(final int code)
	{
		switch(code)
		{
		case NORTH:
			return L("the north");
		case SOUTH:
			return L("the south");
		case EAST:
			return L("the east");
		case WEST:
			return L("the west");
		case UP:
			return L("above");
		case DOWN:
			return L("below");
		case GATE:
			return L("out of nowhere");
		case NORTHEAST:
			return L("the northeast");
		case NORTHWEST:
			return L("the northwest");
		case SOUTHEAST:
			return L("the southeast");
		case SOUTHWEST:
			return L("the southwest");
		}
		return "";
	}

	/**
	 * Returns the proper english compass direction name to relative to somewhere else. 
	 * @param code the direction code
	 * @return the name of the direction phrase
	 */
	public static final String getRelativeCompassDirectionName(final int code)
	{
		switch(code)
		{
		case NORTH:
			return L("north of");
		case SOUTH:
			return L("south of");
		case EAST:
			return L("east of");
		case WEST:
			return L("west of");
		case UP:
			return L("above");
		case DOWN:
			return L("below");
		case GATE:
			return "";
		case NORTHEAST:
			return L("northeast of");
		case NORTHWEST:
			return L("northwest of");
		case SOUTHEAST:
			return L("southeast of");
		case SOUTHWEST:
			return L("southwest of");
		}
		return "";
	}
	/**
	 * Returns the proper english ship direction name to follow the preposition 
	 * "from" when talking about something or someone coming FROM the given direction 
	 * code on a ship.  Completes the following sentence: "Joe arrived from ..."
	 * @param code the direction code
	 * @return the name of the direction phrase
	 */
	public static final String getFromShipDirectionName(final int code)
	{
		switch(code)
		{
		case NORTH:
			return L("foreward");
		case SOUTH:
			return L("aft");
		case EAST:
			return L("starboard");
		case WEST:
			return L("portside");
		case UP:
			return L("above");
		case DOWN:
			return L("below");
		case GATE:
			return L("out of nowhere");
		case NORTHEAST:
			return L("forward starboard");
		case NORTHWEST:
			return L("forward portside");
		case SOUTHEAST:
			return L("aft starboard");
		case SOUTHWEST:
			return L("aft portside");
		}
		return "";
	}

	/**
	 * Returns the proper english compass direction name to follow the preposition 
	 * "happens" when talking about something happening in the given direction 
	 * code.  Completes the following sentence: "You hear something happen ..."
	 * Usually begins with "to the", such as "to the north", "to the northeast", etc.
	 * @param code the direction code the direction the thing is happening in
	 * @return the name of the direction completion phrase
	 */
	public static final String getInDirectionName(final int code)
	{
		switch(code)
		{
		case NORTH:
			return L("to the north");
		case SOUTH:
			return L("to the south");
		case EAST:
			return L("to the east");
		case WEST:
			return L("to the west");
		case NORTHEAST:
			return L("to the northeast");
		case NORTHWEST:
			return L("to the northwest");
		case SOUTHEAST:
			return L("to the southeast");
		case SOUTHWEST:
			return L("to the southwest");
		case UP:
			return L("above you");
		case DOWN:
			return L("below");
		case GATE:
			return L("there");
		}
		return "";
	}

	/**
	 * Returns the proper english ship-talk direction name to follow the preposition 
	 * "happens" when talking about something happening in the given direction 
	 * code on a ship.  Completes the following sentence: "You hear something happen ..."
	 * Usually begins with "to", such as "to foreward", "to portside", etc.
	 * @param code the direction code the direction the thing is happening in
	 * @return the name of the direction completion phrase
	 */
	public static final String getShipInDirectionName(final int code)
	{
		switch(code)
		{
		case NORTH:
			return L("to foreward");
		case SOUTH:
			return L("to aft");
		case EAST:
			return L("to starboard");
		case WEST:
			return L("to portside");
		case NORTHEAST:
			return L("to forward starboard");
		case NORTHWEST:
			return L("to forward port");
		case SOUTHEAST:
			return L("to aft starboard");
		case SOUTHWEST:
			return L("to aft portside");
		case UP:
			return L("above you");
		case DOWN:
			return L("below");
		case GATE:
			return L("there");
		}
		return "";
	}

	/**
	 * Returns the direction code opposite to the given code.
	 * E.G. Returns North for South, Northwest for Southeast, etc.
	 * @param code the direction code
	 * @return the opposite direction code
	 */
	public static final int getOpDirectionCode(final int code)
	{
		switch(code)
		{
		case NORTH:
			return SOUTH;
		case SOUTH:
			return NORTH;
		case WEST:
			return EAST;
		case EAST:
			return WEST;
		case NORTHEAST:
			return SOUTHWEST;
		case NORTHWEST:
			return SOUTHEAST;
		case SOUTHEAST:
			return NORTHWEST;
		case SOUTHWEST:
			return NORTHEAST;
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		case GATE:
			return GATE;
		}
		return -1;
	}

	/**
	 * Returns the direction code next to the given curentCode that
	 * moves towards the target direction code
	 * @param curentCode the current direction code
	 * @param targetCode the target direction code
	 * @return the next direction code
	 */
	public static final int getGradualDirectionCode(final int curentCode, final int targetCode)
	{
		final boolean elevenDirections = Directions.NUM_DIRECTIONS() == 11;
		switch(curentCode)
		{
		case NORTH:
			switch(targetCode)
			{
			case NORTH:
				return NORTH;
			case SOUTH:
				return elevenDirections ? NORTHEAST : EAST;
			case WEST:
				return elevenDirections ? NORTHWEST : WEST;
			case EAST:
				return elevenDirections ? NORTHEAST : EAST;
			case NORTHEAST:
				return NORTHEAST;
			case NORTHWEST:
				return NORTHWEST;
			case SOUTHEAST:
				return NORTHEAST;
			case SOUTHWEST:
				return NORTHWEST;
			default:
				break;
			}
			return targetCode;
		case SOUTH:
			switch(targetCode)
			{
			case NORTH:
				return elevenDirections ? SOUTHWEST : WEST;
			case SOUTH:
				return SOUTH;
			case WEST:
				return elevenDirections ? SOUTHWEST : WEST;
			case EAST:
				return elevenDirections ? SOUTHEAST : EAST;
			case NORTHEAST:
				return SOUTHEAST;
			case NORTHWEST:
				return SOUTHWEST;
			case SOUTHEAST:
				return SOUTHEAST;
			case SOUTHWEST:
				return SOUTHWEST;
			default:
				break;
			}
			return targetCode;
		case WEST:
			switch(targetCode)
			{
			case NORTH:
				return elevenDirections ? NORTHWEST : NORTH;
			case SOUTH:
				return elevenDirections ? SOUTHWEST : SOUTH;
			case WEST:
				return WEST;
			case EAST:
				return elevenDirections ? NORTHWEST : NORTH;
			case NORTHEAST:
				return elevenDirections ? NORTHWEST : NORTH;
			case NORTHWEST:
				return NORTHWEST;
			case SOUTHEAST:
				return elevenDirections ? SOUTHWEST : SOUTH;
			case SOUTHWEST:
				return SOUTHWEST;
			default:
				break;
			}
			return targetCode;
		case EAST:
			switch(targetCode)
			{
			case NORTH:
				return elevenDirections ? NORTHEAST : NORTH;
			case SOUTH:
				return elevenDirections ? SOUTHEAST : SOUTH;
			case WEST:
				return elevenDirections ? SOUTHEAST : SOUTH;
			case EAST:
				return EAST;
			case NORTHEAST:
				return NORTHEAST;
			case NORTHWEST:
				return elevenDirections ? NORTHEAST : NORTH;
			case SOUTHEAST:
				return SOUTHEAST;
			case SOUTHWEST:
				return elevenDirections ? SOUTHEAST : SOUTH;
			default:
				break;
			}
			return targetCode;
		case NORTHEAST:
			switch(targetCode)
			{
			case NORTH:
				return NORTH;
			case SOUTH:
				return EAST;
			case WEST:
				return NORTH;
			case EAST:
				return EAST;
			case NORTHEAST:
				return NORTHEAST;
			case NORTHWEST:
				return NORTH;
			case SOUTHEAST:
				return EAST;
			case SOUTHWEST:
				return EAST;
			default:
				break;
			}
			return targetCode;
		case NORTHWEST:
			switch(targetCode)
			{
			case NORTH:
				return NORTH;
			case SOUTH:
				return WEST;
			case WEST:
				return WEST;
			case EAST:
				return NORTH;
			case NORTHEAST:
				return NORTH;
			case NORTHWEST:
				return NORTHWEST;
			case SOUTHEAST:
				return NORTH;
			case SOUTHWEST:
				return EAST;
			default:
				break;
			}
			return targetCode;
		case SOUTHEAST:
			switch(targetCode)
			{
			case NORTH:
				return EAST;
			case SOUTH:
				return SOUTH;
			case WEST:
				return SOUTH;
			case EAST:
				return EAST;
			case NORTHEAST:
				return EAST;
			case NORTHWEST:
				return NORTH;
			case SOUTHEAST:
				return SOUTHEAST;
			case SOUTHWEST:
				return SOUTH;
			default:
				break;
			}
			return targetCode;
		case SOUTHWEST:
			switch(targetCode)
			{
			case NORTH:
				return WEST;
			case SOUTH:
				return SOUTH;
			case WEST:
				return WEST;
			case EAST:
				return SOUTH;
			case NORTHEAST:
				return WEST;
			case NORTHWEST:
				return WEST;
			case SOUTHEAST:
				return SOUTH;
			case SOUTHWEST:
				return SOUTHWEST;
			default:
				break;
			}
			return targetCode;
		case UP:
			return targetCode;
		case DOWN:
			return targetCode;
		case GATE:
			return targetCode;
		}
		return -1;
	}

	/**
	 * Given a string which is supposed to be a direction name of any type,
	 * this method will make a case-insensitive check against the given
	 * string and return the direction opposite to what it probably represents.  It gives
	 * preference to actual direction names, such as north, aft, below, up, but
	 * if all fail, it will try prefixes, such as nor, por, or just N, S, etc.
	 * @param theDir the direction search string
	 * @return the direction code opposite to the one it represents, or -1 if no match at ALL
	 */
	public static final int getOpDirectionCode(final String theDir)
	{
		final int code=getDirectionCode(theDir);
		return getOpDirectionCode(code);
	}

	/**
	 * Returns the cardinal direction from the FROM point on a graph to the
	 * TO point, where the points are X, then Y, with the northwest corner
	 * being 0,0.
	 * @param xyFrom the starting point
	 * @param xyTo the target point
	 * @return the direction from the start to target
	 */
	public static final int getRelativeDirection(int[] xyFrom, int[] xyTo)
	{
		return getRelativeDirection(xyFrom,xyTo,Directions.NUM_DIRECTIONS()!=11);
	}
	
	/**
	 * Returns the cardinal direction from the FROM point on a graph to the
	 * TO point, where the points are X, then Y, with the northwest corner
	 * being 0,0.
	 * @param xyFrom the starting point
	 * @param xyTo the target point
	 * @param useBase4 true to use NSEW, false to use NORTHEAST, etc.
	 * @return the direction from the start to target
	 */
	public static final int getRelativeDirection(int[] xyFrom, int[] xyTo, boolean useBase4)
	{
		double x =  xyTo[0] - xyFrom[0];
		double y =  xyTo[1] - xyFrom[1];
		double rads = Math.atan2(x,y);
		int dir= (int)Math.round(rads * 180.0 / Math.PI);
		if(useBase4)
		{
			if((dir < -135)||(dir > 135))
				return Directions.NORTH;
			else
			if(dir < -45)
				return Directions.WEST;
			else
			if(dir > 45)
				return Directions.EAST;
			else
				return Directions.SOUTH;
		}
		else
		{
			if((dir < -157)||(dir > 157))
				return Directions.NORTH;
			else
			if(dir < -112)
				return Directions.NORTHWEST;
			else
			if(dir < -67)
				return Directions.WEST;
			else
			if(dir < -22)
				return Directions.SOUTHWEST;
			else
			if(dir > 112)
				return Directions.NORTHEAST;
			else
			if(dir > 67)
				return Directions.EAST;
			else
			if(dir > 22)
				return Directions.SOUTHEAST;
			else
				return Directions.SOUTH;
		}
	}
	
	
	/**
	 * An optional localization pass-through for direction names.
	 * @param str the string to localize
	 * @param xs any variables
	 * @return the localized string
	 */
	@SuppressWarnings("static-access")
	private static final String L(final String str, final String ... xs)
	{
		final LanguageLibrary lib = CMLib.initialize().lang();
		if(lib != null)
			return lib.L(str, xs);
		return str;
	}

}
