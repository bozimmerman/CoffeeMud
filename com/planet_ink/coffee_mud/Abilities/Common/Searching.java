package com.planet_ink.coffee_mud.Abilities.Common;
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

public class Searching extends CommonSkill
{
	public String ID() { return "Searching"; }
	public String name(){ return "Searching";}
	private static final String[] triggerStrings = {"SEARCH","SEARCHING"};
	public String[] triggerStrings(){return triggerStrings;}
	private Room searchRoom=null;

	private boolean success=false;
	public Searching()
	{
		super();
		displayText="You are searching...";
		verb="searching";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if(tickUp==1)
			{
				if(success==false)
				{
					StringBuffer str=new StringBuffer("You get distracted from your search.\n\r");
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
		}
		return super.tick(ticking,tickID);
	}

	public void affectEnvStats(Environmental affectedEnv, EnvStats affectableStats)
	{
		super.affectEnvStats(affectedEnv,affectableStats);
		if((success)&&(affectedEnv instanceof MOB)&&(((MOB)affectedEnv).location()==searchRoom))
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_HIDDEN);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		verb="searching";
		success=false;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(profficiencyCheck(mob,0,auto))
			success=true;
		int duration=3;
		FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,(auto?"":"<S-NAME> start(s) searching."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			searchRoom=mob.location();
			beneficialAffect(mob,mob,asLevel,duration);
			mob.tell(" ");
			CommonMsgs.look(mob,true);
		}
		return true;
	}
}
