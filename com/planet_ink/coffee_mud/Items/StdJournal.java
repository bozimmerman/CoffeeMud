package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

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
		isReadable=true;
	}

	public Environmental newInstance()
	{
		return new StdJournal();
	}
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(affect.amITarget(this))
		switch(affect.targetMinor())
		{
		case Affect.TYP_WRITE:
			if((!SaucerSupport.zapperCheck(getWriteReq(),affect.source()))&&(!affect.source().isASysOp(null)))
			{
				affect.source().tell("You are not allowed to write on "+name());
				return false;
			}
			return true;
		}
		return super.okAffect(myHost,affect);
	}

	public void affect(Environmental myHost, Affect affect)
	{
		MOB mob=affect.source();
		if(affect.amITarget(this))
		switch(affect.targetMinor())
		{
		case Affect.TYP_READSOMETHING:
			if(!Sense.canBeSeenBy(this,mob))
				mob.tell("You can't see that!");
			else
			if((!mob.isMonster())
			&&(mob.playerStats()!=null))
			{
				long lastTime=mob.playerStats().lastDateTime();
				if(!SaucerSupport.zapperCheck(getReadReq(),mob))
				{
					mob.tell("You are not allowed to read "+name()+".");
					return;
				}
				int which=-1;
				if(Util.s_long(affect.targetMessage())>0)
					which=Util.s_int(affect.targetMessage());
				StringBuffer entry=DBRead(Name(),mob.Name(),which-1,lastTime);
				boolean mineAble=false;
				if(entry.charAt(0)=='#')
				{
					which=-1;
					entry.setCharAt(0,' ');
				}
				if((entry.charAt(0)=='*')
				   ||(mob.isASysOp(null)))
				{
					mineAble=true;
					entry.setCharAt(0,' ');
				}
				mob.tell(entry.toString()+"\n\r");
				if((entry.toString().trim().length()>0)
				&&(which>0)
				&&(SaucerSupport.zapperCheck(getWriteReq(),mob)||(mob.isASysOp(null))))
				{
					try
					{
						boolean reply=false;
						if(mineAble)
						{
							String s=mob.session().choose("R)eply, D)elete, or RETURN: ","RD\n","\n");
							if(s.equalsIgnoreCase("R"))
								reply=true;
							else
							if(s.equalsIgnoreCase("D"))
							{
								ExternalPlay.DBDeleteJournal(name(),which-1);
								mob.tell("Entry deleted.");
							}
						}
						else
							reply=mob.session().confirm("Reply (y/N)?","N");
						if(reply)
						{
							String replyMsg=mob.session().prompt("Enter your response\n\r: ");
							if(replyMsg.trim().length()>0)
							{
								ExternalPlay.DBWriteJournal(Name(),mob.Name(),"","",replyMsg,which-1);
								mob.tell("Reply added.");
							}
							else
								mob.tell("Aborted.");
						}

					}
					catch(IOException e)
					{
						Log.errOut("JournalItem",e);
					}
				}
				else
				if(which<0)
					mob.tell(description());
				else
					mob.tell("That message is private.");
				return;
			}
			return;
		case Affect.TYP_WRITE:
			try
			{
				if((affect.targetMessage().toUpperCase().startsWith("DEL"))
				   &&(mob.isASysOp(null))
				   &&(!mob.isMonster()))
				{
					if(mob.session().confirm("Delete all journal entries? Are you sure (y/N)?","N"))
						ExternalPlay.DBDeleteJournal(name(),-1);
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
						if(!ExternalPlay.DBUserSearch(null,to))
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
					if(subject.startsWith("MOTD")&&(!mob.isASysOp(null)))
						subject=subject.substring(4);
					String message=mob.session().prompt("Enter your message\n\r: ");
					if(message.trim().length()==0)
					{
						mob.tell("Aborted.");
						return;
					}
					ExternalPlay.DBWriteJournal(Name(),mob.Name(),to,subject,message,-1);
					mob.tell("Journal entry added.");
				}
				return;
			}
			catch(IOException e)
			{
				Log.errOut("JournalItem",e);
			}
			return;
		}
		super.affect(myHost,affect);
	}

	public StringBuffer DBRead(String Journal, String username, int which, long lastTimeDate)
	{
		StringBuffer buf=new StringBuffer("");
		Vector journal=ExternalPlay.DBReadJournal(Journal);
		if((which<0)||(journal==null)||(which>=journal.size()))
		{
			buf.append("#\n\r "+Util.padRight("#",5)
					   +Util.padRight("From",11)
					   +Util.padRight("To",11)
					   +Util.padRight("Date",20)
					   +"Subject\n\r");
			buf.append("-------------------------------------------------------------------------\n\r");
			if(journal==null) return buf;
		}

		if((which<0)||(which>=journal.size()))
		{
			for(int j=0;j<journal.size();j++)
			{
				Vector entry=(Vector)journal.elementAt(j);
				String from=(String)entry.elementAt(1);
				String date=(String)entry.elementAt(2);
				String to=(String)entry.elementAt(3);
				String subject=(String)entry.elementAt(4);
				if(subject.startsWith("MOTD"))
				{
					subject=subject.substring(4);
					date=""+(Util.s_long(date)+(1000*60*60*24));
				}
				if(to.equals("ALL")||to.equalsIgnoreCase(username)||from.equalsIgnoreCase(username))
				{
					if(Util.s_long(date)>lastTimeDate)
						buf.append("*");
					else
						buf.append(" ");
					buf.append(Util.padRight((j+1)+"",3)+") "
							   +Util.padRight(from,10)+" "
							   +Util.padRight(to,10)+" "
							   +Util.padRight(IQCalendar.d2String(Util.s_long(date)),19)+" "
							   +Util.padRight(subject,25)+"\n\r");
				}
			}
		}
		else
		{
			Vector entry=(Vector)journal.elementAt(which);
			String from=(String)entry.elementAt(1);
			String date=(String)entry.elementAt(2);
			String to=(String)entry.elementAt(3);
			String subject=(String)entry.elementAt(4);
			String message=(String)entry.elementAt(5);
			boolean mineAble=to.equalsIgnoreCase(username)||from.equalsIgnoreCase(username);
			if(mineAble) buf.append("*");
			else buf.append(" ");
			if(to.equals("ALL")||mineAble)
				buf.append("\n\r"+Util.padRight((which+1)+"",3)+")\n\r"
						   +"FROM: "+from
						   +"\n\rTO  : "+to
						   +"\n\rDATE: "+IQCalendar.d2String(Util.s_long(date))
						   +"\n\rSUBJ: "+subject
						   +"\n\r"+message);
		}
		return buf;
	}

	private String getReadReq()
	{
		if(readableText().length()==0) return "";
		String text=readableText().toUpperCase();
		int readeq=text.indexOf("READ=");
		if(readeq<0) return "";
		text=text.substring(readeq+5);
		int writeeq=text.indexOf("WRITE=");
		if(writeeq>=0)
			return text.substring(0,writeeq);
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
		if(readeq>=0)
			return text.substring(0,readeq);
		return text;
	}
}
