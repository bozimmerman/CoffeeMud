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
		name="a journal";
		displayText="a journal sits here.";
		description="Use the READ command to read the journal, and WRITE to add your own entries.";
		material=EnvResource.RESOURCE_PAPER;
		isReadable=true;
	}

	public Environmental newInstance()
	{
		return new StdJournal();
	}
	public boolean okAffect(Affect affect)
	{
		if(affect.amITarget(this))
		switch(affect.targetMinor())
		{
		case Affect.TYP_WRITE:
			if(affect.source().envStats().level()<envStats().level())
			{
				mob.tell("You are not allowed to write on "+name());
				return false;
			}
			return true;
		}
		return super.okAffect(affect);
	}
	
	public void affect(Affect affect)
	{
		MOB mob=affect.source();
		if(affect.amITarget(this))
		switch(affect.targetMinor())
		{
		case Affect.TYP_READSOMETHING:
			if(!Sense.canBeSeenBy(this,mob))
				mob.tell("You can't see that!");
			else
			if(!mob.isMonster())
			{
				int which=-1;
				if(Util.s_long(affect.targetMessage())>0)
					which=Util.s_int(affect.targetMessage());
				long lastTime=mob.lastDateTime().getTimeInMillis();
				StringBuffer entry=DBRead(name(),mob.name(),which-1,lastTime);
				boolean mineAble=false;
				if(entry.charAt(0)=='#')
				{
					which=-1;
					entry.setCharAt(0,' ');
				}
				if((entry.charAt(0)=='*')||(mob.isASysOp(null)))
				{
					mineAble=true;
					entry.setCharAt(0,' ');
				}
				mob.tell(entry.toString()+"\n\r");
				if((entry.toString().trim().length()>0)&&(which>0))
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
								ExternalPlay.DBWriteJournal(name(),mob.name(),"","",replyMsg,which-1);
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
				{
					mob.tell("Enter 'READ [NUMBER] [JOURNAL]' to read an entry.");
					mob.tell("Use your WRITE skill to add new entries. ");
				}
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
					String message=mob.session().prompt("Enter your message\n\r: ");
					if(message.trim().length()==0)
					{
						mob.tell("Aborted.");
						return;
					}
					ExternalPlay.DBWriteJournal(name(),mob.name(),to,subject,message,-1);
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
		super.affect(affect);
	}
	
	public StringBuffer DBRead(String Journal, String username, int which, long lastTimeDate)
	{
		StringBuffer buf=new StringBuffer("");
		Vector journal=ExternalPlay.DBReadJournal(Journal);
		if((which<0)||(journal==null)||(which>=journal.size()))
		{
			buf.append("#\n\r "+Util.padRight("#",5)+Util.padRight("From",16)+Util.padRight("To",16)+"Subject\n\r");
			buf.append("---------------------------------------------\n\r");
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
				if(to.equals("ALL")||to.equalsIgnoreCase(username)||from.equalsIgnoreCase(username))
				{
					if(Util.s_long(date)>lastTimeDate)
						buf.append("*");
					else
						buf.append(" ");
					buf.append(Util.padRight((j+1)+"",3)+") "+Util.padRight(from,15)+" "+Util.padRight(to,15)+" "+subject+"\n\r");
				}
			}
		}
		else
		{
			Vector entry=(Vector)journal.elementAt(which);
			String from=(String)entry.elementAt(1);
			String to=(String)entry.elementAt(3);
			String subject=(String)entry.elementAt(4);
			String message=(String)entry.elementAt(5);
			boolean mineAble=to.equalsIgnoreCase(username)||from.equalsIgnoreCase(username);
			if(mineAble) buf.append("*");
			else buf.append(" ");
			if(to.equals("ALL")||mineAble)
				buf.append("\n\r"+Util.padRight((which+1)+"",3)+")\n\r"+"FROM: "+Util.padRight(from,15)+"\n\rTO  : "+Util.padRight(to,15)+"\n\rSUBJ: "+subject+"\n\r"+message);
		}
		return buf;
	}
}
