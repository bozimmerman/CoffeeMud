package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class BrotherHelper extends StdBehavior
{
	public String ID(){return "BrotherHelper";}
	public Behavior newInstance()
	{
		return new BrotherHelper();
	}
	
	public static boolean isBrother(MOB target, MOB observer)
	{
		if((observer.getStartRoom()!=null)
		&&(observer.getStartRoom()==target.getStartRoom()))
			return true;
		if((observer.ID().equals(target.ID()))
		&&(observer.name().equals(target.name())))
			return true;
		return false;
	}
	
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		MOB source=affect.source();
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB observer=(MOB)affecting;
		if(affect.target()==null)
			return;
		if(!(affect.target() instanceof MOB))
			return;
		MOB target=(MOB)affect.target();

		if((source!=observer)
		&&(target!=observer)
		&&(source!=target)
		&&(Sense.canBeSeenBy(source,observer))
		&&(Sense.canBeSeenBy(target,observer))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(isBrother(target,observer))
		&&(!isBrother(source,observer)))
		{
			boolean yep=Aggressive.startFight(observer,source,false);
			if(yep)	ExternalPlay.quickSay(observer,null,"DON'T HURT MY FRIEND!",false,false);
		}
	}

}
