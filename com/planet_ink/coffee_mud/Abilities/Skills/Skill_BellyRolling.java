package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_BellyRolling extends StdAbility
{
	public String ID() { return "Skill_BellyRolling"; }
	public String name(){ return "Belly Rolling";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	private boolean doneThisRound=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amITarget(mob)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(Sense.isSitting(mob))
		&&(msg.tool()!=null)
		&&(!doneThisRound)
		&&(msg.tool() instanceof Weapon))
		{
			// can't use -NAME for msg.source() lest sitting prevent it
			FullMsg msg2=new FullMsg(mob,msg.source(),null,CMMsg.MSG_SITMOVE,"<S-NAME> roll(s) away from the attack by <T-NAMESELF>!");
			if((profficiencyCheck(null,mob.charStats().getStat(CharStats.DEXTERITY)-50,false))
			&&((msg.source().getVictim()==mob)||(msg.source().getVictim()==null))
			&&(mob.location().okMessage(mob,msg2)))
			{
				doneThisRound=true;
				mob.location().send(mob,msg2);
				helpProfficiency(mob);
				return false;
			}
		}
		return true;
	}
}


