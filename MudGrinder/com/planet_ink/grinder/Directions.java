package com.planet_ink.grinder;

public class Directions
{
	public final static int NUM_DIRECTIONS=6;
	
	public static final int NORTH=0;
	public static final int SOUTH=1;
	public static final int EAST=2;
	public static final int WEST=3;
	public static final int UP=4;
	public static final int DOWN=5;

											   
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
		}
		return "";
	}

	public static char getDirectionChar(int code)
	{
		if(code<NUM_DIRECTIONS)
			return ("NSEWUD").charAt(code);
		return ' ';
	}

	public static int getDirectionCode(char theChar)
	{
		int code=("NSEWUD").indexOf(Character.toUpperCase(theChar));
		return code;
	}
	
	public static int getDirectionCode(String theDir)
	{
		int code=-1;
		if (theDir.length()>0)
			code=("NSEWUD").indexOf(Character.toUpperCase(theDir.charAt(0)));
		return code;
	}
	
	

	public static int getGoodDirectionCode(String theDir)
	{
		int code=-1;
		if(("UP").startsWith(theDir.toUpperCase()))
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
		
		return code;
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
		}
		return -1;
	}
	public static int getOpDirectionCode(String theDir)
	{
		int code=getDirectionCode(theDir);
		return getOpDirectionCode(code);
	}
	
}
