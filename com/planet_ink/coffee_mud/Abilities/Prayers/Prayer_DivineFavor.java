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

public class Prayer_DivineFavor extends Prayer
{
    public String ID() { return "Prayer_DivineFavor"; }
    public String name(){ return "Divine Favor";}
    public String displayText(){ return "(Divine Favor)";}
    public int quality(){ return OK_SELF;}
    public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
    protected int canAffectCode(){return Ability.CAN_MOBS;}
    protected int canTargetCode(){return 0;}
    protected boolean struckDownToday=false;

    public void unInvoke()
    {
        // undo the affects of this spell
        if((affected==null)||(!(affected instanceof MOB)))
            return;
        MOB mob=(MOB)affected;

        super.unInvoke();

        if(canBeUninvoked())
            mob.tell("Your fall out of divine favor.");
    }

    public boolean okMessage(Environmental host, CMMsg msg)
    {
        if((msg.source()==affected)
        &&(msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
        &&(msg.source().getWorshipCharID().length()>0))
        {
            if(msg.value()<0)
                msg.setValue(msg.value()/2);
            else
                msg.setValue(msg.value()*2);
        }
        return super.okMessage(host,msg);
    }
    
    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
        if((affected instanceof MOB)
        &&(((MOB)affected).isInCombat())
        &&(!struckDownToday)
        &&(Dice.roll(1,1000,0)==1)
        &&(((MOB)affected).getWorshipCharID().length()>0)
        &&(!((MOB)affected).getVictim().getWorshipCharID().equals(((MOB)affected).getWorshipCharID())))
        {
            MOB deityM=CMMap.getDeity(((MOB)affected).getWorshipCharID());
            if(deityM!=null)
            {
                struckDownToday=true;
                ((MOB)affected).location().showOthers(deityM,((MOB)affected).getVictim(),null,CMMsg.MSG_OK_ACTION,"<S-NAME> strike(s) down <T-NAME> with all of <T-HIS-HER> divine fury!");
                MUDFight.postDeath(deityM,((MOB)affected).getVictim(),null);
            }
        }
        return true;
    }
    
    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        Environmental target=mob;
        if((auto)&&(givenTarget!=null)) target=givenTarget;
        if(target.fetchEffect(this.ID())!=null)
        {
            mob.tell("You are already affected by "+name()+".");
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
            FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) divinely favored.":"^S<S-NAME> "+prayWord(mob)+" for divine favor.^?");
            if(mob.location().okMessage(mob,msg))
            {
                mob.location().send(mob,msg);
                beneficialAffect(mob,target,asLevel,0);
            }
        }
        else
            return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+", but there's no answer.");


        // return whether it worked
        return success;
    }
}
