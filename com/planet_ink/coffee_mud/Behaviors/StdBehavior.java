package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

/**
 * something that is affected by, or affects
 * the environment around them.
 */
public class StdBehavior implements Behavior
{
	protected String myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	
	/** General descriptive ID for this
	 * object.	Includes everything from 
	 * an item ID (Dagger, etc), to a particular
	 * users name "Bob the Avenger" */
	public String ID(){return myID;}
	
	/** return a new instance of the object*/
	public Behavior newInstance()
	{ 
		return new StdBehavior();
	}
	public Behavior copyOf()
	{
		try
		{
			return (Behavior)this.clone();
		}
		catch(CloneNotSupportedException e)
		{
			return new StdBehavior();
		}
	}
	
	
	
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		return;
	}
	
	/** this method is used to tell the system whether
	 * a PENDING affect may take place
	 */
	public boolean okAffect(Environmental oking, Affect affect)
	{
		return true;
	}
	
	public static boolean canBehave(Environmental affecting)
	{
		if(affecting==null) return false;
		if(!(affecting instanceof MOB)) return false;
		
		MOB monster=(MOB)affecting;
		if(monster.amDead()) return false;
		if(monster.location()==null) return false;
		if(monster.isInCombat()) return false;
		if(monster.amFollowing()!=null)  return false;
		if(!Sense.canPerformAction(monster)) return false;
		if(monster.curState().getHitPoints()<monster.maxState().getHitPoints())
			return false;
		return true;
	}
	
	/**
	 * this method allows any environmental object
	 * to behave according to a timed response.  by
	 * default, it will never be called unless the
	 * object uses the ServiceEngine to setup service.
	 * The tickID allows granularity with the type
	 * of service being requested.
	 */
	public void tick(Environmental ticking, int tickID)
	{
		return;
	}
}
