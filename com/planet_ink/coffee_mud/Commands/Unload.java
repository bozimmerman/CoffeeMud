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
public class Unload extends StdCommand
{
	public Unload(){}

	private String[] access={"UNLOAD"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String str=CMParms.combine(commands,1);
		if(str.length()==0)
		{
			mob.tell("UNLOAD what?");
			return false;
		}
		if(((String)commands.elementAt(1)).equalsIgnoreCase("CLASS"))
		{
			if(commands.size()<3)
			{
				mob.tell("Unload which class?");
				return false;
			}
			commands.removeElementAt(0);
			commands.removeElementAt(0);
			for(int i=0;i<commands.size();i++)
			{
				String name=(String)commands.elementAt(0);
				Object O=CMClass.getClass(name);
				if((O==null)||(!CMClass.delClass(O)))
					mob.tell("Class '"+name+"' was not found in the class loader.");
				else
					mob.tell("Class '"+name+"' was unloaded.");
			}
			return false;
		}
		if(str.equalsIgnoreCase("help"))
		{
			CMLib.help().unloadHelpFile(mob);
			return false;
		}
		if(str.equalsIgnoreCase("all"))
		{
			mob.tell("All resources unloaded.");
            CMLib.factions().removeFaction(null);
			Resources.clearResources();
			return false;
		}
        // Faction Unloading
        if(((String)commands.elementAt(1)).equalsIgnoreCase("FACTION"))
        {
            String which=CMParms.combine(commands,2);
            if(which.length()==0) {
                // No factions specified.  That's fine, they must mean ALL FACTIONS!!! hahahahaha
                CMLib.factions().removeFaction(null);
            }
            else
            {
                if(CMLib.factions().removeFaction(which)) {
                    mob.tell("Faction '"+which+"' unloaded.");
                    return false;
                }
                mob.tell("Unknown Faction '"+which+"'.  Use LIST FACTIONS.");
                return false;
            }
        }
		Vector V=Resources.findResourceKeys(str);
		if(V.size()==0)
		{
			mob.tell("Unknown resource '"+str+"'.  Use LIST RESOURCES.");
			return false;
		}
		for(int v=0;v<V.size();v++)
		{
			String key=(String)V.elementAt(v);
			Resources.removeResource(key);
			mob.tell("Resource '"+key+"' unloaded.");
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"LOADUNLOAD");}

	
}
