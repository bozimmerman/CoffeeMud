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
public class MobileAggressive extends Mobile
{
	public String ID(){return "MobileAggressive";}
	protected int tickWait=0;
	protected int tickDown=0;
	public long flags(){return Behavior.FLAG_POTENTIALLYAGGRESSIVE|Behavior.FLAG_TROUBLEMAKING;}


	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait=Util.getParmInt(newParms,"delay",0);
		tickDown=tickWait;
	}
	public boolean grantsAggressivenessTo(MOB M)
	{
		return MUDZapper.zapperCheck(getParms(),M);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=MudHost.TICK_MOB) return true;
		if((--tickDown)<0)
		{
			tickDown=tickWait;
			Aggressive.tickAggressively(ticking,tickID,(getParms().toUpperCase().indexOf("MOBKILL")>=0),getParms());
			VeryAggressive.tickVeryAggressively(ticking,tickID,wander,(getParms().toUpperCase().indexOf("MOBKILL")>=0),getParms());
		}
		return true;
	}
}
