package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Guard extends StdBehavior
{
	public String ID(){return "Guard";}
	public Behavior newInstance()
	{
		return new Guard();
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
		&&(Sense.canBeSeenBy(source,observer))
		&&(Sense.canBeSeenBy(target,observer))
		&&(!BrotherHelper.isBrother(source,observer))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS)))
			Aggressive.startFight(observer,source,true);
	}
}