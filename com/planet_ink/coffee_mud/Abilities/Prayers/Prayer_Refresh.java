package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_Refresh extends Prayer implements MendingSkill
{
    public String ID() { return "Prayer_Refresh"; }
    public String name(){ return "Refresh";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_RESTORATION;}
    public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
    public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
    protected long minCastWaitTime(){return Tickable.TIME_TICK/2;}

	public boolean supportsMending(Environmental E)
	{ 
		return (E instanceof MOB)
				&&(((((MOB)E).curState()).getFatigue()>0)
						||((((MOB)E).curState()).getMovement()<(((MOB)E).maxState()).getMovement())
						||((((MOB)E).curState()).getMana()<(((MOB)E).maxState()).getMana())
						||((((MOB)E).curState()).getHitPoints()<(((MOB)E).maxState()).getHitPoints())
						);
	}
	
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                if(!supportsMending(target))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
    
   public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        MOB target=this.getTarget(mob,commands,givenTarget);
        if(target==null) return false;

        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;

        boolean success=proficiencyCheck(mob,0,auto);

        if(success)
        {
            // it worked, so build a copy of this ability,
            // and add it to the affects list of the
            // affected MOB.  Then tell everyone else
            // what happened.
            CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"A bright yellow glow surrounds <T-NAME>.":"^S<S-NAME> "+prayWord(mob)+", delivering a strong touch of infusion to <T-NAMESELF>.^?");
            if(mob.location().okMessage(mob,msg))
            {
                mob.location().send(mob,msg);
                target.curState().setMana(target.maxState().getMana());
                target.curState().setHitPoints(target.maxState().getHitPoints());
                target.curState().setMana(target.maxState().getMana());
                target.curState().setMovement(target.maxState().getMovement());
                target.curState().setFatigue(0);
                target.curState().setHunger(target.maxState().getHunger());
                target.curState().setThirst(target.maxState().getThirst());
                target.tell("You feel refreshed!");
                lastCastHelp=System.currentTimeMillis();
            }
        }
        else
            beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>, but nothing happens.");
        // return whether it worked
        return success;
    }
}
