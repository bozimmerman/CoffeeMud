package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_Slip extends StdSkill
{
    boolean doneTicking=false;
    public String ID() { return "Skill_Slip"; }
    public String name(){ return "Slip";}
    public String displayText(){ return "(Slipped)";}
    protected int canAffectCode(){return CAN_MOBS;}
    protected int canTargetCode(){return CAN_MOBS;}
    public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
    private static final String[] triggerStrings = {"SLIPPIFY"};
    public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_DIRTYFIGHTING;}
    public long flags(){return Ability.FLAG_MOVING;}
    protected int enhancement=0;
    public int abilityCode(){return enhancement;}
    public void setAbilityCode(int newCode){enhancement=newCode;}
    public int usageType(){return USAGE_MOVEMENT;}

    public void affectEnvStats(Environmental affected, EnvStats affectableStats)
    {
        super.affectEnvStats(affected,affectableStats);
        if(!doneTicking)
            affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
    }

    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        if((affected==null)||(!(affected instanceof MOB)))
            return true;

        MOB mob=(MOB)affected;
        if((doneTicking)&&(msg.amISource(mob)))
            unInvoke();
        else
        if(msg.amISource(mob)&&(msg.sourceMinor()==CMMsg.TYP_STAND))
            return false;
        return true;
    }

    public void unInvoke()
    {
        if((affected==null)||(!(affected instanceof MOB)))
            return;
        MOB mob=(MOB)affected;
        if(canBeUninvoked())
            doneTicking=true;
        super.unInvoke();
        if(canBeUninvoked())
        {
            if((mob.location()!=null)&&(!mob.amDead()))
            {
                CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> regain(s) <S-HIS-HER> feet.");
                if(mob.location().okMessage(mob,msg)&&(!mob.amDead()))
                {
                    mob.location().send(mob,msg);
                    CMLib.commands().postStand(mob,true);
                }
            }
            else
                mob.tell("You regain your feet.");
        }
    }

    public int castingQuality(MOB mob, Environmental target)
    {
        if((mob!=null)&&(target!=null))
        {
            if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
                return Ability.QUALITY_INDIFFERENT;
            if((target instanceof MOB)&&(((MOB)target).riding()!=null))
                return Ability.QUALITY_INDIFFERENT;
            if(CMLib.flags().isInFlight(target))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }

    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        MOB target=this.getTarget(mob,commands,givenTarget);
        if(target==null) return false;

        if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
        {
            mob.tell(target,null,null,"<S-NAME> is already on the floor!");
            return false;
        }
        if(CMLib.flags().isInFlight(target))
        {
            mob.tell(target.name()+" is flying and can't slip!");
            return false;
        }
        if(target.riding()!=null)
        {
            mob.tell("You can't make someone "+target.riding().stateString(target)+" "+target.riding().name()+" slip!");
            return false;
        }
        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;

        int levelDiff=target.envStats().level()-(mob.envStats().level()+(2*getXLEVELLevel(mob)));
        if(levelDiff>0)
            levelDiff=levelDiff*5;
        else
            levelDiff=0;
        levelDiff-=(abilityCode()*mob.charStats().getStat(CharStats.STAT_DEXTERITY));
        int adjustment=(-levelDiff)+(-(35+((int)Math.round((target.charStats().getStat(CharStats.STAT_DEXTERITY)-9.0)*3.0))));
        boolean success=proficiencyCheck(mob,adjustment,auto);
        success=success&&(target.charStats().getBodyPart(Race.BODY_LEG)>0);
        if(success)
        {
            CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?"<T-NAME> slip(s)!":"^F^<FIGHT^><S-NAME> slip(s) <T-NAMESELF>!^</FIGHT^>^?");
            CMLib.color().fixSourceFightColor(msg);
            if(mob.location().okMessage(mob,msg))
            {
                mob.location().send(mob,msg);
                maliciousAffect(mob,target,asLevel,2,-1);
                target.tell("You hit the floor!");
            }
        }
        else
            return maliciousFizzle(mob,target,auto?"<T-NAMESELF> almost slipped, but maintain(s) <T-HIS-HER> balance.":"<S-NAME> attempt(s) to slip <T-NAMESELF>, but fail(s).");
        return success;
    }
}
