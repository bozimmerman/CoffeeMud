package com.planet_ink.coffee_mud.Abilities.Prayers;

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

public class Prayer_Doomspout extends Prayer implements DiseaseAffect
{
	public String ID() { return "Prayer_Doomspout"; }
	public String name(){ return "Doomspout";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Doomspout)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int difficultyLevel(){return 7;}
	int plagueDown=4;
	String godName="The Demon";

	public int abilityCode(){return DiseaseAffect.SPREAD_PROXIMITY;}

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
			if(mob.location()==null) return false;
			switch(Dice.roll(1,12,0))
			{
			case 1:	CommonMsgs.say(mob,null,"Repent, or "+godName+" will consume your soul!",false,false); break;
			case 2:	CommonMsgs.say(mob,null,"We are all damned! Hope is forgotten!",false,false); break;
			case 3:	CommonMsgs.say(mob,null,godName+" has damned us all!",false,false); break;
			case 4:	CommonMsgs.say(mob,null,"Death is the only way out for us now!",false,false); break;
			case 5:	CommonMsgs.say(mob,null,"The finger of "+godName+" will destroy all!",false,false); break;
			case 6:	CommonMsgs.say(mob,null,"The waters will dry! The air will turn cold! Our bodies will fail! We are Lost!",false,false); break;
			case 7:	CommonMsgs.say(mob,null,"Nothing can save you! Throw yourself on the mercy of "+godName+"!",false,false); break;
			case 8:	CommonMsgs.say(mob,null,godName+" will show us no mercy!",false,false); break;
			case 9:	CommonMsgs.say(mob,null,godName+" has spoken! We will all be destroyed!",false,false);
					break;
			case 10:
			case 11:
			case 12:
					CommonMsgs.say(mob,null,"Our doom is upon us! The end is near!",false,false);
					break;
			}
			MOB target=mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1));
			if((target!=null)&&(target!=invoker)&&(target!=mob)&&(target.fetchEffect(ID())==null))
				if(Dice.rollPercentage()>target.charStats().getSave(CharStats.SAVE_DISEASE))
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> look(s) seriously ill!");
					maliciousAffect(invoker,target,0,0,-1);
				}
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		if(affectableStats.getStat(CharStats.INTELLIGENCE)>3)
			affectableStats.setStat(CharStats.INTELLIGENCE,3);
	}

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
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> doomspout disease clear up.");
			}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"":"^S<S-NAME> inflict(s) an unholy disease upon <T-NAMESELF>.^?");
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_DISEASE|(auto?CMMsg.MASK_GENERAL:0),null);
			FullMsg msg3=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))
			&&(mob.location().okMessage(mob,msg2))
			&&(mob.location().okMessage(mob,msg3)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				mob.location().send(mob,msg3);
				if((msg.value()<=0)&&(msg2.value()<=0)&&(msg3.value()<=0))
				{
					invoker=mob;
					if(mob.getWorshipCharID().length()>0)
						godName=mob.getWorshipCharID();
					maliciousAffect(mob,target,asLevel,0,-1);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> look(s) seriously ill!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to inflict a disease upon <T-NAMESELF>, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
