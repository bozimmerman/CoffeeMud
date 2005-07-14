package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
/* 
   Copyright 2005 Robert from The Looking Glass

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
public class Assist extends Bug
{
    public Assist(){}

    private String[] access={"ASSIST"};
    public String[] getAccessWords(){return access;}
    public boolean execute(MOB mob, Vector commands)
        throws java.io.IOException
    {
        if(Util.combine(commands,1).length()>0)
        {
            if(!review(mob,"SYSTEM_ASSIST","assist",commands,"KILLASSIST"))
            {
                CMClass.DBEngine().DBWriteJournal("SYSTEM_ASSIST",mob.Name(),"ALL","ASSIST: "+Util.padRight(Util.combine(commands,1),10),Util.combine(commands,1),-1);
                mob.tell("If a Game Master or Game Host is online, one will be with you shortly!"); /* Customize this line as you wish */
                CommonMsgs.channel("WIZINFO","",mob.Name()+" request assistance!",true);  /* Customize this line as you wish */         
}
        }
        else
            mob.tell("What do you need assistance with?"); /* Customize this line as you wish */
        return false;
    }
    public int ticksToExecute(){return 0;}
    public boolean canBeOrdered(){return false;}

    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
