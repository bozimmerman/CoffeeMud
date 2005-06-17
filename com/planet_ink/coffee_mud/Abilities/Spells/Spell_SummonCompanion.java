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
public class Spell_SummonCompanion extends Spell
{
    public String ID() { return "Spell_SummonCompanion"; }
    public String name(){return "Summon Companion";}
    protected int canTargetCode(){return 0;}
    public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
    public long flags(){return Ability.FLAG_TRANSPORTING|Ability.FLAG_SUMMONING;}

    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        Room oldRoom=null;
        MOB target=null;
        HashSet H=mob.getGroupMembers(new HashSet());
        if((H.size()==0)||((H.size()==1)&&(H.contains(mob))))
        {
            mob.tell("You don't have any companions!");
            return false;
        }

        boolean allHere=true;
        for(Iterator i=H.iterator();i.hasNext();)
        {
            MOB M=(MOB)i.next();
            if((M!=mob)&&(M.location()!=mob.location())&&(M.location()!=null))
            { 
                allHere=false;
                if((Sense.canAccess(mob,M.location())))
                {
                    target=M;
                    oldRoom=M.location(); 
                    break;
                }
            }
        }
        if((target==null)&&(allHere))
        {
            mob.tell("Better look around first.");
            return false;
        }

        if(target==null)
        {
            mob.tell("You can't seem to fixate on your companions.");
            return false;
        }

        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;

        int adjustment=(target.envStats().level()-mob.envStats().level())*3;
        boolean success=profficiencyCheck(mob,-adjustment,auto);
        
        if(success)
        {
            FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> summon(s) <S-HIS-HER> companion in a mighty cry!^?");
            if((mob.location().okMessage(mob,msg))&&(oldRoom.okMessage(mob,msg)))
            {
                mob.location().send(mob,msg);

                MOB follower=target;
                Room newRoom=mob.location();
                FullMsg enterMsg=new FullMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,("<S-NAME> appear(s) in a burst of light.")+CommonStrings.msp("appear.wav",10));
                FullMsg leaveMsg=new FullMsg(follower,oldRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> disappear(s) in a great summoning swirl created by "+mob.name()+".");
                if(oldRoom.okMessage(follower,leaveMsg))
                {
                    if(newRoom.okMessage(follower,enterMsg))
                    {
                        follower.makePeace();
                        oldRoom.send(follower,leaveMsg);
                        newRoom.bringMobHere(follower,false);
                        newRoom.send(follower,enterMsg);
                        follower.tell("\n\r\n\r");
                        CommonMsgs.look(follower,true);
                    }
                    else
                        mob.tell("Some powerful magic stifles the spell.");
                }
                else
                    mob.tell("Some powerful magic stifles the spell.");
            }

        }
        else
            beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to summon <S-HIS-HER> companion, but fail(s).");

        // return whether it worked
        return success;
    }
}
