package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_Tourettes extends Spell implements DiseaseAffect
{
	public String ID() { return "Spell_Tourettes"; }
	public String name(){return "Tourettes";}
	public String displayText(){return "(Tourettes)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	int plagueDown=4;

	public int abilityCode(){return DiseaseAffect.SPREAD_PROXIMITY;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				Ability A=mob.fetchEffect("TemporaryImmunity");
				if(A==null)
				{
					A=CMClass.getAbility("TemporaryImmunity");
					A.setBorrowed(mob,true);
					A.makeLongLasting();
					mob.addEffect(A);
					A.makeLongLasting();
				}
				A.setMiscText("+"+ID());
				mob.tell("You feel more polite.");
			}
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		if((--plagueDown)<=0)
		{
			MOB mob=(MOB)affected;
			plagueDown=4;
			if(invoker==null) invoker=mob;

			MOB target=mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1));
			if((target!=null)
			&&(!mob.amDead())
			&&(target.charStats().getStat(CharStats.INTELLIGENCE)>5)
			&&(Sense.canSpeak(mob))
			&&(Sense.canBeHeardBy(target,mob)))
			{
				String say="Penis wrinkle!";
				switch(Dice.roll(1,30,0))
				{
				case 1: say="You are a very bad "+target.charStats().displayClassName()+"!"; break;
				case 2: say="I think all "+target.charStats().raceName()+"s are stupid!"; break;
				case 3: say="Damn flark!"; break;
				case 4: say="Squeegee!"; break;
				case 5: say="Ding dong!"; break;
				case 6: say="Goober!"; break;
				case 7: say="Noodle"+((target.charStats().getStat(CharStats.GENDER)=='M')?"boy":"girl")+"!"; break;
				case 8: say="Groin scratcher!"; break;
				case 9: say="Geek!"; break;
				case 10: say="Dork!"; break;
				case 11: say="Orc kisser!"; break;
				case 12: say="Jerk!"; break;
				case 13: say="Tuddleworm!"; break;
				case 14: say="Poopie diaper!"; break;
				case 15: say="Panty stain!"; break;
				case 16: say="Blah blah blah blah blah!"; break;
				case 17: say="Hairpit sniffer!"; break;
				case 18: say="Gluteous maximus cavity!"; break;
				case 19: say="Uncle copulator!"; break;
				case 20: say="Toe jam eater!"; break;
				case 21: say="Partial excrement!"; break;
				case 22: say="Female dog!"; break;
				case 23: say="Illigitimate offspring!"; break;
				case 24: say="You are overweight!"; break;
				case 25: say="You smell funny!"; break;
				case 26: say="You aren't very smart!"; break;
				case 27: say="You.. you.. ah nevermind."; break;
				case 28: say="Yokle!"; break;
				case 29: say="Ugly head!"; break;
				case 30: say="Goop"+((target.charStats().getStat(CharStats.GENDER)=='M')?"boy":"girl")+"!";  break;
				}
				CommonMsgs.say(mob,target,say,false,false);
				if((target!=invoker)&&(target!=mob)&&(target.fetchEffect(ID())==null))
				{
					if(Dice.rollPercentage()>target.charStats().getSave(CharStats.SAVE_DISEASE))
					{
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> feel(s) different somehow...");
						maliciousAffect(invoker,target,0,0,-1);
					}
				}
			}
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setStat(CharStats.CHARISMA,2);
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"":"^S<S-NAME> incant(s) rudely to <T-NAMESELF>.^?");
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_DISEASE|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					invoker=mob;
					maliciousAffect(mob,target,asLevel,0,-1);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> feel(s) different somehow...");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) rudely to <T-NAMESELF>, but the spell fades.");
		// return whether it worked
		return success;
	}
}
