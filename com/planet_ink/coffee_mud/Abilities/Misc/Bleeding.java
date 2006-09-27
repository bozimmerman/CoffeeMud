package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Bleeding extends StdAbility
{
    public String ID() { return "Bleeding"; }
    public String name(){ return "Bleeding";}
    public String displayText(){ return "(Bleeding)";}
    protected int canAffectCode(){return CAN_ITEMS|Ability.CAN_MOBS;}
    protected int canTargetCode(){return 0;}
    public int hpToKeep=-1;
    
    public double healthPct(MOB mob){ return CMath.div(((MOB)affected).curState().getHitPoints(),((MOB)affected).maxState().getHitPoints());}
    
    // TODO: causes; long falls, taking 20% hp hit, special blades, limb loss, min level 15, FLAY gaoler skill
    
    public void affectEnvStats(Environmental affected, EnvStats affectedStats)
    {
        super.affectEnvStats(affected,affectedStats);
        if(affected instanceof MOB)
            affectedStats.setSpeed(affectedStats.speed()*healthPct((MOB)affected));
    }

    public void unInvoke()
    {
        if((affected instanceof MOB)
        &&(canBeUninvoked())
        &&(!((MOB)affected).amDead())
        &&(CMLib.flags().isInTheGame(affected,true)))
            ((MOB)affected).location().show((MOB)affected,null,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> stop(s) bleeding.");
        super.unInvoke();
        
    }
    
    public void affectCharState(MOB affected, CharState affectedState)
    {
        super.affectCharState(affected,affectedState);
        affectedState.setMovement((int)Math.round(affectedState.getMovement()*CMath.div(((MOB)affected).curState().getHitPoints(),((MOB)affected).maxState().getHitPoints())));
        affectedState.setMana((int)Math.round(affectedState.getMana()*CMath.div(((MOB)affected).curState().getHitPoints(),((MOB)affected).maxState().getHitPoints())));
    }

    public void  executeMsg(Environmental myHost, CMMsg msg)
    {
        super.executeMsg(myHost,msg);
        if((myHost!=null)
        &&(msg.amITarget(myHost))
        &&(msg.targetMinor()==CMMsg.TYP_HEALING)) 
            hpToKeep=-1;
    }
    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID)) return false;
        if((ticking instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
        {
            if(hpToKeep<=0)
            {
                hpToKeep=((MOB)ticking).curState().getHitPoints();
                ((MOB)ticking).recoverMaxState();
                if(((MOB)ticking).curState().getMana()>((MOB)ticking).maxState().getMana())
                    ((MOB)ticking).curState().setMana(((MOB)ticking).maxState().getMana());
                if(((MOB)ticking).curState().getMovement()>((MOB)ticking).maxState().getMovement())
                    ((MOB)ticking).curState().setMovement(((MOB)ticking).maxState().getMovement());
            }
            else
            if(((MOB)ticking).curState().getHitPoints()>hpToKeep)
                ((MOB)ticking).curState().setHitPoints(hpToKeep);
        }
        return true;
    }
}
