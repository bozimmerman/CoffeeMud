package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class MOBHelper extends StdBehavior
{
	public String ID(){return "MOBHelper";}
	public Behavior newInstance()
	{
		return new MOBHelper();
	}
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		MOB mob=affect.source();
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB monster=(MOB)affecting;
		if(affect.target()==null)
			return;
		if(!(affect.target() instanceof MOB))
			return;
		MOB target=(MOB)affect.target();

		if((mob!=monster)
		&&(target!=monster)
		&&(mob!=target)
		&&(Sense.canBeSeenBy(mob,monster))
		&&(Sense.canBeSeenBy(target,monster))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(target.isMonster()))
			Aggressive.startFight(monster,mob,true);
	}
}
