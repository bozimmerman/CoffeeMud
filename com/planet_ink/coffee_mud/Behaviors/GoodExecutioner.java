package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class GoodExecutioner  extends StdBehavior
{
	public String ID(){return "GoodExecutioner";}
	public long flags(){return Behavior.FLAG_POTENTIALLYAGGRESSIVE;}
	public Behavior newInstance()
	{
		return new GoodExecutioner();
	}

	public boolean grantsAggressivenessTo(MOB M)
	{
		return (((M.getAlignment()<350)&&(M.isMonster()))
		||((M.baseCharStats().getCurrentClass().baseClass().equalsIgnoreCase("Thief"))
		   &&(M.isMonster())));
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting)) return;
		MOB observer=(MOB)affecting;
		// base 90% chance not to be executed
		if(((source.getAlignment()<350)&&(source.isMonster()))
		||((source.baseCharStats().getCurrentClass().baseClass().equalsIgnoreCase("Thief"))
		   &&(source.isMonster())))
		{
			String reason="EVIL";
			if(source.baseCharStats().getCurrentClass().baseClass().equalsIgnoreCase("Thief"))
				reason="A THIEF";
			MOB oldFollowing=source.amFollowing();
			source.setFollowing(null);
			boolean yep=Aggressive.startFight(observer,source,true);
			if(yep)
				CommonMsgs.say(observer,null,source.name().toUpperCase()+" IS "+reason+", AND MUST BE DESTROYED!",false,false);
			else
			if(oldFollowing!=null)
				source.setFollowing(oldFollowing);
		}
	}
}