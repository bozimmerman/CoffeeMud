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
public class Spell_Confusion extends Spell
{
	public String ID() { return "Spell_Confusion"; }
	public String name(){return "Confusion";}
	public String displayText(){return "(Confusion spell)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	boolean notAgain=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		MOB mob=(MOB)affected;
		if(mob.isInCombat())
		{
			for(int x=0;x<10;x++)
			{
				MOB newvictim=mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1));
				if((newvictim!=mob)&&(mob.mayIFight(newvictim)))
				{
					mob.setVictim(newvictim);
					break;
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("You feel less confused.");
		CommonMsgs.stand(mob,true);
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,-target.envStats().level(),auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> enchant(s) <T-NAMESELF>!^?");
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					success=maliciousAffect(mob,target,asLevel,15,-1);
					if(success)
						if(target.location()==mob.location())
							target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> look(s) confused!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to enchant <T-NAMESELF>, but the spell fizzles");

		// return whether it worked
		return success;
	}
}
