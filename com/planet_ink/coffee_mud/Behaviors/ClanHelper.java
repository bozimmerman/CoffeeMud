package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClanHelper extends StdBehavior
{
	public String ID(){return "ClanHelper";}
	public Behavior newInstance()
	{
		return new ClanHelper();
	}

	protected boolean mobKiller=false;
	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(forMe instanceof MOB)
		{
			if(parms.length()>0)
			{
				((MOB)forMe).setClanID(parms.trim());
				((MOB)forMe).setClanRole(Clan.POS_MEMBER);
			}
		}
	}

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

		if((target==null)||(observer==null)) return;
		if((source!=observer)
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(target!=observer)
		&&(source!=target)
		&&(Sense.canBeSeenBy(source,observer))
		&&(Sense.canBeSeenBy(target,observer))
		&&(observer.getClanID().length()>0)
		&&(!BrotherHelper.isBrother(source,observer)))
		{
			if(observer.getClanID().equalsIgnoreCase(target.getClanID()))
			{
				boolean yep=Aggressive.startFight(observer,source,true);
				String reason="WE ARE UNDER ATTACK!! CHARGE!!";
				if((observer.getClanID().equals(target.getClanID()))
				&&(!observer.getClanID().equals(source.getClanID())))
					reason=observer.getClanID().toUpperCase()+"S UNITE! CHARGE!";
				if(yep)	ExternalPlay.quickSay(observer,null,reason,false,false);
			}
		}
	}
}
