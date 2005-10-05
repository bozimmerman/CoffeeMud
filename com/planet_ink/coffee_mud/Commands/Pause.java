package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import java.io.IOException;

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
public class Pause extends StdCommand
{
    public Pause(){}

    private String[] access={"PAUSE"};
    public String[] getAccessWords(){return access;}

    public boolean errorOut(MOB mob)
    {
        mob.tell("You are not allowed to do that here.");
        return false;
    }
    
    public boolean execute(MOB mob, Vector commands)
        throws java.io.IOException
    {
        
        String cmd=Util.combine(commands,1);
        if(commands.size()<2)
        {
            if(!CMClass.ThreadEngine().isAllSuspended())
            {
                if(!CMSecurity.isAllowedEverywhere(mob,"PAUSE"))
                    mob.tell("You are not allowed to pause all threads.");
                else
                {
                    CMClass.ThreadEngine().suspendAll();
                    mob.tell("All threads have been suspended. Enter PAUSE again to resume.");
                }
            }
            else
            {
                CMClass.ThreadEngine().resumeAll();
                mob.tell("All threads have been resumed.");
            }
        }
        else
        if(cmd.equalsIgnoreCase("RESUME"))
        {
            if(!CMClass.ThreadEngine().isAllSuspended())
                mob.tell("Threads are not currently suspended.");
            else
            {
                CMClass.ThreadEngine().resumeAll();
                mob.tell("All threads have been resumed.");
            }
        }
        else
        {
            Environmental E=null;
            if(cmd.equalsIgnoreCase("AREA"))
                E=mob.location().getArea();
            else
            if(cmd.equalsIgnoreCase("ROOM"))
                E=mob.location();
            else
                E=mob.location().fetchFromRoomFavorMOBs(null,cmd,Item.WORN_REQ_ANY);
            if(E==null)
                mob.tell("'"+cmd+"' is an unknown object here.");
            else
            if(CMClass.ThreadEngine().isTicking(E,-1))
            {
                CMClass.ThreadEngine().suspendTicking(E,-1);
                mob.tell("Object"+E.name()+"' ticks have been suspended. Enter PAUSE "+cmd.toUpperCase()+" again to resume.");
            }
            else
            {
                CMClass.ThreadEngine().resumeTicking(E,-1);
                mob.tell("Object"+E.name()+"' ticks have been resumed.");
            }
        }
        return false;
    }
    public int ticksToExecute(){return 0;}
    public boolean canBeOrdered(){return true;}
    public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"PAUSE");}

    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}