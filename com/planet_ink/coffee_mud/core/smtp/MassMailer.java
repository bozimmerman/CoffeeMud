package com.planet_ink.coffee_mud.core.smtp;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount.AccountFlag;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.net.*;
import java.util.*;

import com.planet_ink.coffee_mud.core.exceptions.*;
import java.io.*;

/*
   Copyright 2011-2025 Bo Zimmerman

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
public class MassMailer implements Runnable
{
	private final List<MassMail>entries	= new LinkedList<MassMail>();
	private final CMProps		page;
	private final String		domain;
	private final Set<String>	oldEmailComplaints;

	private static class MassMail
	{
		public final JournalEntry	mail;
		public final String			journalName;
		public final String			overrideReplyTo;
		public final boolean		usePrivateRules;

		public MassMail(final JournalEntry mail, final String journalName, final String overrideReplyTo, final boolean usePrivateRules)
		{
			this.mail=mail;
			this.journalName=journalName;
			this.overrideReplyTo=overrideReplyTo;
			this.usePrivateRules=usePrivateRules;
		}
	}

	public MassMailer(final CMProps page, final String domain, final Set<String> oldEmailComplaints)
	{
		this.page=page;
		this.domain=domain;
		this.oldEmailComplaints=oldEmailComplaints;
	}

	public String domainName()
	{
		return domain;
	}

	public int getFailureDays()
	{
		final String s=page.getStr("FAILUREDAYS");
		if(s==null)
			return (365*20);
		final int x=CMath.s_int(s);
		if(x==0)
			return (365*20);
		return x;
	}

	public int getEmailDays()
	{
		final String s=page.getStr("EMAILDAYS");
		if(s==null)
			return (365*20);
		final int x=CMath.s_int(s);
		if(x==0)
			return (365*20);
		return x;
	}

	public boolean deleteEmailIfOld(final String journalName, final String key, final long date, final int days)
	{
		final Calendar IQE=Calendar.getInstance();
		IQE.setTimeInMillis(date);
		IQE.add(Calendar.DATE,days);
		if(IQE.getTimeInMillis()<System.currentTimeMillis())
		{
			// email is a goner
			CMLib.database().DBDeleteJournal(journalName, key);
			return true;
		}
		return false;
	}

	public void addMail(final JournalEntry mail, final String journalName, final String overrideReplyTo, final boolean usePrivateRules)
	{
		entries.add(new MassMail(mail,journalName,overrideReplyTo,usePrivateRules));
	}

	protected boolean rightTimeToSendEmail(final long email)
	{
		final long curr=System.currentTimeMillis();
		final Calendar IQE=Calendar.getInstance();
		IQE.setTimeInMillis(email);
		final Calendar IQC=Calendar.getInstance();
		IQC.setTimeInMillis(curr);
		if(CMath.absDiff(email,curr)<(30*60*1000))
			return true;
		while(IQE.before(IQC))
		{
			if(CMath.absDiff(IQE.getTimeInMillis(),IQC.getTimeInMillis())<(30*60*1000))
				return true;
			IQE.add(Calendar.DATE,1);
		}
		return false;
	}

	@Override
	public void run()
	{
		//final boolean debugging=CMSecurity.isDebugging(DbgFlag.SMTPCLIENT)||CMSecurity.isDebugging(DbgFlag.SMTPSERVER);
		for(final MassMail entry : entries)
		{
			try
			{
				final JournalEntry mail=entry.mail;
				final String journalName=entry.journalName;
				final String overrideReplyTo=entry.overrideReplyTo;
				final boolean usePrivateRules=entry.usePrivateRules;

				final String key=mail.key();
				final String from=mail.from();
				final String to=mail.to();
				final long date=mail.update();
				final String subj=mail.subj();
				final String msg=mail.msg().trim();

				// check email age
				if((usePrivateRules)
				&&(!CMath.bset(mail.attributes(), JournalEntry.JournalAttrib.PROTECTED.bit))
				&&(deleteEmailIfOld(journalName, key, date, getEmailDays())))
					continue;

				if(to.equalsIgnoreCase("ALL")||(to.toUpperCase().trim().startsWith("MASK=")))
					continue;

				if(!rightTimeToSendEmail(date))
					continue;

				// check for valid recipient
				final String toEmail;
				final String toName;
				if(CMLib.players().playerExistsAllHosts(to))
				{
					MOB toM=CMLib.players().getLoadPlayer(to);
					if(toM == null)
						toM = CMLib.players().getPlayerAllHosts(to);
					if(toM == null)
						continue;
					// check to see if the sender is ignored
					final PlayerStats toMpStats=toM.playerStats();
					if(toMpStats==null)
						continue;
					if(toMpStats.isIgnored(from)
					||(toMpStats.isIgnored("MAIL."+from)))
					{
						// email is ignored
						CMLib.database().DBDeleteJournal(journalName,key);
						continue;
					}
					if(CMLib.players().playerExistsAllHosts(from))
					{
						MOB fromM=CMLib.players().getPlayer(from);
						if(fromM == null)
							fromM=CMLib.players().getPlayerAllHosts(from);
						if(fromM != null)
						{
							if(toMpStats.isIgnored("MAIL",fromM))
							{
								// email is ignored
								CMLib.database().DBDeleteJournal(journalName,key);
								continue;
							}
						}
						else
						{
							fromM = CMLib.players().getLoadPlayer(from);
							if(fromM != null)
							{
								if(toMpStats.isIgnored("MAIL",fromM))
								{
									// email is ignored
									CMLib.database().DBDeleteJournal(journalName,key);
									continue;
								}
								CMLib.players().unloadOfflinePlayer(fromM);
							}
						}
					}
					final PlayerAccount acct=toMpStats.getAccount();
					if((acct != null)
					&&(acct.isSet(AccountFlag.NOAUTOFORWARD)))
						continue;
					if(toM.isAttributeSet(MOB.Attrib.AUTOFORWARD)) // forwarding OFF
						continue;
					if(toM.playerStats().getEmail().length()==0) // no email addy to forward TO
						continue;
					toName=toM.Name();
					toEmail=toM.playerStats().getEmail();
				}
				else
				if(CMLib.players().accountExistsAllHosts(to))
				{
					PlayerAccount P=CMLib.players().getLoadAccount(to);
					if(P == null)
						P=CMLib.players().getAccountAllHosts(to);
					if(P == null)
						continue;
					if((P.getEmail().length()==0)) // no email addy to forward TO
						continue;
					if(P.isSet(AccountFlag.NOAUTOFORWARD))
						continue;
					toName=P.getAccountName();
					toEmail=P.getEmail();
				}
				else
				{
					Log.errOut("SMTPServer","Invalid to address '"+to+"' in email: "+msg);
					CMLib.database().DBDeleteJournal(journalName,key);
					continue;
				}

				SMTPLibrary.SMTPClient SC=null;
				try
				{
					if(CMProps.getVar(CMProps.Str.SMTPSERVERNAME).length()>0)
						SC=CMLib.smtp().getClient(CMProps.getVar(CMProps.Str.SMTPSERVERNAME),SMTPLibrary.DEFAULT_PORT);
					else
						SC=CMLib.smtp().getClient(toEmail);
				}
				catch(final BadEmailAddressException be)
				{
					if((!usePrivateRules)
					&&(!CMath.bset(mail.attributes(), JournalEntry.JournalAttrib.PROTECTED.bit)))
					{
						// email is a goner if its a list
						CMLib.database().DBDeleteJournal(journalName,key);
						continue;
					}
					// otherwise it has its n days
					continue;
				}
				catch(final java.io.IOException ioe)
				{
					if(!oldEmailComplaints.contains(toName))
					{
						oldEmailComplaints.add(toName);
						Log.errOut("SMTPServer","Unable to send '"+toEmail+"' for '"+toName+"': "+ioe.getMessage());
					}
					if(!CMath.bset(mail.attributes(), JournalEntry.JournalAttrib.PROTECTED.bit))
						deleteEmailIfOld(journalName, key, date,getFailureDays());
					continue;
				}

				final String replyTo=(overrideReplyTo!=null)?(overrideReplyTo):from;
				try
				{
					SC.sendMessage(from+"@"+domainName(),
								   replyTo+"@"+domainName(),
								   toEmail,
								   usePrivateRules?toEmail:replyTo+"@"+domainName(),
								   subj,
								   CMLib.coffeeFilter().simpleOutFilter(msg));
					//this email is HISTORY!
					CMLib.database().DBDeleteJournal(journalName, key);
				}
				catch(final java.io.IOException ioe)
				{
					// it has FAILUREDAYS days to get better.
					if(deleteEmailIfOld(journalName, key, date,getFailureDays()))
						Log.errOut("SMTPServer","Permanently unable to send email from '"+from+"@"+domainName()+"' to '"+toEmail+"' for user '"+toName+"': "+ioe.getMessage()+".");
					else
						Log.errOut("SMTPServer","Failure to send from '"+from+"@"+domainName()+"' to '"+toEmail+"' for user '"+toName+"': "+ioe.getMessage()+".");
				}
			}
			catch(final Exception e)
			{
				Log.errOut("MassMailer",e);
			}
		}
	}

}
