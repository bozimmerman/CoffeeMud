package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
