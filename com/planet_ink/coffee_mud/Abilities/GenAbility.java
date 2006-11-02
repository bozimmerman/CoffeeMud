package com.planet_ink.coffee_mud.Abilities;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/*
   Copyright 2000-2006 Bo Zimmerman

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

public class GenAbility extends StdAbility
{
    // data should be stored in a common instance object .. something common to all genability of same id, 
    // but diff to others.n  I'm thinking like a DVector, and just have 
    public String ID() { return "GenAbility"; }
    public String Name(){return name();}
    public String name(){ return "a generic ability";}
    public String description(){return "&";}
    public String displayText(){return "Affected list display for "+ID();}
    public static final String[] empty={};
    public String[] triggerStrings(){return empty;}
    public int maxRange(){return 0;}
    public int minRange(){return 0;}
    public boolean isAutoInvoked(){return false;}
    public long flags(){return 0;}
    public int usageType(){return USAGE_MANA;}
    protected int overrideMana(){return -1;} //-1=normal, Integer.MAX_VALUE=all, Integer.MAX_VALUE-100
    public int classificationCode(){ return Ability.ACODE_SKILL; }
    protected int canAffectCode(){return Ability.CAN_AREAS|
                                         Ability.CAN_ITEMS|
                                         Ability.CAN_MOBS|
                                         Ability.CAN_ROOMS|
                                         Ability.CAN_EXITS;}
    protected int canTargetCode(){return Ability.CAN_AREAS|
                                         Ability.CAN_ITEMS|
                                         Ability.CAN_MOBS|
                                         Ability.CAN_ROOMS|
                                         Ability.CAN_EXITS;}
    public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel)
    {
        // dont forget to allow super. calls to Spell.invoke, Chant.invoke, etc.. based on classification?
        return super.invoke(mob,commands,target,auto,asLevel);
    }
    
    public boolean preInvoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
    {
        return true;
    }
    
    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        return;
    }

    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        return true;
    }

    public boolean tick(Tickable ticking, int tickID)
    {
        if((unInvoked)&&(canBeUninvoked()))
            return false;
        return super.tick(ticking,tickID);
    }
    
    // lots of work to be done here
    public int getSaveStatIndex(){return getStatCodes().length;}
    private static final String[] CODES={"CLASS","TEXT"};
    public String[] getStatCodes(){return CODES;}
    protected int getCodeNum(String code){
        for(int i=0;i<CODES.length;i++)
            if(code.equalsIgnoreCase(CODES[i])) return i;
        return -1;
    }
    public String getStat(String code){
        switch(getCodeNum(code))
        {
        case 0: return ID();
        case 1: return text();
        }
        return "";
    }
    public void setStat(String code, String val)
    {
        switch(getCodeNum(code))
        {
        case 0: return;
        case 1: setMiscText(val); break;
        }
    }
    public boolean sameAs(Environmental E)
    {
        if(!(E instanceof StdAbility)) return false;
        for(int i=0;i<CODES.length;i++)
            if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
                return false;
        return true;
    }
}
