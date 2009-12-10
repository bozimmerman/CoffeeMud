package com.planet_ink.coffee_mud.Abilities.Songs;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Skill_Befriend extends BardSkill
{
    public String ID() { return "Skill_Befriend"; }
    public String name(){ return "Befriend";}
    protected int canAffectCode(){return 0;}
    protected int canTargetCode(){return CAN_MOBS;}
    public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
    private static final String[] triggerStrings = {"BEFRIEND"};
    public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_INFLUENTIAL;}
    public int usageType(){return USAGE_MANA;}

    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        if(commands.size()<1)
        {
            mob.tell("You must specify someone to befriend!");
            return false;
        }
        MOB target=getTarget(mob,commands,givenTarget);
        if(target==null) return false;
        
        if(target==mob)
        {
            mob.tell("You are already your own friend.");
            return false;
        }
        if(target.envStats().level()>mob.envStats().level()+(mob.envStats().level()/10))
        {
            mob.tell(target.charStats().HeShe()+" is a bit too powerful to befriend.");
            return false;
        }
        if(!CMLib.flags().isMobile(target))
        {
            mob.tell("You can only befriend fellow travellers.");
            return false;
        }
        
        if(!target.isMonster())
        {
            mob.tell("You need to ask "+target.charStats().himher());
            return false;
        }
        
        if(target.amFollowing()!=null)
        {
            mob.tell(target,null,null,"<S-NAME> is already someone elses friend.");
            return false;
        }
        
        if(!target.charStats().getMyRace().racialCategory().equals(mob.charStats().getMyRace().racialCategory()))
        {
            mob.tell(target,null,null,"<S-NAME> is not a fellow "+mob.charStats().getMyRace().racialCategory()+".");
            return false;
        }

        Faction F=CMLib.factions().getFaction(CMLib.factions().AlignID());
        if(F!=null)
        {
            int his=target.fetchFaction(F.factionID());
            int mine=target.fetchFaction(F.factionID());
            if(F.fetchRange(his)!=F.fetchRange(mine))
            {
                mob.tell(target,null,null,"<S-NAME> is not "+F.fetchRangeName(mine)+", like yourself.");
                return false;
            }
        }
        
        if((!auto)&&(!CMLib.flags().canSpeak(mob)))
        {
            mob.tell("You can't speak!");
            return false;
        }

        // if they can't hear the sleep spell, it
        // won't happen
        if((!auto)&&(!CMLib.flags().canBeHeardBy(mob,target)))
        {
            mob.tell(target.charStats().HeShe()+" can't hear your words.");
            return false;
        }
        
        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;

        int levelDiff=mob.envStats().level()-target.envStats().level();
        if(levelDiff>0) 
            levelDiff=(-(levelDiff*levelDiff))/(1+super.getXLEVELLevel(mob));
        else
            levelDiff=(levelDiff*(-levelDiff))/(1+super.getXLEVELLevel(mob));

        boolean success=proficiencyCheck(mob,levelDiff,auto);
        if(success)
        {
            CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),"<S-NAME> befriend(s) <T-NAME>.");
            if(mob.location().okMessage(mob,msg))
            {
                mob.location().send(mob,msg);
                CMLib.commands().postFollow(target,mob,false);
                CMLib.combat().makePeaceInGroup(mob);
                if(target.amFollowing()!=mob)
                    mob.tell(target.name()+" seems unwilling to be your friend.");
            }
        }
        else
            return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to befriend <T-NAMESELF>, but fail(s).");

        return success;
    }

}