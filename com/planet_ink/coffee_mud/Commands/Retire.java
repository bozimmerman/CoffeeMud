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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
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

public class Retire extends StdCommand
{
	public Retire()
	{
	}

	private final String[]	access	= I(new String[] { "RETIRE" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final Session session=mob.session();
		if(session==null)
			return false;
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null)
			return false;

		mob.tell(L("^HThis will delete your player from the system FOREVER!"));
		session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",120000)
		{
			@Override
			public void showPrompt()
			{
				session.promptPrint(L("If that's what you want, re-enter your password: "));
			}

			@Override
			public void timedOut()
			{
			}

			@Override
			public void callBack()
			{
				if (input.trim().length() == 0)
					return;
				if (!pstats.matchesPassword(input.trim()))
					mob.tell(L("Password incorrect."));
				else
				{
					if (CMSecurity.isDisabled(CMSecurity.DisFlag.RETIREREASON))
					{
						Log.sysOut("Retire", "Retired: " + mob.Name());
						CMLib.achievements().possiblyBumpAchievement(mob, Event.RETIRE, 1);
						CMLib.players().obliteratePlayer(mob, true, false);
						session.logout(true);
					}
					else
					session.prompt(new InputCallback(InputCallback.Type.PROMPT, "")
					{
						@Override
						public void showPrompt()
						{
							session.promptPrint(L("OK.  Please leave us a short message as to why you are deleting this character.  Your answers will be kept confidential, and are for administrative purposes only.\n\r: "));
						}

						@Override
						public void timedOut()
						{
						}

						@Override
						public void callBack()
						{
							Log.sysOut("Retire", "Retired: " + mob.Name() + ": " + this.input);
							CMLib.achievements().possiblyBumpAchievement(mob, Event.RETIRE, 1);
							CMLib.players().obliteratePlayer(mob, true, false);
							session.logout(true);
						}
					});
				}
			}
		});
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
		return false;
	}

}
