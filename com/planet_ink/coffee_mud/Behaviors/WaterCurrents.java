package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WaterCurrents extends ActiveTicker
{
	public String ID(){return "WaterCurrents";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}
	public WaterCurrents()
	{
		minTicks=3;maxTicks=5;chance=75;
		tickReset();
	}
	
	public Behavior newInstance()
	{
		return new WaterCurrents();
	}

	public void applyCurrents(Room R)
	{
	}
	
	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			if(ticking instanceof Room)
				applyCurrents((Room)ticking);
			else
			if(ticking instanceof Area)
			{
				for(Iterator r=((Area)ticking).getMap();r.hasNext();)
				{
					Room R=(Room)r.next();
					applyCurrents(R);
				}
			}
		}
	}
	
}