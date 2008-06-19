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
   Copyright 2000-2008 Bo Zimmerman

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
public class TickTock extends StdCommand
{
	public TickTock(){}

	private String[] access={"TICKTOCK"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String s=CMParms.combine(commands,1).toLowerCase();
        try
        {
    		if(CMath.isInteger(s))
    		{
    			int h=CMath.s_int(s);
    			if(h==0) h=1;
    			mob.tell("..tick..tock..");
    			mob.location().getArea().getTimeObj().tickTock(h);
    			mob.location().getArea().getTimeObj().save();
    		}
    		else
    		if(s.startsWith("clantick"))
    			CMLib.clans().tickAllClans();
    		else
    		if(s.startsWith("savethread"))
    			CMLib.mud(-1).executeCommand("FORCE savethread");
            else
            if(s.startsWith("utilithread"))
                CMLib.mud(-1).executeCommand("FORCE utilithread");
    		else
    			mob.tell("Ticktock what?  Enter a number of mud-hours, or clanticks, savethread, or utilithread.");
        }
        catch(Exception e)
        {
            mob.tell("Ticktock failed: "+e.getMessage());
        }
		
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"TICKTOCK");}

	
}
