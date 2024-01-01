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

import java.io.IOException;
import java.util.*;

/*
   Copyright 2005-2024 Bo Zimmerman

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
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final PlayerStats pStats=mob.playerStats();
		final Session session=mob.session();
		if((pStats==null)||(session==null))
			return false;

		if((commands.size()>2)
		&&("DEFINE".startsWith(commands.get(1).toUpperCase())))
		{
			final String key=commands.get(2).toUpperCase();
			final String define=CMParms.combineQuoted(commands,3);
			if(define.length()==0)
			{
				final String alias = pStats.getAlias(key);
				if(alias==null)
					mob.tell(L("No such alias to delete: @x1",key));
				else
				{
					pStats.delAliasName(key);
					mob.tell(L("Alias @x1 deleted.",key));
				}
				return false;
			}
			else
			{
				if(CMParms.contains(access,key))
				{
					mob.tell(L("You may not alias alias."));
					return false;
				}
				for(int i=0;i<key.length();i++)
				{
					if(!Character.isLetterOrDigit(key.charAt(i)))
					{
						mob.tell(L("Your alias name may only contain letters and numbers without spaces. "));
						return false;
					}
				}
				pStats.setAlias(key, define);
				mob.tell(L("Alias @x1 defined.",key));
				return false;
			}
		}
		if((commands.size()>2)
		&&("COPY".equalsIgnoreCase(commands.get(1))))
		{
			final String whomName=(commands.size()>2)?CMParms.combine(commands,2):"";
			if(whomName.length()==0)
				mob.tell(L("Copy whose aliases?"));
			else
			if(!CMLib.players().playerExists(whomName))
				mob.tell(L("Player '@x1' doesn't exist.",whomName));
			else
			{
				final boolean unloadAfter = CMLib.players().isLoadedPlayer(whomName);
				final MOB M=CMLib.players().getLoadPlayer(whomName);
				if((M!=null)
				&&(M!=mob)
				&&(!mob.isMonster()))
				{
					try
					{
						final Session sess = mob.session();
						final java.util.List<String> changes = new ArrayList<String>();
						final PlayerStats opStats =M.playerStats();
						final PlayerStats mpStats =mob.playerStats();
						if((opStats == null)||(mpStats==null))
							return false;
						for(final String alias : opStats.getAliasNames())
						{
							if(mpStats.getAlias(alias).length()==0)
								changes.add(alias);
						}
						final MOB M1=mob;
						final MOB M2=M;
						if(changes.size()==0)
							mob.tell(L("Your aliases already match @x1s, at least in key words.",M.name()));
						else
						sess.prompt(new InputCallback(InputCallback.Type.CONFIRM,"N",0)
						{
							final Session S=sess;
							final MOB mob=M1;
							final MOB M=M2;
							@Override
							public void showPrompt()
							{
								S.promptPrint(L("\n\rCopy the aliases '@x1' from player @x2 (y/N)? ",
										CMLib.english().toEnglishStringList(changes),
										M.name()));
							}
							@Override
							public void timedOut()
							{
							}
							@Override
							public void callBack()
							{
								if(this.input.equals("Y"))
								{
									if((mob == null)||(M==null))
										return;
									final PlayerStats opStats1 =M.playerStats();
									final PlayerStats mpStats1 =mob.playerStats();
									if((opStats1 == null)||(mpStats1==null))
										return;
									for(final String alias : opStats1.getAliasNames())
									{
										if(mpStats1.getAlias(alias).length()==0)
											mpStats1.setAlias(alias,opStats1.getAlias(alias));
									}
									mob.tell(L("Aliases copied and active."));
								}
							}
						});
					}
					finally
					{
						if(unloadAfter
						&&(M!=null)
						&&((M.session()==null)||(M.session().isStopped())))
							CMLib.players().unloadOfflinePlayer(M);
					}
				}
			}
			return true;
		}
		if((commands.size()>2)
		&&("TEST".equals(commands.get(1).toUpperCase())))
		{
			final String key=commands.get(2).toUpperCase();
			final String rawAliasDefinition=(pStats!=null)?pStats.getAlias(key):"";
			final List<List<String>> executableCommands=new LinkedList<List<String>>();
			if(rawAliasDefinition.length()>0)
			{
				final List<String> cmds = new XVector<String>(commands);
				cmds.remove(0);
				cmds.remove(0);
				cmds.remove(0);
				final boolean[] echo = new boolean[1];
				CMLib.utensils().deAlias(rawAliasDefinition, cmds, executableCommands, echo);
				mob.tell(L("^HTest of alias '@x1':^N",key));
				int i=1;
				for(final List<String> cmd : executableCommands)
				{
					mob.tell("^H"+CMStrings.padRight(""+i, 2)+"^N: "+CMParms.combineQuoted(cmd, 0));
					i++;
				}
				mob.tell(L("^HEnd of Test"));
				return false;
			}
			else
			{
				mob.tell(L("'@x1' is not a known alias.",key));
				return false;
			}
		}
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
		return false;
	}

}

