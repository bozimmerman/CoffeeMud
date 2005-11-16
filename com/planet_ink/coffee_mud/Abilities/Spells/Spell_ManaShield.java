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
public class Spell_ManaShield extends Spell
{
    public String ID() { return "Spell_ManaShield"; }
    public String name(){return "Mana Shield";}
    public String displayText(){return "(Mana Shield)";}
    public int quality(){ return BENEFICIAL_SELF;}
    protected int canAffectCode(){return CAN_MOBS;}
    public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ABJURATION;}
    protected double protection=0.5;
    protected String adjective=" a";

    public void unInvoke()
    {
        // undo the affects of this spell
        if((affected==null)||(!(affected instanceof MOB)))
            return;
        MOB mob=(MOB)affected;

        super.unInvoke();

        if(canBeUninvoked())
            if((mob.location()!=null)&&(!mob.amDead()))
                mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"The mana shield around <S-NAME> fades.");
    }

    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        if(!super.okMessage(myHost,msg))
            return false;

        if((affected==null)||(!(affected instanceof MOB)))
            return true;

        MOB mob=(MOB)affected;
        if((msg.amITarget(mob))
           &&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
        {
            int recovery=(int)Math.round(Util.mul((msg.value()),protection));
            if(recovery>mob.curState().getMana())
                recovery=mob.curState().getMana();
            if(recovery>0)
            {
                msg.setValue(msg.value()-recovery);
                mob.curState().adjMana(-recovery,mob.maxState());
            }
        }
        return true;
    }
    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        Environmental target=mob;
        if((auto)&&(givenTarget!=null)) target=givenTarget;
        boolean oldOne=false;
        for(int a=0;a<target.numEffects();a++)
            if(target.fetchEffect(a) instanceof Spell_ManaShield)
                oldOne=true;
        if(oldOne)
        {
            mob.tell(mob,target,null,"<T-NAME> <T-IS-ARE> already affected by "+name()+".");
            return false;
        }

        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;

        boolean success=profficiencyCheck(mob,0,auto);

        if(success)
        {
            // it worked, so build a copy of this ability,
            // and add it to the affects list of the
            // affected MOB.  Then tell everyone else
            // what happened.
            FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s)"+adjective+" protective shield.^?");
            if(mob.location().okMessage(mob,msg))
            {
                mob.location().send(mob,msg);
                mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,Util.capitalizeAndLower(adjective).trim()+" protective aura of mana surrounds <T-NAME>.");
                beneficialAffect(mob,target,asLevel,0);
            }
        }
        else
            return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke"+adjective+" protective shield, but mess(es) up.");


        // return whether it worked
        return success;
    }
}