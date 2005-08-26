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
        Vector mypolls=Polls.getMyVoteablePolls(mob,(commands==null));
        Vector myrespolls=Polls.getPollList();
        for(int r=myrespolls.size()-1;r>=0;r--)
            if((!((Polls)myrespolls.elementAt(r)).mayISeeResults(mob))
            ||(mypolls.contains(myrespolls.elementAt(r))))
                myrespolls.removeElementAt(r);
        if((mypolls.size()==0)&&(myrespolls.size()==0)&&(commands!=null))
        {
            mob.tell("No polls are available at this time.");
            return false;
        }
        for(int i=0;i<mypolls.size();i++)
        {
            Polls P=(Polls)mypolls.elementAt(i);
            P.processVote(mob);
            if(P.mayISeeResults(mob))
                P.processResults(mob);
            if(i<(mypolls.size()-1))
                mob.session().prompt("Press ENTER to continue:");
        }
        if(myrespolls.size()>0)
            mob.tell("\n\rPrevious polling results:\n\r");
        for(int i=0;i<myrespolls.size();i++)
        {
            Polls P=(Polls)myrespolls.elementAt(i);
            if(P.mayISeeResults(mob))
                P.processResults(mob);
            if(i<(myrespolls.size()-1))
                mob.session().prompt("Press ENTER to continue:");
        }
        return false;
    }
    public int ticksToExecute(){return 1;}
    public boolean canBeOrdered(){return false;}

    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
