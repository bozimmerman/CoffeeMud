package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2004 Jeremy Vyska</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 * <p>Company: http://www.falserealities.com</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */
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
