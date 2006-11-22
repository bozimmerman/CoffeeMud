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
public class Expire extends StdCommand
{
    public Expire(){}

    public boolean execute(MOB mob, Vector commands)
        throws java.io.IOException
    {
        if(mob.session()==null) return false;
        commands.removeElementAt(0);
        if(commands.size()<1) {
            mob.tell(getScr("Expire","expireformat"));
            return false;
        }
        else 
        if(commands.size()==1)
        {
            String playerName=(String)commands.elementAt(0);
            MOB player=CMLib.map().getLoadPlayer(playerName);
            if((player==null)||(player.playerStats()==null)) 
            {
                mob.tell(getScr("Expire","noplayer",playerName));
                return false;
            }
            long timeLeft=player.playerStats().getAccountExpiration()-System.currentTimeMillis();
            mob.tell(getScr("Expire","playergone",player.Name(),(CMLib.english().returnTime(timeLeft,0))));
            return false;
        }
        else 
        {
            String playerName=(String)commands.elementAt(0);
            long days=CMath.s_long((String)commands.elementAt(1))*1000*60*60*24;
            MOB player=CMLib.map().getLoadPlayer(playerName);
            if((player==null)||(player.playerStats()==null)) 
            {
                mob.tell(getScr("Expire","noplayer",playerName));
                return false;
            }
            player.playerStats().setAccountExpiration(days+System.currentTimeMillis());
            mob.tell(getScr("Expire","daysleft",player.Name(),(CMLib.english().returnTime(player.playerStats().getAccountExpiration()-System.currentTimeMillis(),0))));
            return false;
        }
    }

    private String[] access={getScr("Expire","cmd1")};
    public String[] getAccessWords(){return access;}

    
    public boolean canBeOrdered(){return false;}
    public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS");}

    
}
