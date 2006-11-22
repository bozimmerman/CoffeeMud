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
public class I3Cmd extends StdCommand
{
	public I3Cmd(){}

	private String[] access={getScr("I3Cmd","cmd1")};
	public String[] getAccessWords(){return access;}

	public void i3Error(MOB mob)
	{
		if(CMSecurity.isAllowed(mob,mob.location(),"I3"))
			mob.tell(getScr("I3Cmd","i3cmds"));
		else
			mob.tell(getScr("I3Cmd","i3cmds2"));
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!(CMLib.intermud().i3online()))
		{
			mob.tell(getScr("I3Cmd","noi3"));
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()<1)
		{
			i3Error(mob);
			return false;
		}
		String str=(String)commands.firstElement();
		if(!(CMLib.intermud().i3online()))
			mob.tell(getScr("I3Cmd","noi3"));
		else
		if(str.equalsIgnoreCase(getScr("I3Cmd","cmdlist")))
			CMLib.intermud().giveI3MudList(mob);
		else
		if(str.equalsIgnoreCase(getScr("I3Cmd","cmdadd")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"I3")){ i3Error(mob); return false;}
			if(commands.size()<2)
			{
				mob.tell(getScr("I3Cmd","nochannel"));
				return false;
			}
			CMLib.intermud().i3channelAdd(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase(getScr("I3Cmd","cmdchannels")))
			CMLib.intermud().giveI3ChannelsList(mob);
		else
		if(str.equalsIgnoreCase(getScr("I3Cmd","cmddelete")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"I3")){ i3Error(mob); return false;}
			if(commands.size()<2)
			{
				mob.tell(getScr("I3Cmd","nochannel"));
				return false;
			}
			CMLib.intermud().i3channelRemove(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase(getScr("I3Cmd","cmdlisten")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"I3")){ i3Error(mob); return false;}
			if(commands.size()<2)
			{
				mob.tell(getScr("I3Cmd","nochannel"));
				return false;
			}
			CMLib.intermud().i3channelListen(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase(getScr("I3Cmd","cmdlocate")))
		{
			if(commands.size()<2)
			{
				mob.tell(getScr("I3Cmd","noname"));
				return false;
			}
			CMLib.intermud().i3locate(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase(getScr("I3Cmd","cmdsilence")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"I3")){ i3Error(mob); return false;}
			if(commands.size()<2)
			{
				mob.tell(getScr("I3Cmd","nochannel"));
				return false;
			}
			CMLib.intermud().i3channelSilence(mob,CMParms.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase(getScr("I3Cmd","cmdinfo")))
			CMLib.intermud().i3mudInfo(mob,CMParms.combine(commands,1));
		else
			i3Error(mob);

		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
