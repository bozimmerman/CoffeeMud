package com.planet_ink.coffee_mud.Abilities.Thief;
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
@SuppressWarnings("unchecked")
public class Thief_Sap extends ThiefSkill
{
	public String ID() { return "Thief_Sap"; }
	public String name(){ return "Sap";}
	public String displayText(){ return "(Knocked out)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"SAP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DIRTYFIGHTING;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS)))
		{
			if((CMath.bset(msg.sourceMajor(),CMMsg.MASK_EYES))
			||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOUTH))
			||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE)))
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
				CMLib.commands().postStand(mob,true);
			}
			else
				mob.tell("You regain consciousness.");
		}
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
            if(!(target instanceof MOB))
                return Ability.QUALITY_INDIFFERENT;
            if(CMLib.flags().canBeSeenBy(mob,(MOB)target))
                return Ability.QUALITY_INDIFFERENT;
            if(mob.baseWeight()<(((MOB)target).baseWeight()-100))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!auto)
		{
			if(mob.isInCombat())
			{
				mob.tell("Not while you are fighting!");
				return false;
			}

			if(CMLib.flags().canBeSeenBy(mob,target))
			{
				mob.tell(target.name()+" is watching you way too closely.");
				return false;
			}

			if(mob.baseWeight()<(target.baseWeight()-100))
			{
				mob.tell(target.name()+" is too big to knock out!");
				return false;
			}
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.envStats().level()-(mob.envStats().level()+(2*super.getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*3;
		else
			levelDiff=0;
		// now see if it worked
		boolean hit=(auto)||CMLib.combat().rollToHit(mob,target);
		boolean success=proficiencyCheck(mob,(-levelDiff)+(-((target.charStats().getStat(CharStats.STAT_STRENGTH)-mob.charStats().getStat(CharStats.STAT_STRENGTH)))),auto)&&(hit);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_THIEF_ACT|CMMsg.MASK_SOUND|CMMsg.MSK_MALICIOUS_MOVE|(auto?CMMsg.MASK_ALWAYS:0),auto?"<T-NAME> hit(s) the floor!":"^F^<FIGHT^><S-NAME> sneak(s) up behind <T-NAMESELF> and knock(s) <T-HIM-HER> out!^</FIGHT^>^?");
            CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
					success=maliciousAffect(mob,target,asLevel,3,-1);
				HashSet H=mob.getGroupMembers(new HashSet());
				MOB M=null;
				mob.makePeace();
				for(Iterator i=H.iterator();i.hasNext();)
				{
					M=(MOB)i.next();
					if(M.getVictim()==target) M.setVictim(null);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> sneak(s) up and attempt(s) to knock <T-NAMESELF> out, but fail(s).");

		// return whether it worked
		return success;
	}
}
