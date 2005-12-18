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
public class Load extends StdCommand
{
	public Load(){}

	private String[] access={"LOAD"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell("LOAD what? Use LOAD RESOURCE/ABILITY/ITEM/WEAPON/ETC.. [CLASS NAME]");
			return false;
		}
		String what=(String)commands.elementAt(1);
		String name=CMParms.combine(commands,2);
        if(what.equalsIgnoreCase("FACTION"))
        {
            Faction F=CMLib.factions().getFaction(name);
            if(F==null)
                mob.tell("Faction file '"+name+"' was not found.");
            else
                mob.tell("Faction '"+F.name()+"' from file '"+name+"' was loaded.");
            return false;
        }
		if(what.equalsIgnoreCase("RESOURCE"))
		{
			StringBuffer buf=Resources.getFileResource(name,true);
			if((buf==null)||(buf.length()==0))
				mob.tell("Resource '"+name+"' was not found.");
			else
				mob.tell("Resource '"+name+"' was loaded.");
		}
		else
		if(CMClass.classCode(what)<0)
			mob.tell("'"+what+"' is not a valid class type.");
		else
        {
            try
            {
        		if(CMClass.loadClass(what,name))
                {
        			mob.tell(CMStrings.capitalizeAndLower(what)+" "+name+" was loaded.");
                    return true;
                }
            }
            catch(Exception e)
            {
                Log.errOut("Load",e);
            }
			mob.tell(CMStrings.capitalizeAndLower(what)+" "+name+" was not loaded.");
        }

		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"LOADUNLOAD");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
