package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class MobileAggressive extends Mobile
{
	public String ID(){return "MobileAggressive";}
	public Behavior newInstance()
	{
		return new MobileAggressive();
	}
	public boolean grantsAggressivenessTo(MOB M)
	{
		return !ExternalPlay.zapperCheck(getParms(),M);
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Host.MOB_TICK) return;
		Aggressive.tickAggressively(ticking,tickID,this);
		VeryAggressive.tickVeryAggressively(ticking,tickID,this);
	}
}
