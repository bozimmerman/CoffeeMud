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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.MsgMkrResolution;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;

import java.util.*;
import java.io.IOException;

/*
   Copyright 2006-2017 Bo Zimmerman

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

public class StdBook extends StdItem
{
	@Override
	public String ID()
	{
		return "StdBook";
	}

	public StdBook()
	{
		super();
		setName("a book");
		setDisplayText("a book sits here.");
		setDescription("Enter `READ [NUMBER] [BOOK]` to read a chapter.%0D%0AUse your WRITE skill to add new chapters. ");
		material=RawMaterial.RESOURCE_PAPER;
		basePhyStats().setSensesMask(PhyStats.SENSE_ITEMREADABLE);
		recoverPhyStats();
	}

	protected MOB lastReadTo=null;
	protected long lastDateRead=-1;
	
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
				final boolean admin=(adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,msg.source(),true);
				if((!CMLib.masking().maskCheck(getWriteReq(),msg.source(),true))
				&&(!admin)
				&&(!(CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.JOURNALS))))
				{
					msg.source().tell(L("You are not allowed to write on @x1",name()));
					return false;
				}
				if(msg.targetMajor(CMMsg.MASK_CNTRLMSG))
				{
					if(CMath.isInteger(msg.targetMessage()))
					{
						int msgNum=CMath.s_int(msg.targetMessage());
						if((msgNum <0)||(msgNum>=this.getChapterCount("ALL")))
						{
							msg.source().tell(L("There is no Chapter @x1",""+(msgNum+1)));
							return false;
						}
						if((!admin)
						&&(!(CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.JOURNALS))))
						{
							JournalEntry entry = this.readChaptersByCreateDate().get(msgNum);
							if(!entry.from().equalsIgnoreCase(msg.source().Name()))
							{
								msg.source().tell(L("You need permission to edit that chapter."));
								return false;
							}
						}
					}
					else
					if(msg.targetMessage().toUpperCase().startsWith("DELETE "))
					{
						String entryStr=msg.targetMessage().substring(7).trim();
						int entryNum=CMath.s_int(entryStr);
						int numEntries = this.getChapterCount("ALL");
						if((entryNum < 0)||(numEntries>=numEntries)||(!CMath.isInteger(entryStr)))
						{
							msg.source().tell(L("There is no Chapter @x1",""+(entryNum+1)));
							return false;
						}
						if((!admin)
						&&(!(CMSecurity.isAllowed(msg.source(),msg.source().location(),CMSecurity.SecFlag.JOURNALS))))
						{
							JournalEntry entry = this.readChaptersByCreateDate().get(entryNum);
							if(!entry.from().equalsIgnoreCase(msg.source().Name()))
							{
								msg.source().tell(L("You need permission to remove that chapter."));
								return false;
							}
						}
					}
				}
				return true;
			}
			}
		}
		return super.okMessage(myHost,msg);
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
					final boolean admin=(adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,mob,true);
					final long lastTime=mob.playerStats().getLastDateTime();
					if((admin)&&(!CMLib.masking().maskCheck(getReadReq(),mob,true)))
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
					final Triad<String,String,StringBuffer> read=DBRead(mob,which-1,lastTime, newOnly, all);
					boolean megaRepeat=true;
					while(megaRepeat)
					{
						megaRepeat=false;
						final StringBuffer entry=read.third;
						if(entry.charAt(0)=='#')
						{
							which=-1;
							entry.setCharAt(0,' ');
						}
						if((entry.charAt(0)=='*')
						   ||(admin)
						   ||(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.JOURNALS)))
							entry.setCharAt(0,' ');
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
						}
						else
						if(which<0)
							mob.tell(description());
					}
					return;
				}
				return;
			}
			case CMMsg.TYP_WRITE:
			{
				final MOB M=mob;
				final String[] subject=new String[1];
				final String[] message=new String[1];
				final String editKey[] = new String[1];
				if(!mob.isMonster())
				{
					final Runnable addComplete=new Runnable() 
					{
						final String to="ALL";
						final MOB mob=M;
						@Override
						public void run()
						{
							final Room R=mob.location();
							final String adminReq=getAdminReq().trim();
							final boolean admin=(adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,mob,true);
							if(message[0].startsWith("<cmvp>")
							&&(!admin)
							&&(!(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.JOURNALS))))
							{
								mob.tell(L("Illegal code, aborted."));
								return;
							}
							if((editKey==null)||(editKey[0]==null))
							{
								addNewChapter(mob.Name(),to,subject[0],message[0]);
								if((R!=null)&&(msg.targetMessage().length()<=1))
									R.send(mob, ((CMMsg)msg.copyOf()).modify(CMMsg.MSG_WROTE, L("Chapter added."), CMMsg.MSG_WROTE, subject+"\n\r"+message, -1, null));
								else
									mob.tell(L("Chapter added."));
							}
							else
							{
								editOldChapter(mob.Name(),to,editKey[0],subject[0],message[0]);
								if((R!=null)&&(msg.targetMessage().length()<=1))
									R.send(mob, ((CMMsg)msg.copyOf()).modify(CMMsg.MSG_WROTE, L("Chapter modified."), CMMsg.MSG_WROTE, subject+"\n\r"+message, -1, null));
								else
									mob.tell(L("Chapter modified."));
							}
						}
					};
					if((msg.targetMajor(CMMsg.MASK_CNTRLMSG))
					&&(msg.targetMessage().toUpperCase().startsWith("DELETE ")))
					{
						int entryNum=CMath.s_int(msg.targetMessage().substring(7).trim());
						JournalEntry entry = this.readChaptersByCreateDate().get(entryNum);
						delOldChapter(mob.Name(),"ALL",entry.key());
						mob.tell(L("Chapter removed."));
					}
					else
					if((msg.targetMessage().length()>1)
					&&(!msg.targetMajor(CMMsg.MASK_CNTRLMSG))
					&&(!CMath.isInteger(msg.targetMessage())))
					{
						message[0]=msg.targetMessage();
						int numMessages=getChapterCount("ALL");
						subject[0]=L("Chapter @x1",""+(numMessages+1));
						addComplete.run();
					}
					else
					{
						final InputCallback contentCallBack = new InputCallback(InputCallback.Type.PROMPT,"",0)
						{
							final MOB mob=M;
							final Runnable completer=addComplete; 
							@Override
							public void showPrompt()
							{
								if((subject[0]!=null)&&(subject[0].length()>0))
									mob.session().promptPrint(L("Enter the name of the chapter ("+subject[0]+"): "));
								else
									mob.session().promptPrint(L("Enter the name of the chapter: "));
							}

							@Override
							public void timedOut()
							{
							}

							@Override
							public void callBack()
							{
								String subj=this.input.trim();
								if((subj.trim().length()==0)
								&&((subject[0]==null)||(subject[0].length()==0)))
									subj=subject[0];
								if(subj.trim().length()==0)
								{
									mob.tell(L("Aborted."));
									return;
								}
								subject[0]=subj;
								final String messageTitle="The contents of this chapter";
								mob.session().println(L("\n\rEnter the contents of this chapter:"));
								final List<String> vbuf=new Vector<String>();
								if(message[0]!=null)
									vbuf.addAll(CMParms.parseAny(message[0],"\\n",false));
								CMLib.journals().makeMessageASync(mob, messageTitle, vbuf, true, new JournalsLibrary.MsgMkrCallback(){
									@Override
									public void callBack(MOB mob, Session sess, MsgMkrResolution res)
									{
										if(res ==JournalsLibrary.MsgMkrResolution.CANCELFILE)
										{
											mob.tell(L("Aborted."));
											return;
										}
										message[0]=CMParms.combineWith(vbuf, "\\n");
										completer.run();
									}
								});
							}
						};
						if((CMath.isInteger(msg.targetMessage()))
						&&(msg.targetMajor(CMMsg.MASK_CNTRLMSG)))
						{
							try
							{
								JournalEntry entry = this.readChaptersByCreateDate().get(CMath.s_int(msg.targetMessage()));
								if(entry != null)
								{
									subject[0]=entry.subj();
									message[0]=entry.msg();
									editKey[0] = entry.key();
									msg.setTargetMessage("");
								}
							}
							catch(Exception e)
							{
							}
						}
						mob.session().prompt(contentCallBack);
					}
				}
				return;
			}
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}

	protected String getTOCHeader()
	{
		return L("\n\rTable of Contents\n\r");
	}
	
	protected String getJournalName()
	{
		return Name();
	}
	
	protected int getChapterCount(final String to)
	{
		return CMLib.database().DBCountJournal(getJournalName(), null, to);
	}
	
	protected List<JournalEntry> readChaptersByCreateDate()
	{
		return CMLib.database().DBReadJournalMsgsByCreateDate(getJournalName(), true);
	}
	
	protected void addNewChapter(final String from, final String to, final String subject, final String message)
	{
		CMLib.database().DBWriteJournal(getJournalName(),from,to,subject,message);
	}

	protected void editOldChapter(final String from, final String to, final String key, final String subject, final String message)
	{
		JournalEntry entry = CMLib.database().DBReadJournalEntry(getJournalName(), key);
		entry.from(from);
		entry.to(to);
		entry.subj(subject);
		entry.msg(message);
		CMLib.database().DBUpdateJournal(getJournalName(), entry);
	}

	protected void delOldChapter(final String from, final String to, final String key)
	{
		CMLib.database().DBDeleteJournal(getJournalName(), key);
	}

	public Triad<String,String,StringBuffer> DBRead(MOB readerMOB, int which, long lastTimeDate, boolean newOnly, boolean all)
	{
		final StringBuffer buf=new StringBuffer("");
		final Triad<String,String,StringBuffer> reply=new Triad<String,String,StringBuffer>("","",new StringBuffer(""));
		final List<JournalEntry> journal=this.readChaptersByCreateDate();
		if((which<0)||(journal==null)||(which>=journal.size()))
		{
			buf.append(getTOCHeader());
			buf.append("-------------------------------------------------------------------------\n\r");
			if(journal==null)
			{
				reply.first="";
				reply.second="";
				reply.third = buf;
				return reply;
			}
		}

		if((which<0)||(which>=journal.size()))
		{
			if(journal.size()>0)
			{
				reply.first = journal.get(0).from();
				reply.second = journal.get(0).subj();
			}
			final ArrayList<Object> selections=new ArrayList<Object>();
			for(int j=0;j<journal.size();j++)
			{
				final JournalEntry entry=journal.get(j);
				final String from=entry.from();
				final String to=entry.to();
				final String subject=entry.subj();
				final StringBuffer selection=new StringBuffer("");
				if(to.equals("ALL")
				||to.equalsIgnoreCase(readerMOB.Name())
				||from.equalsIgnoreCase(readerMOB.Name())
				||(to.toUpperCase().trim().startsWith("MASK=")&&CMLib.masking().maskCheck(to.trim().substring(5),readerMOB,true)))
				{
					//if(CMath.s_long(compdate)>lastTimeDate)
					//    selection.append("*");
					//else
					//if(newOnly)
					//    continue;
					//else
					//    selection.append(" ");
					selection.append(subject+"\n\r");
					selections.add(selection);
				}
			}
			int numToAdd=CMProps.getIntVar(CMProps.Int.JOURNALLIMIT);
			if((numToAdd==0)||(all))
				numToAdd=Integer.MAX_VALUE;
			for(int v=selections.size()-1;v>=0;v--)
			{
				if (numToAdd == 0)
				{
					selections.set(v,"");
					continue;
				}
				final StringBuffer str = (StringBuffer) selections.get(v);
				if ((newOnly) && (str.charAt(0) != '*'))
				{
					selections.set(v,"");
					continue;
				}
				numToAdd--;
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
				buf.append(L("\n\rUse READ ALL [BOOK] to see missing chapters."));
		}
		else
		{
			final JournalEntry entry=journal.get(which);
			final String from=entry.from();
			final String to=entry.to();
			final String subject=entry.subj();
			String message=entry.msg();

			reply.first = entry.from();
			reply.second = entry.subj();

			//String compdate=(String)entry.elementAt(6);
			final boolean mineAble=to.equalsIgnoreCase(readerMOB.Name())
							||(to.toUpperCase().trim().startsWith("MASK=")&&(CMLib.masking().maskCheck(to.trim().substring(5),readerMOB,true)))
							||from.equalsIgnoreCase(readerMOB.Name());
			if(mineAble)
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

			if(to.equals("ALL")||mineAble)
				buf.append("\n\r"+subject
						   +"\n\r"+message);
		}
		reply.third = buf;
		return reply;
	}

	protected String getParm(String parmName)
	{
		if(readableText().length()==0)
			return "";
		final Map<String,String> h=CMParms.parseEQParms(readableText().toUpperCase(), new String[]{"KEY","READ","WRITE","REPLY","ADMIN"});
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

	private String getAdminReq()
	{
		return getParm("ADMIN");
	}

	@Override
	public void recoverPhyStats()
	{
		CMLib.flags().setReadable(this, true);
		super.recoverPhyStats();
	}
}
