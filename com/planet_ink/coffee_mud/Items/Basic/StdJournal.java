package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.exceptions.HTTPRedirectException;
import java.util.*;
import java.io.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
	public String ID(){	return "StdJournal";}
	public StdJournal()
	{
		super();
		setName("a journal");
		setDisplayText("a journal sits here.");
		setDescription("Enter `READ [NUMBER] [JOURNAL]` to read an entry.%0D%0AUse your WRITE skill to add new entries. ");
		material=EnvResource.RESOURCE_PAPER;
		baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMREADABLE);
		recoverEnvStats();
	}

	protected MOB lastReadTo=null;
	protected long lastDateRead=-1;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WRITE:
        {
            String adminReq=getAdminReq().trim();
            boolean admin=(adminReq.length()>0)&&MUDZapper.zapperCheck(adminReq,msg.source());
			if((!MUDZapper.zapperCheck(getWriteReq(),msg.source()))
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
			if(!Sense.canBeSeenBy(this,mob))
				mob.tell("You can't see that!");
			else
			if((!mob.isMonster())
			&&(mob.playerStats()!=null))
			{
                String adminReq=getAdminReq().trim();
                boolean admin=(adminReq.length()>0)&&MUDZapper.zapperCheck(adminReq,mob);
				long lastTime=mob.playerStats().lastDateTime();
				if((admin)&&(!MUDZapper.zapperCheck(getReadReq(),mob)))
				{
					mob.tell("You are not allowed to read "+name()+".");
					return;
				}
				int which=-1;
				boolean newOnly=false;
				boolean all=false;
				Vector parse=Util.parse(msg.targetMessage());
				for(int v=0;v<parse.size();v++)
				{
				    String s=(String)parse.elementAt(v);
					if(Util.s_long(s)>0)
						which=Util.s_int(msg.targetMessage());
					else
					if(s.equalsIgnoreCase("NEW"))
					    newOnly=true;
					else
					if(s.equalsIgnoreCase("ALL")||s.equalsIgnoreCase("OLD"))
					    all=true;
				}
				Vector read=DBRead(Name(),mob.Name(),which-1,lastTime, newOnly, all);
				boolean megaRepeat=true;
				while(megaRepeat)
				{
				    megaRepeat=false;
					String from=(String)read.firstElement();
					StringBuffer entry=(StringBuffer)read.lastElement();
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
					&&(MUDZapper.zapperCheck(getWriteReq(),mob)
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
                                if(MUDZapper.zapperCheck(getReplyReq(),mob)
                                ||admin
                                ||(CMSecurity.isAllowed(msg.source(),msg.source().location(),"JOURNALS")))
                                {
                                    prompt+="^<MENU^>R^</MENU^>)eply ";
                                    cmds+="R";
                                }
								if((CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX).length()>0)
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
										read=DBRead(Name(),mob.Name(),which-1,lastTime, newOnly, all);
										entry=(StringBuffer)read.lastElement();
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
									MOB M=CMMap.getLoadPlayer(from);
									if((M==null)||(M.playerStats()==null)||(M.playerStats().getEmail().indexOf("@")<0))
									{
										mob.tell("Player '"+from+"' does not exist, or has no email address.");
										repeat=true;
									}
									else
									if(!mob.session().choose("Send email to "+M.Name()+" (Y/n)?","YN\n","Y").equalsIgnoreCase("N"))
									{
										String replyMsg=mob.session().prompt("Enter your email response\n\r: ");
										if(replyMsg.trim().length()>0)
										{
											CMClass.DBEngine().DBWriteJournal(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX),
																			  mob.Name(),
																			  M.Name(),
																			  "RE: "+((String)read.elementAt(1)),
																			  replyMsg,-1);
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
                                        for(int i=0;i<ChannelSet.getNumCommandJournals();i++)
                                            if(journal.equalsIgnoreCase(ChannelSet.getCommandJournalName(i))
                                            ||journal.equalsIgnoreCase(ChannelSet.getCommandJournalName(i)+"s"))
                                            {
                                                realName="SYSTEM_"+ChannelSet.getCommandJournalName(i).toUpperCase()+"S";
                                                break;
                                            }
                                        if(realName==null)
                                            realName=CMClass.DBEngine().DBGetRealJournalName(journal);
                                        if(realName==null)
                                            realName=CMClass.DBEngine().DBGetRealJournalName(journal.toUpperCase());
								        if(realName==null)
											mob.tell("The journal '"+journal+"' does not presently exist.  Aborted.");
								        else
								        {
											Vector journal2=CMClass.DBEngine().DBReadJournal(Name());
											Vector entry2=(Vector)journal2.elementAt(which-1);
											String from2=(String)entry2.elementAt(1);
											String to=(String)entry2.elementAt(3);
											String subject=(String)entry2.elementAt(4);
											String message=(String)entry2.elementAt(5);
											CMClass.DBEngine().DBDeleteJournal(Name(),which-1);
											CMClass.DBEngine().DBWriteJournal(realName,
																			  from2,
																			  to,
																			  subject,
																			  message,-1);
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
									CMClass.DBEngine().DBDeleteJournal(Name(),which-1);
									msg.setValue(-1);
									mob.tell("Entry deleted.");
								}
								else
								if(s.equalsIgnoreCase("R"))
								{
									String replyMsg=mob.session().prompt("Enter your response\n\r: ");
									if(replyMsg.trim().length()>0)
									{
										CMClass.DBEngine().DBWriteJournal(Name(),mob.Name(),"","",replyMsg,which-1);
										mob.tell("Reply added.");
									}
									else
									{
										mob.tell("Aborted.");
										repeat=true;
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
                boolean admin=(adminReq.length()>0)&&MUDZapper.zapperCheck(adminReq,mob);
				if((msg.targetMessage().toUpperCase().startsWith("DEL"))
				   &&(CMSecurity.isAllowed(mob,mob.location(),"JOURNALS")||admin)
				   &&(!mob.isMonster()))
				{
					if(mob.session().confirm("Delete all journal entries? Are you sure (y/N)?","N"))
						CMClass.DBEngine().DBDeleteJournal(name(),-1);
				}
				else
				if(!mob.isMonster())
				{
					String to="ALL";
					if(getWriteReq().toUpperCase().indexOf("PRIVATE")>=0)
						to=mob.Name();
					else
					if(mob.session().confirm("Is this a private message (y/N)?","N"))
					{
						to=mob.session().prompt("To whom:");
						if(!CMClass.DBEngine().DBUserSearch(null,to))
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

					CMClass.DBEngine().DBWriteJournal(Name(),mob.Name(),to,subject,message,-1);
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
		&&(MUDZapper.zapperCheck(getReadReq(),msg.source())))
		{
			long lastDate=CMClass.DBEngine().DBReadNewJournalDate(Name(),msg.source().Name());
			if((lastDate>msg.source().playerStats().lastDateTime())
			&&((lastDate!=lastDateRead)||(msg.source()!=lastReadTo)))
			{
				lastReadTo=msg.source();
				lastDateRead=lastDate;
				msg.addTrailerMsg(new FullMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,name()+" has new messages.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			}
		}
		else
		if(((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&(msg.target() instanceof Room)
		&&(msg.source()==owner)
		&&(msg.source().playerStats()!=null)
		&&(MUDZapper.zapperCheck(getReadReq(),msg.source())))
		{
			long lastDate=CMClass.DBEngine().DBReadNewJournalDate(Name(),msg.source().Name());
			if((lastDate>msg.source().playerStats().lastDateTime())
			&&((lastDate!=lastDateRead)||(msg.source()!=lastReadTo)))
			{
				lastReadTo=msg.source();
				lastDateRead=lastDate;
				msg.addTrailerMsg(new FullMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,name()+" has new messages.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			}
		}
		super.executeMsg(myHost,msg);
	}

	public Vector DBRead(String Journal, String username, int which, long lastTimeDate, boolean newOnly, boolean all)
	{
		StringBuffer buf=new StringBuffer("");
		Vector reply=new Vector();
		Vector journal=CMClass.DBEngine().DBReadJournal(Journal);
		boolean shortFormat=readableText().toUpperCase().indexOf("SHORTLIST")>=0;
		if((which<0)||(journal==null)||(which>=journal.size()))
		{
			buf.append("#\n\r "+Util.padRight("#",5)
					   +((shortFormat)?"":""
					   +Util.padRight("From",11)
					   +Util.padRight("To",11))
					   +Util.padRight("Date",20)
					   +"Subject\n\r");
			buf.append("-------------------------------------------------------------------------\n\r");
			if(journal==null)
			{
				reply.addElement("");
				reply.addElement("");
				reply.addElement(buf);
				return reply;
			}
		}

		if((which<0)||(which>=journal.size()))
		{
			if(journal.size()>0)
			{
				reply.addElement(((Vector)journal.firstElement()).elementAt(1));
				reply.addElement(((Vector)journal.firstElement()).elementAt(4));
			}
			Vector selections=new Vector();
			for(int j=0;j<journal.size();j++)
			{
				Vector entry=(Vector)journal.elementAt(j);
				String from=(String)entry.elementAt(1);
				String date=(String)entry.elementAt(2);
				String to=(String)entry.elementAt(3);
				String subject=(String)entry.elementAt(4);
				// message is 5, but dont matter.
				String compdate=(String)entry.elementAt(6);
				StringBuffer selection=new StringBuffer("");
				if(to.equals("ALL")||to.equalsIgnoreCase(username)||from.equalsIgnoreCase(username))
				{
					if(Util.s_long(compdate)>lastTimeDate)
					    selection.append("*");
					else
					if(newOnly)
					    continue;
					else
					    selection.append(" ");
					selection.append("^<JRNL \""+name()+"\"^>"+Util.padRight((j+1)+"",3)+"^</JRNL^>) "
								   +((shortFormat)?"":""
								   +Util.padRight(from,10)+" "
								   +Util.padRight(to,10)+" ")
								   +Util.padRight(IQCalendar.d2String(Util.s_long(date)),19)+" "
								   +Util.padRight(subject,25+(shortFormat?22:0))+"\n\r");
					selections.addElement(selection);
				}
			}
			int numToAdd=CommonStrings.getIntVar(CommonStrings.SYSTEMI_JOURNALLIMIT);
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
			Vector entry=(Vector)journal.elementAt(which);
			String from=(String)entry.elementAt(1);
			String date=(String)entry.elementAt(2);
			String to=(String)entry.elementAt(3);
			String subject=(String)entry.elementAt(4);
			String message=(String)entry.elementAt(5);
			
			reply.addElement(entry.elementAt(1));
			reply.addElement(entry.elementAt(4));
			
			//String compdate=(String)entry.elementAt(6);
			boolean mineAble=to.equalsIgnoreCase(username)||from.equalsIgnoreCase(username);
			if(mineAble)
				buf.append("*");
			else
				buf.append(" ");
			try
			{
				if(message.startsWith("<cmvp>"))
					message=new String(CMClass.httpUtils().doVirtualPage(message.substring(6).getBytes()));
			}
			catch(HTTPRedirectException e){}

			if(to.equals("ALL")||mineAble)
				buf.append("\n\r^<JRNL \""+name()+"\"^>"+Util.padRight((which+1)+"",3)+"^</JRNL^>)\n\r"
						   +"FROM: "+from
						   +"\n\rTO  : "+to
						   +"\n\rDATE: "+IQCalendar.d2String(Util.s_long(date))
						   +"\n\rSUBJ: "+subject
						   +"\n\r"+message);
		}
		while(reply.size()<2)
			reply.addElement("");
		reply.addElement(buf);
		return reply;
	}

	private String getReadReq()
	{
		if(readableText().length()==0) return "";
		String text=readableText().toUpperCase();
		int readeq=text.indexOf("READ=");
		if(readeq<0) return "";
		text=text.substring(readeq+5);
		int writeeq=text.indexOf("WRITE=");
		if(writeeq>=0)text= text.substring(0,writeeq);
        int replyreq=text.indexOf("REPLY=");
        if(replyreq>=0) text=text.substring(0,replyreq);
        int adminreq=text.indexOf("ADMIN=");
        if(adminreq>=0) text=text.substring(0,adminreq);
		return text;
	}
	private String getWriteReq()
	{
		if(readableText().length()==0) return "";
		String text=readableText().toUpperCase();
		int writeeq=text.indexOf("WRITE=");
		if(writeeq<0) return "";
		text=text.substring(writeeq+6);
		int readeq=text.indexOf("READ=");
		if(readeq>=0) text=text.substring(0,readeq);
        int replyreq=text.indexOf("REPLY=");
        if(replyreq>=0) text=text.substring(0,replyreq);
        int adminreq=text.indexOf("ADMIN=");
        if(adminreq>=0) text=text.substring(0,adminreq);
		return text;
	}
    private String getReplyReq()
    {
        if(readableText().length()==0) return "";
        String text=readableText().toUpperCase();
        int replyreq=text.indexOf("REPLY=");
        if(replyreq<0) return "";
        text=text.substring(replyreq+6);
        int readeq=text.indexOf("READ=");
        if(readeq>=0) text=text.substring(0,readeq);
        int writeeq=text.indexOf("WRITE=");
        if(writeeq>=0)text= text.substring(0,writeeq);
        int adminreq=text.indexOf("ADMIN=");
        if(adminreq>=0) text=text.substring(0,adminreq);
        return text;
    }
    private String getAdminReq()
    {
        if(readableText().length()==0) return "";
        String text=readableText().toUpperCase();
        int adminreq=text.indexOf("ADMIN=");
        if(adminreq<0) return "";
        text=text.substring(adminreq+6);
        int readeq=text.indexOf("READ=");
        if(readeq>=0) text=text.substring(0,readeq);
        int writeeq=text.indexOf("WRITE=");
        if(writeeq>=0)text= text.substring(0,writeeq);
        int replyreq=text.indexOf("REPLY=");
        if(replyreq>=0) text=text.substring(0,replyreq);
        return text;
    }
	public void recoverEnvStats(){Sense.setReadable(this,true); super.recoverEnvStats();}
}
