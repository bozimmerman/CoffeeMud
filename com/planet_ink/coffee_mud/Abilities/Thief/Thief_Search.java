package com.planet_ink.coffee_mud.Abilities.Thief;

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
public class Thief_Search extends ThiefSkill
{
	public String ID() { return "Thief_Search"; }
	public String name(){ return "Search";}
	public String displayText(){return "(Searching)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"SEARCH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_HIDDEN);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already aware of hidden things.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		FullMsg msg=new FullMsg(mob,target,this,auto?CMMsg.MASK_GENERAL:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,auto?"<T-NAME> become(s) very observant.":"<S-NAME> examine(s) <S-HIS-HER> surroundings carefully.");
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":"<S-NAME> look(s) around carefully, but become(s) distracted.");
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
			target.envStats().setSensesMask(mob.envStats().sensesMask()|EnvStats.CAN_SEE_HIDDEN);
			target.envStats().setSensesMask(mob.envStats().sensesMask()|EnvStats.CAN_SEE_SNEAKERS);
			CommonMsgs.look(target,false);
			target.recoverEnvStats();
		}
		return success;
	}
}
