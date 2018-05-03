package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
public class At extends StdCommand
{
	public At(){}

	private final String[] access=I(new String[]{"AT"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		commands.remove(0);
		if(commands.size()==0)
		{
			mob.tell(L("At where do what?"));
			return false;
		}
		final String cmd=commands.get(0);
		commands.remove(0);
		Room room=CMLib.map().getRoom(cmd.toString());
		if(room == null)
			room=CMLib.map().findWorldRoomLiberally(mob,cmd,"APMIR",100,120000);
		if(room==null)
		{
			if(CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.AT))
				mob.tell(L("At where? Try a Room ID, player name, area name, or room text!"));
			else
				mob.tell(L("You aren't powerful enough to do that."));
			return false;
		}
		if(!CMSecurity.isAllowed(mob,room,CMSecurity.SecFlag.AT))
		{
			mob.tell(L("You aren't powerful enough to do that there."));
			return false;
		}
		final Room R=mob.location();
		if(R!=room)
			room.bringMobHere(mob,false);
		mob.doCommand(new XVector<String>(CMParms.toStringArray(commands)),metaFlags);
		if(mob.location()!=R)
			R.bringMobHere(mob,false);
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.AT);
	}

}
