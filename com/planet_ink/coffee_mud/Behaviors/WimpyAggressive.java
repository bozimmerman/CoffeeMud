package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class WimpyAggressive extends Aggressive
{
	public String ID(){return "WimpyAggressive";}
	public long flags(){return Behavior.FLAG_POTENTIALLYAGGRESSIVE|Behavior.FLAG_TROUBLEMAKING;}
	protected int tickWait=0;
	protected int tickDown=0;
	protected boolean mobKiller=false;


	public boolean grantsAggressivenessTo(MOB M)
	{
		return ((M!=null)&&(Sense.isSleeping(M)))&&
			MUDZapper.zapperCheck(getParms(),M);
	}
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait=Util.getParmInt(newParms,"delay",0);
		tickDown=tickWait;
	}

	public static void pickAWimpyFight(MOB observer, boolean mobKiller)
	{
		if(!canFreelyBehaveNormal(observer)) return;
		for(int i=0;i<observer.location().numInhabitants();i++)
		{
			MOB mob=observer.location().fetchInhabitant(i);
			if((mob!=null)&&(mob!=observer)&&(Sense.isSleeping(mob)))
			{
				startFight(observer,mob,mobKiller);
				if(observer.isInCombat()) break;
			}
		}
	}

	public static void tickWimpyAggressively(Tickable ticking, boolean mobKiller, int tickID)
	{
		if(tickID!=MudHost.TICK_MOB) return;
		if(ticking==null) return;
		if(!(ticking instanceof MOB)) return;

		pickAWimpyFight((MOB)ticking,mobKiller);
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID!=MudHost.TICK_MOB) return true;
		if((--tickDown)<0)
		{
			tickDown=tickWait;
			tickWimpyAggressively(ticking,(getParms().toUpperCase().indexOf("MOBKILL")>=0),tickID);
		}
		return true;
	}
}
