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
public class Expire extends StdCommand
{
    public Expire(){}

    public boolean execute(MOB mob, Vector commands)
        throws java.io.IOException
    {
        if(mob.session()==null) return false;
        commands.removeElementAt(0);
        if(commands.size()<1) {
            mob.tell("You must use the format EXPIRE [PLAYER NAME] or EXPIRE [PLAYER NAME] [NUMBER OF DAYS]");
            return false;
        }
        else 
        if(commands.size()==1)
        {
            String playerName=(String)commands.elementAt(0);
            MOB player=CMMap.getLoadPlayer(playerName);
            if((player==null)||(player.playerStats()==null)) 
            {
                mob.tell("No player named '"+playerName+"' was found.");
                return false;
            }
            long timeLeft=player.playerStats().getAccountExpiration()-System.currentTimeMillis();
            mob.tell("Player '"+player.Name()+"' currently has "+(Util.returnTime(timeLeft,0))+" left.");
            return false;
        }
        else 
        {
            String playerName=(String)commands.elementAt(0);
            int days=new Integer((String)commands.elementAt(1)).intValue()*1000*60*60*24;
            MOB player=CMMap.getLoadPlayer(playerName);
            if((player==null)||(player.playerStats()==null)) 
            {
                mob.tell("No player named '"+playerName+"' was found.");
                return false;
            }
            player.playerStats().setAccountExpiration(days+System.currentTimeMillis());
            mob.tell("Player '"+player.Name()+"' now has "+(Util.returnTime(player.playerStats().getAccountExpiration()-System.currentTimeMillis(),0))+" days left.");
            return false;
        }
    }

    private String[] access={"EXPIRE"};
    public String[] getAccessWords(){return access;}

    public int ticksToExecute(){return 0;}
    public boolean canBeOrdered(){return false;}
    public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS");}

    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
