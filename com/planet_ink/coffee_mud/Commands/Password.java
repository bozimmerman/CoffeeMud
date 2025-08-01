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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2025 Bo Zimmerman

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
public class Password extends StdCommand
{
	public Password()
	{
	}

	private final String[]	access	= I(new String[] { "PASSWORD" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags) throws java.io.IOException
	{
		final PlayerStats pstats = mob.playerStats();
		if (pstats == null)
			return false;
		if (mob.isMonster())
			return false;
		final Session sess = mob.session();
		if (sess != null)
		{
			sess.prompt(new InputCallback(InputCallback.Type.PROMPT)
			{
				@Override
				public void showPrompt()
				{
					sess.changeTelnetMode(Session.TELNET_ECHO, true);
					sess.setClientTelnetMode(Session.TELNET_ECHO, false);
					sess.promptPrint(L("\n\rEnter your old password : "));
				}

				@Override
				public void timedOut()
				{
					sess.changeTelnetMode(Session.TELNET_ECHO, false);
				}

				@Override
				public void callBack()
				{
					final String old = this.input;
					if(old.length()==0)
					{
						sess.changeTelnetMode(Session.TELNET_ECHO, false);
						sess.println(L("\n\rOld password not entered."));
						return;
					}
					sess.prompt(new InputCallback(InputCallback.Type.PROMPT)
					{
						@Override
						public void showPrompt()
						{
							sess.changeTelnetMode(Session.TELNET_ECHO, true);
							sess.setClientTelnetMode(Session.TELNET_ECHO, false);
							sess.promptPrint(L("\n\rEnter a new password    : "));
						}
						@Override
						public void timedOut()
						{
							sess.changeTelnetMode(Session.TELNET_ECHO, false);
						}

						@Override
						public void callBack()
						{
							final String nep = this.input;
							if(nep.length()==0)
							{
								sess.changeTelnetMode(Session.TELNET_ECHO, false);
								sess.println(L("\n\rNew password not entered."));
								return;
							}
							sess.prompt(new InputCallback(InputCallback.Type.PROMPT)
							{
								@Override
								public void showPrompt()
								{
									sess.promptPrint(L("\n\rEnter new password again: "));
								}

								@Override
								public void timedOut()
								{
									sess.changeTelnetMode(Session.TELNET_ECHO, false);
								}

								@Override
								public void callBack()
								{
									sess.changeTelnetMode(Session.TELNET_ECHO, false);
									final String ne2 = this.input;
									if (!pstats.matchesPassword(old))
										mob.tell(L("\n\rYour old password was not entered correctly."));
									else
									if (!nep.equals(ne2))
										mob.tell(L("\n\rYour new password was not entered the same way twice!"));
									else
									if (nep.length()>40)
										mob.tell(L("\n\rYour new password is too long!"));
									else
									{
										pstats.setPassword(nep);
										mob.tell(L("\n\rYour password has been changed."));
										if (pstats.getAccount() != null)
											CMLib.database().DBUpdateAccount(pstats.getAccount());
										CMLib.database().DBUpdatePassword(mob.Name(), pstats.getPasswordStr());
									}
								}
							});
						}
					});
				}
			});
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
