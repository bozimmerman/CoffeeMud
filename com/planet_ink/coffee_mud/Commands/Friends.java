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
public class Friends extends StdCommand
{
	public Friends(){}

	private String[] access={getScr("Friends","cmd1")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		HashSet h=pstats.getFriends();

		if((commands.size()<2)||(((String)commands.elementAt(1)).equalsIgnoreCase(getScr("Friends","cmdlist"))))
		{
			if(h.size()==0)
				mob.tell(getScr("Friends","errmsg"));
			else
			{
				StringBuffer str=new StringBuffer(getScr("Friends","yourfriends"));
				for(Iterator e=h.iterator();e.hasNext();)
					str.append(((String)e.next())+" ");
				mob.tell(str.toString());
			}
		}
		else
		if(((String)commands.elementAt(1)).equalsIgnoreCase(getScr("Friends","cmdadd")))
		{
			String name=CMParms.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell(getScr("Friends","whom"));
				return false;
			}
			MOB M=CMClass.getMOB("StdMOB");
			if(name.equalsIgnoreCase(getScr("Friends","all")))
				M.setName(getScr("Friends","descall"));
			else
			if(!CMLib.database().DBUserSearch(M,name))
			{
				mob.tell(getScr("Friends","nofound"));
                M.destroy();
				return false;
			}
			if(h.contains(M.Name()))
			{
				mob.tell(getScr("Friends","already"));
                M.destroy();
				return false;
			}
			h.add(M.Name());
			mob.tell(getScr("Friends","added",M.Name()));
            M.destroy();
		}
		else
		if(((String)commands.elementAt(1)).equalsIgnoreCase(getScr("Friends","cmdremove")))
		{
			String name=CMParms.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell(getScr("Friends","remwhom"));
				return false;
			}
			if(!h.contains(name))
			{
				mob.tell(getScr("Friends","nolist",name));
				return false;
			}
			h.remove(name);
			mob.tell(getScr("Friends","removed",name));
		}
		else
		{
			mob.tell(getScr("Friends","noparm",((String)commands.elementAt(1))));
			return false;
		}
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
