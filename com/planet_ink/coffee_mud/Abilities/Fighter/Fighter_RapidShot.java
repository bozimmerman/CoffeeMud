package com.planet_ink.coffee_mud.Abilities.Fighter;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Fighter_RapidShot extends StdAbility
{
	public String ID() { return "Fighter_RapidShot"; }
	public String name(){ return "Rapid Shot";}
	public String displayText(){ return "";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){return Ability.SKILL;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(mob.isInCombat())
		{
			Item w=mob.fetchWieldedItem();
			if((w!=null)
			&&(w instanceof Weapon)
			&&(((Weapon)w).weaponClassification()==Weapon.CLASS_RANGED)
			&&(((Weapon)w).ammunitionType().length()>0)
			&&((mob.rangeToTarget()>0)||((w.envStats().sensesMask()&EnvStats.SENSE_ITEMNOMINRANGE)==EnvStats.SENSE_ITEMNOMINRANGE))
			&&((mob.fetchAbility(ID())==null)||profficiencyCheck(null,0,false)))
			{
				helpProfficiency(mob);
				for(int i=0;i<(adjustedLevel(mob,0)/7);i++)
					MUDFight.postAttack(mob,mob.getVictim(),w);
			}
		}
		return true;
	}
}
