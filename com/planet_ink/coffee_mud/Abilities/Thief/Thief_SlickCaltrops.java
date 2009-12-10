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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
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
public class Thief_SlickCaltrops extends Thief_Caltrops
{
    public String ID() { return "Thief_SlickCaltrops"; }
    public String name(){ return "Slick Caltrops";}
    private static final String[] triggerStrings = {"SLICKCALTROPS"};
    public String[] triggerStrings(){return triggerStrings;}
    public String caltropTypeName(){return "slick ";}
    public void spring(MOB mob)
    {
        if((!invoker().mayIFight(mob))
        ||(invoker().getGroupMembers(new HashSet()).contains(mob))
        ||(CMLib.dice().rollPercentage()<mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
            mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) some "+caltropTypeName()+"caltrops on the floor.");
        else
            
        {
            Ability A=CMClass.getAbility("Slip");
            if((A!=null)&&(A.castingQuality(invoker(),mob)==Ability.QUALITY_MALICIOUS))
            {
                mob.location().show(invoker(),mob,this,CMMsg.MSG_OK_ACTION,"The "+caltropTypeName()+"caltrops on the ground cause <T-NAME> to slip!");
                if(A.invoke(invoker(),mob,true,adjustedLevel(invoker(),0))) {
                    if(CMLib.dice().rollPercentage()<mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS))
                        CMLib.combat().postDamage(invoker(),mob,null,CMLib.dice().roll(5,6,6*adjustedLevel(invoker(),0)),
                                CMMsg.MASK_MALICIOUS|CMMsg.TYP_JUSTICE,Weapon.TYPE_PIERCING,"The "+caltropTypeName()+"caltrops on the ground <DAMAGE> <T-NAME>.");
                }
            }
        }
        // does not set sprung flag -- as this trap never goes out of use
    }
}