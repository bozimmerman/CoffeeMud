package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class Fighter_Intimidate extends StdAbility
{
	public String ID() { return "Fighter_Intimidate"; }
	public String name(){ return "Intimidation";}
	public String displayText(){ return "";}
	public int quality(){return Ability.OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){ return Ability.SKILL;}
	public Room lastRoom=null;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0)
        &&(!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL))
		&&((msg.amITarget(affected))))
		{
			MOB target=(MOB)msg.target();
			MOB mob=(MOB)affected;
			int levelDiff=((msg.source().envStats().level()-target.envStats().level())*10);
			// 1 level off = -10
			// 10 levels off = -100
			if((!target.isInCombat())
			&&(msg.source().getVictim()!=target)
			&&(levelDiff<0)
            &&(mob.location()==target.location())
			&&((mob.fetchAbility(ID())==null)||profficiencyCheck(null,(-(100+levelDiff))+(target.charStats().getStat(CharStats.CHARISMA)*2),false)))
			{
				msg.source().tell("You are too intimidated by "+target.name());
				if(msg.source().location()!=lastRoom)
				{
					lastRoom=msg.source().location();
					helpProfficiency(target);
				}
				if(target.getVictim()==msg.source())
				{
					target.makePeace();
					target.setVictim(null);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

}