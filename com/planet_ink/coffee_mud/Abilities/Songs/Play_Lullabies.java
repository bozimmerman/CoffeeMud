package com.planet_ink.coffee_mud.Abilities.Songs;

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
public class Play_Lullabies extends Song
{
	public String ID() { return "Play_Lullabies"; }
	public String name(){ return "Lullabies";}
	public int quality(){ return MALICIOUS;}

	boolean asleep=false;
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;
		if(asleep)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SLEEPING);
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(mob==invoker) return true;
		boolean oldasleep=asleep;
		if(Dice.rollPercentage()>50)
			asleep=true;
		else
			asleep=false;
		if(asleep!=oldasleep)
		{
			if(oldasleep)
			{
				if(Sense.isSleeping(mob))
					mob.envStats().setDisposition(mob.envStats().disposition()-EnvStats.IS_SLEEPING);
				mob.location().show(mob,null,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> wake(s) up.");
			}
			else
			{
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> fall(s) asleep.");
				mob.envStats().setDisposition(mob.envStats().disposition()|EnvStats.IS_SLEEPING);
			}
		}

		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(msg.source()==invoker)
			return true;

		if(msg.source()!=affected)
			return true;


		if((!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
		&&(msg.targetMinor()==CMMsg.TYP_STAND)&&(asleep))
			return false;
		return true;
	}
}
