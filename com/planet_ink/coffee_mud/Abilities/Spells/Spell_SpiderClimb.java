package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_SpiderClimb extends Spell
{
	public String ID() { return "Spell_SpiderClimb"; }
	public String name(){return "Spider Climb";}
	public String displayText(){return "(Spider Climb)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected instanceof MOB)
		{
		    if((!Sense.isSitting(affected))&&(!Sense.isSleeping(affected)))
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_CLIMBING);
		}
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		Room room=((MOB)affected).location();
		if(canBeUninvoked())
			room.show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> no longer has a spidery gate.");
		super.unInvoke();
		if(canBeUninvoked())
			room.recoverRoomStats();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> spidery magic.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"^S<S-NAME> attains a climbers stance!":"^S<S-NAME> invoke(s) a spidery spell upon <S-HIM-HERSELF>!^?");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,10);
		}
		else
			beneficialWordsFizzle(mob,mob.location(),"<S-NAME> attempt(s) to invoke a spell, but fail(s).");

		return success;
	}
}
