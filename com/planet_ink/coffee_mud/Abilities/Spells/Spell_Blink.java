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
public class Spell_Blink extends Spell
{
	public String ID() { return "Spell_Blink"; }
	public String name(){return "Blink";}
	public String displayText(){return "(Blink spell)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> stop(s) blinking.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==MudHost.TICK_MOB)&&(affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			int roll=Dice.roll(1,8,0);
			if(mob.isInCombat())
			{
				int move=0;
				switch(roll)
				{
				case 1: move=-2; break;
				case 2: move=-1; break;
				case 7: move=1; break;
				case 8: move=2; break;
				default: move=0;
				}
				if(move==0)
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> vanish(es) and reappear(s) again.");
				else
				{
					int rangeTo=mob.rangeToTarget();
					rangeTo+=move;
					if((move==0)||(rangeTo<0)||(rangeTo>mob.location().maxRange()))
						move=0;
					else
					{
						mob.setAtRange(rangeTo);
						if(mob.getVictim().getVictim()==mob)
							mob.getVictim().setAtRange(rangeTo);
					}
					switch(move)
					{
					case 0:
						mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> vanish(es) and reappear(s) again.");
						break;
					case 1:
						mob.location().show(mob,null,mob.getVictim(),CMMsg.MSG_OK_VISUAL,"<S-NAME> vanish(es) and reappear(s) a bit further from <O-NAMESELF>.");
						break;
					case 2:
						mob.location().show(mob,null,mob.getVictim(),CMMsg.MSG_OK_VISUAL,"<S-NAME> vanish(es) and reappear(s) much further from <O-NAMESELF>.");
						break;
					case -1:
						mob.location().show(mob,null,mob.getVictim(),CMMsg.MSG_OK_VISUAL,"<S-NAME> vanish(es) and reappear(s) a bit closer to <O-NAMESELF>.");
						break;
					case -2:
						mob.location().show(mob,null,mob.getVictim(),CMMsg.MSG_OK_VISUAL,"<S-NAME> vanish(es) and reappear(s) much closer to <O-NAMESELF>.");
						break;
					}
				}
				if(mob.getVictim()==null) mob.setVictim(null); // correct range
			}
			else
			if((roll>2)&&(roll<7))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> vanish(es) and reappear(s) a few feet away.");
			else
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> vanish(es) and reappear(s) again.");
		}
		return super.tick(ticking,tickID);
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

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> begin(s) to blink!":"^S<S-NAME> cast(s) a spell at <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.location()==mob.location())
					if(mob.envStats().level()>5)
						success=beneficialAffect(mob,target,asLevel,mob.envStats().level()-4);
					else
						success=beneficialAffect(mob,target,asLevel,mob.envStats().level());
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> cast(s) a spell to <T-NAMESELF>, but the magic fizzles.");

		// return whether it worked
		return success;
	}
}
