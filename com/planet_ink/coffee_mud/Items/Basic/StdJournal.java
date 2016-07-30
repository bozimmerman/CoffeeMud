package com.planet_ink.coffee_mud.Items.Basic;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;

import java.util.*;
import java.io.IOException;

/*
   Copyright 2002-2016 Bo Zimmerman

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

public class StdJournal extends StdItem
{
	@Override
	public String ID()
	{
		return "StdJournal";
	}

	protected MOB lastReadTo=null;
	protected long[] lastDateRead={-1,0};

	public StdJournal()
	{
		super();
		setName("a journal");
		setDisplayText("a journal sits here.");
		setDescription("Enter `READ [NUMBER] [JOURNAL]` to read an entry.%0D%0AUse your WRITE skill to add new entries. ");
		material=RawMaterial.RESOURCE_PAPER;
		basePhyStats().setSensesMask(PhyStats.SENSE_ITEMREADABLE);
		recoverPhyStats();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WRITE:
		{
			final String adminReq=getAdminReq().trim();
			final boolean admin=((adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,msg.source(),true))
							||CMSecurity.isJournalAccessAllowed(msg.source(),Name());
			if((!CMLib.masking().maskCheck(getWriteReq(),msg.source(),true))
			&&(!admin)
			&&(!(CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.JOURNALS))))
			{
				msg.source().tell(L("You are not allowed to write on @x1",name()));
				return false;
			}
			return true;
		}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_READ:
			if(!CMLib.flags().canBeSeenBy(this,mob))
				mob.tell(L("You can't see that!"));
			else
			if((!mob.isMonster())
			&&(mob.playerStats()!=null))
			{
				final String adminReq=getAdminReq().trim();
				final boolean admin=((adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,mob,true))
								||CMSecurity.isJournalAccessAllowed(mob,Name());
				final long lastTime=mob.playerStats().getLastDateTime();
				if((!admin)&&(!CMLib.masking().maskCheck(getReadReq(),mob,true)))
				{
					mob.tell(L("You are not allowed to read @x1.",name()));
					return;
				}
				int which=-1;
				boolean newOnly=false;
				boolean all=false;
				final Vector<String> parse=CMParms.parse(msg.targetMessage());
				for(int v=0;v<parse.size();v++)
				{
					final String s=parse.elementAt(v);
					if(CMath.s_long(s)>0)
						which=CMath.s_int(msg.targetMessage());
					else
					if(s.equalsIgnoreCase("NEW"))
						newOnly=true;
					else
					if(s.equalsIgnoreCase("ALL")||s.equalsIgnoreCase("OLD"))
						all=true;
				}
				JournalEntry read=DBRead(mob,Name(),which-1,lastTime, newOnly, all);
				boolean megaRepeat=true;
				while((megaRepeat) && (read!=null))
				{
					megaRepeat=false;
					final String from=read.from();
					StringBuffer entry=read.derivedBuildMessage();
					boolean mineAble=false;
					if(entry.charAt(0)=='#')
					{
						which=-1;
						entry.setCharAt(0,' ');
					}
					if((entry.charAt(0)=='*')
					   ||(admin)
					   ||(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JOURNALS)))
					{
						mineAble=true;
						entry.setCharAt(0,' ');
					}
					else
					if((newOnly)&&(msg.value()>0))
						return;
					mob.tell(entry.toString()+"\n\r");
					if((entry.toString().trim().length()>0)
					&&(which>0)
					&&(CMLib.masking().maskCheck(getWriteReq(),mob,true)
						||(admin)
						||(CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.JOURNALS))))
					{
						boolean repeat=true;
						while((repeat)&&(mob.session()!=null)&&(!mob.session().isStopped()))
						{
							repeat=false;
							try
							{
								String prompt="";
								String cmds="";
								if(CMLib.masking().maskCheck(getReplyReq(),mob,true)
								||admin
								||(CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.JOURNALS)))
								{
									prompt+="^<MENU^>R^</MENU^>)eply ";
									cmds+="R";
								}
								if((CMProps.getVar(CMProps.Str.MAILBOX).length()>0)
								&&(from.length()>0))
									prompt+="^<MENU^>E^</MENU^>)mail "; cmds+="E";
								if(msg.value()>0){ prompt+="S)top "; cmds+="S";}
								else
								{
									prompt+="^<MENU^>N^</MENU^>)ext ";
									cmds+="N";
								}
								if(mineAble){ prompt+="^<MENU^>D^</MENU^>)elete "; cmds+="D";}
								if((admin)
								||(CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.JOURNALS)))
								{
									prompt+="^<MENU^>T^</MENU^>)ransfer ";
									cmds+="T";
								}
								prompt+="or RETURN: ";
								final String s=mob.session().choose(prompt,cmds+"\n","\n");
								if(s.equalsIgnoreCase("S"))
									msg.setValue(0);
								else
								if(s.equalsIgnoreCase("N"))
								{
									while(entry!=null)
									{
										which++;
										read=DBRead(mob,Name(),which-1,lastTime, newOnly, all);
										entry=read.derivedBuildMessage();
										if(entry==null)
											break;
										if(entry.toString().trim().length()>0)
										{
											if(entry.charAt(0)=='#')
												return;
											if((entry.charAt(0)=='*')||(!newOnly))
												break;
										}
									}
									megaRepeat=true;
								}
								else
								if(s.equalsIgnoreCase("E"))
								{
									final MOB M=CMLib.players().getLoadPlayer(from);
									if((M==null)||(M.playerStats()==null)||(M.playerStats().getEmail().indexOf('@')<0))
									{
										mob.tell(L("Player '@x1' does not exist, or has no email address.",from));
										repeat=true;
									}
									else
									if(!mob.session().choose(L("Send email to @x1 (Y/n)?",M.Name()),L("YN\n"),"Y").equalsIgnoreCase("N"))
									{
										final String replyMsg=mob.session().prompt(L("Enter your email response\n\r: "));
										if((replyMsg.trim().length()>0) && (read != null))
										{
											CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.Str.MAILBOX),
																			  mob.Name(),
																			  M.Name(),
																			  "RE: "+read.subj(),
																			  replyMsg);
											mob.tell(L("Email queued."));
										}
										else
										{
											mob.tell(L("Aborted."));
											repeat=true;
										}
									}
									else
										repeat=true;
								}
								else
								if(s.equalsIgnoreCase("T"))
								{
									String journal;
									try {
										journal=mob.session().prompt(L("Enter the journal to transfer this msg to: "),"",30000);
									}
									catch(final IOException e)
									{
										mob.tell(L("Timed out."));
										repeat=true;
										continue;
									}
									journal=journal.trim();
									if(journal.length()>0)
									{
										String realName=null;
										for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
										{
											final JournalsLibrary.CommandJournal CMJ=e.nextElement();
											if(journal.equalsIgnoreCase(CMJ.NAME())
											||journal.equalsIgnoreCase(CMJ.NAME()+"S"))
											{
												realName="SYSTEM_"+CMJ.NAME()+"S";
												break;
											}
										}
										if(realName==null)
											realName=CMLib.database().DBGetRealJournalName(journal);
										if(realName==null)
											realName=CMLib.database().DBGetRealJournalName(journal.toUpperCase());
										if(realName==null)
											mob.tell(L("The journal '@x1' does not presently exist.  Aborted.",journal));
										else
										{
											final List<JournalEntry> journal2;
											if(!getSortBy().toUpperCase().startsWith("CREAT"))
												journal2=CMLib.database().DBReadJournalMsgsByUpdateDate(Name(), true);
											else
												journal2=CMLib.database().DBReadJournalMsgsByCreateDate(Name(), true);
											final JournalEntry entry2=journal2.get(which-1);
											CMLib.database().DBDeleteJournal(Name(),entry2.key());
											CMLib.database().DBWriteJournal(realName,
																			  entry2.from(),
																			  entry2.to(),
																			  entry2.subj(),
																			  entry2.msg());
											msg.setValue(-1);
										}
										mob.tell(L("Message transferred."));
									}
									else
										mob.tell(L("Aborted."));
								}
								else
								if(s.equalsIgnoreCase("D"))
								{
									read=DBRead(mob,Name(),which-1,lastTime, newOnly, all);
									if(read != null)
									{
										CMLib.database().DBDeleteJournal(Name(),read.key());
										msg.setValue(-1);
										mob.tell(L("Entry deleted."));
									}
									else
										mob.tell(L("Failed to delete entry."));
								}
								else
								if(s.equalsIgnoreCase("R"))
								{
									if(read != null)
									{
										final String replyMsg=mob.session().prompt(L("Enter your response\n\r: "));
										if(replyMsg.trim().length()>0)
										{
											CMLib.database().DBWriteJournalReply(Name(),read.key(),mob.Name(),"","",replyMsg);
											mob.tell(L("Reply added."));
										}
										else
										{
											mob.tell(L("Aborted."));
											repeat=true;
										}
									}
								}
							}
							catch(final IOException e)
							{
								Log.errOut("JournalItem",e.getMessage());
							}
						}
					}
					else
					if(which<0)
						mob.tell(description());
					else
						mob.tell(L("That message is private."));
				}
				return;
			}
			return;
		case CMMsg.TYP_WRITE:
			try
			{
				final String adminReq=getAdminReq().trim();
				final boolean admin=((adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,mob,true))
								||CMSecurity.isJournalAccessAllowed(mob,Name());
				if((msg.targetMessage().toUpperCase().startsWith("DEL"))
				   &&(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JOURNALS)||admin)
				   &&(!mob.isMonster()))
				{
					if(mob.session().confirm(L("Delete all journal entries? Are you sure (y/N)?"),"N"))
						CMLib.database().DBDeleteJournal(name(),null);
				}
				else
				if(!mob.isMonster())
				{
					String to="ALL";
					if(CMath.s_bool(getParm("PRIVATE")))
						to=mob.Name();
					else
					if(CMath.s_bool(getParm("MAILBOX"))
					||mob.session().confirm(L("Is this a private message (y/N)?"),"N"))
					{
						to=mob.session().prompt(L("To whom:"));
						if(((!to.toUpperCase().trim().startsWith("MASK=")
								||(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JOURNALS)&&(!admin))))
						&&(!CMLib.players().playerExists(to)))
						{
							mob.tell(L("I'm sorry, there is no such user."));
							return;
						}
					}
					String subject=mob.session().prompt(L("Enter a subject: "));
					if(subject.trim().length()==0)
					{
						mob.tell(L("Aborted."));
						return;
					}
					if((subject.toUpperCase().startsWith("MOTD")||subject.toUpperCase().startsWith("MOTM")||subject.toUpperCase().startsWith("MOTY"))
					   &&(!admin)
					   &&(!(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JOURNALS))))
						subject=subject.substring(4);
					final String message=mob.session().prompt(L("Enter your message\n\r: "));
					if(message.trim().length()==0)
					{
						mob.tell(L("Aborted."));
						return;
					}
					if(message.startsWith("<cmvp>")
					&&(!admin)
					&&(!(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JOURNALS))))
					{
						mob.tell(L("Illegal code, aborted."));
						return;
					}

					CMLib.database().DBWriteJournal(Name(),mob.Name(),to,subject,message);
					mob.tell(L("Journal entry added."));
				}
				return;
			}
			catch(final IOException e)
			{
				Log.errOut("JournalItem",e.getMessage());
			}
			return;
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(owner instanceof Room)
		&&(msg.source().playerStats()!=null)
		&&(CMLib.masking().maskCheck(getReadReq(),msg.source(),true)))
		{

			final long[] newestDate=CMLib.database().DBJournalLatestDateNewerThan(Name(),msg.source().Name(),msg.source().playerStats().getLastDateTime());
			if((newestDate[0]>0)
			&&((newestDate[0]!=lastDateRead[0])||(msg.source()!=lastReadTo)))
			{
				lastReadTo=msg.source();
				lastDateRead=newestDate;
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,L("@x1 has @x2 new messages.",name(),""+newestDate[1]),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			}
		}
		else
		if(((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&(msg.target() instanceof Room)
		&&(msg.source()==owner)
		&&(msg.source().playerStats()!=null)
		&&(CMLib.masking().maskCheck(getReadReq(),msg.source(),true)))
		{
			final long[] newestDate=CMLib.database().DBJournalLatestDateNewerThan(Name(),msg.source().Name(),msg.source().playerStats().getLastDateTime());
			if((newestDate[0]>0)
			&&((newestDate[0]!=lastDateRead[0])||(msg.source()!=lastReadTo)))
			{
				lastReadTo=msg.source();
				lastDateRead=newestDate;
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,L("@x1 has @x2 new messages.",name(),""+newestDate[1]),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			}
		}
		super.executeMsg(myHost,msg);
	}

	public JournalEntry DBRead(MOB reader, String journal, int which, long lastTimeDate, boolean newOnly, boolean all)
	{
		final StringBuffer buf=new StringBuffer("");
		final List<JournalEntry> journalEntries;
		final boolean useCreateDate=getSortBy().toUpperCase().startsWith("CREAT");
		if(useCreateDate)
			journalEntries=CMLib.database().DBReadJournalMsgsByCreateDate(journal, true);
		else
			journalEntries=CMLib.database().DBReadJournalMsgsByUpdateDate(journal, true);
		final boolean shortFormat=readableText().toUpperCase().indexOf("SHORTLIST")>=0;
		if((which<0)||(journalEntries==null)||(which>=journalEntries.size()))
		{
			buf.append("#\n\r "+CMStrings.padRight("#",6)
					   +((shortFormat)?"":""
					   +CMStrings.padRight(L("From"),11)
					   +CMStrings.padRight(L("To"),11))
					   +CMStrings.padRight(L("Date"),20)
					   +"Subject\n\r");
			buf.append("-------------------------------------------------------------------------\n\r");
			if(journalEntries==null)
			{
				final JournalEntry fakeEntry = (JournalEntry)CMClass.getCommon("DefaultJournalEntry");
				fakeEntry.key("");
				fakeEntry.from("");
				fakeEntry.subj("");
				fakeEntry.derivedBuildMessage(buf);
				return fakeEntry;
			}
		}

		final JournalEntry fakeEntry = (JournalEntry)CMClass.getCommon("DefaultJournalEntry");
		if((which<0)||(which>=journalEntries.size()))
		{
			if(journalEntries.size()>0)
			{
				final JournalEntry entry=journalEntries.get(0);
				fakeEntry.key(entry.key());
				fakeEntry.from(entry.from());
				fakeEntry.subj(entry.subj());
			}
			int numToAdd=CMProps.getIntVar(CMProps.Int.JOURNALLIMIT);
			if((numToAdd==0)||(all))
				numToAdd=Integer.MAX_VALUE;
			final ArrayList<Integer> finalEntries = new ArrayList<Integer>();
			for(int j=journalEntries.size()-1;j>=0;j--)
			{
				final JournalEntry entry=journalEntries.get(j);
				final String from=entry.from();
				final String to=entry.to();
				// message is 5, but dont matter.
				final long compdate=entry.update();
				boolean mayRead=(to.equals("ALL")
								||to.equalsIgnoreCase(reader.Name())
								||from.equalsIgnoreCase(reader.Name()));
				if((to.toUpperCase().trim().startsWith("MASK="))&&(CMLib.masking().maskCheck(to.trim().substring(5),reader,true)))
					mayRead=true;
				if(mayRead)
				{
					if((compdate<=lastTimeDate)&&(newOnly))
						continue;
					if (numToAdd == 0)
						break;
					numToAdd--;
					finalEntries.add(Integer.valueOf(j));
					fakeEntry.key(entry.key());
					fakeEntry.from(entry.from());
					fakeEntry.subj(entry.subj());
					fakeEntry.cardinal(entry.cardinal());
				}
			}
			final ArrayList<CharSequence> selections=new ArrayList<CharSequence>();
			for(int j=finalEntries.size()-1;j>=0;j--)
			{
				final Integer J = finalEntries.get(j);
				final JournalEntry entry=journalEntries.get(J.intValue());
				final String from=entry.from();
				final long date=entry.date();
				String to=entry.to();
				final String subject=entry.subj();
				// message is 5, but dont matter.
				final long compdate=entry.update();
				final StringBuffer selection=new StringBuffer("");
				if(compdate>lastTimeDate)
					selection.append("*");
				else
					selection.append(" ");
				if(to.toUpperCase().trim().startsWith("MASK="))
					to=CMLib.masking().maskDesc(to.trim().substring(5),true);
				selection.append("^<JRNL \""+CMStrings.removeColors(name())+"\"^>"+CMStrings.padRight((J.intValue()+1)+"",4)+"^</JRNL^>) "
							   +((shortFormat)?"":""
							   +CMStrings.padRight(from,10)+" "
							   +CMStrings.padRight(to,10)+" ")
							   +CMStrings.padRight(CMLib.time().date2String(date),19)+" "
							   +CMStrings.padRight(subject,25+(shortFormat?22:0))+"\n\r");
				selections.add(selection);
			}
			boolean notify=false;
			for(int v=0;v<selections.size();v++)
			{
				if(!(selections.get(v) instanceof StringBuffer))
				{
					notify=true;
					continue;
				}
				buf.append((StringBuffer)selections.get(v));
			}
			if(notify)
				buf.append(L("\n\rUse READ ALL [JOURNAL] to see missing entries."));
		}
		else
		{
			final JournalEntry entry=journalEntries.get(which);
			final String from=entry.from();
			final long date=entry.date();
			String to=entry.to();
			final String subject=entry.subj();
			String message=entry.msg();

			fakeEntry.key(entry.key());
			fakeEntry.from(entry.from());
			fakeEntry.subj(entry.subj());
			fakeEntry.cardinal(entry.cardinal());

			boolean allMine=(to.equalsIgnoreCase(reader.Name())
							||from.equalsIgnoreCase(reader.Name()));
			if((to.toUpperCase().trim().startsWith("MASK="))&&(CMLib.masking().maskCheck(to.trim().substring(5),reader,true)))
			{
				allMine=true;
				to=CMLib.masking().maskDesc(to.trim().substring(5),true);
			}
			if(allMine)
				buf.append("*");
			else
				buf.append(" ");
			try
			{
				if(message.startsWith("<cmvp>"))
					message=new String(CMLib.webMacroFilter().virtualPageFilter(message.substring(6).getBytes()));
			}
			catch(final HTTPRedirectException e){}

			if((allMine)||(to.equals("ALL")))
			{
				buf.append("\n\r^<JRNL \""+CMStrings.removeColors(name())+"\"^>"+CMStrings.padRight((which+1)+"",3)+"^</JRNL^>)\n\r"
						   +"FROM: "+from
						   +"\n\rTO  : "+to
						   +"\n\rDATE: "+CMLib.time().date2String(date)
						   +"\n\rSUBJ: "+subject
						   +"\n\r"+message);
			}
		}
		fakeEntry.derivedBuildMessage(buf);
		return fakeEntry;
	}

	private String getParm(String parmName)
	{
		if(readableText().length()==0)
			return "";
		final Map<String,String> h=CMParms.parseEQParms(readableText().toUpperCase(), new String[]{"READ","WRITE","REPLY","ADMIN","PRIVATE","MAILBOX","SORTBY"});
		String req=h.get(parmName.toUpperCase().trim());
		if(req==null)
			req="";
		return req;
	}

	protected String getReadReq()
	{
		return getParm("READ");
	}

	protected String getWriteReq()
	{
		return getParm("WRITE");
	}

	private String getReplyReq()
	{
		return getParm("REPLY");
	}

	private String getAdminReq()
	{
		return getParm("ADMIN");
	}

	private String getSortBy()
	{
		return getParm("SORTBY");
	}

	@Override
	public void recoverPhyStats()
	{
		CMLib.flags().setReadable(this, true);
		super.recoverPhyStats();
	}
}
