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
public class Thief_Autocaltrops extends ThiefSkill
{
    public String ID() { return "Thief_Autocaltrops"; }
    public String displayText() {return "(Autocaltropping)";}
    public String name(){ return "AutoCaltrops";}
    protected int canAffectCode(){return CAN_MOBS;}
    protected int canTargetCode(){return 0;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_TRAPPING;}
    public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
    private static final String[] triggerStrings = {"AUTOCALTROPS"};
    public String[] triggerStrings(){return triggerStrings;}
    protected boolean noRepeat=false;

    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        super.executeMsg(myHost,msg);
        if((affected instanceof MOB)
        &&(!noRepeat)
        &&(msg.targetMinor()==CMMsg.TYP_ENTER)
        &&(msg.source()==affected)
        &&(msg.target() instanceof Room)
        &&(msg.tool() instanceof Exit)
        &&(((MOB)affected).location()!=null))
            dropem(msg.source(),(Room)msg.target());
    }
    
    public void dropem(MOB mob, Room R)
    {
        Ability A=mob.fetchAbility("Thief_Caltrops");
        if(A==null)
        {
            A=CMClass.getAbility("Thief_Caltrops");
            A.setProficiency(100);
        }
        int mana=mob.curState().getMana();
        int movement=mob.curState().getMovement();
        A.invoke(mob,R,false,0);
        mob.curState().setMana(mana);
        mob.curState().setMana(movement);
    }
    
    public void unInvoke()
    {
        if((affected instanceof MOB)&&(!((MOB)affected).amDead()))
            ((MOB)affected).tell("You stop throwing down caltrops.");
        super.unInvoke();    
    }

    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        MOB target=(givenTarget instanceof MOB)?(MOB)givenTarget:mob;
        if(target.fetchEffect(ID())!=null)
        {
            target.tell("You are no longer automatically dropping caltrops.");
            target.delEffect(mob.fetchEffect(ID()));
            return false;
        }
        if((!auto)&&(target.fetchAbility("Thief_Caltrops")==null))
        {
            target.tell("You don't know how to make and drop caltrops yet!");
            return false;
        }
        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;

        boolean success=proficiencyCheck(mob,0,auto);

        if(success)
        {
            target.tell("You will now automatically drop caltrops around when you enter a room.");
            beneficialAffect(mob,target,asLevel,5+(3*getXLEVELLevel(mob)));
            dropem(target,target.location());
        }
        else
            beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to prepare some caltrops for quick dropping, but mess(es) up.");
        return success;
    }
}