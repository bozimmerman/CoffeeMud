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
import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount.AccountFlag;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.CommandJournalFlags;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.MsgMkrCallback;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.MsgMkrResolution;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;

import java.util.*;

/*
   Copyright 2004-2024 Bo Zimmerman

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
	public MOTD()
	{
	}

	private final String[] access=I(new String[]{"MOTD","NEWS"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private static List<String> DEFAULT_CMD=new ReadOnlyVector<String>(new String[]{"MOTD","AGAIN"});

	@Override
	public boolean execute(final MOB mob, List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		boolean pause=false;
		boolean oldOk = false;
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
		oldOk = "PREVIOUS".startsWith(parm) || parm.equals("OLD");
		final PlayerStats pStats = mob.playerStats();
		final CMFile motdFile = new CMFile(Resources.buildResourcePath("text")+"motd.txt",mob);
		if((pStats!=null)
		&&(parm.equals("AGAIN")||parm.equals("NEW")||oldOk))
		{
			final StringBuffer buf=new StringBuffer("");
			try
			{
				String msg = motdFile.text().toString();
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


				//should read these descending, trim to the max, then iterate.
				for(final ReverseFakeIterator<JournalEntry> entries = new ReverseFakeIterator<JournalEntry>(multiJournal)
						;entries.hasNext() && (max>=0); max--)
				{
					final JournalEntry entry=entries.next();
					final String from=entry.from();
					final long last=entry.date();
					String to=entry.to();
					final String subject=entry.subj();
					String message=entry.msg();
					final long compdate=entry.update();
					if((compdate>pStats.getLastDateTime())||(oldOk))
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
							buf.append("\n\rNews: "+CMLib.time().date2String(last)
									 +"\n\rFROM: "+CMStrings.padRight(from,15)
									 +"\n\rTO  : "+CMStrings.padRight(to,15)
									 +"\n\rSUBJ: "+subject+"\n\r"+message);
							buf.append("\n\r--------------------------------------\n\r");
						}
					}
				}

				for(final Pair<Clan,Integer> clanPair : CMLib.clans().findPrivilegedClans(mob, Clan.Function.CLAN_BENEFITS))
				{
					multiJournal.clear();
					multiJournal.addAll(CMLib.database().DBReadJournalMsgsByUpdateDate("CLAN_MOTD_"+clanPair.first.clanID(), false, max));
					for(final ReverseFakeIterator<JournalEntry> entries = new ReverseFakeIterator<JournalEntry>(multiJournal)
							;entries.hasNext() && (max>=0); max--)
					{
						final JournalEntry entry=entries.next();
						final String subject=entry.subj();
						final String message=entry.msg();
						buf.append("\n\r"+subject+":^N\n\r"+message);
						buf.append("\n\r^N--------------------------------------\n\r");
					}
				}

				final List<String> postalChains=new ArrayList<String>();
				final List<String> postalBranches=new ArrayList<String>();
				PostOffice P=null;
				for(final Enumeration<PostOffice> e=CMLib.city().postOffices();e.hasMoreElements();)
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
					final Map<PostOffice,int[]> res=getPostalResults(V,pStats.getLastDateTime());
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
									res2.putAll(getPostalResults(V,pStats.getLastDateTime()));
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

				final List<JournalsLibrary.CommandJournal> myEchoableCommandJournals=new ArrayList<JournalsLibrary.CommandJournal>();
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
					final List<JournalEntry> items=CMLib.database().DBReadJournalMsgsNewerThan("SYSTEM_"+CMJ.NAME()+"S", "ALL", pStats.getLastDateTime());
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

				if(CMProps.getVar(CMProps.Str.MAILBOX).length()>0)
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
				final Session session = mob.session();
				if(session!=null)
				{
					if(buf.length()>0)
					{
						if(qQVec.size()>0)
							buf.append(L("\n\r^HYou are on @x1 quest(s).  Enter QUESTS to see them!.^?^.\n\r",""+qQVec.size()));
						session.wraplessPrintln("\n\r--------------------------------------\n\r"+buf.toString());
						if (pause)
						{
							session.prompt(L("\n\rPress ENTER: "), 10000);
							session.println("\n\r");
						}
					}
					else
					if(qQVec.size()>0)
						session.println(L("\n\r^HYou are on @x1 quest(s).  Enter QUESTS to see them!.^?^.\n\r",""+qQVec.size()));
					else
					if(parm.equals("AGAIN"))
						session.println(L("No @x1 to re-read.",what));

					// check for new commandjournal postings that require a reply-to-self...
					for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
					{
						final JournalsLibrary.CommandJournal CMJ=e.nextElement();
						if(CMJ.getFlag(CommandJournalFlags.MOTD)==null)
							continue;
						final List<JournalEntry> items=CMLib.database().DBReadJournalMsgsNewerThan(CMJ.JOURNAL_NAME(), mob.Name(), -1);
						if((items!=null)&&(items.size()>0))
						{
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
									mob.phyStats().setSensesMask(mob.phyStats().sensesMask()|PhyStats.CAN_SEE_DARK);
									journalItem.executeMsg(mob,msg2);
									mob.recoverPhyStats();
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
					if((pStats.getSubscriptions().size()>0))
					{
						for(String sub : pStats.getSubscriptions())
						{
							if(sub.startsWith(" P :"))
							{
								sub=sub.substring(4).trim();
								final List<JournalEntry> items=CMLib.database().DBReadJournalMsgsNewerThan(sub, null, pStats.getLastDateTime());
								int newPosts = 0;
								int newReplies=0;
								final Map<String,JournalEntry> newEntries = new HashMap<String,JournalEntry>();
								for(final JournalEntry J : items)
									newEntries.put(J.key(), J);
								for(final JournalEntry J : items)
								{
									if((J.to().equalsIgnoreCase(mob.Name())||J.to().equalsIgnoreCase("ALL"))
									&&(!J.from().equalsIgnoreCase(mob.Name())))
									{
										if((J.parent()==null)
										||(J.parent().length()==0))
											newPosts++;
										else
										{
											if(!newEntries.containsKey(J.parent()))
											{
												final JournalEntry E=CMLib.database().DBReadJournalEntry(sub, J.parent());
												if(E!=null)
													newEntries.put(E.key(), E);
											}
											final JournalEntry E=newEntries.get(J.parent());
											if((E!=null)&&(E.from().equalsIgnoreCase(mob.Name())))
												newReplies++;
										}
									}
								}
								if((session!=null)
								&&(!session.isStopped()))
								{
									if((newPosts > 0) && (newReplies > 0))
										session.println(L("The journal @x1 has @x2 new entries and @x3 replies for you.",sub,""+newPosts,""+newReplies));
									else
									if(newPosts > 0)
										session.println(L("The journal @x1 has @x2 new entries.",sub,""+newPosts));
									else
									if(newReplies > 0)
										session.println(L("The journal @x1 has @x2 new replies for you.",sub,""+newReplies));
								}
							}
							else
							if(sub.startsWith(" C :"))
							{
								sub=sub.substring(4).trim();
								final int cn = CMLib.channels().getChannelIndex(sub);
								final int lowestIndex = CMLib.channels().getChannelQueIndex(cn, mob, pStats.getLastDateTime());
								if(lowestIndex >= 0)
								{
									final String channelName = CMLib.channels().getChannel(cn).name();
									final int lastIndex = CMLib.channels().getChannelQuePageEnd(cn, mob);
									session.println(L("You missed the last @x1 message(s) on the @x2 channel.",""+(lastIndex-lowestIndex)+1,channelName));
									/*
									final Command C = CMClass.getCommand("Channel");
									final List<String> cmd = new XVector<String>(channelName,"LAST",(""+(lastIndex-lowestIndex)));
									C.execute(mob, cmd, metaFlags);
									*/
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
		if((parm.equals("SET")||parm.equals("NEW"))
		&&(motdFile.canWrite()))
		{
			final CMFile file=motdFile;
			if((!file.canWrite())
			||(file.isDirectory()))
			{
				mob.tell(L("^xError: You are not authorized to create/modify that file.^N"));
				return false;
			}
			StringBuffer buf=file.textUnformatted();
			final String CR=Resources.getEOLineMarker(buf);
			final List<String> vbuf=Resources.getFileLineVector(buf);
			buf=null;
			mob.tell(L("@x1 has been loaded.\n\r\n\r",file.getName()));
			final String messageTitle="File: "+file.getVFSPathAndName();
			CMLib.journals().makeMessageASync(mob, messageTitle, vbuf, false, new MsgMkrCallback()
			{
				@Override
				public void callBack(final MOB mob, final Session sess, final MsgMkrResolution resolution)
				{
					if(resolution==JournalsLibrary.MsgMkrResolution.SAVEFILE)
					{
						final StringBuffer text=new StringBuffer("");
						for(int i=0;i<vbuf.size();i++)
							text.append((vbuf.get(i))+CR);
						if(file.saveText(text))
						{
							for(final Iterator<String> i=Resources.findResourceKeys(file.getName());i.hasNext();)
								Resources.removeResource(i.next());
							mob.tell(L("MOTD saved."));
						}
						else
							mob.tell(L("^XError: could not save the file!^N^."));
					}
				}
			});
			return false;
		}
		else
		{
			if(motdFile.canWrite())
				mob.tell(L("'@x1' is not a valid parameter.  Try ON, OFF, PREVIOUS, AGAIN, or SET.",parm));
			else
				mob.tell(L("'@x1' is not a valid parameter.  Try ON, OFF, PREVIOUS, or AGAIN.",parm));
		}
		return false;
	}

	private String report(final String whom, final PostOffice P, final int[] ct)
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

	private Map<PostOffice,int[]> getPostalResults(final List<PlayerData> mailData, final long newTimeDate)
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
			P=CMLib.city().getPostOffice(chain,branch);
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
