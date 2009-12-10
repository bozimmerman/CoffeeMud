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
public class MobileAggressive extends Mobile
{
	public String ID(){return "MobileAggressive";}
	protected int tickWait=0;
	public long flags(){return Behavior.FLAG_POTENTIALLYAGGRESSIVE|Behavior.FLAG_TROUBLEMAKING;}
	protected boolean mobkill=false;
	protected boolean misbehave=false;
	protected String attackMsg=null;

	public MobileAggressive()
	{
	    super();

	    tickDown = 0;
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait=CMParms.getParmInt(newParms,"delay",0);
		attackMsg=CMParms.getParmStr(newParms,"MESSAGE",null);
		tickDown=tickWait;
		Vector V=CMParms.parse(newParms.toUpperCase());
		mobkill=V.contains("MOBKILL");
		misbehave=V.contains("MISBEHAVE");
	}
	public boolean grantsAggressivenessTo(MOB M)
	{
		if(M==null) return true;
		return CMLib.masking().maskCheck(getParms(),M,false);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
        tickStatus=Tickable.STATUS_MISC+0;
		super.tick(ticking,tickID);
        tickStatus=Tickable.STATUS_MISC+1;
		if(tickID!=Tickable.TICKID_MOB)
        {
            tickStatus=Tickable.STATUS_NOT;
            return true;
        }
		if((--tickDown)<0)
		{
			tickDown=tickWait;
            tickStatus=Tickable.STATUS_MISC+2;
			Aggressive.tickAggressively(ticking,tickID,mobkill,misbehave,getParms(),attackMsg);
            tickStatus=Tickable.STATUS_MISC+3;
			VeryAggressive.tickVeryAggressively(ticking,tickID,wander,mobkill,misbehave,getParms(),attackMsg);
		}
        tickStatus=Tickable.STATUS_NOT;
		return true;
	}
}
