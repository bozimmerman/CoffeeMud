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
public class Load extends StdCommand
{
	public Load(){}

	private String[] access={getScr("Load","cmd1")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Load","what"));
			return false;
		}
		String what=(String)commands.elementAt(1);
		String name=CMParms.combine(commands,2);
        if(what.equalsIgnoreCase(getScr("Load","cmdfaction")))
        {
            Faction F=CMLib.factions().getFaction(name);
            if(F==null)
                mob.tell(getScr("Load","nofac",name));
            else
                mob.tell(getScr("Load","facloaded",F.name(),name));
            return false;
        }
		if(what.equalsIgnoreCase(getScr("Load","cmdresource")))
		{
			StringBuffer buf=Resources.getFileResource(name,true);
			if((buf==null)||(buf.length()==0))
				mob.tell(getScr("Load","noresource",name));
			else
				mob.tell(getScr("Load","resourceloaded",name));
		}
		else
		if(CMClass.classCode(what)<0)
			mob.tell("'"+what+getScr("Load","noclasstype"));
		else
        {
            try
            {
        		if(CMClass.loadClass(what,name))
                {
        			mob.tell(CMStrings.capitalizeAndLower(what)+" "+name+getScr("Load","classloaded"));
                    return true;
                }
            }
            catch(Throwable t)
            {
                Log.errOut("Load",t.getClass().getName()+": "+t.getMessage());
            }
			mob.tell(CMStrings.capitalizeAndLower(what)+" "+name+getScr("Load","notloaded"));
        }

		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"LOADUNLOAD");}

	
}
