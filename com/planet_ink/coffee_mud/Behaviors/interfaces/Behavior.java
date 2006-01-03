package com.planet_ink.coffee_mud.Behaviors.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.Vector;


/* 
   Copyright 2000-2006 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 * something that is affected by, or affects
 * the environment around them.
 */
/**
 * A Behavior is a pro-active modifier of Environmental objects.  Behaviors
 * are expected to do their work in a Tickable.tick(Tickable,int) method which
 * is called periodically by either the host object, or the serviceengine.
 * Behaviors are also message listeners however, and can overlap Ability/properties
 * in that way.
 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental
 * @see com.planet_ink.coffee_mud.core.interfaces.Tickable
 */
public interface Behavior extends Tickable, MsgListener, CMObject
{
	/**
	 * Called after a behavior is added to an Environmental object.
	 * The point is to do any initializing.  This method assumes
	 * setParms() has already been called as well.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#addBehavior(Behavior)
	 * @see Behavior#setParms(String)
	 * @param forMe the object to which this behavior has been added
	 */
	public void startBehavior(Environmental forMe);
	
	/**
	 * Returns the raw parameter string for this behavior.  
	 * Parameters are meant to modify or specify specific behavior of this
	 * Behavior.
	 * @see Behavior#setParms(String)
	 * @return the parameter string for this behavior
	 */
	public String getParms();
	
	/**
	 * Sets the raw parameter string for this behavior.  
	 * Parameters are meant to modify or specify specific behavior of this
	 * Behavior.
	 * @see Behavior#getParms()
	 * @param parameters the parameter string for this behavior
	 */
	public void setParms(String parameters);
	
	/**
	 * Unimplemented as of yet, but will hold a string telling the system what
	 * the proper format of any parms data.  Will use the CMParms.MTFORMAT_*
	 * constants for definition.
	 * @see com.planet_ink.coffee_mud.core.CMParms
	 * @see Behavior#getParms()
	 * @see Behavior#setParms(String)
	 * @return the format expected for the parms field
	 */
	public String parmsFormat();
	
	/**
	 * Returns a string list of any external files which 
	 * may be required to make this ability work.  Usually
	 * derived from the parameters.
	 * 
	 * Files returned by this method should not be base distrib files!
	 * @see Behavior#setParms(String)
	 * @return a list of the path/names of files used by this behavior
	 */
	public Vector externalFiles();
	
	/**
	 * Sets whether this behavior can be saved as a permanent aspect of
	 * its host.
	 * @see Behavior#isSavable()
	 * @param truefalse whether this behavior can be saved as part of its host.
	 */
	
    public void setSavable(boolean truefalse);
	/**
	 * Returns whether this behavior can be saved as a permanent aspect of
	 * its host.
	 * @see Behavior#setSavable(boolean)
	 * @return truefalse whether this behavior can be saved as part of its host.
	 */
    public boolean isSavable();
    
    /**
     * Returns whether this behavior is capable of enhancing the given type
     * of object designated by E.  It derives from the protected 
     * Behavior.canImproveCode() method.  
     * @see com.planet_ink.coffee_mud.Behaviors.StdBehavior#canImproveCode()
     * @param E the object to evaluate for this behavior
     * @return whether the given object can be enhanced by this behavior
     */
	public boolean canImprove(Environmental E);
	
    /**
     * Returns whether this behavior is capable of enhancing the given type
     * of object designated by the can_code.  It derives from the protected 
     * Behavior.canImproveCode() method and uses the Behavior.CAN_* constants.
     * @see com.planet_ink.coffee_mud.Behaviors.StdBehavior#canImproveCode()
     * @see Behavior
     * @param can_code the Behavior.CAN_* mask to evaluate for this behavior
     * @return whether the given type can be enhanced by this behavior
     */
	public boolean canImprove(int can_code);
	
	/**
	 * Returns a bitmap made of up Behavior.FLAG_* constant masks which 
	 * designates certain aspects about this behavior the rest of the 
	 * system may need to know.
	 * @return a bitmap made up of Behavior.FLAG_* constants
	 */
	public long flags();
	
	/**
	 * Returns the result of a very specific test, namely whether this
	 * behavior would be the direct cause of a malicious act against
	 * the given MOB object.
	 * @param M the target to test for maliciousness to
	 * @return whether this behavior would harm the mob
	 */
	public boolean grantsAggressivenessTo(MOB M);
	
	/** 
     * Returns an array of the string names of those fields which are modifiable on this object at run-time by
     * builders.
     * @see Behavior#getStat(String)
     * @see Behavior#setStat(String, String)
     * @return list of the fields which may be set.
     */
	public String[] getStatCodes();
	
    /**
     * An alternative means of retreiving the values of those fields on this object which are modifiable at 
     * run-time by builders.  See getStatCodes() for possible values for the code passed to this method.  
     * Values returned are always strings, even if the field itself is numeric or a list.
     * @see Behavior#getStatCodes()
     * @param code the name of the field to read.
     * @return the value of the field read
     */
	public String getStat(String code);
	
    /**
     * An alternative means of setting the values of those fields on this object which are modifiable at 
     * run-time by builders.  See getStatCodes() for possible values for the code passed to this method.  
     * The value passed in is always a string, even if the field itself is numeric or a list.
     * @see Behavior#getStatCodes()
     * @param code the name of the field to set
     * @param val the value to set the field to
     */
	public void setStat(String code, String val);
	
    /**
     * Whether this object instance is functionally identical to the object passed in.  Works by repeatedly
     * calling getStat on both objects and comparing the values.
     * @see Behavior#getStatCodes()
     * @see Behavior#getStat(String)
     * @param B the object to compare this one to
     * @return whether this object is the same as the one passed in
     */
	public boolean sameAs(Behavior B);
	
	/** constant mask for the canImprove() and canImproveCode() methods.  Means it can improve mobs @see Behavior#canImprove(Environmental) */
	public static final int CAN_MOBS=1;
	/** constant mask for the canImprove() and canImproveCode() methods.  Means it can improve items @see Behavior#canImprove(Environmental) */
	public static final int CAN_ITEMS=2;
	/** constant mask for the canImprove() and canImproveCode() methods.  Means it can improve areas @see Behavior#canImprove(Environmental) */
	public static final int CAN_AREAS=4;
	/** constant mask for the canImprove() and canImproveCode() methods.  Means it can improve rooms @see Behavior#canImprove(Environmental) */
	public static final int CAN_ROOMS=8;
	/** constant mask for the canImprove() and canImproveCode() methods.  Means it can improve exits @see Behavior#canImprove(Environmental) */
	public static final int CAN_EXITS=16;
	
	/** constant mask for the flags() method designating that this behavior makes the host move @see Behavior#flags() */
	public static final long FLAG_MOBILITY=1;
	/** constant mask for the flags() method designating that this behavior makes the host a trouble-maker @see Behavior#flags() */
	public static final long FLAG_TROUBLEMAKING=2;
	/** constant mask for the flags() method designating that this behavior makes the host aggressive @see Behavior#flags() */
	public static final long FLAG_POTENTIALLYAGGRESSIVE=4;
	/** constant mask for the flags() method designating that this behavior makes the host inforce laws @see Behavior#flags() */
	public static final long FLAG_LEGALBEHAVIOR=8;
}
