package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class FightFlee extends ActiveTicker
{
	public String ID(){return "FightFlee";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public FightFlee()
	{
		minTicks=1;maxTicks=1;chance=33;
		tickReset();
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			if(mob.isInCombat()
			   &&(mob.getVictim()!=null)
			   &&(mob.getVictim().getVictim()==mob))
				CommonMsgs.flee(mob,"");
		}
		return true;
	}
}