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
public class Thief_HideInPlainSight extends ThiefSkill
{
    public String ID() { return "Thief_HideInPlainSight"; }
    public String name(){ return "Hide In Plain Sight";}
    public String displayText(){ return "";}
    protected int canAffectCode(){return CAN_MOBS;}
    protected int canTargetCode(){return 0;}
    public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
    private static final String[] triggerStrings = {"HIDEINPLAINSITE","HIPS"};
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALTHY;}
    public String[] triggerStrings(){return triggerStrings;}
    public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
    public int code=0;
    public int abilityCode(){return code;}
    public void setAbilityCode(int newCode){code=newCode;}
    public Ability obscureAbility=null;
    
    public Ability makeObscurinator(MOB mob){
        if(obscureAbility!=null) return obscureAbility;
        obscureAbility=CMClass.getAbility("Spell_ObscureSelf");
        if(obscureAbility==null) return null;
        obscureAbility.setAffectedOne(mob);
        return obscureAbility;
    }

    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        if((affected==null)||(!(affected instanceof MOB)))
            return true;
        if((msg.target()==affected)
        &&((msg.targetMinor()==CMMsg.TYP_EXAMINE)||(msg.targetMinor()==CMMsg.TYP_LOOK)))
            return true;
        else
        if((msg.othersMessage()!=null)&&(msg.othersMessage().length()>0))
        {
            if(msg.source()==affected)
            {
                if(!CMath.bset(msg.othersMajor(),CMMsg.MASK_SOUND))
                    msg.setOthersMessage(null);
                else
                if((msg.sourceMinor()!=CMMsg.TYP_SPEAK)
                &&(makeObscurinator(msg.source())!=null))
                    return makeObscurinator(msg.source()).okMessage(myHost,msg);
            }
            else
            if((msg.sourceMinor()!=CMMsg.TYP_SPEAK)
            &&(affected instanceof MOB)
            &&(makeObscurinator((MOB)affected)!=null))
                return makeObscurinator((MOB)affected).okMessage(myHost,msg);
        }
        return true;
    }
    
    
    public void unInvoke()
    {
        MOB M=(MOB)affected;
        super.unInvoke();
        if((M!=null)&&(!M.amDead()))
            M.tell("You are no longer hiding in plain site.");
    }
    
    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        if(mob.fetchEffect(this.ID())!=null)
        {
            Ability A=mob.fetchEffect(ID());
            if(A!=null) A.unInvoke();
            A=mob.fetchEffect(ID());
            if(A!=null)
                mob.tell("You are already hiding in plain site.");
            return false;
        }

        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;

        MOB highestMOB=getHighestLevelMOB(mob,null);
        int levelDiff=mob.envStats().level()-getMOBLevel(highestMOB)-(this.getXLEVELLevel(mob)*2);

        String str="You step to the side and become totally inconspicuous.";

        boolean success=proficiencyCheck(mob,levelDiff*10,auto);

        if(!success)
        {
        	if(highestMOB!=null)
	            beneficialVisualFizzle(mob,highestMOB,"<S-NAME> step(s) to the side of <T-NAMESELF>, but end(s) up looking like an idiot.");
        	else
	            beneficialVisualFizzle(mob,null,"<S-NAME> step(s) to the side and look(s) like an idiot.");
        }
        else
        {
            CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_MOVE),str,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
            if(mob.location().okMessage(mob,msg))
            {
                mob.location().send(mob,msg);
                invoker=mob;
                beneficialAffect(mob,mob,asLevel,0);
                mob.recoverEnvStats();
            }
            else
                success=false;
        }
        return success;
    }
}
