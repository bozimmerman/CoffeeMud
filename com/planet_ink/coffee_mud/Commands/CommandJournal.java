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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2005-2016 Bo Zimmerman

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

public class CommandJournal extends StdCommand
{
	public CommandJournal(){}

	public static String[] access=null;
	@Override
	public String[] getAccessWords()
	{
		if(access!=null)
			return access;
		synchronized(this)
		{
			if(access!=null)
				return access;

			access=new String[CMLib.journals().getNumCommandJournals()];
			int x=0;
			for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
			{
				final JournalsLibrary.CommandJournal CMJ=e.nextElement();
				access[x]=CMJ.NAME();
				x++;
			}
		}
		return access;
	}

	public boolean transfer(MOB mob,
							String journalID,
							String journalWord,
							List<String> commands,
							String security)
	{
		final String first=commands.get(1);
		final String second=(commands.size()>2)?(String)commands.get(2):"";
		final String rest=(commands.size()>3)?CMParms.combine(commands,3):"";
		if(!("TRANSFER".startsWith(first.toUpperCase().trim())))
		   return false;
		if(!CMSecurity.isJournalAccessAllowed(mob,security))
		{
			mob.tell(L("Transfer not allowed."));
			return true;
		}
		if((second.length()>0)&&(!CMath.isNumber(second)))
		{
			mob.tell(L("@x1 is not a number",second));
			return true;
		}
		final int count=CMath.s_int(second);
		final int size=CMLib.database().DBCountJournal(journalID,null,null);
		if(size<=0)
		{
			mob.tell(L("There are no @x1 listed at this time.",journalWord));
			return true;
		}
		if(count>size)
		{
			mob.tell(L("Maximum count of @x1 is @x2.",journalWord,""+size));
			return true;
		}
		String realName=null;
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if(rest.equalsIgnoreCase(CMJ.NAME())
			||rest.equalsIgnoreCase(CMJ.NAME()+"s"))
			{
				realName=CMJ.JOURNAL_NAME();
				break;
			}
		}
		if(realName==null)
			realName=CMLib.database().DBGetRealJournalName(rest);
		if(realName==null)
			realName=CMLib.database().DBGetRealJournalName(rest.toUpperCase());
		if(realName==null)
		{
			mob.tell(L("@x1 is not a journal",rest));
			return true;
		}
		final List<JournalEntry> journal2=CMLib.database().DBReadJournalMsgsByUpdateDate(journalID, true);
		final JournalEntry entry2=journal2.get(count-1);
		final String from2=entry2.from();
		final String to=entry2.to();
		final String subject=entry2.subj();
		final String message=entry2.msg();
		CMLib.database().DBDeleteJournal(journalID,entry2.key());
		CMLib.database().DBWriteJournal(realName,
										  from2,
										  to,
										  subject,
										  message);
		mob.tell(L("Message transferred."));
		return true;
	}

	public boolean review(MOB mob,
						  String journalID,
						  String journalWord,
						  List<String> commands,
						  String security)
	{
		final String first=commands.get(1);
		final String second=(commands.size()>2)?CMParms.combine(commands,2):"";
		if(!("REVIEW".startsWith(first.toUpperCase().trim())))
		   return false;
		if(!CMSecurity.isJournalAccessAllowed(mob,security))
			return false;
		if((second.length()>0)&&(!CMath.isNumber(second)))
			return false;
		int count=CMath.s_int(second);

		final Item journalItem=CMClass.getItem("StdJournal");
		if(journalItem==null)
			mob.tell(L("This feature has been disabled."));
		else
		{
			int size=CMLib.database().DBCountJournal(journalID,null,null);
			if(size<=0)
				mob.tell(L("There are no @x1 listed at this time.",journalWord));
			else
			{
				journalItem.setName(journalID);
				if(count>size)
					mob.tell(L("Maximum count of @x1 is @x2.",journalWord,""+size));
				else
				while(count<=size)
				{
					final CMMsg msg=CMClass.getMsg(mob,journalItem,null,CMMsg.MSG_READ,null,CMMsg.MSG_READ,""+count,CMMsg.MSG_READ,null);
					msg.setValue(1);
					journalItem.executeMsg(mob,msg);
					if(msg.value()==0)
						break;
					else
					if(msg.value()<0)
						size--;
					else
						count++;
				}
			}
		}
		return true;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands==null)||(commands.size()<2))
		{
			mob.tell(L("@x1 what??!?!",((commands==null)||(commands.size()==0))?"":commands.get(0).toString()));
			return false;
		}
		JournalsLibrary.CommandJournal journal=null;
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if(CMJ.NAME().equals(commands.get(0).toUpperCase().trim()))
			{
				journal=CMJ;
				break;
			}
		}
		if(journal==null)
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if(CMJ.NAME().startsWith(commands.get(0).toUpperCase().trim()))
			{
				journal=CMJ;
				break;
			}
		}
		if(journal==null)
		{
			mob.tell(L("!!!!!"));
			return false;
		}
		if((journal.mask().length()>0)
		&&(!CMLib.masking().maskCheck(journal.mask(),mob,true)))
		{
			mob.tell(L("This command is not available to you."));
			return false;
		}
		if((!review(mob,journal.JOURNAL_NAME(),journal.NAME().toLowerCase()+"s",commands,journal.NAME()))
		&&(!transfer(mob,journal.JOURNAL_NAME(),journal.NAME().toLowerCase()+"s",commands,journal.NAME())))
		{
			String msgString=CMParms.combine(commands,1);
			if((mob.session()!=null)&&(!mob.session().isStopped()))
				msgString=CMLib.journals().getScriptValue(mob,journal.NAME(),msgString);
			if(msgString.trim().length()>0)
			{
				if(journal.getFlag(JournalsLibrary.CommandJournalFlags.CONFIRM)!=null)
				{
					if(!mob.session().confirm(L("\n\r^HSubmit this @x1: '^N@x2^H' (Y/n)?^.^N",journal.NAME().toLowerCase(),msgString),"Y"))
						return false;
				}
				String prePend="";
				if(journal.getFlag(JournalsLibrary.CommandJournalFlags.ADDROOM)!=null)
					prePend="(^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(mob.location())+"^</LSTROOMID^>) ";
				CMLib.database().DBWriteJournal(journal.JOURNAL_NAME(),mob.Name(),"ALL",
						CMStrings.padRight("^.^N"+msgString+"^.^N",20),
						prePend+msgString);
				mob.tell(L("Your @x1 message has been sent.  Thank you.",journal.NAME().toLowerCase()));
				if(journal.getFlag(JournalsLibrary.CommandJournalFlags.CHANNEL)!=null)
					CMLib.commands().postChannel(journal.getFlag(JournalsLibrary.CommandJournalFlags.CHANNEL).toUpperCase().trim(),null,L("@x1 posted to @x2: @x3",mob.Name(),journal.NAME(),CMParms.combine(commands,1)),true);
			}
			else
			{
				mob.tell(L("What's the @x1? Be Specific!",journal.NAME().toLowerCase()));
				return false;
			}

		}
		return true;
	}

	@Override public boolean canBeOrdered(){return false;}


}
