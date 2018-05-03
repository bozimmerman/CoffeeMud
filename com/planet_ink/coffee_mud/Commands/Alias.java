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
   Copyright 2005-2018 Bo Zimmerman

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
public class Alias extends StdCommand
{
	private final String[]	access	= I(new String[] { "ALIAS" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final PlayerStats pStats=mob.playerStats();
		final Session session=mob.session();
		if((pStats==null)||(session==null))
			return false;
		final InputCallback IC[]=new InputCallback[1];
		IC[0]=new InputCallback(InputCallback.Type.PROMPT,"",0)
		{
			@Override
			public void showPrompt()
			{
				final StringBuffer menu=new StringBuffer("^xAlias definitions:^.^?\n\r");
				final String[] aliasNames=pStats.getAliasNames();
				for(int i=0;i<aliasNames.length;i++)
					menu.append(CMStrings.padRight((i+1)+". "+aliasNames[i],15)+": "+pStats.getAlias(aliasNames[i])+"\n\r");
				menu.append((aliasNames.length+1)+". Add a new alias\n\r");
				mob.tell(menu.toString());
				session.promptPrint(L("Enter a selection: "));
			}

			@Override
			public void timedOut()
			{
			}

			@Override
			public void callBack()
			{
				if(this.input.length()==0)
					return;
				final int num=CMath.s_int(this.input);
				if(num<=0)
					return;
				if(num<=(pStats.getAliasNames().length))
				{
					final String selection=pStats.getAliasNames()[num-1];
					session.prompt(new InputCallback(InputCallback.Type.CHOOSE,"","MD\n",0)
					{
						@Override
						public void showPrompt()
						{
							session.promptPrint(L("\n\rAlias selected '@x1'.\n\rWould you like to D)elete or M)odify this alias (d/M)? ", selection));
						}

						@Override
						public void timedOut()
						{
						}

						@Override
						public void callBack()
						{
							final String check=this.input;
							if(check.trim().length()==0)
							{
								session.prompt(IC[0].reset());
								return;
							}
							if(check.equals("D"))
							{
								pStats.delAliasName(selection);
								mob.tell(L("Alias deleted."));
								session.prompt(IC[0].reset());
								return;
							}
							modifyAlias(mob,session,pStats,selection,IC);
						}
					});
				}
				else
				{
					session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
					{
						@Override
						public void showPrompt()
						{
							session.promptPrint(L("\n\rEnter a new alias string consisting of letters and numbers only.\n\r: "));
						}

						@Override
						public void timedOut()
						{
						}

						@Override
						public void callBack()
						{
							if(this.input.trim().length()==0)
							{
								session.prompt(IC[0].reset());
								return;
							}
							final String commandStr=this.input.toUpperCase().trim();
							if(pStats.getAlias(commandStr).length()>0)
							{
								mob.tell(L("That alias already exists.  Select it from the menu to delete or modify."));
								session.prompt(IC[0].reset());
								return;
							}
							if(CMParms.contains(access,commandStr))
							{
								mob.tell(L("You may not alias alias."));
								session.prompt(IC[0].reset());
								return;
							}
							for(int i=0;i<commandStr.length();i++)
							{
								if(!Character.isLetterOrDigit(commandStr.charAt(i)))
								{
									mob.tell(L("Your alias name may only contain letters and numbers without spaces. "));
									session.prompt(IC[0].reset());
									return;
								}
							}
							pStats.addAliasName(commandStr);
							modifyAlias(mob,session,pStats,commandStr,IC);
						}
					});
				}
			}
		};
		session.prompt(IC[0]);
		return true;
	}

	public void modifyAlias(final MOB mob, final Session session, final PlayerStats pStats, final String aliasName, final InputCallback[] IC)
	{
		session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
		{
			@Override
			public void showPrompt()
			{
				session.safeRawPrintln(L("\n\rEnter a value for alias '@x1'.  Use ~ to separate commands. Prefix with noecho to turn off command echo.", aliasName));
				session.promptPrint(": ");
			}

			@Override
			public void timedOut()
			{
			}

			@Override
			public void callBack()
			{
				String value=this.input;
				value=CMStrings.replaceAll(value,"<","");
				value=CMStrings.replaceAll(value,"&","");
				if((value.length()==0)&&(pStats.getAlias(aliasName).length()>0))
					mob.tell(L("(No change)"));
				else
				if(value.length()==0)
				{
					mob.tell(L("Aborted."));
					pStats.delAliasName(aliasName);
				}
				else
				{
					pStats.setAlias(aliasName,value);
					mob.tell(L("The alias was successfully changed."));
				}
				session.prompt(IC[0].reset());
			}
		});
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}

