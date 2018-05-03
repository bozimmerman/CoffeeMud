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

public class PlayerKill extends StdCommand
{
	public PlayerKill(){}

	private final String[] access=I(new String[]{"PLAYERKILL","PKILL","PVP"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if(CMProps.getVar(CMProps.Str.PKILL).startsWith("ALWAYS")
			||CMProps.getVar(CMProps.Str.PKILL).startsWith("NEVER"))
		{
			mob.tell(L("This option has been disabled."));
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell(L("YOU CANNOT TOGGLE THIS FLAG WHILE IN COMBAT!"));
			return false;
		}
		if(mob.isAttributeSet(MOB.Attrib.PLAYERKILL))
		{
			if(CMProps.getVar(CMProps.Str.PKILL).startsWith("ONEWAY"))
			{
				mob.tell(L("Once turned on, this flag may not be turned off again."));
				return false;
			}

			if((mob.session()!=null)
			&&(mob.session().getLastPKFight()>0)
			&&((System.currentTimeMillis()-mob.session().getLastPKFight())<(5*60*1000)))
			{
				mob.tell(L("You'll need to wait a few minutes before you can turn off your PK flag."));
				return false;
			}

			mob.setAttribute(MOB.Attrib.PLAYERKILL,false);
			mob.tell(L("Your playerkill flag has been turned off."));
		}
		else
		if(!mob.isMonster())
		{
			mob.tell(L("Turning on this flag will allow you to kill and be killed by other players."));
			if(CMProps.getVar(CMProps.Str.PKILL).startsWith("ONEWAY"))
				mob.tell(L("Once turned on, this flag may not be turned off again."));
			if(mob.session().confirm(L("Are you absolutely sure (y/N)?"),"N"))
			{
				mob.setAttribute(MOB.Attrib.PLAYERKILL,true);
				mob.tell(L("Your playerkill flag has been turned on."));
			}
			else
				mob.tell(L("Your playerkill flag remains OFF."));
			if(!CMProps.getVar(CMProps.Str.PKILL).startsWith("ONEWAY"))
				mob.tell(L("Both players must have their playerkill flag turned on for sparring."));
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
