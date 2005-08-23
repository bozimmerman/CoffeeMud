package com.planet_ink.coffee_mud.Behaviors;

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
public class Employer extends StdBehavior
{
    public String ID(){return "Employer";}
    private DVector employees=null;
    
    public DVector employees()
    {
        if(employees!=null) return employees;
        employees=new DVector(3);
        return employees;
    }
    
    public void setParms(String newParms)
    {
        super.setParms(newParms);
    }
    
    public boolean tick(Tickable ticking, int tickID)
    {
        super.tick(ticking,tickID);
        if(tickID!=MudHost.TICK_MOB) return true;
        return true;
    }
    
    public boolean okMessage(Environmental affecting, CMMsg msg)
    {
        if(affecting instanceof MOB)
        {
        }
        return super.okMessage(affecting,msg);
    }
    
    public void allDone(MOB employerM)
    {
    }
    
    public void handleQuit(MOB employerM, MOB employeeM)
    {
        
    }
    
    /** this method defines how this thing responds
     * to environmental changes.  It may handle any
     * and every message listed in the CMMsg interface
     * from the given Environmental source */
    public void executeMsg(Environmental affecting, CMMsg msg)
    {
        super.executeMsg(affecting,msg);
        if(!canActAtAll(affecting)) return;
        if(!(affecting instanceof MOB)) return;
        MOB observer=(MOB)affecting;
        if((msg.sourceMinor()==CMMsg.TYP_QUIT)
        &&(msg.amISource(observer)||msg.amISource(observer.amFollowing())))
           allDone(observer);
        else
        if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
        &&(!msg.amISource(observer))
        &&(!msg.source().isMonster()))
        {
            if(  ((msg.sourceMessage().toUpperCase().indexOf(" HIRING")>0)
                ||(msg.sourceMessage().toUpperCase().indexOf("'HIRE")>0)
                ||(msg.sourceMessage().toUpperCase().indexOf(" JOB")>0)) )
            {
                
                CommonMsgs.say(observer,null,"I'm hiring.",false,false);
            }
            else
            if((msg.sourceMessage().toUpperCase().indexOf(" I QUIT")>0))
            {
                CommonMsgs.say(observer,msg.source(),"Suit yourself.  Goodbye.",false,false);
                handleQuit(observer,msg.source());
            }
        }
    }
}
