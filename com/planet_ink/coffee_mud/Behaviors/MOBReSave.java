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


import java.lang.ref.WeakReference;
import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class MOBReSave extends ActiveTicker
{
	public String ID(){return "MOBReSave";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public long flags(){return 0;}
	protected static HashSet roomsReset=new HashSet();
	protected boolean noRecurse=false;
	protected CharStats startStats=null;
	protected WeakReference host=null;

	public MOBReSave()
	{
		super();
		minTicks=140; maxTicks=140; chance=100;
		tickReset();
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		startStats=(CharStats)CMClass.getCommon("DefaultCharStats");
		for(int c: CharStats.CODES.ALL())
			startStats.setStat(c,CMParms.getParmInt(parms,CharStats.CODES.ABBR(c),-1));
	}
	
	public String getParms() 
	{
		if(host==null) return super.getParms();
		MOB M=(MOB)host.get();
		if(M==null) return super.getParms();
		StringBuffer rebuiltParms=new StringBuffer(super.rebuildParms());
		for(int c: CharStats.CODES.ALL())
			rebuiltParms.append(" "+CharStats.CODES.ABBR(c)+"="+M.baseCharStats().getStat(c));
		return rebuiltParms.toString();
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if((ticking instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB)
		&&(!((MOB)ticking).amDead())
		&&(!noRecurse)
		&&(CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
		&&(((MOB)ticking).getStartRoom()!=null)
		&&(((MOB)ticking).getStartRoom().roomID().length()>0)
		&&(((MOB)ticking).databaseID().length()>0))
		{
			MOB mob=(MOB)ticking;
			if((host==null)||(host.get()==null))
				host=new WeakReference(mob);
			noRecurse=true;
			if(startStats != null)
			{
				synchronized(startStats)
				{
					for(int c: CharStats.CODES.ALL())
						if(startStats.getStat(c)>0)
							mob.baseCharStats().setStat(c,startStats.getStat(c));
					startStats=null;
				}
			}
			if(canAct(ticking,tickID))
			{
				Room R=mob.getStartRoom();
				CMLib.database().DBUpdateMOB(R.roomID(),mob);
			}
		}
		noRecurse=false;
		return true;
	}


}
