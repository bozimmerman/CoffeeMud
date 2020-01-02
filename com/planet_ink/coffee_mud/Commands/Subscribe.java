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
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournal;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class Subscribe extends StdCommand
{
	public Subscribe()
	{
	}

	private final String[] access=I(new String[]{"SUBSCRIBE","SUBSCRIPTIONS"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null)
			return false;
		final Set<String> h=pstats.getSubscriptions();

		if((commands.size()<2)||(commands.get(1).equalsIgnoreCase("list")))
		{
			if(h.size()==0)
				mob.tell(L("You have no journal subscriptions listed.  Use SUBSCRIBE ADD to add more."));
			else
			{
				final StringBuffer str=new StringBuffer(L("Your listed subscriptions are: \n\r"));
				final Set<String> names=new HashSet<String>();
				for (final String code : h)
				{
					if(code.indexOf(':')==3)
					{
						final String name=code.substring(4);
						names.add(name);
					}
				}
				for(final String name : names)
				{
					str.append("^H"+name+"^N: ");
					if(h.contains(" E :"+name))
						str.append(L("Email and In-Game notifications\n\r"));
					else
						str.append(L("In-Game notifications\n\r"));
				}
				mob.tell(str.toString());
			}
		}
		else
		if(commands.get(1).equalsIgnoreCase("ADD"))
		{
			String name=CMParms.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell(L("Add which journal?"));
				return false;
			}
			name=CMStrings.capitalizeAndLower(name);
			final PhysicalAgent A=mob.location().fetchFromMOBRoomFavorsItems(mob, null, name, Wearable.FILTER_UNWORNONLY);
			String journalName=null;
			if(A!=null)
			{
				if((A==null)||(!CMLib.flags().canBeSeenBy(A, mob)))
				{
					mob.tell(L("You don't see '@x1' here.",name));
					return false;
				}
				if((!(A instanceof Item)
				||(!(A instanceof Book)))
				||(!((Book)A).isJournal())
				||(CMLib.journals().isArchonJournalName(((Book)A).Name()))&&(!CMSecurity.isASysOp(mob)))
				{
					mob.tell(L("@x1 is not a suitable journal to subcribe to.",A.Name()));
					return false;
				}
				final Book B=(Book)A;
				journalName=B.Name().toUpperCase().trim();
				if(!B.canRead(mob))
				{
					mob.tell(L("@x1 is not allowed to be subcribed to.",B.Name()));
					return false;
				}
			}
			else
			{
				boolean found=false;
				for(final Enumeration<ForumJournal> f=CMLib.journals().forumJournals();f.hasMoreElements();)
				{
					final ForumJournal F=f.nextElement();
					if((F!=null)
					&&(F.NAME().equalsIgnoreCase(name))
					&&(F.maskCheck(mob, F.readMask())
						||CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.JOURNALS)))
					{
						found=true;
						journalName=F.NAME().toUpperCase().trim();
						break;
					}
				}
				if(!found)
				{
					for(final Enumeration<JournalsLibrary.CommandJournal> c=CMLib.journals().commandJournals();c.hasMoreElements();)
					{
						final JournalsLibrary.CommandJournal F=c.nextElement();
						if((F!=null)
						&&(F.NAME().equalsIgnoreCase(name))
						&&(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.JOURNALS)))
						{
							found=true;
							journalName=F.NAME().toUpperCase().trim();
							break;
						}
					}
				}
				if(!found)
				{
					for(final Enumeration<ForumJournal> f=CMLib.journals().forumJournals();f.hasMoreElements();)
					{
						final ForumJournal F=f.nextElement();
						if((F!=null)
						&&(CMLib.english().containsString(F.NAME(),name))
						&&(F.maskCheck(mob, F.readMask())
							||CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.JOURNALS)))
						{
							found=true;
							journalName=F.NAME().toUpperCase().trim();
							break;
						}
					}
				}
				if(!found)
				{
					for(final Enumeration<JournalsLibrary.CommandJournal> c=CMLib.journals().commandJournals();c.hasMoreElements();)
					{
						final JournalsLibrary.CommandJournal F=c.nextElement();
						if((F!=null)
						&&(CMLib.english().containsString(F.NAME(),name))
						&&(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.JOURNALS)))
						{
							found=true;
							journalName=F.NAME().toUpperCase().trim();
							break;
						}
					}
				}
			}
			if((journalName==null)||(journalName.trim().length()==0))
			{
				mob.tell(L("You don't see the journal '@x1' here.",name));
				return false;
			}
			if(h.contains(" E :"+journalName) && h.contains(" P :"+journalName) )
			{
				mob.tell(L("That journal is already on your subscription list."));
				return false;
			}
			h.add(" P :"+journalName);
			//final Session session=mob.session();
			//final String jName=journalName;
			/*
			if((!h.contains(" E :"+journalName))
			&&(session!=null))
			{
				mob.session().prompt(new InputCallback(InputCallback.Type.CONFIRM,"N",0)
				{
					private final String journalName = jName;
					@Override
					public void showPrompt()
					{
						session.promptPrint(L("Would you also like to receive email notifications (y/N)? "));
					}

					@Override
					public void timedOut()
					{
					}

					@Override
					public void callBack()
					{
						if(this.input.equals("Y"))
							h.add(" E :"+journalName);
						mob.tell(L("The Journal '@x1' has been subscribed to.",journalName));
					}
				});
			}
			else
			*/
			{
				//h.add(" E :"+journalName);
				mob.tell(L("The journal '@x1' has been subscribed to for notifications.",journalName));
			}
		}
		else
		if(commands.get(1).equalsIgnoreCase("REMOVE"))
		{
			final String name=CMParms.combine(commands,2).toUpperCase().trim();
			if(name.length()==0)
			{
				mob.tell(L("Remove which journal?"));
				return false;
			}
			if((!h.contains(" E :"+name))&&(!h.contains(" P :"+name)))
			{
				mob.tell(L("That journal '@x1' does not appear on your list.",name));
				return false;
			}
			h.remove(" E :"+name);
			h.remove(" P :"+name);
			mob.tell(L("The journal '@x1' has been un-subscribed from.",name));
		}
		else
		{
			mob.tell(L("Parameter '@x1' is not recognized.  Try LIST, ADD, or REMOVE.",(commands.get(1))));
			return false;
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
