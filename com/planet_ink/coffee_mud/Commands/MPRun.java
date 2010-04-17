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

import org.mozilla.javascript.*;

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
public class MPRun extends StdCommand
{
    public MPRun(){}

    private String[] access={"MPRUN"};
    public String[] getAccessWords(){return access;}
    
    public boolean execute(MOB mob, Vector commands, int metaFlags)
        throws java.io.IOException
    {
    	if(mob.fetchTattoo("SYSTEM_MPRUNDOWN")!=null)
			return CMLib.commands().handleUnknownCommand(mob, commands);
    	MOB checkMOB=mob;
    	if(commands.size()>1)
    	{
    		String firstParm=(String)commands.elementAt(1);
    		int x=firstParm.indexOf(':');
    		if(x>0)
    		{
    			checkMOB=CMLib.players().getLoadPlayer(firstParm.substring(0,x));
    			if(checkMOB==null)
    			{
    				mob.addTattoo(Tickable.TICKS_PER_RLMIN+" SYSTEM_MPRUNDOWN");
    				return CMLib.commands().handleUnknownCommand(mob, commands);
    			}
    			String pw=firstParm.substring(x+1);
    			if(!pw.equalsIgnoreCase(checkMOB.playerStats().password()))
    			{
    				mob.addTattoo((2 * Tickable.TICKS_PER_RLMIN)+" SYSTEM_MPRUNDOWN");
    				return CMLib.commands().handleUnknownCommand(mob, commands);
    			}
    			commands.removeElementAt(1);
    		}
    	}
    	if(!CMSecurity.isAllowed(checkMOB,mob.location(),"JSCRIPTS"))
			return CMLib.commands().handleUnknownCommand(mob, commands);
        if(commands.size()<2)
        {
            mob.tell("mprun (user:password) [script]");
            return false;
        }
        commands.removeElementAt(0);

        String cmd = CMParms.combineWithQuotes(commands, 0);
        executeScript(mob, cmd);
        mob.tell("Completed.");
        return false;
    }

    private void executeScript(MOB mob, String script) 
    {
        ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
        S.setSavable(false);
        S.setVarScope("*");
        S.setScript(script);
        CMMsg msg2=CMClass.getMsg(mob,mob,null,CMMsg.MSG_OK_VISUAL,null,null,"MPRUN");
        S.executeMsg(mob, msg2);
        S.dequeResponses();
        S.tick(mob,Tickable.TICKID_MOB);
    }

    public boolean canBeOrdered(){return false;}
    public boolean securityCheck(MOB mob){return true; }
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
