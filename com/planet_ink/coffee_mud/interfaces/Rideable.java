package com.planet_ink.coffee_mud.interfaces;

import java.util.Hashtable;

public interface Rideable extends Environmental
{
	public final static int RIDEABLE_LAND=0;
	public final static int RIDEABLE_WATER=1;
	public final static int RIDEABLE_AIR=2;
	public final static int RIDEABLE_SIT=3;
	public final static int RIDEABLE_SLEEP=4;
	public final static int RIDEABLE_TABLE=5;
	public final static int RIDEABLE_ENTERIN=6;
	public final static int RIDEABLE_LADDER=7;
	public final static String[] RIDEABLE_DESCS=
	{
		"LAND-BASED","WATER-BASED","AIR-FLYING","FURNITURE-SIT","FURNITURE-SLEEP","FURNITURE-TABLE",
		"ENTER-IN","LADDER"
	};
	
	public int rideBasis();
	public void setRideBasis(int basis);
	public int riderCapacity();
	public void setRiderCapacity(int newCapacity);
	public int numRiders();
	public Rider fetchRider(int which);
	public void addRider(Rider mob);
	public void delRider(Rider mob);
	public boolean amRiding(Rider mob);
	public String stateString();
	public String stateStringSubject();
	public boolean mobileRideBasis();
	public String mountString(int commandType);
	public String dismountString();
	public Hashtable getRideBuddies(Hashtable list);
}
