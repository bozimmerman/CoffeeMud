package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class TargetPlayer extends ActiveTicker
{
	public String ID(){return "TargetPlayer";}
	protected int canImproveCode() {return Behavior.CAN_MOBS;}
	public Behavior newInstance() { return new TargetPlayer(); }

	public TargetPlayer() 
	{
	    minTicks=3; maxTicks=12; chance=100;
		tickReset();
	}

	public boolean tick(Tickable ticking, int tickID) 
	{
		if(canAct(ticking,tickID))
		{
			MOB mob = (MOB) ticking;
			if (mob.getVictim() != null) 
			{
				HashSet theBadGuys = mob.getVictim().getGroupMembers(new HashSet());
				MOB shouldFight = null;
				for (Iterator e = theBadGuys.iterator(); e.hasNext(); ) 
				{
					MOB consider = (MOB) e.next();
					if (consider.isMonster())
						continue;
					if (shouldFight == null) 
					{
						shouldFight = consider;
					}
					else
					if (shouldFight != null) 
					{
						if (((shouldFight.envStats()!=null)&&(consider.envStats()!=null)) 
						&&(shouldFight.envStats().level() > consider.envStats().level()))
							shouldFight = consider;
					}
				}
				if(shouldFight!=null) 
				{
					if(shouldFight.equals(mob.getVictim()))
						return true;
					else 
					if(Sense.canBeSeenBy(shouldFight,mob))
					{
						mob.setVictim(shouldFight);
					}
				}
			}
			return true;
		}
		return true;
	}
}
