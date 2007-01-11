package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2007 Bo Zimmerman

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
	protected static HashSet roomsReset=new HashSet();
	protected boolean noRecurse=false;

	public MOBReSave()
	{
		super();
		minTicks=140; maxTicks=140; chance=100;
		tickReset();
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if((ticking instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB)
		&&(!((MOB)ticking).amDead())
		&&(!noRecurse)
		&&(CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
		&&(((MOB)ticking).getStartRoom()!=null)
		&&(((MOB)ticking).getStartRoom().roomID().length()>0))
		{
			noRecurse=true;
			MOB mob=(MOB)ticking;
			Room R=mob.getStartRoom();
	    	synchronized(("SYNC"+R.roomID()).intern())
	    	{
	    		R=CMLib.map().getRoom(R);
				synchronized(roomsReset)
				{
					if(!roomsReset.contains(R.roomID()))
					{
						if(mob.location()!=R)
							R.bringMobHere(mob,false);
						roomsReset.add(R.roomID());
						CMLib.map().resetRoom(R);
						CMLib.database().DBUpdateMOBs(R);
					}
				}
				if(canAct(ticking,tickID))
					CMLib.database().DBUpdateRoomMOB(""+mob,R,mob);
	    	}
		}
		noRecurse=false;
		return true;
	}


}
