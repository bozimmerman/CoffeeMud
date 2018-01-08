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
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
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
public class Email extends StdCommand
{
	public Email(){}

	private final String[] access=I(new String[]{"EMAIL"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob.session()==null)
			return true;
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null)
			return true;

		if((commands!=null)
		&&(commands.size()>1))
		{

			if(CMProps.getVar(CMProps.Str.MAILBOX).length()==0)
			{
				mob.tell(L("A mailbox has not been defined by this muds administrators, so mail can be neither sent, or received."));
				return false;
			}
			int max=10;
			if((commands.get(1).equalsIgnoreCase("BOX") && (commands.size()>2)) && (CMath.isInteger(commands.get(commands.size()-1))))
			{
				max=CMath.s_int(commands.get(commands.size()-1));
				commands.remove(commands.size()-1);
			}
			final String name=CMParms.combine(commands,1);
			if(name.equalsIgnoreCase("BOX"))
			{
				final String journalName=CMProps.getVar(CMProps.Str.MAILBOX);
				final String[] queries=new String[] { mob.Name(),"ALL","MASK=%" };
				final List<JournalEntry> msgs=CMLib.database().DBReadJournalMsgsByUpdateDate(journalName, false, max, queries);
				for(int num=0;num<msgs.size();num++)
				{
					final JournalEntry thismsg=msgs.get(num);
					final String to=thismsg.to();
					if(to.equalsIgnoreCase("ALL")
					||to.equalsIgnoreCase(mob.Name())
					||(to.toUpperCase().trim().startsWith("MASK=")&&CMLib.masking().maskCheck(to.trim().substring(5),mob,true)))
					{
						// keep this one
					}
					else
						msgs.remove(num);
				}
				
				final int[] cols={
						CMLib.lister().fixColWidth(48,mob.session()),
						CMLib.lister().fixColWidth(15,mob.session()),
						CMLib.lister().fixColWidth(20,mob.session())
					};
				while((mob.session()!=null)&&(!mob.session().isStopped()))
				{
					StringBuffer messages=new StringBuffer("^X"+CMStrings.padCenter(mob.Name()+"'s MailBox",cols[0])+"^?^.");
					if(msgs.size()==max)
						messages.append(L(" (Newest @x1 messages)",""+max));
					messages.append("\n\r");
					messages.append("^X### "+CMStrings.padRight(L("From"),cols[1])+" "+CMStrings.padRight(L("Date"),cols[2])+" Subject^?^.\n\r");
					for(int num=0;num<msgs.size();num++)
					{
						final JournalEntry thismsg=msgs.get(num);
						messages.append(CMStrings.padRight(""+(num+1),4)
								+CMStrings.padRight((thismsg.from()),cols[1])+" "
								+CMStrings.padRight(CMLib.time().date2String(thismsg.date()),cols[2])+" "
								+(thismsg.subj())
								+"\n\r");
					}
					if((msgs.size()==0)
					||(CMath.bset(metaFlags,MUDCmdProcessor.METAFLAG_POSSESSED))
					||(CMath.bset(metaFlags,MUDCmdProcessor.METAFLAG_AS)))
					{
						if(!mob.isAttributeSet(MOB.Attrib.AUTOFORWARD))
							mob.tell(L("You have no email waiting, but then, it's probably been forwarded to you already."));
						else
							mob.tell(L("You have no email waiting."));
						return false;
					}
					final Session S=mob.session();
					try
					{
						if(S!=null)
							S.snoopSuspension(1);
						mob.tell(messages.toString());
					}
					finally
					{
						if(S!=null)
							S.snoopSuspension(-1);
					}
					if(mob.session()==null)
						continue;
					String s=mob.session().prompt(L("Enter a message #"),"");
					if((!CMath.isInteger(s))||(mob.session().isStopped()))
						return false;
					final int num=CMath.s_int(s);
					if((num<=0)||(num>msgs.size()))
						mob.tell(L("That is not a valid number."));
					else
					while((mob.session()!=null)&&(!mob.session().isStopped()))
					{
						final JournalEntry thismsg=msgs.get(num-1);
						final String key=thismsg.key();
						final String from=thismsg.from();
						final String date=CMLib.time().date2String(thismsg.date());
						final String subj=thismsg.subj();
						final String message=thismsg.msg();
						messages=new StringBuffer("");
						messages.append("^XMessage :^?^."+num+"\n\r");
						messages.append("^XFrom    :^?^."+from+"\n\r");
						messages.append("^XDate    :^?^."+date+"\n\r");
						messages.append("^XSubject :^?^."+subj+"\n\r");
						messages.append("^X------------------------------------------------^?^.\n\r");
						messages.append(message+"\n\r\n\r");
						try
						{
							if(S!=null)
								S.snoopSuspension(1);
							mob.tell(messages.toString());
						}
						finally
						{
							if(S!=null)
								S.snoopSuspension(-1);
						}
						if(mob.session()==null)
							continue;
						s=mob.session().choose(L("Would you like to D)elete, H)old, or R)eply (D/H/R)? "),"DHR","H");
						if(s.equalsIgnoreCase("H"))
							break;
						if(s.equalsIgnoreCase("R"))
						{
							if((from.length()>0)
							&&(!from.equals(mob.Name()))
							&&(!from.equalsIgnoreCase("BOX"))
							&&(CMLib.players().getLoadPlayer(from)!=null))
								execute(mob,new XVector<String>(getAccessWords()[0],from),metaFlags);
							else
								mob.tell(L("You can not reply to this email."));
						}
						else
						if(s.equalsIgnoreCase("D"))
						{
							CMLib.database().DBDeleteJournal(journalName,key);
							msgs.remove(thismsg);
							mob.tell(L("Deleted."));
							break;
						}
					}
				}
			}
			else
			{
				final MOB M=CMLib.players().getLoadPlayer(name);
				if(M==null)
				{
					mob.tell(L("There is no player called '@x1' to send email to.  If you were trying to read your mail, try EMAIL BOX.  If you were trying to change your email address, just enter EMAIL without any parameters.",name));
					return false;
				}
				if(!M.isAttributeSet(MOB.Attrib.AUTOFORWARD))
				{
					if(!mob.session().confirm(L("Send email to '@x1' (Y/n)?",M.Name()),"Y"))
						return false;
				}
				else
				{
					if(!mob.session().confirm(L("Send email to '@x1', even though their AUTOFORWARD is turned off (y/N)?",M.Name()),"N"))
						return false;
				}
				if(CMProps.getIntVar(CMProps.Int.MAXMAILBOX)>0)
				{
					final int count=CMLib.database().DBCountJournal(CMProps.getVar(CMProps.Str.MAILBOX),null,M.Name());
					if(count>=CMProps.getIntVar(CMProps.Int.MAXMAILBOX))
					{
						mob.tell(L("@x1's mailbox is full.",M.Name()));
						return false;
					}
				}
				if(mob.session()==null)
					return false;
				final String subject=mob.session().prompt(L("Email Subject: "),"").trim();
				if(subject.length()==0)
				{
					mob.tell(L("Aborted"));
					return false;
				}
				if(mob.session()==null)
					return false;
				String message=mob.session().prompt(L("Enter your message\n\r: "),"").trim();
				if(message.trim().length()==0)
				{
					mob.tell(L("Aborted"));
					return false;
				}
				if(mob.session()==null)
					return false;
				message+="\n\r\n\rThis message was sent through the "+CMProps.getVar(CMProps.Str.MUDNAME)+" mail server at "+CMProps.getVar(CMProps.Str.MUDDOMAIN)+", port"+CMProps.getVar(CMProps.Str.MUDPORTS)+".  Please contact the administrators regarding any abuse of this system.\n\r";
				CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.Str.MAILBOX), mob.Name(), M.Name(), subject, message);
				mob.tell(L("Your email has been sent."));
				return true;
			}
		}
		if((pstats.getEmail()==null)||(pstats.getEmail().length()==0))
		{
			if(CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("DISABLED"))
			{
				if(commands!=null)
					mob.session().println(L("\n\rAn email address is not required by this system."));
				return true;
			}
			mob.session().println(L("\n\rYou have no email address on file for this character."));
		}
		else
		{
			if(commands==null)
				return true;
			final String change=mob.session().prompt(L("You currently have '@x1' set as the email address for this character.\n\rChange it (y/N)?",pstats.getEmail()),"N");
			if(change.toUpperCase().startsWith("N"))
				return false;
		}
		if((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
		&&(commands!=null)
		&&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0))
			mob.session().println(L("\n\r** Changing your email address will cause you to be logged off, and a new password to be generated and emailed to the new address. **\n\r"));
		String newEmail=mob.session().prompt(L("New E-mail Address:"));
		if(newEmail==null)
			return false;
		newEmail=newEmail.trim();
		if(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("OPTION"))
		{
			if(newEmail.length()<6)
				return false;
			if(newEmail.indexOf('@')<0)
				return false;
			String confirmEmail=mob.session().prompt(L("Confirm that '@x1' is correct by re-entering.\n\rRe-enter:",newEmail));
			if(confirmEmail==null)
				return false;
			confirmEmail=confirmEmail.trim();
			if(confirmEmail.length()==0)
				return false;
			if(!(newEmail.equalsIgnoreCase(confirmEmail)))
				return false;
		}
		pstats.setEmail(newEmail);
		CMLib.database().DBUpdateEmail(mob);
		if((commands!=null)
		&&(CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
		&&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0))
		{
			final String password=CMLib.encoder().generateRandomPassword();
			pstats.setPassword(password);
			CMLib.database().DBUpdatePassword(mob.Name(),pstats.getPasswordStr());
			CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.Str.MAILBOX),
					  mob.Name(),
					  mob.Name(),
					  "Password for "+mob.Name(),
					  "Your new password for "+mob.Name()+" is: "+password+"\n\rYou can login by pointing your mud client at "+CMProps.getVar(CMProps.Str.MUDDOMAIN)+" port(s):"+CMProps.getVar(CMProps.Str.MUDPORTS)+".\n\rYou may use the PASSWORD command to change it once you are online.");
			mob.tell(L("You will receive an email with your new password shortly.  Goodbye."));
			if(mob.session()!=null)
			{
				CMLib.s_sleep(1000);
				mob.session().stopSession(false,false,false);
			}
		}
		return true;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
