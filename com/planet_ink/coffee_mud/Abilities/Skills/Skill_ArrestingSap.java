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
public class Skill_ArrestingSap extends StdAbility
{
	public String ID() { return "Skill_ArrestingSap"; }
	public String name(){ return "Arresting Sap";}
	public String displayText(){ return "(Knocked out)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"ASAP"};
	public String[] triggerStrings(){return triggerStrings;}
	private int enhancement=0;
	public int abilityCode(){return enhancement;}
	public void setAbilityCode(int newCode){enhancement=newCode;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))&&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL)))
		{
			if((Util.bset(msg.sourceMajor(),CMMsg.MASK_EYES))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOUTH))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOVE)))
			{
				if(msg.sourceMessage()!=null)
					mob.tell("You are way too drowsy.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SLEEPING);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> regain(s) consciousness.");
				CommonMsgs.stand(mob,true);
			}
			else
				mob.tell("You regain consciousness.");
		}
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

		if(!auto)
		{
			if(mob.baseWeight()<(target.baseWeight()-450))
			{
				mob.tell(target.name()+" is way to big to knock out!");
				return false;
			}
		}
		if(Skill_Arrest.getWarrantsOf(target, CoffeeUtensils.getLegalObject(mob.location().getArea())).size()==0)
		{
		    mob.tell(target.name()+" has no warrants out here.");
		    return false;
		}
		int levelDiff=target.envStats().level()-adjustedLevel(mob,asLevel);
		if(levelDiff>0)
			levelDiff=levelDiff*3;
		else
			levelDiff=0;
		levelDiff-=(abilityCode()*mob.charStats().getStat(CharStats.STRENGTH));

		// now see if it worked
		boolean success=profficiencyCheck(mob,(-levelDiff)+(-((target.charStats().getStat(CharStats.STRENGTH)-mob.charStats().getStat(CharStats.STRENGTH)))),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_GENERAL:CMMsg.MASK_MALICIOUS),(mob==target)?"<T-NAME> hit(s) the floor!":"^F^<FIGHT^><S-NAME> rear(s) back and sap(s) <T-NAMESELF>, knocking <T-HIM-HER> out!^</FIGHT^>^?");
            CMColor.fixSourceFightColor(msg);
            if(target.riding()!=null)
                msg.addTrailerMsg(new FullMsg(target,target.riding(),CMMsg.TYP_DISMOUNT,null));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
                if(target.riding()!=null) 
                    target.setRiding(null);
				success=maliciousAffect(mob,target,asLevel,3,-1);
				if(mob.getVictim()==target) mob.setVictim(null);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> rear(s) back and attempt(s) to knock <T-NAMESELF> out, but fail(s).");

		// return whether it worked
		return success;
	}
}
