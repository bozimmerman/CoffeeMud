package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/**
 * something that is affected by, or affects
 * the environment around them.
 */
public class StdBehavior implements Behavior
{
	protected String myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	protected String parms="";
	protected boolean mobileType=false;

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
	public void startBehavior(Environmental forMe)
	{

	}

	public boolean grantsMobility(){return mobileType;}
	protected MOB getBehaversMOB(Environmental ticking)
	{
		if(ticking==null) return null;

		if(ticking instanceof MOB)
			return (MOB)ticking;
		else
		if(ticking instanceof Item)
			if(((Item)ticking).owner() != null)
				if(((Item)ticking).owner() instanceof MOB)
					return (MOB)((Item)ticking).owner();

		return null;
	}

	protected Room getBehaversRoom(Environmental ticking)
	{
		if(ticking==null) return null;

		if(ticking instanceof Room)
			return (Room)ticking;

		MOB mob=getBehaversMOB(ticking);
		if(mob!=null)
			return mob.location();

		if(ticking instanceof Item)
			if(((Item)ticking).owner() != null)
				if(((Item)ticking).owner() instanceof Room)
					return (Room)((Item)ticking).owner();

		return null;
	}

	public String getParms(){return parms;}
	public void setParms(String parameters){parms=parameters;}

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

	public static boolean canActAtAll(Environmental affecting)
	{
		if(affecting==null) return false;
		if(!(affecting instanceof MOB)) return false;

		MOB monster=(MOB)affecting;
		if(monster.amDead()) return false;
		if(monster.location()==null) return false;
		if(!Sense.aliveAwakeMobile(monster,true)) return false;
		return true;
	}

	public static boolean canFreelyBehaveNormal(Environmental affecting)
	{
		if(affecting==null) return false;
		if(!(affecting instanceof MOB)) return false;

		MOB monster=(MOB)affecting;
		if(!canActAtAll(monster))
			return false;
		if(monster.isInCombat()) return false;
		if(monster.amFollowing()!=null)  return false;
		if(monster.curState().getHitPoints()<((int)Math.round(monster.maxState().getHitPoints()/2.0)))
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
	}
}
