package com.planet_ink.coffee_mud.interfaces;

public interface SpaceObject extends Environmental
{
	public long[] coordinates();
	public void setCoords(long[] coords);
	public double[] direction();
	public void setDirection(double[] dir);
	public long velocity();
	public void setVelocity(long v);
	
	public SpaceObject knownTarget();
	public void setKnownTarget(SpaceObject O);
	public SpaceObject knownSource();
	public void setKnownSource(SpaceObject O);
	public SpaceObject orbiting();
	public void setOrbiting(SpaceObject O);
	
	public static final long DISTANCE_LIGHTYEAR=30000000;
	public static final long DISTANCE_AROUNDGALAXY=DISTANCE_LIGHTYEAR*(long)10000;
	public static final long DISTANCE_ORBITING=5;
	public static final long DISTANCE_QUADRANT=DISTANCE_LIGHTYEAR*(long)133333;
	
	public static final long VELOCITY_LIGHT=200000;
	public static final long VELOCITY_SUBLIGHT=180000;
	public static final long VELOCITY_SOUND=100;
	public static final long VELOCITY_ORBITING=25000;
	public static final long VELOCITY_1MPH=1;
}
