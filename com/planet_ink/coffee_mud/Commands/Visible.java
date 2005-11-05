package com.planet_ink.coffee_mud.Commands;
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
public class Visible extends StdCommand
{
    public Visible(){}

    private String[] access={"VISIBLE","VIS"};
    public String[] getAccessWords(){return access;}
    
    public static Vector returnOffensiveAffects(Environmental fromMe)
    {
        MOB newMOB=CMClass.getMOB("StdMOB");
        Vector offenders=new Vector();
        for(int a=0;a<fromMe.numEffects();a++)
        {
            Ability A=fromMe.fetchEffect(a);
            if((A!=null)&&(A.canBeUninvoked()))
            {
                try
                {
                    newMOB.recoverEnvStats();
                    A.affectEnvStats(newMOB,newMOB.envStats());
                    if(Sense.isInvisible(newMOB)||Sense.isHidden(newMOB))
                      offenders.addElement(A);
                }
                catch(Exception e)
                {}
            }
        }
        return offenders;
    }

    public boolean execute(MOB mob, Vector commands)
        throws java.io.IOException
    {
        String str="Prop_WizInvis";
        Ability A=mob.fetchEffect(str);
        boolean didSomething=false;
        if(A!=null)
        {
            Command C=CMClass.getCommand("WizInv");
            if((C!=null)&&(C.securityCheck(mob)))
            {
                didSomething=true;
                C.execute(mob,Util.makeVector("WIZINV","OFF"));
            }
        }
        Vector V=returnOffensiveAffects(mob);
        if(V.size()==0)
        {
            if(!didSomething)
            mob.tell("You are not invisible or hidden!");
        }
        else
        for(int v=0;v<V.size();v++)
            ((Ability)V.elementAt(v)).unInvoke();
        return false;
    }
    public int ticksToExecute(){return 0;}
    public boolean canBeOrdered(){return true;}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}