package com.planet_ink.coffee_mud.interfaces;

import java.util.*;

/**
 * something that is affected by, or affects
 * the environment around them.
 */
public interface Environmental extends Cloneable, Tickable, StatsAffecting, MsgListener
{
	/** General descriptive ID for this
	 * object.	Includes everything from
	 * an item ID (Dagger, etc), to a particular
	 * users name "Bob the Avenger" */
	public String ID();
	public String name();
	public void setName(String newName);
	public String displayText();
	public void setDisplayText(String newDisplayText);
	public Environmental copyOf();
	public boolean isGeneric();

	/** For internal use by items. This text
	 * is saved for each room instance of an item, and
	 * may be used for behavior modification, description
	 * change, or anything else.
	 */
	public void setMiscText(String newMiscText);
	public String text();

	/** Text displayed when this item is LOOKED at*/
	public String description();
	public void setDescription(String newDescription);

	/** return a new instance of the object*/
	public Environmental newInstance();

	/** some general statistics about such an item
	 * see class "EnvStats" for more information. */
	public EnvStats baseEnvStats();
	public EnvStats envStats();
	public void setBaseEnvStats(EnvStats newBaseEnvStats);
	public void recoverEnvStats();

	/** quick and easy access to the basic values in this object */
	public String[] getStatCodes();
	public String getStat(String code);
	public void setStat(String code, String val);
	public boolean sameAs(Environmental E);
		

	/** Manipulation of affect objects, which includes
	 * spells, traits, skills, etc.*/
	public void addAffect(Ability to);
	public void addNonUninvokableAffect(Ability to);
	public void delAffect(Ability to);
	public int numAffects();
	public Ability fetchAffect(int index);
	public Ability fetchAffect(String ID);

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to);
	public void delBehavior(Behavior to);
	public int numBehaviors();
	public Behavior fetchBehavior(int index);
	public Behavior fetchBehavior(String ID);

	/**
	 * Parameters for using in 3 dimensional space
	 */
	public int maxRange();
	public int minRange();
}
