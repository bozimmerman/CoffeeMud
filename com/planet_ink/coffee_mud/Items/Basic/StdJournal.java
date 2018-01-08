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
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.CommandJournalFlags;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.MsgMkrCallback;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.MsgMkrResolution;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;

import java.util.*;
import java.io.IOException;

/*
   Copyright 2002-2018 Bo Zimmerman

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

public class StdJournal extends StdItem implements Book
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
		{
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
			case CMMsg.TYP_REWRITE:
			{
				final MOB mob=msg.source();
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
				if(msg.targetMinor()==CMMsg.TYP_REWRITE)
				{
					String entryStr;
					if((msg.targetMessage()!=null)&&(msg.targetMessage().startsWith("DELETE ")))
						entryStr=msg.targetMessage().substring(7).trim();
					else
					if((msg.targetMessage()!=null)&&(msg.targetMessage().startsWith("EDIT ")))
					{
						int x=msg.targetMessage().indexOf(' ',5);
						if(x>5)
							entryStr=msg.targetMessage().substring(5,x).trim();
						else
							entryStr=msg.targetMessage().substring(5).trim();
					}
					else
						entryStr=msg.targetMessage();
					if(!CMath.isInteger(entryStr))
					{
						mob.tell(L("The journal does not have an entry #@x1.",entryStr));
						return false;
					}
					int which=CMath.s_int(entryStr);
					final List<JournalEntry> journal2;
					String[] tos=new String[0];
					if(getReadFilter().length()>0)
						tos=CMParms.parseCommas(this.getReadFilter(), true).toArray(new String[0]);
					if(!getSortBy().toUpperCase().startsWith("CREAT"))
						journal2=CMLib.database().DBReadJournalMsgsByUpdateDate(Name(), true, 100000,tos);
					else
						journal2=CMLib.database().DBReadJournalMsgsByCreateDate(Name(), true, 100000, tos);
					if((which<1)||(which>journal2.size()))
					{
						mob.tell(L("The journal does not have an entry #@x1.",""+which));
						return false;
					}
					else
					{
						final JournalEntry read=journal2.get(which-1);
						if((!read.from().equalsIgnoreCase(mob.Name()))
						&&(!read.to().equalsIgnoreCase(mob.Name()))
						&&(!(CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.JOURNALS)))
						&&(!admin))
						{
							mob.tell(L("You are not allowed to alter or remove entry #@x1.",""+which));
							return false;
						}
					}
				}
				return true;
			}
			}
		}
		return super.okMessage(myHost,msg);
	}

	protected boolean completeTransfer(final MOB mob, final CMMsg msg, String journal, JournalEntry entry2)
	{
		String realName=null;
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if(journal.equalsIgnoreCase(CMJ.NAME())
			||journal.equalsIgnoreCase(CMJ.NAME()+"S"))
			{
				realName=CMJ.JOURNAL_NAME();
				break;
			}
		}
		if(realName==null)
			realName=CMLib.database().DBGetRealJournalName(journal);
		if(realName==null)
			realName=CMLib.database().DBGetRealJournalName(journal.toUpperCase());
		if(realName!=null)
		{
			CMLib.database().DBDeleteJournal(Name(),entry2.key());
			CMLib.database().DBWriteJournal(realName,
											  entry2.from(),
											  entry2.to(),
											  entry2.subj(),
											  entry2.msg());
			msg.setValue(-1);
			mob.tell(L("Message transferred."));
			return true;
		}
		else
		{
			JournalsLibrary.CommandJournal cmJournalAlias=this.getMyCommandJournal();
			if(journal.equalsIgnoreCase("ALL"))
			{
				if(entry2.to().equalsIgnoreCase("ALL"))
					mob.tell(L("Message already assigned to ALL."));
				else
					realName="ALL";
			}
			else
			if(journal.equalsIgnoreCase("FROM"))
			{
				if(entry2.to().equalsIgnoreCase(entry2.from()))
					mob.tell(L("Message already assigned to @x1.",entry2.from()));
				else
				if(entry2.from().equalsIgnoreCase(mob.Name()))
					mob.tell(L("Message already accessible to @x1.",mob.Name()));
				else
					realName=entry2.from();
			}
			else
			if(CMLib.players().playerExists(CMStrings.capitalizeAndLower(journal)))
				realName=CMStrings.capitalizeAndLower(journal);
			else
			if(cmJournalAlias!=null)
			{
				String otherToStr=cmJournalAlias.getFlag(CommandJournalFlags.ASSIGN);
				List<String> otherTos = CMParms.parseAny(otherToStr, ':', true);
				if(otherTos.contains(journal.toUpperCase().trim()))
					realName=journal.toUpperCase().trim();
			}
			if(realName == null)
				mob.tell(L("The journal or user '@x1' does not presently exist.",journal));
			else
			{
				entry2.to(realName);
				CMLib.database().DBUpdateJournal(Name(), entry2);
				msg.setValue(-1);
				mob.tell(L("Message transferred."));
				return true;
			}
		}
		return false;
	}
	
	public JournalsLibrary.CommandJournal getMyCommandJournal()
	{
		JournalsLibrary.CommandJournal cmJournalAlias=null;
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if(Name().equalsIgnoreCase(CMJ.NAME())
			||Name().equalsIgnoreCase(CMJ.JOURNAL_NAME()))
				cmJournalAlias=CMJ;
		}
		return cmJournalAlias;
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_READ:
			{
				final Room R=mob.location();
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
						||(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.JOURNALS)))
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
							||(CMSecurity.isAllowed(msg.source(),R,CMSecurity.SecFlag.JOURNALS))))
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
									||(CMSecurity.isAllowed(msg.source(),R,CMSecurity.SecFlag.JOURNALS)))
									{
										prompt+="^<MENU^>R^</MENU^>)eply ";
										cmds+="R";
									}
									if((CMProps.getVar(CMProps.Str.MAILBOX).length()>0)
									&&(from.length()>0))
										prompt+="^<MENU^>E^</MENU^>)mail "; cmds+="E";
									if(msg.value()>0)
									{
										prompt+="^<MENU^>S^</MENU^>)top ";
										cmds+="S";
									}
									else
									{
										prompt+="^<MENU^>N^</MENU^>)ext ";
										cmds+="N";
									}
									if(mineAble)
									{
										prompt+="^<MENU^>D^</MENU^>)elete ";
										cmds+="D";
									}
									if((admin)
									||(CMSecurity.isAllowed(msg.source(),R,CMSecurity.SecFlag.JOURNALS)))
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
											journal=mob.session().prompt(L("Enter the journal or user to transfer this msg to: "),"",30000);
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
											if(completeTransfer(mob,msg,journal,read))
												msg.setValue(-1);
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
												read = CMLib.database().DBWriteJournalReply(Name(),read.key(),mob.Name(),"","",replyMsg);
												if(R!=null)
													R.send(mob, ((CMMsg)msg.copyOf()).modify(CMMsg.MSG_WROTE, L("Reply added."), CMMsg.MSG_WROTE, replyMsg, -1, null));
												megaRepeat=true;
												JournalsLibrary.CommandJournal cmJournalAlias=this.getMyCommandJournal();
												if(cmJournalAlias != null)
												{
													if(read.to().equals(read.from()) && (read.to().equalsIgnoreCase(mob.Name())))
													{
														if((cmJournalAlias.getFlag(CommandJournalFlags.REPLYSELF)!=null)
														&&(cmJournalAlias.getFlag(CommandJournalFlags.REPLYSELF).length()>0))
														{
															final String journal=cmJournalAlias.getFlag(CommandJournalFlags.REPLYSELF);
															completeTransfer(mob,msg,journal,read);
															megaRepeat=false;
														}
													}
													else
													if(read.to().equalsIgnoreCase("ALL") 
													&& (!read.from().equalsIgnoreCase("ALL")))
													{
														if((cmJournalAlias.getFlag(CommandJournalFlags.REPLYALL)!=null)
														&&(cmJournalAlias.getFlag(CommandJournalFlags.REPLYALL).length()>0))
														{
															final String journal=cmJournalAlias.getFlag(CommandJournalFlags.REPLYALL);
															completeTransfer(mob,msg,journal,read);
															megaRepeat=false;
														}
													}
												}
												if(megaRepeat)
												{
													read=DBRead(mob,Name(),which-1,lastTime, newOnly, all);
												}
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
			}
			case CMMsg.TYP_REWRITE:
			{
				String[] tos=new String[0];
				if(getReadFilter().length()>0)
					tos=CMParms.parseCommas(this.getReadFilter(), true).toArray(new String[0]);
				if((msg.targetMessage()!=null)&&(msg.targetMessage().startsWith("DELETE ")))
				{
					String entryStr=msg.targetMessage().substring(7).trim();
					int which=CMath.s_int(entryStr);
					final List<JournalEntry> journal2;
					if(!getSortBy().toUpperCase().startsWith("CREAT"))
						journal2=CMLib.database().DBReadJournalMsgsByUpdateDate(Name(), true, 100000, tos);
					else
						journal2=CMLib.database().DBReadJournalMsgsByCreateDate(Name(), true, 100000, tos);
					final JournalEntry entry2=journal2.get(which-1);
					CMLib.database().DBDeleteJournal(Name(),entry2.key());
				}
				else
				if((msg.targetMessage()!=null)&&(msg.targetMessage().startsWith("EDIT ")))
				{
					String entryStr;
					String messageStr="";
					int x=msg.targetMessage().indexOf(' ',5);
					if(x>5)
					{
						entryStr=msg.targetMessage().substring(5,x).trim();
						messageStr=msg.targetMessage().substring(x+1).trim();
					}
					else
						entryStr=msg.targetMessage().substring(5).trim();
					int which=CMath.s_int(entryStr);
					final List<JournalEntry> journal2;
					if(!getSortBy().toUpperCase().startsWith("CREAT"))
						journal2=CMLib.database().DBReadJournalMsgsByUpdateDate(Name(), true, 100000, tos);
					else
						journal2=CMLib.database().DBReadJournalMsgsByCreateDate(Name(), true, 100000, tos);
					final JournalEntry entry2=journal2.get(which-1);
					entry2.msg(messageStr);
					CMLib.database().DBUpdateJournal(Name(), entry2);
				}
				else
				if(CMath.isInteger(msg.targetMessage()))
				{
					int which=CMath.s_int(msg.targetMessage());
					final List<JournalEntry> journal2;
					if(!getSortBy().toUpperCase().startsWith("CREAT"))
						journal2=CMLib.database().DBReadJournalMsgsByUpdateDate(Name(), true, 100000, tos);
					else
						journal2=CMLib.database().DBReadJournalMsgsByCreateDate(Name(), true, 100000, tos);
					final JournalEntry entry2=journal2.get(which-1);
					final List<String> vbuf=new ArrayList<String>();
					vbuf.addAll(CMParms.parseAny(entry2.msg(),"\n",false));
					CMLib.journals().makeMessageASync(mob, entry2.subj(), vbuf, false, new MsgMkrCallback(){
						@Override
						public void callBack(MOB mob, Session sess, MsgMkrResolution res)
						{
							if(res == MsgMkrResolution.SAVEFILE)
							{
								entry2.msg(CMParms.combineWith(vbuf,"\n"));
								CMLib.database().DBUpdateJournal(Name(), entry2);
							}
						}
						
					});
				}
				return;
			}
			case CMMsg.TYP_WRITE:
			{
				try
				{
					final Room R=mob.location();
					final String adminReq=getAdminReq().trim();
					final boolean admin=((adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,mob,true))
									||CMSecurity.isJournalAccessAllowed(mob,Name());
					if((msg.targetMessage().toUpperCase().startsWith("DEL"))
					   &&(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.JOURNALS)||admin)
					   &&(!mob.isMonster()))
					{
						if(mob.session().confirm(L("Delete all journal entries? Are you sure (y/N)?"),"N"))
							CMLib.database().DBDeleteJournal(name(),null);
					}
					else
					if(!mob.isMonster())
					{
						String to="ALL";
						String subject;
						final String message;
						if(msg.targetMessage().length()>1)
						{
							message=msg.targetMessage();
							subject=CMStrings.ellipse(message, 40);
						}
						else
						{
							if(CMath.s_bool(getParm("PRIVATE")))
								to=mob.Name();
							else
							if(CMath.s_bool(getParm("MAILBOX"))
							||mob.session().confirm(L("Is this a private message (y/N)?"),"N"))
							{
								to=mob.session().prompt(L("To whom:"));
								if(((!to.toUpperCase().trim().startsWith("MASK=")
										||(!CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.JOURNALS)&&(!admin))))
								&&(!CMLib.players().playerExists(to)))
								{
									mob.tell(L("I'm sorry, there is no such user."));
									return;
								}
							}
							subject=mob.session().prompt(L("Enter a subject: "));
							if(subject.trim().length()==0)
							{
								mob.tell(L("Aborted."));
								return;
							}
							if((subject.toUpperCase().startsWith("MOTD")||subject.toUpperCase().startsWith("MOTM")||subject.toUpperCase().startsWith("MOTY"))
							   &&(!admin)
							   &&(!(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.JOURNALS))))
								subject=subject.substring(4);
							message=mob.session().prompt(L("Enter your message\n\r: "));
							if(message.trim().length()==0)
							{
								mob.tell(L("Aborted."));
								return;
							}
						}
						if(message.startsWith("<cmvp>")
						&&(!admin)
						&&(!(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.JOURNALS))))
						{
							mob.tell(L("Illegal code, aborted."));
							return;
						}
						CMLib.database().DBWriteJournal(Name(),mob.Name(),to,subject,message);
						if((R!=null)&&(msg.targetMessage().length()<=1))
							R.send(mob, ((CMMsg)msg.copyOf()).modify(CMMsg.MSG_WROTE, L("Journal entry added."), CMMsg.MSG_WROTE, subject+"\n\r"+message, -1, null));
						else
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
			default:
				break;
			}
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
		final List<JournalEntry> journalEntries;
		final boolean useCreateDate=getSortBy().toUpperCase().startsWith("CREAT");
		String[] tos=new String[0];
		if(getReadFilter().length()>0)
			tos=CMParms.parseCommas(this.getReadFilter(), true).toArray(new String[0]);
		if(useCreateDate)
			journalEntries=CMLib.database().DBReadJournalMsgsByCreateDate(journal, true, 100000, tos);
		else
			journalEntries=CMLib.database().DBReadJournalMsgsByUpdateDate(journal, true, 100000, tos);
		final StringBuffer buf=new StringBuffer("");
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
				fakeEntry.to("");
				fakeEntry.subj("");
				fakeEntry.derivedBuildMessage(buf);
				return fakeEntry;
			}
		}

		JournalEntry fakeEntry = (JournalEntry)CMClass.getCommon("DefaultJournalEntry");
		if((which<0)||(which>=journalEntries.size()))
		{
			if(journalEntries.size()>0)
			{
				final JournalEntry entry=journalEntries.get(0);
				fakeEntry = entry.copyOf();
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
					fakeEntry = entry.copyOf();
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

			fakeEntry = entry.copyOf();

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
			catch(final HTTPRedirectException e)
			{
			}

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

	private static final String[] JOURNAL_PARMS_LIST=new String[]{"READ","WRITE","REPLY","ADMIN","PRIVATE","MAILBOX","SORTBY","FILTER"};
	
	
	private String getParm(String parmName)
	{
		if(readableText().length()==0)
			return "";
		final Map<String,String> h;
		h=CMParms.parseEQParms(readableText().toUpperCase(), JOURNAL_PARMS_LIST);
		if((parmName.equals("FILTER"))&&(h.containsKey("FILTER")))
		{
			Map<String,String> h2=CMParms.parseEQParms(readableText(), JOURNAL_PARMS_LIST);
			for(String key : h2.keySet())
			{
				if(key.equalsIgnoreCase("FILTER"))
				{
					h.put("FILTER", h2.get(key));
				}
			}
		}
		String req=h.get(parmName.toUpperCase().trim());
		if(req==null)
			req="";
		return req;
	}

	protected String getReadReq()
	{
		return getParm("READ");
	}

	protected String getReadFilter()
	{
		return getParm("FILTER");
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
	public int getUsedPages()
	{
		return CMLib.database().DBCountJournal(Name(), null, null);
	}
	
	@Override
	public int getMaxPages()
	{
		return 0;
	}
	
	@Override
	public int getMaxCharsPerPage()
	{
		return 0;
	}

	@Override
	public void setMaxCharsPerPage(int max)
	{
	}

	@Override
	public void setMaxPages(int max)
	{
	}
	
	@Override
	public String getRawContent(int page)
	{
		String[] tos=new String[0];
		if(getReadFilter().length()>0)
			tos=CMParms.parseCommas(this.getReadFilter(), true).toArray(new String[0]);
		final List<JournalEntry> journal=CMLib.database().DBReadJournalMsgsByCreateDate(Name(), true, 100000, tos);
		if((page < 1)||(page>journal.size()))
			return "";
		else
		{
			JournalEntry J=journal.get(page-1);
			if((J.subj()!=null)&&(J.subj().length()>0))
				return "::"+J.subj()+"::"+J.msg();
			else
				return "::"+J.subj()+"::"+J.msg();
		}
	}
	
	@Override
	public String getContent(int page)
	{
		JournalEntry J=this.DBRead(null, Name(), page-1, 0, false, false);
		if(J==null)
			return "";
		if((J.subj()!=null)&&(J.subj().length()>0))
			return "::"+J.subj()+"::"+J.msg();
		else
			return "::"+J.subj()+"::"+J.msg();
	}
	
	@Override
	public void addRawContent(String authorName, String content)
	{
		if(content.startsWith("::")&&(content.length()>2)&&(content.charAt(2)!=':'))
		{
			int x=content.indexOf("::",2);
			if(x>2)
				CMLib.database().DBWriteJournal(Name(),authorName,"ALL",content.substring(2,x),content.substring(x+2));
			else
				CMLib.database().DBWriteJournal(Name(),authorName,"ALL",CMStrings.limit(content, 10),content);
		}
		else
			CMLib.database().DBWriteJournal(Name(),authorName,"ALL",CMStrings.limit(content, 10),content);
	}
	
	@Override
	public boolean isJournal()
	{
		return true;
	}
	
	@Override
	public void recoverPhyStats()
	{
		CMLib.flags().setReadable(this, true);
		super.recoverPhyStats();
	}
}
