package com.planet_ink.coffee_mud.Commands;
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
public class PollCmd extends StdCommand
{
    public PollCmd(){}

    private String[] access={"POLL"};
    public String[] getAccessWords(){return access;}
    public boolean execute(MOB mob, Vector commands, int metaFlags)
        throws java.io.IOException
    {
        if((mob==null)||mob.isMonster()) return false;
        Vector[] mypolls=CMLib.polls().getMyPolls(mob,(commands==null));
        
        if((mypolls[0].size()==0)&&(mypolls[2].size()==0))
        {
            if((commands!=null)&&(mypolls[1].size()==0))
            {
                mob.tell("No polls are available at this time.");
                return false;
            }
            else
            if(commands==null)
            {
                if(mypolls[1].size()>0)
                    mob.tell(mypolls[1].size()+" poll(s) <-IS-ARE> awaiting your participation.");
                return false;
            }
        }
        
        for(int i=0;i<mypolls[0].size();i++)
        {
            Poll P=(Poll)mypolls[0].elementAt(i);
            CMLib.polls().processVote(P, mob);
            if(P.mayISeeResults(mob))
            {
                CMLib.polls().processResults(P, mob);
                mob.session().prompt("Press ENTER to continue:\n\r");
            }
        }
        if(commands==null)
        {
            if(mypolls[1].size()==1)
                mob.tell("\n\r^H"+mypolls[1].size()+" other poll(s) <-IS-ARE> awaiting your participation.^N\n\r");
            if(mypolls[2].size()>0)
                mob.tell("\n\r^HResults from "+mypolls[2].size()+" poll(s) <-IS-ARE> still available.^N\n\r");
            return true;
        }
        for(int i=0;i<mypolls[1].size();i++)
        {
            Poll P=(Poll)mypolls[1].elementAt(i);
            CMLib.polls().processVote(P, mob);
            if(P.mayISeeResults(mob))
            {
                CMLib.polls().processResults(P, mob);
                mob.session().prompt("Press ENTER to continue:");
            }
        }
            
        if(mypolls[2].size()>0)
            mob.tell("\n\r^HPrevious polling results:^N\n\r");
        for(int i=0;i<mypolls[2].size();i++)
        {
            Poll P=(Poll)mypolls[2].elementAt(i);
            if(P.mayISeeResults(mob))
            {
                CMLib.polls().processResults(P, mob);
                if(i<mypolls[2].size()-1)
                    mob.session().prompt("Press ENTER to continue:\n\r");
            }
        }
        return false;
    }
    
    public boolean canBeOrdered(){return false;}

    
}
