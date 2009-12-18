package com.planet_ink.coffee_mud.Abilities.Diseases;
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
public class Disease_FrostBite extends Disease
{
    public String ID() { return "Disease_FrostBite"; }
    public String name(){ return "Frost Bite";}
    private String where="feet";
    public String displayText(){ return "(Frost bitten "+where+")";}
    protected int canAffectCode(){return CAN_MOBS;}
    protected int canTargetCode(){return CAN_MOBS;}
    public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
    public boolean putInCommandlist(){return false;}
    public int difficultyLevel(){return 1;}
    public int[] limbsAffectable={Race.BODY_EAR,Race.BODY_ANTENEA,Race.BODY_FOOT,Race.BODY_HAND,Race.BODY_NOSE};
    protected int DISEASE_TICKS(){return (CMProps.getIntVar( CMProps.SYSTEMI_TICKSPERMUDDAY ) / 2);}
    protected int DISEASE_DELAY(){return 50;}
    protected String DISEASE_DONE()
    {
        if(tickDown>0)
            return "Your frost bite heals.";
        return "Your frost bite has cost you dearly.";
    }
    protected String DISEASE_START(){return "^G<S-NAME> <S-IS-ARE> getting frost bite.^?";}
    protected String DISEASE_AFFECT(){return "";}
    public int abilityCode(){return 0;}

    public void unInvoke()
    {
        if((affected instanceof MOB)&&(tickDown<=0))
        {
            MOB mob=(MOB)affected;
            Ability A=CMClass.getAbility("Amputation");
            if(A!=null)
            {
                super.unInvoke();
                A.invoke(mob,CMParms.parse(where),mob,true,0);
                mob.recoverCharStats();
                mob.recoverEnvStats();
                mob.recoverMaxState();
            }
            else
                super.unInvoke();
        }
        else
            super.unInvoke();
    }

    public void affectCharStats(MOB affected, CharStats affectableStats)
    {
        super.affectCharStats(affected,affectableStats);
        if(affected==null) return;
        if(where==null)
        {
            Vector choices=new Vector();
            for(int i=0;i<limbsAffectable.length;i++)
                if(affected.charStats().getBodyPart(limbsAffectable[i])>0)
                    choices.addElement(Integer.valueOf(limbsAffectable[i]));
            if(choices.size()<=0)
            {
                where="nowhere";
                unInvoke();
            }
            else
                where=Race.BODYPARTSTR[((Integer)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1))).intValue()];
        }
    }

    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        where=null;
        return super.invoke(mob,commands,givenTarget,auto,asLevel);
    }
}
