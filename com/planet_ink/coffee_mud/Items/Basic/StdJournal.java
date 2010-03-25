package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class StdJournal extends StdItem
{
	public String ID(){	return "StdJournal";}
	protected MOB lastReadTo=null;
	protected long[] lastDateRead={-1,0};
	
	public StdJournal()
	{
		super();
		setName("a journal");
		setDisplayText("a journal sits here.");
		setDescription("Enter `READ [NUMBER] [JOURNAL]` to read an entry.%0D%0AUse your WRITE skill to add new entries. ");
		material=RawMaterial.RESOURCE_PAPER;
		baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMREADABLE);
		recoverEnvStats();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WRITE:
        {
            String adminReq=getAdminReq().trim();
            boolean admin=((adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,msg.source(),true))
                            ||CMSecurity.isAllowed(msg.source(),msg.source().location(),Name());
			if((!CMLib.masking().maskCheck(getWriteReq(),msg.source(),true))
            &&(!admin)
            &&(!(CMSecurity.isAllowed(msg.source(),msg.source().location(),"JOURNALS"))))
			{
				msg.source().tell("You are not allowed to write on "+name());
				return false;
			}
			return true;
        }
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_READ:
			if(!CMLib.flags().canBeSeenBy(this,mob))
				mob.tell("You can't see that!");
			else
			if((!mob.isMonster())
			&&(mob.playerStats()!=null))
			{
                String adminReq=getAdminReq().trim();
                boolean admin=((adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,mob,true))
                                ||CMSecurity.isAllowed(mob,mob.location(),Name());
				long lastTime=mob.playerStats().lastDateTime();
				if((!admin)&&(!CMLib.masking().maskCheck(getReadReq(),mob,true)))
				{
					mob.tell("You are not allowed to read "+name()+".");
					return;
				}
				int which=-1;
				boolean newOnly=false;
				boolean all=false;
				Vector parse=CMParms.parse(msg.targetMessage());
				for(int v=0;v<parse.size();v++)
				{
				    String s=(String)parse.elementAt(v);
					if(CMath.s_long(s)>0)
						which=CMath.s_int(msg.targetMessage());
					else
					if(s.equalsIgnoreCase("NEW"))
					    newOnly=true;
					else
					if(s.equalsIgnoreCase("ALL")||s.equalsIgnoreCase("OLD"))
					    all=true;
				}
				JournalsLibrary.JournalEntry read=DBRead(mob,Name(),which-1,lastTime, newOnly, all);
				boolean megaRepeat=true;
				while((megaRepeat) && (read!=null))
				{
				    megaRepeat=false;
					String from=read.from;
					StringBuffer entry=read.derivedBuildMessage;
					boolean mineAble=false;
					if(entry.charAt(0)=='#')
					{
						which=-1;
						entry.setCharAt(0,' ');
					}
					if((entry.charAt(0)=='*')
                       ||(admin)
					   ||(CMSecurity.isAllowed(mob,mob.location(),"JOURNALS")))
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
                        ||(CMSecurity.isAllowed(msg.source(),msg.source().location(),"JOURNALS"))))
					{
						boolean repeat=true;
						while(repeat)
						{
							repeat=false;
							try
							{
								String prompt="";
                                String cmds="";
                                if(CMLib.masking().maskCheck(getReplyReq(),mob,true)
                                ||admin
                                ||(CMSecurity.isAllowed(msg.source(),msg.source().location(),"JOURNALS")))
                                {
                                    prompt+="^<MENU^>R^</MENU^>)eply ";
                                    cmds+="R";
                                }
								if((CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0)
								&&(from.length()>0))
									prompt+="^<MENU^>E^</MENU^>)mail "; cmds+="E";
								if(msg.value()>0){ prompt+="S)top "; cmds+="S";}
								else
								{ prompt+="^<MENU^>N^</MENU^>)ext "; cmds+="N";}
								if(mineAble){ prompt+="^<MENU^>D^</MENU^>)elete "; cmds+="D";}
								if((admin)
                                ||(CMSecurity.isAllowed(msg.source(),msg.source().location(),"JOURNALS")))
								{ prompt+="^<MENU^>T^</MENU^>)ransfer "; cmds+="T";}
								prompt+="or RETURN: ";
								String s=mob.session().choose(prompt,cmds+"\n","\n");
								if(s.equalsIgnoreCase("S"))
									msg.setValue(0);
								else
								if(s.equalsIgnoreCase("N"))
								{
								    while(entry!=null)
								    {
								        which++;
										read=DBRead(mob,Name(),which-1,lastTime, newOnly, all);
										entry=read.derivedBuildMessage;
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
									MOB M=CMLib.players().getLoadPlayer(from);
									if((M==null)||(M.playerStats()==null)||(M.playerStats().getEmail().indexOf("@")<0))
									{
										mob.tell("Player '"+from+"' does not exist, or has no email address.");
										repeat=true;
									}
									else
									if(!mob.session().choose("Send email to "+M.Name()+" (Y/n)?","YN\n","Y").equalsIgnoreCase("N"))
									{
										String replyMsg=mob.session().prompt("Enter your email response\n\r: ");
										if((replyMsg.trim().length()>0) && (read != null))
										{
											CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX),
																			  mob.Name(),
																			  M.Name(),
																			  "RE: "+read.subj,
																			  replyMsg);
											mob.tell("Email queued.");
										}
										else
										{
											mob.tell("Aborted.");
											repeat=true;
										}
									}
									else
										repeat=true;
								}
								else
								if(s.equalsIgnoreCase("T"))
								{
								    String journal=mob.session().prompt("Enter the journal to transfer this msg to: ","");
								    journal=journal.trim();
								    if(journal.length()>0)
								    {
                                        String realName=null;
                                        for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
                                        {
                                        	JournalsLibrary.CommandJournal CMJ=e.nextElement();
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
											mob.tell("The journal '"+journal+"' does not presently exist.  Aborted.");
								        else
								        {
											Vector journal2=CMLib.database().DBReadJournalMsgs(Name());
											JournalsLibrary.JournalEntry entry2=(JournalsLibrary.JournalEntry)journal2.elementAt(which-1);
											CMLib.database().DBDeleteJournal(Name(),entry2.key);
											CMLib.database().DBWriteJournal(realName,
																			  entry2.from,
																			  entry2.to,
																			  entry2.subj,
																			  entry2.msg);
											msg.setValue(-1);
								        }
										mob.tell("Message transferred.");
								    }
							        else
										mob.tell("Aborted.");
								}
								else
								if(s.equalsIgnoreCase("D"))
								{
									read=DBRead(mob,Name(),which-1,lastTime, newOnly, all);
									if(read != null)
									{
										CMLib.database().DBDeleteJournal(Name(),read.key);
										msg.setValue(-1);
										mob.tell("Entry deleted.");
									}
									else
										mob.tell("Failed to delete entry.");
								}
								else
								if(s.equalsIgnoreCase("R"))
								{
									if(read != null)
									{
										String replyMsg=mob.session().prompt("Enter your response\n\r: ");
										if(replyMsg.trim().length()>0)
										{
											CMLib.database().DBWriteJournalReply(Name(),read.key,mob.Name(),"","",replyMsg);
											mob.tell("Reply added.");
										}
										else
										{
											mob.tell("Aborted.");
											repeat=true;
										}
									}
								}
							}
							catch(IOException e)
							{
								Log.errOut("JournalItem",e.getMessage());
							}
						}
					}
					else
					if(which<0)
						mob.tell(description());
					else
						mob.tell("That message is private.");
				}
				return;
			}
			return;
		case CMMsg.TYP_WRITE:
			try
			{
                String adminReq=getAdminReq().trim();
                boolean admin=((adminReq.length()>0)&&CMLib.masking().maskCheck(adminReq,mob,true))
                                ||CMSecurity.isAllowed(mob,mob.location(),Name());
				if((msg.targetMessage().toUpperCase().startsWith("DEL"))
				   &&(CMSecurity.isAllowed(mob,mob.location(),"JOURNALS")||admin)
				   &&(!mob.isMonster()))
				{
					if(mob.session().confirm("Delete all journal entries? Are you sure (y/N)?","N"))
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
					||mob.session().confirm("Is this a private message (y/N)?","N"))
					{
						to=mob.session().prompt("To whom:");
                        if(((!to.toUpperCase().trim().startsWith("MASK=")
                                ||(!CMSecurity.isAllowed(mob,mob.location(),"JOURNALS")&&(!admin))))
						&&(!CMLib.players().playerExists(to)))
						{
							mob.tell("I'm sorry, there is no such user.");
							return;
						}
					}
					String subject=mob.session().prompt("Enter a subject: ");
					if(subject.trim().length()==0)
					{
						mob.tell("Aborted.");
						return;
					}
					if((subject.toUpperCase().startsWith("MOTD")||subject.toUpperCase().startsWith("MOTM")||subject.toUpperCase().startsWith("MOTY"))
                       &&(!admin)
					   &&(!(CMSecurity.isAllowed(mob,mob.location(),"JOURNALS"))))
						subject=subject.substring(4);
					String message=mob.session().prompt("Enter your message\n\r: ");
					if(message.trim().length()==0)
					{
						mob.tell("Aborted.");
						return;
					}
					if(message.startsWith("<cmvp>")
                    &&(!admin)
					&&(!(CMSecurity.isAllowed(mob,mob.location(),"JOURNALS"))))
					{
						mob.tell("Illegal code, aborted.");
						return;
					}

					CMLib.database().DBWriteJournal(Name(),mob.Name(),to,subject,message);
					mob.tell("Journal entry added.");
				}
				return;
			}
			catch(IOException e)
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

			long[] newestDate=CMLib.database().DBJournalLatestDateNewerThan(Name(),msg.source().Name(),msg.source().playerStats().lastDateTime());
			if((newestDate[0]>0)
			&&((newestDate[0]!=lastDateRead[0])||(msg.source()!=lastReadTo)))
			{
				lastReadTo=msg.source();
				lastDateRead=newestDate;
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,name()+" has "+newestDate[1]+" new messages.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			}
		}
		else
		if(((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&(msg.target() instanceof Room)
		&&(msg.source()==owner)
		&&(msg.source().playerStats()!=null)
		&&(CMLib.masking().maskCheck(getReadReq(),msg.source(),true)))
		{
			long[] newestDate=CMLib.database().DBJournalLatestDateNewerThan(Name(),msg.source().Name(),msg.source().playerStats().lastDateTime());
			if((newestDate[0]>0)
			&&((newestDate[0]!=lastDateRead[0])||(msg.source()!=lastReadTo)))
			{
				lastReadTo=msg.source();
				lastDateRead=newestDate;
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,name()+" has "+newestDate[1]+" new messages.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			}
		}
		super.executeMsg(myHost,msg);
	}

	public JournalsLibrary.JournalEntry DBRead(MOB reader, String Journal, int which, long lastTimeDate, boolean newOnly, boolean all)
	{
		StringBuffer buf=new StringBuffer("");
		Vector journal=CMLib.database().DBReadJournalMsgs(Journal);
		boolean shortFormat=readableText().toUpperCase().indexOf("SHORTLIST")>=0;
		if((which<0)||(journal==null)||(which>=journal.size()))
		{
			buf.append("#\n\r "+CMStrings.padRight("#",6)
					   +((shortFormat)?"":""
					   +CMStrings.padRight("From",11)
					   +CMStrings.padRight("To",11))
					   +CMStrings.padRight("Date",20)
					   +"Subject\n\r");
			buf.append("-------------------------------------------------------------------------\n\r");
			if(journal==null)
			{
				JournalsLibrary.JournalEntry fakeEntry = new JournalsLibrary.JournalEntry();
				fakeEntry.key="";
				fakeEntry.from="";
				fakeEntry.subj="";
				fakeEntry.derivedBuildMessage=buf;
				return fakeEntry;
			}
		}

		JournalsLibrary.JournalEntry fakeEntry = new JournalsLibrary.JournalEntry();
		if((which<0)||(which>=journal.size()))
		{
			if(journal.size()>0)
			{
				JournalsLibrary.JournalEntry entry=(JournalsLibrary.JournalEntry)journal.firstElement();
				fakeEntry.key=entry.key;
				fakeEntry.from=entry.from;
				fakeEntry.subj=entry.subj;
			}
			Vector selections=new Vector();
			for(int j=0;j<journal.size();j++)
			{
				JournalsLibrary.JournalEntry entry=(JournalsLibrary.JournalEntry)journal.elementAt(j);
				String from=entry.from;
				long date=entry.date;
				String to=entry.to;
				String subject=entry.subj;
				// message is 5, but dont matter.
				long compdate=entry.update;
				StringBuffer selection=new StringBuffer("");
                boolean mayRead=(to.equals("ALL")
                                ||to.equalsIgnoreCase(reader.Name())
                                ||from.equalsIgnoreCase(reader.Name()));
                if((to.toUpperCase().trim().startsWith("MASK="))&&(CMLib.masking().maskCheck(to.trim().substring(5),reader,true)))
                {
                    mayRead=true;
                    to=CMLib.masking().maskDesc(to.trim().substring(5),true);
                }
				if(mayRead)
				{
					if(compdate>lastTimeDate)
					    selection.append("*");
					else
					if(newOnly)
					    continue;
					else
					    selection.append(" ");
					selection.append("^<JRNL \""+name()+"\"^>"+CMStrings.padRight((j+1)+"",4)+"^</JRNL^>) "
								   +((shortFormat)?"":""
								   +CMStrings.padRight(from,10)+" "
								   +CMStrings.padRight(to,10)+" ")
								   +CMStrings.padRight(CMLib.time().date2String(date),19)+" "
								   +CMStrings.padRight(subject,25+(shortFormat?22:0))+"\n\r");
					selections.addElement(selection);
				}
			}
			int numToAdd=CMProps.getIntVar(CMProps.SYSTEMI_JOURNALLIMIT);
			if((numToAdd==0)||(all)) numToAdd=Integer.MAX_VALUE;
			for(int v=selections.size()-1;v>=0;v--)
			{
			    if(numToAdd==0){ selections.setElementAt("",v); continue;}
			    StringBuffer str=(StringBuffer)selections.elementAt(v);
			    if((newOnly)&&(str.charAt(0)!='*'))
			    { selections.setElementAt("",v); continue;}
			    numToAdd--;
			}
			boolean notify=false;
			for(int v=0;v<selections.size();v++)
			{
			    if(!(selections.elementAt(v) instanceof StringBuffer))
			    {
			        notify=true;
			        continue;
			    }
			    buf.append((StringBuffer)selections.elementAt(v));
			}
			if(notify)
			    buf.append("\n\rUse READ ALL [JOURNAL] to see missing entries.");
		}
		else
		{
			JournalsLibrary.JournalEntry entry=(JournalsLibrary.JournalEntry)journal.elementAt(which);
			String from=entry.from;
			long date=entry.date;
			String to=entry.to;
			String subject=entry.subj;
			String message=entry.msg;
			
			fakeEntry.key=entry.key;
			fakeEntry.from=entry.from;
			fakeEntry.subj=entry.subj;
			
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
					message=new String(CMLib.httpUtils().doVirtualPage(message.substring(6).getBytes()));
			}
			catch(HTTPRedirectException e){}

			if((allMine)||(to.equals("ALL")))
				buf.append("\n\r^<JRNL \""+name()+"\"^>"+CMStrings.padRight((which+1)+"",3)+"^</JRNL^>)\n\r"
						   +"FROM: "+from
						   +"\n\rTO  : "+to
						   +"\n\rDATE: "+CMLib.time().date2String(date)
						   +"\n\rSUBJ: "+subject
						   +"\n\r"+message);
		}
		fakeEntry.derivedBuildMessage=buf;
		return fakeEntry;
	}

	private String getParm(String parmName)
	{
        if(readableText().length()==0) return "";
	    Hashtable h=CMParms.parseEQParms(readableText().toUpperCase(),
                                         new String[]{"READ","WRITE","REPLY","ADMIN","PRIVATE","MAILBOX"});
        String req=(String)h.get(parmName.toUpperCase().trim());
        if(req==null) req="";
        return req;
	}
	
	protected String getReadReq() { return getParm("READ");}
	protected String getWriteReq() {return getParm("WRITE");}
    private String getReplyReq() { return getParm("REPLY");}
    private String getAdminReq() { return getParm("ADMIN");}
    
	public void recoverEnvStats(){CMLib.flags().setReadable(this,true); super.recoverEnvStats();}
}
