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
public class Boot extends StdCommand
{
	public Boot(){}

	private String[] access={getScr("Boot","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(mob.session()==null) return false;
		if(commands.size()==0)
		{
			mob.tell(getScr("Boot","bootwho"));
			return false;
		}
		String whom=CMParms.combine(commands,0);
		boolean boot=false;
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session S=CMLib.sessions().elementAt(s);
			if(((S.mob()!=null)&&(CMLib.english().containsString(S.mob().name(),whom)))
			||(S.getAddress().equalsIgnoreCase(whom)))
			{
				if(S==mob.session())
				{
					mob.tell(getScr("Boot","tryquit"));
					return false;
				}
			    if(S.mob()!=null)
			    {
					mob.tell(getScr("Boot","youboot",S.mob().name()));
					if(S.mob().location()!=null)
						S.mob().location().show(S.mob(),null,CMMsg.MSG_OK_VISUAL,getScr("Boot","bootsom"));
			    }
			    else
			        mob.tell(getScr("Boot","youboot2",S.getAddress()));
				S.setKillFlag(true);
				boot=true;
				break;
			}
		}
		if(!boot)
			mob.tell(getScr("Boot","cantfind"));
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"BOOT");}

	
}
