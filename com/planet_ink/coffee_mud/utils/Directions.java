package com.planet_ink.coffee_mud.utils;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
	public final static int NUM_DIRECTIONS=7;
	
	public static final int NORTH=0;
	public static final int SOUTH=1;
	public static final int EAST=2;
	public static final int WEST=3;
	public static final int UP=4;
	public static final int DOWN=5;
	
	public static final int GATE=NUM_DIRECTIONS-1;

											   
	public static String getDirectionName(String theDir)
	{
		return getDirectionName(getDirectionCode(theDir));
	}
	
	public static String getDirectionName(int code)
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
		}
		return "";
	}

	public static char getDirectionChar(int code)
	{
		if(code<NUM_DIRECTIONS)
			return ("NSEWUDV").charAt(code);
		return ' ';
	}

	public static int getDirectionCode(char theChar)
	{
		int code=("NSEWUDV").indexOf(Character.toUpperCase(theChar));
		return code;
	}
	
	public static int getDirectionCode(String theDir)
	{
		int code=-1;
		if (theDir.length()>0)
			code=("NSEWUDV").indexOf(Character.toUpperCase(theDir.charAt(0)));
		return code;
	}
	
	public static int getGoodDirectionCode(String theDir)
	{
		int code=-1;
		if(theDir.length()==0) 
			return code;
		else
		if(("UP").startsWith(theDir.toUpperCase()))
			return UP;
		else
		if(("ABOVE").startsWith(theDir.toUpperCase()))
			return UP;
		else
		if(("NORTH").startsWith(theDir.toUpperCase()))
		   return NORTH;
		else
		if(("SOUTH").startsWith(theDir.toUpperCase()))
		   return SOUTH;
		else
		if(("EAST").startsWith(theDir.toUpperCase()))
		   return EAST;
		else
		if(("WEST").startsWith(theDir.toUpperCase()))
		   return WEST;
		else
		if(("DOWN").startsWith(theDir.toUpperCase()))
		   return DOWN;
		else
		if(("BELOW").startsWith(theDir.toUpperCase()))
		   return DOWN;
		else
		if(("NOWHERE").startsWith(theDir.toUpperCase()))
		   return GATE;
		else
		if(("HERE").startsWith(theDir.toUpperCase()))
		   return GATE;
		else
		if(("THERE").startsWith(theDir.toUpperCase()))
		   return GATE;
		else
		if(("VORTEX").startsWith(theDir.toUpperCase()))
		   return GATE;
		
		return code;
	}
	
	public static int[] adjustXYByDirections(int x, int y, int direction)
	{
		switch(direction)
		{
		case Directions.NORTH: y--; break;
		case Directions.SOUTH: y++; break;
		case Directions.EAST: x++; break;
		case Directions.WEST: x--; break;
		}
		int[] xy=new int[2];
		xy[0]=x;
		xy[1]=y;
		return xy;
	}
	

	public static String getFromDirectionName(String theDir)
	{
		int code=getDirectionCode(theDir);
		return getFromDirectionName(code);
	}
	
	public static String getFromDirectionName(int code)
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
		}
		return "";
	}
	
	public static String getInDirectionName(int code)
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
		case UP:
			return "above you";
		case DOWN:
			return "below";
		case GATE:
			return "there";
		}
		return "";
	}
	
	public static int getOpDirectionCode(int code)
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
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		case GATE:
			return GATE;
		}
		return -1;
	}
	public static int getOpDirectionCode(String theDir)
	{
		int code=getDirectionCode(theDir);
		return getOpDirectionCode(code);
	}
	
}
