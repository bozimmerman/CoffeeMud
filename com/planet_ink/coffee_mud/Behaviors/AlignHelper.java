package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class AlignHelper extends StdBehavior
{
	public String ID(){return "AlignHelper";}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB observer=(MOB)affecting;
		if(msg.target()==null)
			return;
		if(!(msg.target() instanceof MOB))
			return;
		MOB target=(MOB)msg.target();

		if((source!=observer)
		&&(target!=observer)
		&&(source!=target)
		&&(Sense.canBeSeenBy(source,observer))
		&&(Sense.canBeSeenBy(target,observer))
		&&(!BrotherHelper.isBrother(source,observer))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(CommonStrings.shortAlignmentStr(target.getAlignment()).equals(CommonStrings.shortAlignmentStr(observer.getAlignment()))))
		{
			boolean yep=Aggressive.startFight(observer,source,true);
			if(yep)	CommonMsgs.say(observer,null,CommonStrings.shortAlignmentStr(observer.getAlignment()).toUpperCase()+" PEOPLE UNITE! CHARGE!",false,false);
		}
	}
}
