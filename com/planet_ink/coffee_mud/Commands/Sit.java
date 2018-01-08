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

public class Sit extends StdCommand
{
	public Sit()
	{
	}

	private final String[]	access	= I(new String[] { "SIT", "REST", "R" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Vector<String> origCmds=new XVector<String>(commands);
		if(CMLib.flags().isSitting(mob))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You are already sitting!"));
			return false;
		}
		final Room R=mob.location();
		if(commands.size()<=1)
		{
			CMMsg msg;
			if(CMLib.flags().isSleeping(mob))
				msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SIT,L("<S-NAME> awake(s) and sit(s) up."));
			else
				msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SIT,L("<S-NAME> sit(s) down and take(s) a rest."));
			if(R.okMessage(mob,msg))
				R.send(mob,msg);
			return false;
		}
		final String possibleRideable=CMParms.combine(commands,1);
		Environmental E=null;
		if(possibleRideable.length()>0)
		{
			E=R.fetchFromRoomFavorItems(null,possibleRideable);
			if(E==null)
				E=R.fetchExit(possibleRideable);
			if((E==null)||(!CMLib.flags().canBeSeenBy(E,mob)))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' here.",possibleRideable));
				return false;
			}
			if(E instanceof MOB)
			{
				final Command C=CMClass.getCommand("Mount");
				if(C!=null)
					return C.execute(mob,commands,metaFlags);
			}
		}
		String mountStr=null;
		if(E instanceof Rideable)
			mountStr=L("<S-NAME> "+((Rideable)E).mountString(CMMsg.TYP_SIT,mob)+" <T-NAME>.");
		else
			mountStr=L("<S-NAME> sit(s) on <T-NAME>.");
		final CMMsg msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_SIT,mountStr);
		if(R.okMessage(mob,msg))
			R.send(mob,msg);
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID());
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
