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
public class MOBReSave extends ActiveTicker
{
	public String ID(){return "MOBReSave";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public long flags(){return 0;}
	private static HashSet roomsReset=new HashSet();
	private boolean noRecurse=false;

	public MOBReSave()
	{
		super();
		minTicks=140; maxTicks=140; chance=100;
		tickReset();
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if((ticking instanceof MOB)
		&&(tickID==MudHost.TICK_MOB)
		&&(!((MOB)ticking).amDead())
		&&(!noRecurse)
		&&(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
		&&(((MOB)ticking).getStartRoom()!=null)
		&&(((MOB)ticking).getStartRoom().roomID().length()>0))
		{
			noRecurse=true;
			MOB mob=(MOB)ticking;
			synchronized(roomsReset)
			{
				if(!roomsReset.contains(mob.getStartRoom().roomID()))
				{
					if(mob.location()!=mob.getStartRoom())
						mob.getStartRoom().bringMobHere(mob,false);
					roomsReset.add(mob.getStartRoom().roomID());
					CoffeeUtensils.resetRoom(mob.getStartRoom());
					CMClass.DBEngine().DBUpdateMOBs(mob.getStartRoom());
				}
			}
			if(canAct(ticking,tickID))
				CMClass.DBEngine().DBUpdateRoomMOB(""+mob,mob.getStartRoom(),mob);
		}
		noRecurse=false;
		return true;
	}


}
