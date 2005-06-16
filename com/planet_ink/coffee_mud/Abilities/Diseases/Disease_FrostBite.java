package com.planet_ink.coffee_mud.Abilities.Diseases;
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

public class Disease_FrostBite extends Disease
{
    public String ID() { return "Disease_FrostBite"; }
    public String name(){ return "Frost Bite";}
    private String where="feet";
    public String displayText(){ return "(Frost bitten "+where+")";}
    protected int canAffectCode(){return CAN_MOBS;}
    protected int canTargetCode(){return CAN_MOBS;}
    public int quality(){return Ability.MALICIOUS;}
    public boolean putInCommandlist(){return false;}
    public int difficultyLevel(){return 1;}
    public int[] limbsAffectable={Race.BODY_EAR,Race.BODY_ANTENEA,Race.BODY_FOOT,Race.BODY_HAND,Race.BODY_NOSE};
    protected int DISEASE_TICKS(){return new Long(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)/2).intValue();}
    protected int DISEASE_DELAY(){return 50;}
    protected String DISEASE_DONE()
    {
        if(tickDown>0)
            return "Your frost bite heals.";
        else
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
                A.invoke(mob,Util.parse(where),mob,true,0);
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
                    choices.addElement(new Integer(limbsAffectable[i]));
            if(choices.size()<=0)
            {
                where="nowhere";
                unInvoke();
            }
            else
                where=Race.BODYPARTSTR[((Integer)choices.elementAt(Dice.roll(1,choices.size(),-1))).intValue()];
        }
    }
    
    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        where=null;
        return super.invoke(mob,commands,givenTarget,auto,asLevel);
    }
}
