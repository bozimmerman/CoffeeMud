package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CombatAssister extends StdBehavior
{
	public String ID(){return "CombatAssister";}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		MOB mob=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB monster=(MOB)affecting;
		if(msg.target()==null)
			return;
		if(!(msg.target() instanceof MOB))
			return;
		MOB target=(MOB)msg.target();

		if((mob!=monster)
		&&(target!=monster)
		&&(mob!=target)
		&&(Sense.canBeSeenBy(mob,monster))
		&&(Sense.canBeSeenBy(target,monster))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(MUDZapper.zapperCheck(getParms(),target)))
			Aggressive.startFight(monster,mob,true);
	}
}
