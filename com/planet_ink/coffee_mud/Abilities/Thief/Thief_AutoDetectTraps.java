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
public class Thief_AutoDetectTraps extends ThiefSkill
{
    public String ID() { return "Thief_AutoDetectTraps"; }
    public String displayText() {return "(Autodetecting traps)";}
    public String name(){ return "AutoDetect Traps";}
    protected int canAffectCode(){return CAN_MOBS;}
    protected int canTargetCode(){return 0;}
    public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
    private static final String[] triggerStrings = {"AUTODETECTTRAPS"};
    public int classificationCode(){    return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_ALERT;}
    public String[] triggerStrings(){return triggerStrings;}
    protected boolean noRepeat=false;
    protected String skillName(){return "detect";}

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
        {
            Room R=(Room)msg.target();
            Room R2=null;
            dropem(msg.source(),R);
            Exit E=null;
            Item I=null;
            for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
            {
                R2=R.getRoomInDir(d);
                E=R.getExitInDir(d);
                if((E!=null)&&(CMLib.utensils().fetchMyTrap(E)!=null)) dropem(msg.source(),E);
                E=R.getReverseExit(d);
                if((E!=null)&&(CMLib.utensils().fetchMyTrap(E)!=null)) dropem(msg.source(),E);
                if((R2!=null)&&(CMLib.utensils().fetchMyTrap(R2)!=null)) dropem(msg.source(),R2);
                for(int i=0;i<R.numItems();i++)
                {
                    I=R.fetchItem(i);
                    if((I.container()==null)&&(CMLib.utensils().fetchMyTrap(E)!=null))
                        dropem(msg.source(),I);
                }
            }
        }
    }
    
    public void dropem(MOB mob, Environmental E)
    {
        Ability A=mob.fetchAbility("Thief_IdentifyTraps");
        if(A==null)
            A=mob.fetchAbility("Thief_DetectTraps");
        if(A==null)
        {
            A=CMClass.getAbility("Thief_DetectTraps");
            A.setProficiency(100);
        }
        CharState savedState=(CharState)mob.curState().copyOf();
        A.invoke(mob,E,false,0);
        mob.curState().setMana(savedState.getMana());
        mob.curState().setHitPoints(savedState.getHitPoints());
        mob.curState().setMana(savedState.getMovement());
    }
    
    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        MOB target=(givenTarget instanceof MOB)?(MOB)givenTarget:mob;
        if(target.fetchEffect(ID())!=null)
        {
            target.tell("You are no longer automatically automatically "+skillName()+"ing traps.");
            target.delEffect(mob.fetchEffect(ID()));
            return false;
        }
        if((!auto)&&(target.fetchAbility("Thief_DetectTraps")==null))
        {
            target.tell("You don't know how to detect traps yet!");
            return false;
        }
        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;

        boolean success=proficiencyCheck(mob,0,auto);

        if(success)
        {
            target.tell("You will now automatically "+skillName()+" traps when you enter a room.");
            beneficialAffect(mob,target,asLevel,0);
            Ability A=mob.fetchEffect(ID());
            if(A!=null) A.makeLongLasting();
            dropem(target,target.location());
        }
        else
            beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to "+skillName()+" traps, but can't seem to concentrate.");
        return success;
    }
}