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

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Host.MOB_TICK) return;
		Aggressive.tickAggressively(ticking,tickID);
		VeryAggressive.tickVeryAggressively(ticking,tickID);
	}
}
