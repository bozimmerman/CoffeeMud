package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class FaithHelper extends StdBehavior
{
	public String ID(){return "FaithHelper";}
	public Behavior newInstance()
	{
		return new FaithHelper();
	}
	
	public void startBehavior(Environmental forMe)
	{
		startBehavior(forMe);
		if(forMe instanceof MOB)
		{
			if(parms.length()>0)
				((MOB)forMe).setWorshipCharID(parms.trim());
		}
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
		
		if((target==null)||(observer==null)) return;
		if((source!=observer)
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(target!=observer)
		&&(source!=target)
		&&(observer.getWorshipCharID().length()>0)
		&&(Sense.canBeSeenBy(source,observer))
		&&(Sense.canBeSeenBy(target,observer))
		&&(!BrotherHelper.isBrother(source,observer)))
		{
			if(observer.getWorshipCharID().equalsIgnoreCase(target.getWorshipCharID()))
			{
				boolean yep=Aggressive.startFight(observer,source,false);
				String reason="THAT`S MY FRIEND!! CHARGE!!";
				if((observer.getWorshipCharID().equals(target.getWorshipCharID()))
				&&(!observer.getWorshipCharID().equals(source.getWorshipCharID())))
					reason="BELIEVERS OF "+observer.getWorshipCharID().toUpperCase()+" UNITE! CHARGE!";
				if(yep)	ExternalPlay.quickSay(observer,null,reason,false,false);
			}
		}
	}
}
