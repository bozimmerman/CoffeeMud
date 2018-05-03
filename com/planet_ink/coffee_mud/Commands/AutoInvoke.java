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

public class AutoInvoke extends StdCommand
{
	public AutoInvoke(){}

	private final String[] access=I(new String[]{"AUTOINVOKE"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}
	
	protected enum AutoInvokeCommand
	{
		TOGGLE,
		INVOKE,
		UNINVOKE
	}
	
	protected void autoInvoke(MOB mob, Ability foundA, String s, Set<String> effects, AutoInvokeCommand cmd)
	{
		final PlayerStats pStats = mob.playerStats();
		if(foundA==null)
			mob.tell(L("'@x1' is invalid.",s));
		else
		if(effects.contains(foundA.ID()))
		{
			if((cmd == AutoInvokeCommand.UNINVOKE) || (cmd == AutoInvokeCommand.TOGGLE))
			{
				if(pStats != null)
					pStats.addAutoInvokeList(foundA.ID());
				foundA=mob.fetchEffect(foundA.ID());
				if(foundA!=null)
				{
					mob.delEffect(foundA);
					if(mob.fetchEffect(foundA.ID())!=null)
						mob.tell(L("@x1 failed to successfully deactivate.",foundA.name()));
					else
						mob.tell(L("@x1 successfully deactivated.",foundA.name()));
				}
			}
		}
		else
		{
			if((cmd == AutoInvokeCommand.INVOKE) || (cmd == AutoInvokeCommand.TOGGLE))
			{
				if(pStats != null)
					pStats.removeAutoInvokeList(foundA.ID());
				foundA.autoInvocation(mob, true);
				if(mob.fetchEffect(foundA.ID())!=null)
					mob.tell(L("@x1 successfully invoked.",foundA.name()));
				else
					mob.tell(L("@x1 failed to successfully invoke.",foundA.name()));
			}
		}
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final List<Ability> abilities=new Vector<Ability>();
		final Set<String> abilityids=new TreeSet<String>();
		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability A=mob.fetchAbility(a);
			if((A!=null)
			&&(A.isAutoInvoked())
			&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_LANGUAGE)
			&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_PROPERTY))
			{
				abilities.add(A);
				abilityids.add(A.ID());
			}
		}

		final Race R=mob.charStats().getMyRace();
		for(final Ability a : R.racialEffects(mob))
		{
			if(a!=null)
			{
				final Ability A=mob.fetchAbility(a.ID());
				if(A!=null)
				{
					if(abilities.remove(A))
						abilityids.remove(A.ID());
				}
			}
		}
		
		final Set<String> effects=new TreeSet<String>();
		for(int a=0;a<mob.numEffects();a++)
		{
			final Ability A=mob.fetchEffect(a);
			if((A!=null)
			&&(abilityids.contains(A.ID()))
			&&(!A.isSavable()))
				effects.add(A.ID());
		}
		abilityids.clear();

		Collections.sort(abilities,new Comparator<Ability>()
		{
			@Override
			public int compare(Ability o1, Ability o2)
			{
				if(o1==null)
				{
					if(o2==null)
						return 0;
					return -1;
				}
				else
				if(o2==null)
					return 1;
				else
					return o1.name().compareToIgnoreCase(o2.name());
			}
			
		});
		final StringBuffer str=new StringBuffer(L("^xAuto-invoking abilities:^?^.\n\r^N"));
		int col=0;
		for(Ability A : abilities)
		{
			if(A!=null)
			{
				if(effects.contains(A.ID()))
					str.append(L("@x1.^xACTIVE^?^.^N ",CMStrings.padRightWith(A.Name(),'.',29)));
				else
					str.append(L("@x1^xINACTIVE^?^.^N",CMStrings.padRightWith(A.Name(),'.',29)));
				if(++col==2)
				{
					col=0;
					str.append("\n\r");
				}
				else
					str.append("  ");
			}
		}
		if(col==1)
			str.append("\n\r");

		mob.tell(str.toString());
		final Session session=mob.session();
		if(session!=null)
		{
			final AutoInvoke me=this;
			session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
			{
				@Override
				public void showPrompt()
				{
					session.promptPrint(L("Enter one to toggle or RETURN: "));
				}

				@Override
				public void timedOut()
				{
				}

				@Override
				public void callBack()
				{
					String s=this.input;
					if(s.trim().length()==0)
						return;
					AutoInvokeCommand cmd=AutoInvokeCommand.TOGGLE;
					if(s.toUpperCase().startsWith("INVOKE "))
					{
						s=s.substring(7).trim();
						cmd=AutoInvokeCommand.INVOKE;
					}
					else
					if(s.toUpperCase().startsWith("UNINVOKE "))
					{
						s=s.substring(9).trim();
						cmd=AutoInvokeCommand.UNINVOKE;
					}
					boolean startsWith=s.endsWith("*");
					if(startsWith)
						s=s.substring(0,s.length()-1).toLowerCase();
					boolean endsWith=s.startsWith("*");
					if(endsWith)
						s=s.substring(1).toLowerCase();
					if(startsWith || endsWith)
					{
						for(Ability A : abilities)
						{
							if((A!=null)
							&&(A.name().equalsIgnoreCase(s) 
								|| (startsWith && A.name().toLowerCase().startsWith(s))
								|| (endsWith && A.name().toLowerCase().endsWith(s))))
							{
								me.autoInvoke(mob, A, s, effects, cmd);
							}
						}
					}
					else
					if(s.length()>0)
					{
						Ability foundA=null;
						for(Ability A : abilities)
						{
							if((A!=null)
							&&(A.name().equalsIgnoreCase(s) 
								|| (startsWith && A.name().toLowerCase().startsWith(s))
								|| (endsWith && A.name().toLowerCase().endsWith(s))))
							{
								foundA = A;
								break;
							}
						}
						if(foundA==null)
						{
							for(Ability A : abilities)
							{
								if((A!=null)&&(CMLib.english().containsString(A.name(),s)))
								{
									foundA = A;
									break;
								}
							}
						}
						me.autoInvoke(mob, foundA, s, effects, cmd);
					}
					mob.recoverCharStats();
					mob.recoverPhyStats();
					mob.recoverMaxState();
					if(mob.location()!=null)
						mob.location().recoverRoomStats();
					mob.recoverCharStats();
					mob.recoverPhyStats();
					mob.recoverMaxState();
					CMLib.threads().executeRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
							{
								@Override
								public void showPrompt()
								{
									session.promptPrint(L("Enter to continue: "));
								}

								@Override
								public void timedOut()
								{
								}

								@Override
								public void callBack()
								{
									try
									{
										me.execute(mob, commands, metaFlags);
									}
									catch(Exception e)
									{
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
		return true;
	}

}
