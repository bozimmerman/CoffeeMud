package com.planet_ink.coffee_mud.interfaces;

public interface Rideable extends Environmental
{
	public final static int RIDEABLE_LAND=0;
	public final static int RIDEABLE_WATER=1;
	public final static int RIDEABLE_AIR=2;
	
	public int rideBasis();
	public void setRideBasis(int basis);
	public int mobCapacity();
	public void setMobCapacity(int newCapacity);
	public int numRiders();
	public MOB fetchRider(int which);
	public void addRider(MOB mob);
	public void delRider(MOB mob);
	public boolean amRiding(MOB mob);
}
