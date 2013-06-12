package com.planet_ink.coffee_mud.core;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
	public Directions(){
		super();
		char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(dirs[c]==null) dirs[c]=this;
	}
	private static Directions d(){ return dirs[Thread.currentThread().getThreadGroup().getName().charAt(0)];}
	public static Directions d(char c){return dirs[c];}
	public static Directions instance(){
		final Directions d=d();
		if(d==null) return new Directions();
		return d;
	}
	private static final Directions[] dirs=new Directions[256];
	
	private int[] DIRECTIONS_BASE={NORTH,SOUTH,EAST,WEST};
	private String DIRECTIONS_DESC="N, S, E, W, U, D, or V";
	private int NUM_DIRECTIONS=7;

	public static final int NUM_DIRECTIONS(){
		return d().NUM_DIRECTIONS;
	}

	public static final int[] DIRECTIONS_BASE(){
		return d().DIRECTIONS_BASE;
	}
	
	public static final String DIRECTIONS_DESC(){
		return d().DIRECTIONS_DESC;
	}
	
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
	
	public static final String[] DIRECTION_CHARS={"N","S","E","W","U","D","V","NE","NW","SE","SW"};
	public static final String[][] DIRECTIONS_FULL_CHART={
		{"UP",""+UP},
		{"ABOVE",""+UP},
		{"NORTH",""+NORTH},
		{"EAST",""+EAST},
		{"WEST",""+WEST},
		{"SOUTH",""+SOUTH},
		{"NORTHEAST",""+NORTHEAST},
		{"NORTHWEST",""+NORTHWEST},
		{"SOUTHWEST",""+SOUTHWEST},
		{"SOUTHEAST",""+SOUTHEAST},
		{"NW",""+NORTHWEST},
		{"NE",""+NORTHEAST},
		{"SW",""+SOUTHWEST},
		{"SE",""+SOUTHEAST},
		{"DOWN",""+DOWN},
		{"BELOW",""+DOWN},
		{"NOWHERE",""+GATE},
		{"HERE",""+GATE},
		{"THERE",""+GATE},
		{"VORTEX",""+GATE}
	};
											   
	public static final String getDirectionName(final String theDir)
	{
		return getDirectionName(getDirectionCode(theDir));
	}

	public final void reInitialize(final int dirs)
	{
		NUM_DIRECTIONS=dirs;
		if(dirs<11)
		{
			DIRECTIONS_BASE=new int[4];
			DIRECTIONS_BASE[0]=NORTH;
			DIRECTIONS_BASE[1]=SOUTH;
			DIRECTIONS_BASE[2]=EAST;
			DIRECTIONS_BASE[3]=WEST;
			DIRECTIONS_DESC="N, S, E, W, U, D, or V";
		}
		else
		{
			DIRECTIONS_BASE=new int[8];
			DIRECTIONS_BASE[0]=NORTH;
			DIRECTIONS_BASE[1]=SOUTH;
			DIRECTIONS_BASE[2]=EAST;
			DIRECTIONS_BASE[3]=WEST;
			DIRECTIONS_BASE[4]=NORTHEAST;
			DIRECTIONS_BASE[5]=NORTHWEST;
			DIRECTIONS_BASE[6]=SOUTHEAST;
			DIRECTIONS_BASE[7]=SOUTHWEST;
			DIRECTIONS_DESC="N, S, E, W, NE, NW, SE, SW, U, D, or V";
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
	
	public static final int getGoodDirectionCode(final String theDir)
	{
		if(theDir.length()==0) return -1;
		final String upDir=theDir.toUpperCase();
		for(int i=0;i<DIRECTIONS_FULL_CHART.length;i++)
			if((DIRECTIONS_FULL_CHART[i][0].startsWith(upDir))
			&&(CMath.s_int(DIRECTIONS_FULL_CHART[i][1])<NUM_DIRECTIONS()))
				return CMath.s_int(DIRECTIONS_FULL_CHART[i][1]); 
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
	

	public static final String getFromDirectionName(final String theDir)
	{
		final int code=getDirectionCode(theDir);
		return getFromDirectionName(code);
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
		int code=getDirectionCode(theDir);
		return getOpDirectionCode(code);
	}
	
}
