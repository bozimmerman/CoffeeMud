package com.planet_ink.coffee_mud.Abilities.Fighter;
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

public class Fighter_Intimidate extends FighterSkill
{
	public String ID() { return "Fighter_Intimidate"; }
	public String name(){ return "Intimidation";}
	public String displayText(){ return "";}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
    public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_INFLUENTIAL; }
	public Room lastRoom=null;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0)
        &&(!CMath.bset(msg.sourceCode(),CMMsg.MASK_ALWAYS))
		&&((msg.amITarget(affected))))
		{
			MOB target=(MOB)msg.target();
			MOB attacker=msg.source();
			int levelDiff=((attacker.envStats().level()-(target.envStats().level()+((2*getXLEVELLevel(target)))))*10);
			// 1 level off = -10
			// 10 levels off = -100
			if((!target.isInCombat())
			&&(msg.source().getVictim()!=target)
			&&(levelDiff<0)
            &&(attacker.location()==target.location())
			&&((target.fetchAbility(ID())==null)||proficiencyCheck(null,(-(100+levelDiff))+(target.charStats().getStat(CharStats.STAT_CHARISMA)*2),false)))
			{
                attacker.tell("You are too intimidated by "+target.name());
				if(target.location()!=lastRoom)
				{
					lastRoom=target.location();
					helpProficiency(target);
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