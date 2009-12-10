package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_TurnUndead extends StdSkill
{
	public String ID() { return "Skill_TurnUndead"; }
	public String name(){ return "Turn Undead";}
	public String displayText(){ return "(Turned)";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_DEATHLORE;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"TURN"};
	public String[] triggerStrings(){return triggerStrings;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if((mob!=null)&&(target!=null))
        {
            if(!(target instanceof MOB)) return Ability.QUALITY_INDIFFERENT;
            MOB targetM=(MOB)target;
            if((targetM.baseCharStats().getMyRace()==null)
            ||(!targetM.baseCharStats().getMyRace().racialCategory().equals("Undead")))
                return Ability.QUALITY_INDIFFERENT;
            if(CMLib.flags().isEvil(mob))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }

    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((target.baseCharStats().getMyRace()==null)
		   ||(!target.baseCharStats().getMyRace().racialCategory().equals("Undead")))
		{
			mob.tell(auto?"Only the undead can be turned.":"You can only turn the undead.");
			return false;
		}

		if(CMLib.flags().isEvil(mob))
		{
			mob.tell("Only the riteous may turn the undead.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,((mob.envStats().level()+(4*getXLEVELLevel(mob)))-target.envStats().level())*30,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_CAST_ATTACK_SOMANTIC_SPELL|(auto?CMMsg.MASK_ALWAYS:0),auto?"<T-NAME> turn(s) away.":"^S<S-NAME> turn(s) <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if((mob.envStats().level()-target.envStats().level())>6)
					{
						mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> wither(s)"+(auto?".":" under <S-HIS-HER> holy power!"));
						CMLib.combat().postDamage(mob,target,this,target.curState().getHitPoints(),CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,-1,null);
					}
					else
					{
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> shake(s) in fear!");
						CMLib.commands().postFlee(target,"");
					}
					invoker=mob;
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to turn <T-NAMESELF>, but fail(s).");


		// return whether it worked
		return success;
	}
}
