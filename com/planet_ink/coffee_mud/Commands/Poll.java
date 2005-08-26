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
public class Poll extends StdCommand
{
    public Poll(){}

    private String[] access={"POLL"};
    public String[] getAccessWords(){return access;}
    public boolean execute(MOB mob, Vector commands)
        throws java.io.IOException
    {
        if((mob==null)||mob.isMonster()) return false;
        Vector[] mypolls=Polls.getMyPolls(mob,(commands==null));
        
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
                    mob.tell(mypolls[1].size()+" polls are presently waiting for your participation.");
                return false;
            }
        }
        
        for(int i=0;i<mypolls[0].size();i++)
        {
            Polls P=(Polls)mypolls[0].elementAt(i);
            P.processVote(mob);
            if(P.mayISeeResults(mob))
            {
                P.processResults(mob);
                mob.session().prompt("Press ENTER to continue:\n\r");
            }
        }
        if(commands==null)
        {
            if(mypolls[1].size()>0)
                mob.tell("\n\r^HThere are "+mypolls[1].size()+" other polls awaiting your participation.^N\n\r");
            if(mypolls[2].size()>0)
                mob.tell("\n\r^HThere are "+mypolls[2].size()+" poll results still available.^N\n\r");
            return true;
        }
        else
        for(int i=0;i<mypolls[1].size();i++)
        {
            Polls P=(Polls)mypolls[1].elementAt(i);
            P.processVote(mob);
            if(P.mayISeeResults(mob))
            {
                P.processResults(mob);
                mob.session().prompt("Press ENTER to continue:");
            }
        }
            
        if(mypolls[2].size()>0)
            mob.tell("\n\r^HPrevious polling results:^N\n\r");
        for(int i=0;i<mypolls[2].size();i++)
        {
            Polls P=(Polls)mypolls[2].elementAt(i);
            if(P.mayISeeResults(mob))
            {
                P.processResults(mob);
                if((i<mypolls[2].size()-1)||(commands==null))
                    mob.session().prompt("Press ENTER to continue:\n\r");
            }
        }
        return false;
    }
    public int ticksToExecute(){return 1;}
    public boolean canBeOrdered(){return false;}

    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
