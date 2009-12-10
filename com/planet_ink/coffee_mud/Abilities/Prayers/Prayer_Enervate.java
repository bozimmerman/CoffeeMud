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
public class Prayer_Enervate extends Prayer
{
    public String ID() { return "Prayer_Enervate"; }
    public String name(){ return "Enervate";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CORRUPTION;}
    public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
    public long flags(){return Ability.FLAG_UNHOLY;}

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
            CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|verbalCastCode(mob,target,auto),(auto?"A light fatigue overcomes <T-NAME>.":"^S<S-NAME> "+prayWord(mob)+" for extreme fatigue to overcome <T-NAMESELF>!^?"));
            if(mob.location().okMessage(mob,msg))
            {
                mob.location().send(mob,msg);
                if(msg.value()<=0)
                {
                    int harming=CMLib.dice().roll(10,adjustedLevel(mob,asLevel),50);
                    if(target.curState().getFatigue()<=CharState.FATIGUED_MILLIS)
                        target.curState().setFatigue(CharState.FATIGUED_MILLIS+1);
                    target.curState().adjMovement(-harming,target.maxState());
                    target.tell("You feel fatigued!");
                }
            }
        }
        else
            return maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+", but nothing happens.");

        // return whether it worked
        return success;
    }
}