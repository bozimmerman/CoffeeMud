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
public class Expire extends StdCommand
{
    public Expire(){}

    public boolean execute(MOB mob, Vector commands, int metaFlags)
        throws java.io.IOException
    {
        if(mob.session()==null) return false;
    	AccountStats stats = null;
        MOB M=null;
        commands.removeElementAt(0);
        if(commands.size()<1) {
        	if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1)
	            mob.tell("You must use the format EXPIRE [ACCOUNT NAME] or EXPIRE [ACCOUNT NAME] [NUMBER OF DAYS]");
        	else
	            mob.tell("You must use the format EXPIRE [PLAYER NAME] or EXPIRE [PLAYER NAME] [NUMBER OF DAYS]");
            return false;
        }
        else 
        if(commands.size()==1)
        {
            String playerName=CMStrings.capitalizeAndLower((String)commands.elementAt(0));
        	if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1)
        		stats = CMLib.players().getLoadAccount(playerName);
        	else
        	if(CMLib.players().playerExists(playerName))
        	{
        		M=CMLib.players().getLoadPlayer(playerName);
        		if(M!=null)
        			stats = CMLib.players().getLoadPlayer(playerName).playerStats();
        	}
            if(stats==null)
            {
                mob.tell("No player/account named '"+playerName+"' was found.");
                return false;
            }
            long timeLeft=stats.getAccountExpiration()-System.currentTimeMillis();
            mob.tell("Player '"+playerName+"' currently has "+(CMLib.english().returnTime(timeLeft,0))+" left.");
            return false;
        }
        else 
        {
            long days=CMath.s_long((String)commands.elementAt(1))*1000*60*60*24;
            String playerName=CMStrings.capitalizeAndLower((String)commands.elementAt(0));
        	if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1)
        		stats = CMLib.players().getLoadAccount(playerName);
        	else
        	if(CMLib.players().playerExists(playerName))
        	{
        		M=CMLib.players().getLoadPlayer(playerName);
        		if(M!=null)
	        		stats = M.playerStats();
        	}
            if(stats==null)
            {
                mob.tell("No player/account named '"+playerName+"' was found.");
                return false;
            }
        	stats.setLastUpdated(System.currentTimeMillis());
            stats.setAccountExpiration(days+System.currentTimeMillis());
            mob.tell("Player '"+playerName+"' now has "+(CMLib.english().returnTime(stats.getAccountExpiration()-System.currentTimeMillis(),0))+" days left.");
            return false;
        }
    }

    private String[] access={"EXPIRE"};
    public String[] getAccessWords(){return access;}

    
    public boolean canBeOrdered(){return false;}
    public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS");}

    
}
