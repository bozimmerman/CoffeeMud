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

public class Fighter_CounterAttack extends FighterSkill
{
	public String ID() { return "Fighter_CounterAttack"; }
	public String name(){ return "Counter-Attack";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
    public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amISource(mob)
		&&(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
		&&(msg.target() instanceof MOB)
		&&(msg.tool() instanceof Ability)
		&&((mob.fetchAbility(ID())==null)||proficiencyCheck(mob,0,false))
		&&(mob.rangeToTarget()==0))
		{
			if(msg.tool().ID().equals("Skill_Parry"))
			{
				CMMsg msg2=CMClass.getMsg(mob,msg.target(),this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> position(s) <S-HIM-HERSELF> for a counterattack!");
				msg.addTrailerMsg(msg2);
			}
			else
			if(msg.tool().ID().equals(ID()))
				CMLib.combat().postAttack(mob,(MOB)msg.target(),mob.fetchWieldedItem());
		}
		return true;
	}
}
