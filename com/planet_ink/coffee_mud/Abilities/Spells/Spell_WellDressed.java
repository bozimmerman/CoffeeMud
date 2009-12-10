package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_WellDressed extends Spell
{
    public String ID() { return "Spell_WellDressed"; }
    public String name(){return "Well Dressed";}
    public String displayText(){return "(Well Dressed)";}
    public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
    protected int canAffectCode(){return CAN_MOBS;}
    public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}
    protected int dressCode=1;
    
    public void affectCharStats(MOB affected, CharStats affectableStats)
    {
        super.affectCharStats(affected,affectableStats);
        affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+dressCode);
    }


    public void setMiscText(String newText)
    {
        super.setMiscText(newText);
        if(CMath.isInteger(newText)) dressCode=CMath.s_int(newText);
    }
    
    public String text(){return ""+dressCode;}
    
    public void unInvoke()
    {
        if((affected==null)||(!(affected instanceof MOB)))
            return;
       // MOB mob=(MOB)affected;

        super.unInvoke();
        /*
        if(canBeUninvoked())
            if((mob.location()!=null)&&(!mob.amDead()))
                mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> clothe(s) aren't quite as appealing any more.");
        */
    }

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                if((((MOB)target).isInCombat())
                &&(!((MOB)target).charStats().getCurrentClass().baseClass().equalsIgnoreCase("Bard")))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }

   public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        int newDressCode=1;
        MOB target=this.getTarget(mob,commands,givenTarget);
        if(target==null) return false;

        // the invoke method for spells receives as
        // parameters the invoker, and the REMAINING
        // command line parameters, divided into words,
        // and added as String objects to a vector.
        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;


        // now see if it worked
        boolean success=proficiencyCheck(mob,0,auto);

        if(success)
        {
            // it worked, so build a copy of this ability,
            // and add it to the affects list of the
            // affected MOB.  Then tell everyone else
            // what happened.
            CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> speak(s) exquisitely to <T-NAMESELF>.^?");
            if(mob.location().okMessage(mob,msg))
            {
                mob.location().send(mob,msg);
                if(target.location()==mob.location())
                {
                    //target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> <S-IS-ARE> very well dressed.");
                    beneficialAffect(mob,target,asLevel,0);
                    Ability A=target.fetchEffect(ID());
                    if(A!=null) A.setMiscText(""+newDressCode);
                }
            }
        }
        else
            return beneficialWordsFizzle(mob,target,"<S-NAME> speak(s) exquisitely to <T-NAMESELF>, but nothing more happens.");


        // return whether it worked
        return success;
    }
}