package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClanHelper extends StdBehavior
{

	public ClanHelper()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{
		return new ClanHelper();
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
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(observer.charStats().getMyRace().ID().equals(target.charStats().getMyRace().ID()))
		&&((!observer.isGeneric())||(!target.isGeneric())||(observer.name().equals(target.name()))))
		{
			boolean yep=Aggressive.startFight(observer,source,false);
			String reason="DON'T HURT MY FRIEND!";
			if(observer.charStats().getMyRace().ID().equals(target.charStats().getMyRace().ID()))
				reason=observer.charStats().getMyRace().ID().toUpperCase()+"s UNITE! CHARGE!";
			if(yep)	ExternalPlay.quickSay(observer,null,reason,false,false);
		}
	}
}
