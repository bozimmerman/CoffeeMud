package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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
	@Override public String ID() { return "Fighter_Intimidate"; }
	@Override public String name(){ return "Intimidation";}
	@Override public String displayText(){ return "";}
	@Override public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	@Override protected int canAffectCode(){return Ability.CAN_MOBS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public boolean isAutoInvoked(){return true;}
	@Override public boolean canBeUninvoked(){return false;}
	@Override public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_INFLUENTIAL; }
	public Room lastRoom=null;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
		&&((msg.amITarget(affected))))
		{
			MOB targetM=(MOB)msg.target();
			MOB attackerM=msg.source();
			int levelDiff=((attackerM.phyStats().level()-(targetM.phyStats().level()+((2*getXLEVELLevel(targetM)))))*10);
			// 1 level off = -10
			// 10 levels off = -100
			if((!targetM.isInCombat())
			&&(msg.source().getVictim()!=targetM)
			&&(levelDiff<0)
			&&(attackerM.location()==targetM.location())
			&&((targetM.fetchAbility(ID())==null)||proficiencyCheck(null,(-(100+levelDiff))+(targetM.charStats().getStat(CharStats.STAT_CHARISMA)*2),false)))
			{
				attackerM.tell("You are too intimidated by "+targetM.name(attackerM));
				if(targetM.location()!=lastRoom)
				{
					lastRoom=targetM.location();
					helpProficiency(targetM, 0);
				}
				if(targetM.getVictim()==msg.source())
				{
					targetM.makePeace();
					targetM.setVictim(null);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

}
