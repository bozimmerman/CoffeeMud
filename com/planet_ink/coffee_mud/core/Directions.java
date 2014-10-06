package com.planet_ink.coffee_mud.core;

import java.util.Arrays;

import com.planet_ink.coffee_mud.Items.interfaces.SpaceShip;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;

/*
   Copyright 2001-2014 Bo Zimmerman

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
	public Directions()
	{
		super();
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(dirs[c]==null) dirs[c]=this;
	}
	private static Directions d(){ return dirs[Thread.currentThread().getThreadGroup().getName().charAt(0)];}
	public static Directions d(char c){return dirs[c];}
	public static Directions instance()
	{
		final Directions d=d();
		if(d==null) return new Directions();
		return d;
	}
	private static final Directions[] dirs=new Directions[256];

	public static final int NORTH=0;
	public static final int SOUTH=1;
	public static final int EAST=2;
	public static final int WEST=3;
	public static final int UP=4;
	public static final int DOWN=5;

	public static final int GATE=6;

	public static final int NORTHEAST=7;
	public static final int NORTHWEST=8;
	public static final int SOUTHEAST=9;
	public static final int SOUTHWEST=10;

	private final static int[] DIRECTIONS_7_BASE={NORTH,SOUTH,EAST,WEST};
	private final static int[] DIRECTIONS_11_BASE={NORTH,SOUTH,EAST,WEST,NORTHEAST,NORTHWEST,SOUTHEAST,SOUTHWEST};
	private final static String DIRECTION_7_LETTERS="N, S, E, W, U, D, or V";
	private final static String DIRECTION_11_LETTERS="N, S, E, W, NE, NW, SE, SW, U, D, or V";
	private final static String DIRECTION_7_NAMES="North, South, East, West, Up, or Down";
	private final static String DIRECTION_11_NAMES="North, South, East, West, Northeast, Northwest, Southeast, Southwest, Up, or Down";
	private final static String DIRECTION_7_SHIPNAMES="Foreward, Aft, Starboard, Port, Above, or Below";
	private final static String DIRECTION_11_SHIPNAMES="Foreward, Aft, Starboard, Port, Foreward Starboard, Foreward Port, Aft Starboard, Aft Port, Above, or Below";

	private int[] DIRECTIONS_CODES={NORTH,SOUTH,EAST,WEST};
	private String DIRECTION_LETTERS=DIRECTION_7_LETTERS;
	private String DIRECTION_NAMES=DIRECTION_7_NAMES;
	private String DIRECTION_SHIPNAMES=DIRECTION_7_SHIPNAMES;

	private int NUM_DIRECTIONS=7;

	public static final String[] DIRECTION_CHARS={"N","S","E","W","U","D","V","NE","NW","SE","SW"};

	public static final Object[][] DIRECTIONS_COMPASS_CHART={
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
	public static final Object[][] DIRECTIONS_SHIP_CHART={
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

	public static final Object[][] DIRECTIONS_FULL_CHART=concat(DIRECTIONS_COMPASS_CHART, DIRECTIONS_SHIP_CHART);

	public static final int NUM_DIRECTIONS()
	{
		return d().NUM_DIRECTIONS;
	}

	public static final int[] CODES()
	{
		return d().DIRECTIONS_CODES;
	}

	public static final String LETTERS()
	{
		return d().DIRECTION_LETTERS;
	}

	public static final String NAMES_LIST()
	{
		return d().DIRECTION_NAMES;
	}

	public static final String SHIP_NAMES_LIST()
	{
		return d().DIRECTION_SHIPNAMES;
	}

	public static final String getDirectionName(final String theDir)
	{
		return getDirectionName(getDirectionCode(theDir));
	}

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
	}

	public static final String getDirectionName(final int code)
	{
		switch(code)
		{
			case NORTH:
				return "North";
			case SOUTH:
				return "South";
			case EAST:
				return "East";
			case WEST:
				return "West";
			case UP:
				return "Up";
			case DOWN:
				return "Down";
			case GATE:
				return "There";
			case NORTHEAST:
				return "Northeast";
			case NORTHWEST:
				return "Northwest";
			case SOUTHEAST:
				return "Southeast";
			case SOUTHWEST:
				return "Southwest";
		}
		return "";
	}

	public static final String getShipDirectionName(final int code)
	{
		switch(code)
		{
			case NORTH:
				return "Foreward";
			case SOUTH:
				return "Aft";
			case EAST:
				return "Starboard";
			case WEST:
				return "Portside";
			case UP:
				return "Above";
			case DOWN:
				return "Below";
			case GATE:
				return "There";
			case NORTHEAST:
				return "Fore Starboard";
			case NORTHWEST:
				return "Fore Portside";
			case SOUTHEAST:
				return "Aft Starboard";
			case SOUTHWEST:
				return "Aft Portside";
		}
		return "";
	}

	public static final String getDirectionChar(final int code)
	{
		if(code<NUM_DIRECTIONS())
			return DIRECTION_CHARS[code];
		return " ";
	}

	public static final int getDirectionCode(final String theDir)
	{
		final int code=getGoodDirectionCode(theDir);
		if(code<0)
		{
			final String upDir=theDir.toUpperCase();
			for(int i=0;i<NUM_DIRECTIONS();i++)
				if(upDir.startsWith(DIRECTION_CHARS[i]))
					return i;
		}
		return code;
	}

	public static final int getShipDirectionCode(final String theDir)
	{
		return getGoodShipDirectionCode(theDir);
	}

	public static final int getCompassDirectionCode(final String theDir)
	{
		final int code=getGoodCompassDirectionCode(theDir);
		if(code<0)
		{
			final String upDir=theDir.toUpperCase();
			for(int i=0;i<NUM_DIRECTIONS();i++)
				if(upDir.startsWith(DIRECTION_CHARS[i]))
					return i;
		}
		return code;
	}

	public static final int getGoodDirectionCode(final String theDir)
	{
		if(theDir.length()==0) return -1;
		final String upDir=theDir.toUpperCase();
		for (final Object[] element : DIRECTIONS_FULL_CHART)
			if((element[0].toString().startsWith(upDir))
			&&(((Integer)element[1]).intValue()<NUM_DIRECTIONS()))
				return ((Integer)element[1]).intValue();
		return -1;
	}

	public static final int getGoodCompassDirectionCode(final String theDir)
	{
		if(theDir.length()==0) return -1;
		final String upDir=theDir.toUpperCase();
		for (final Object[] element : DIRECTIONS_COMPASS_CHART)
			if((element[0].toString().startsWith(upDir))
			&&(((Integer)element[1]).intValue()<NUM_DIRECTIONS()))
				return ((Integer)element[1]).intValue();
		return -1;
	}

	public static final int getGoodShipDirectionCode(final String theDir)
	{
		if(theDir.length()==0) return -1;
		final String upDir=theDir.toUpperCase();
		for (final Object[] element : DIRECTIONS_SHIP_CHART)
			if((element[0].toString().startsWith(upDir))
			&&(((Integer)element[1]).intValue()<NUM_DIRECTIONS()))
				return ((Integer)element[1]).intValue();
		return -1;
	}

	public static final int[] adjustXYByDirections(int x, int y, final int direction)
	{
		switch(direction)
		{
		case Directions.NORTH: y--; break;
		case Directions.SOUTH: y++; break;
		case Directions.EAST: x++; break;
		case Directions.WEST: x--; break;
		case Directions.NORTHEAST: x++; y--; break;
		case Directions.NORTHWEST: x--; y--; break;
		case Directions.SOUTHEAST: x++; y++; break;
		case Directions.SOUTHWEST: x--; y++; break;
		}
		final int[] xy=new int[2];
		xy[0]=x;
		xy[1]=y;
		return xy;
	}


	public static final String getFromDirectionName(final int code)
	{
		switch(code)
		{
		case NORTH:
			return "the north";
		case SOUTH:
			return "the south";
		case EAST:
			return "the east";
		case WEST:
			return "the west";
		case UP:
			return "above";
		case DOWN:
			return "below";
		case GATE:
			return "out of nowhere";
		case NORTHEAST:
			return "the northeast";
		case NORTHWEST:
			return "the northwest";
		case SOUTHEAST:
			return "the southeast";
		case SOUTHWEST:
			return "the southwest";
		}
		return "";
	}

	public static final String getShipFromDirectionName(final int code)
	{
		switch(code)
		{
		case NORTH:
			return "foreward";
		case SOUTH:
			return "aft";
		case EAST:
			return "starboard";
		case WEST:
			return "portside";
		case UP:
			return "above";
		case DOWN:
			return "below";
		case GATE:
			return "out of nowhere";
		case NORTHEAST:
			return "forward starboard";
		case NORTHWEST:
			return "forward portside";
		case SOUTHEAST:
			return "aft starboard";
		case SOUTHWEST:
			return "aft portside";
		}
		return "";
	}

	public static final String getInDirectionName(final int code)
	{
		switch(code)
		{
		case NORTH:
			return "to the north";
		case SOUTH:
			return "to the south";
		case EAST:
			return "to the east";
		case WEST:
			return "to the west";
		case NORTHEAST:
			return "to the northeast";
		case NORTHWEST:
			return "to the northwest";
		case SOUTHEAST:
			return "to the southeast";
		case SOUTHWEST:
			return "to the southwest";
		case UP:
			return "above you";
		case DOWN:
			return "below";
		case GATE:
			return "there";
		}
		return "";
	}

	public static final String getShipInDirectionName(final int code)
	{
		switch(code)
		{
		case NORTH:
			return "to foreward";
		case SOUTH:
			return "to aft";
		case EAST:
			return "to starboard";
		case WEST:
			return "to portside";
		case NORTHEAST:
			return "to forward starboard";
		case NORTHWEST:
			return "to forward port";
		case SOUTHEAST:
			return "to aft starboard";
		case SOUTHWEST:
			return "to aft portside";
		case UP:
			return "above you";
		case DOWN:
			return "below";
		case GATE:
			return "there";
		}
		return "";
	}

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
	public static final int getOpDirectionCode(final String theDir)
	{
		final int code=getDirectionCode(theDir);
		return getOpDirectionCode(code);
	}

}
