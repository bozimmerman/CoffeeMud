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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.CommandJournalFlags;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;

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

public class MOTD extends StdCommand
{
	public MOTD(){}

	private final String[] access=I(new String[]{"MOTD","NEWS"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private static Vector<String> DEFAULT_CMD=new ReadOnlyVector<String>(new String[]{"MOTD","AGAIN"});

	@Override
	public boolean execute(final MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		boolean pause=false;
		String what="MOTD";
		if((commands!=null)&&(commands.size()>0))
		{
			final String firstWord=commands.get(0).toUpperCase();
			if(CMParms.indexOf(this.getAccessWords(), firstWord)>0)
				what=firstWord;
			if((commands.get(commands.size()-1).equalsIgnoreCase("PAUSE")))
			{
				pause = true;
				commands.remove(commands.size()-1);
			}
			if(commands.size()==1)
			{
				commands.add("AGAIN");
			}
		}
		else
			commands=DEFAULT_CMD;
		int max=5;
		if((commands.size()>2)&&(CMath.isInteger(commands.get(commands.size()-1))))
		{
			max=CMath.s_int(commands.get(commands.size()-1));
			commands.remove(commands.size()-1);
		}
		final String parm=CMParms.combine(commands,1).toUpperCase();
		final boolean oldOk = "PREVIOUS".startsWith(parm) || parm.equals("OLD"); 
		if((mob.playerStats()!=null)
		&&(parm.equals("AGAIN")||parm.equals("NEW")||oldOk))
		{
			final StringBuffer buf=new StringBuffer("");
			try
			{
				String msg = new CMFile(Resources.buildResourcePath("text")+"motd.txt",null).text().toString();
				if(msg.length()>0)
				{
					if(msg.startsWith("<cmvp>"))
						msg=new String(CMLib.webMacroFilter().virtualPageFilter(msg.substring(6).getBytes()));
					buf.append(msg+"\n\r--------------------------------------\n\r");
				}

				final MultiList<JournalEntry> multiJournal=new MultiList<JournalEntry>();
				multiJournal.addAll(CMLib.database().DBReadJournalMsgsByUpdateDate("SYSTEM_NEWS", false, max));
				if(max>multiJournal.size())
					multiJournal.addAll(CMLib.database().DBReadJournalMsgsByUpdateDate("CoffeeMud News", false, max-multiJournal.size())); // deprecated
				final ReverseFakeIterator<JournalEntry> entries = new ReverseFakeIterator<JournalEntry>(multiJournal);
				
				//should read these descending, trim to the max, then iterate.
				for(;entries.hasNext() && (max>=0); max--)
				{
					final JournalEntry entry=entries.next();
					final String from=entry.from();
					final long last=entry.date();
					String to=entry.to();
					final String subject=entry.subj();
					String message=entry.msg();
					final long compdate=entry.update();
					if((compdate>mob.playerStats().getLastDateTime())||(oldOk))
					{
						boolean allMine=to.equalsIgnoreCase(mob.Name())
										||from.equalsIgnoreCase(mob.Name());
						if(to.toUpperCase().trim().startsWith("MASK=")&&CMLib.masking().maskCheck(to.trim().substring(5),mob,true))
						{
							allMine=true;
							to=CMLib.masking().maskDesc(to.trim().substring(5),true);
						}
						if(to.equalsIgnoreCase("ALL")||allMine)
						{
							if(message.startsWith("<cmvp>"))
								message=new String(CMLib.webMacroFilter().virtualPageFilter(message.substring(6).getBytes()));
							buf.append("\n\rNews: "+CMLib.time().date2String(last)+"\n\rFROM: "+CMStrings.padRight(from,15)+"\n\rTO  : "+CMStrings.padRight(to,15)+"\n\rSUBJ: "+subject+"\n\r"+message);
							buf.append("\n\r--------------------------------------\n\r");
						}
					}
				}
				final Vector<String> postalChains=new Vector<String>();
				final Vector<String> postalBranches=new Vector<String>();
				PostOffice P=null;
				for(final Enumeration<PostOffice> e=CMLib.map().postOffices();e.hasMoreElements();)
				{
					P=e.nextElement();
					if(!postalChains.contains(P.postalChain()))
						postalChains.add(P.postalChain());
					if(!postalBranches.contains(P.postalBranch()))
						postalBranches.add(P.postalBranch());
				}
				if((postalChains.size()>0)&&(P!=null))
				{
					List<PlayerData> V=CMLib.database().DBReadPlayerData(mob.Name(),postalChains);
					final Map<PostOffice,int[]> res=getPostalResults(V,mob.playerStats().getLastDateTime());
					for(final Iterator<PostOffice> e=res.keySet().iterator();e.hasNext();)
					{
						P=e.next();
						final int[] ct=res.get(P);
						buf.append("\n\r"+report("You have",P,ct));
					}
					final Map<PostOffice,int[]> res2=new Hashtable<PostOffice,int[]>();
					for(final Pair<Clan,Integer> clanPair : CMLib.clans().findPrivilegedClans(mob, Clan.Function.WITHDRAW))
					{
						if(clanPair!=null)
						{
							final Clan C=clanPair.first;
							if(C.getAuthority(clanPair.second.intValue(),Clan.Function.WITHDRAW)!=Clan.Authority.CAN_NOT_DO)
							{
								V=CMLib.database().DBReadPlayerData(C.name(),postalChains);
								if(V.size()>0)
								{
									res2.putAll(getPostalResults(V,mob.playerStats().getLastDateTime()));
								}
							}
							for(final Iterator<PostOffice> e=res2.keySet().iterator();e.hasNext();)
							{
								P=e.next();
								final int[] ct=res2.get(P);
								buf.append("\n\r"+report("Your "+C.getGovernmentName()+" "+C.getName()+" has",P,ct));
							}
						}
					}
					if((res.size()>0)||(res2.size()>0))
						buf.append("\n\r--------------------------------------\n\r");
				}

				final Vector<JournalsLibrary.CommandJournal> myEchoableCommandJournals=new Vector<JournalsLibrary.CommandJournal>();
				for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
				{
					final JournalsLibrary.CommandJournal CMJ=e.nextElement();
					if((CMJ.getFlag(JournalsLibrary.CommandJournalFlags.ADMINECHO)!=null)
					&&((CMSecurity.isJournalAccessAllowed(mob,CMJ.NAME()))
						||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN)))
							myEchoableCommandJournals.add(CMJ);
				}
				boolean CJseparator=false;
				for(int cj=0;cj<myEchoableCommandJournals.size();cj++)
				{
					final JournalsLibrary.CommandJournal CMJ=myEchoableCommandJournals.get(cj);
					final List<JournalEntry> items=CMLib.database().DBReadJournalMsgsNewerThan("SYSTEM_"+CMJ.NAME()+"S", "ALL", mob.playerStats().getLastDateTime());
					if(items!=null)
					{
						for(int i=0;i<items.size();i++)
						{
							final JournalEntry entry=items.get(i);
							final String from=entry.from();
							final String message=entry.msg();
							buf.append("\n\rNEW "+CMJ.NAME()+" from "+from+": "+message+"\n\r");
							CJseparator=true;
						}
					}
				}
				if(CJseparator)
					buf.append("\n\r--------------------------------------\n\r");

				if((mob.isAttributeSet(MOB.Attrib.AUTOFORWARD))
				&&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0))
				{
					final String[] queries=new String[] { mob.Name(),"ALL","MASK=%" };
					final List<JournalEntry> msgs=CMLib.database().DBReadJournalMsgsByUpdateDate(CMProps.getVar(CMProps.Str.MAILBOX), false, max, queries);
					for(int num=0;num<msgs.size();num++)
					{
						final JournalEntry thismsg=msgs.get(num);
						final String to=thismsg.to();
						if(to.equalsIgnoreCase("all")
						||to.equalsIgnoreCase(mob.Name())
						||(to.toUpperCase().trim().startsWith("MASK=")&&CMLib.masking().maskCheck(to.trim().substring(5),mob,true)))
						{
							buf.append(L("\n\r^ZYou have mail waiting. Enter 'EMAIL BOX' to read.^?^.\n\r"));
							break;
						}
					}
				}

				if((CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDPLAYERS))
				&&(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION)))
				{
					final List<String> l=CMLib.login().getExpiredAcctOrCharsList();
					if(l.size()>0)
					{
						buf.append(L("\n\r^XThere are currently @x1 expired "+((CMProps.isUsingAccountSystem())?"accounts":"characters"),""+l.size()));
						buf.append(L(".  Enter LIST EXPIRED to view them.^?^.\n\r"));
					}
				}

				final List<Quest> qQVec=CMLib.quests().getPlayerPersistentQuests(mob);
				if(mob.session()!=null)
				{
					if(buf.length()>0)
					{
						if(qQVec.size()>0)
							buf.append(L("\n\r^HYou are on @x1 quest(s).  Enter QUESTS to see them!.^?^.\n\r",""+qQVec.size()));
						mob.session().wraplessPrintln("\n\r--------------------------------------\n\r"+buf.toString());
						if (pause)
						{
							mob.session().prompt(L("\n\rPress ENTER: "), 10000);
							mob.session().println("\n\r");
						}
					}
					else
					if(qQVec.size()>0)
						buf.append(L("\n\r^HYou are on @x1 quest(s).  Enter QUESTS to see them!.^?^.\n\r",""+qQVec.size()));
					else
					if(parm.equals("AGAIN"))
						mob.session().println(L("No @x1 to re-read.",what));
					
					// check for new commandjournal postings that require a reply-to-self...
					for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
					{
						final JournalsLibrary.CommandJournal CMJ=e.nextElement();
						if(CMJ.getFlag(CommandJournalFlags.MOTD)==null)
							continue;
						final List<JournalEntry> items=CMLib.database().DBReadJournalMsgsNewerThan(CMJ.JOURNAL_NAME(), mob.Name(), -1);
						if((items!=null)&&(items.size()>0))
						{
							final Session session=mob.session();
							if(session.confirm(L("You have messages waiting response in @x1. Read now (y/N)? ", CMJ.NAME()),"N",5000))
							{
								int count=1;
								final Item journalItem=CMClass.getItem("StdJournal");
								journalItem.setName(CMJ.JOURNAL_NAME());
								journalItem.setReadableText("FILTER="+mob.Name());
								while(count<=items.size())
								{
									final CMMsg msg2=CMClass.getMsg(mob,journalItem,null,CMMsg.MSG_READ,null,CMMsg.MSG_READ,""+count,CMMsg.MSG_READ,null);
									msg2.setValue(1);
									journalItem.executeMsg(mob,msg2);
									if(msg2.value()==0)
										break;
									else
									if(msg2.value()<0)
										items.remove(count-1);
									else
									if(msg2.value()>0)
										count++;
								}
							}
						}
					}
				}
			}
			catch(final HTTPRedirectException e)
			{
			}
			return false;
		}
		if(parm.equals("ON"))
		{
			if(mob.isAttributeSet(MOB.Attrib.DAILYMESSAGE))
			{
				mob.setAttribute(MOB.Attrib.DAILYMESSAGE,false);
				mob.tell(L("The daily messages have been turned on."));
			}
			else
			{
				mob.tell(L("The daily messages are already on."));
			}
		}
		else
		if(parm.equals("OFF"))
		{
			if(!mob.isAttributeSet(MOB.Attrib.DAILYMESSAGE))
			{
				mob.setAttribute(MOB.Attrib.DAILYMESSAGE,true);
				mob.tell(L("The daily messages have been turned off."));
			}
			else
			{
				mob.tell(L("The daily messages are already off."));
			}
		}
		else
		{
			mob.tell(L("'@x1' is not a valid parameter.  Try ON, OFF, PREVIOUS, or AGAIN.",parm));
		}
		return false;
	}

	private String report(String whom, PostOffice P, int[] ct)
	{
		String branchName=P.postalBranch();
		if((P instanceof MOB)&&(((MOB)P).getStartRoom()!=null))
			branchName=((MOB)P).getStartRoom().getArea().Name();
		else
		{
			final int x=branchName.indexOf('#');
			if(x>=0)
				branchName=branchName.substring(0,x);
		}
		if(ct[0]>0)
			return L("@x1  @x2 new of @x3 items at the @x4 branch of the @x5 post office.",whom,""+ct[0],""+ct[1],branchName,P.postalChain());
		return L("@x1 @x2 items still waiting at the @x3 branch of the @x4 post office.",whom,""+ct[1],branchName,P.postalChain());
	}

	private Map<PostOffice,int[]> getPostalResults(List<PlayerData> mailData, long newTimeDate)
	{
		final Hashtable<PostOffice,int[]> results=new Hashtable<PostOffice,int[]>();
		PostOffice P=null;
		for(int i=0;i<mailData.size();i++)
		{
			final DatabaseEngine.PlayerData letter=mailData.get(i);
			final String chain=letter.section();
			String branch=letter.key();
			final int x=branch.indexOf(';');
			if(x<0)
				continue;
			branch=branch.substring(0,x);
			P=CMLib.map().getPostOffice(chain,branch);
			if(P==null)
				continue;
			final PostOffice.MailPiece pieces=P.parsePostalItemData(letter.xml());
			int[] ct=results.get(P);
			if(ct==null)
			{
				ct=new int[2];
				results.put(P,ct);
			}
			ct[1]++;
			if(CMath.s_long(pieces.time)>newTimeDate)
				ct[0]++;
		}
		return results;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
