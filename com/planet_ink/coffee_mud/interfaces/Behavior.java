package com.planet_ink.coffee_mud.interfaces;

import java.util.*;

/**
 * something that is affected by, or affects
 * the environment around them.
 */
public interface Behavior extends Cloneable, Tickable, MsgListener
{
	/** General descriptive ID for this
	 * object.	Includes everything from 
	 * an item ID (Dagger, etc), to a particular
	 * users name "Bob the Avenger" */
	public String ID();
	
	/** return a new instance of the object*/
	public Behavior newInstance();
	public Behavior copyOf();
	
	public void startBehavior(Environmental forMe);
	
	public String getParms();
	public void setParms(String parameters);
	
	public boolean canImprove(Environmental E);
	// improve flag
	public static final int CAN_MOBS=1;
	public static final int CAN_ITEMS=2;
	public static final int CAN_AREAS=4;
	public static final int CAN_ROOMS=8;
	public static final int CAN_EXITS=16;
	
	public boolean grantsMobility();
	public boolean grantsAggressivenessTo(MOB M);
	
	/** quick and easy access to the basic values in this object */
	public String[] getStatCodes();
	public String getStat(String code);
	public void setStat(String code, String val);
	public boolean sameAs(Behavior  B);
}
