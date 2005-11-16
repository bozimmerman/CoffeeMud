package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_WordRecall extends StdAbility
{
    public String ID() { return "Spell_WordRecall"; }
    public String name(){ return "Word of Recall";}
    protected int canAffectCode(){return 0;}
    protected int canTargetCode(){return 0;}
    public int quality(){return Ability.INDIFFERENT;}
    public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
    protected int overrideMana(){return Integer.MAX_VALUE-90;}
    public long flags(){return Ability.FLAG_TRANSPORTING;}

    protected int affectType(boolean auto)
    {
        int affectType=CMMsg.MSK_CAST_VERBAL|CMMsg.TYP_RECALL;
        if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
        return affectType;
    }

    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;

        boolean success=(!mob.isInCombat())||profficiencyCheck(mob,0,auto);
        if(success)
        {
            int AUTO=auto?CMMsg.MASK_GENERAL:0;
            Room recalledRoom=mob.location();
            Room recallRoom=mob.getStartRoom();
            FullMsg msg=new FullMsg(mob,recalledRoom,this,affectType(auto),AUTO|CMMsg.MSG_LEAVE,affectType(auto),auto?getScr(ID(),"recallgo1"):getScr(ID(),"recallgo2"));
            FullMsg msg2=new FullMsg(mob,recallRoom,this,affectType(auto),AUTO|CMMsg.MASK_MOVE|CMMsg.MSG_ENTER,affectType(auto),null);
            if((recalledRoom.okMessage(mob,msg))&&(recallRoom.okMessage(mob,msg2)))
            {
                recalledRoom.send(mob,msg);
                recallRoom.send(mob,msg2);
                if(recalledRoom.isInhabitant(mob))
                    recallRoom.bringMobHere(mob,false);
                for(int f=0;f<mob.numFollowers();f++)
                {
                    MOB follower=mob.fetchFollower(f);
                    
                    msg=new FullMsg(follower,recalledRoom,this,affectType(auto),AUTO|CMMsg.MSG_LEAVE,affectType(auto),auto?getScr(ID(),"recallgo1"):getScr(ID(),"recallgo3",mob.name()));
                    if((follower!=null)
                    &&(follower.isMonster())
                    &&(!follower.isPossessing())
                    &&(follower.location()==recalledRoom)
                    &&(recalledRoom.isInhabitant(follower))
                    &&(recalledRoom.okMessage(follower,msg)))
                    {
                        msg2=new FullMsg(follower,recallRoom,this,affectType(auto),AUTO|CMMsg.MASK_MOVE|CMMsg.MSG_ENTER,affectType(auto),null);
                        if(recallRoom.okMessage(follower,msg2))
                        {
                            recallRoom.send(follower,msg2);
                            if(recalledRoom.isInhabitant(follower))
                                recallRoom.bringMobHere(follower,false);
                        }
                    }
                }
            }
        }
        else
            beneficialWordsFizzle(mob,null,getScr(ID(),"recallgo4"));

        // return whether it worked
        return success;
    }

}
