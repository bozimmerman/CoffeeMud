package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class GenJournal extends GenItem
{
	public GenJournal()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Journal";
		displayText="a journal sits here.";
		description="Use the READ command to read the journal, and WRITE to add your own entries.";
		isReadable=true;
	}

	public Environmental newInstance()
	{
		return new GenJournal();
	}
	public boolean isGeneric(){return true;}
	
	public boolean okAffect(Affect affect)
	{
		MOB mob=affect.source();
		if(affect.amITarget(this))
		switch(affect.targetMinor())
		{
		case Affect.TYP_WRITE:
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
				if(Util.s_int(affect.targetMessage())>0)
					which=Util.s_int(affect.targetMessage());
				StringBuffer entry=DBRead(name(),mob.name(),which-1,Util.s_int(this.readableText()));
				mob.tell(entry.toString()+"\n\r");
				readableText=IQCalendar.getInstance().getTime().getTime()+"";
				if((entry.toString().trim().length()>0)&&(which>0))
				{
					try
					{
						if(mob.session().confirm("Would you like to add a reply (y/N)?","N"))
						{
							String reply=mob.session().prompt("Enter your response:\n\r");
							if(reply.trim().length()>0)
								ExternalPlay.DBWriteJournal(name(),mob.name(),"","",reply,which-1);
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
				String message=mob.session().prompt("Enter your message: ");
				if(message.trim().length()==0)
				{
					mob.tell("Aborted.");
					return;
				}
				ExternalPlay.DBWriteJournal(name(),mob.name(),to,subject,message,-1);
				mob.tell("Journal entry added.");
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
		if(which<0)
		{
			buf.append("\n\r"+Util.padRight("#",6)+Util.padRight("From",16)+Util.padRight("To",16)+"Subject\n\r");
			buf.append("---------------------------------------------\n\r");
		}
		if(journal==null) return buf;
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
					if(Util.s_int(date)>lastTimeDate)
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
			String date=(String)entry.elementAt(2);
			String to=(String)entry.elementAt(3);
			String subject=(String)entry.elementAt(4);
			String message=(String)entry.elementAt(5);
			if(to.equals("ALL")||to.equalsIgnoreCase(username)||from.equalsIgnoreCase(username))
				buf.append("\n\r"+Util.padRight((which+1)+"",3)+")\n\r"+"FROM: "+Util.padRight(from,15)+"\n\rTO  : "+Util.padRight(to,15)+"\n\rSUBJ: "+subject+"\n\r"+message);
		}
		return buf;
	}
	
}
