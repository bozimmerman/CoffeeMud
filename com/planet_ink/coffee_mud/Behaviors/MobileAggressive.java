package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class MobileAggressive extends Mobile
{
	public String ID(){return "MobileAggressive";}
	protected int tickWait=0;
	protected int tickDown=0;
	public long flags(){return Behavior.FLAG_POTENTIALLYAGGRESSIVE|Behavior.FLAG_TROUBLEMAKING;}
	
	public Behavior newInstance()
	{
		return new MobileAggressive();
	}
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait=getParmVal(newParms,"delay",0);
		tickDown=tickWait;
	}
	public boolean grantsAggressivenessTo(MOB M)
	{
		return SaucerSupport.zapperCheck(getParms(),M);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Host.MOB_TICK) return true;
		if((--tickDown)<0)
		{
			tickDown=tickWait;
			Aggressive.tickAggressively(ticking,tickID,(getParms().toUpperCase().indexOf("MOBKILL")>=0),getParms());
			VeryAggressive.tickVeryAggressively(ticking,tickID,wander,(getParms().toUpperCase().indexOf("MOBKILL")>=0),getParms());
		}
		return true;
	}
}
