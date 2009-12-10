package com.planet_ink.coffee_mud.Abilities.Songs;
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
public class Play_Blues extends Play
{
	public String ID() { return "Play_Blues"; }
	public String name(){ return "Blues";}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	protected boolean maliciousButNotAggressiveFlag(){return true;}
	protected String songOf(){return "the "+name();}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		// the sex rules
		if(!(affected instanceof MOB)) return true;

		MOB myChar=(MOB)affected;
		if((msg.target()!=null)&&(msg.target() instanceof MOB))
		{
			if((msg.amISource(myChar)||(msg.amITarget(myChar))
			&&(msg.tool() instanceof Social)
			&&(msg.tool().Name().equals("MATE <T-NAME>")
				||msg.tool().Name().equals("SEX <T-NAME>"))))
			{
				if(msg.amISource(myChar))
					myChar.tell("You really don't feel like it.");
				else
				if(msg.amITarget(myChar))
					msg.source().tell(myChar.name()+" doesn't look like "+myChar.charStats().heshe()+" feels like it.");
				return false;
			}
		}
		return true;
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			mob.curState().adjHunger(-2,mob.maxState().maxHunger(mob.baseWeight()));
            if(CMLib.dice().rollPercentage()>(adjustedLevel(invoker(),0)/4))
            {
                Ability A=CMClass.getAbility("Disease_Depression");
                if(A!=null) A.invoke(invoker(),affected,true,0);
            }
		}
		return true;
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()
											-((invoker().charStats().getStat(CharStats.STAT_CHARISMA)/4)
													+(adjustedLevel(invoker(),0))));
	}
	public void affectCharStats(MOB mob, CharStats stats)
	{
		super.affectCharStats(mob,stats);
		if(invoker()!=null)
			stats.setStat(CharStats.STAT_SAVE_JUSTICE,stats.getStat(CharStats.STAT_SAVE_JUSTICE)-(invoker().charStats().getStat(CharStats.STAT_CHARISMA)+getXLEVELLevel(invoker())));
	}
}

