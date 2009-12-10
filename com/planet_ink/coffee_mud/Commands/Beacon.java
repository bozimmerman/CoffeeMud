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
public class Beacon extends StdCommand
{
	public Beacon(){}

	private String[] access={"BEACON"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			if(mob.getStartRoom()==mob.location())
				mob.tell("This is already your beacon.");
			else
			{
				mob.setStartRoom(mob.location());
				mob.tell("You have modified your beacon.");
			}
		}
		else
		{
			String name=CMParms.combine(commands,0);
			MOB M=null;
			for(int s=0;s<CMLib.sessions().size();s++)
			{
				Session S=CMLib.sessions().elementAt(s);
				if((S.mob()!=null)&&(CMLib.english().containsString(S.mob().name(),name)))
				{ M=S.mob(); break;}
			}
			if(M==null)
			{
				mob.tell("No one is online called '"+name+"'!");
				return false;
			}
			if(M.getStartRoom()==M.location())
			{
				mob.tell(M.name()+" is already at their beacon.");
				return false;
			}
			if(!CMSecurity.isAllowed(mob,M.location(),"BEACON"))
			{
				mob.tell("You cannot beacon "+M.name()+" there.");
				return false;
			}
			M.setStartRoom(M.location());
			mob.tell("You have modified "+M.name()+"'s beacon.");
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"BEACON");}

	
}
