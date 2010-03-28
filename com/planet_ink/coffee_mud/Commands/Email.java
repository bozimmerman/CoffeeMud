package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

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
public class Email extends StdCommand
{
	public Email(){}

	private String[] access={"EMAIL"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob.session()==null)	return true;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return true;

        if((commands!=null)
        &&(commands.size()>1)
        &&(commands.elementAt(1) instanceof String)
        &&(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0))
        {
            String name=CMParms.combine(commands,1);
            if(name.equalsIgnoreCase("BOX"))
            {
            	String journalName=CMProps.getVar(CMProps.SYSTEM_MAILBOX);
                Vector<JournalsLibrary.JournalEntry> msgs=CMLib.database().DBReadJournalMsgs(journalName);
                while((mob.session()!=null)&&(!mob.session().killFlag()))
                {
                    Vector mymsgs=new Vector();
                    StringBuffer messages=new StringBuffer("^X"+CMStrings.padCenter(mob.Name()+"'s MailBox",48)+"^?^.\n\r");
                    messages.append("^X### "+CMStrings.padRight("From",15)+" "+CMStrings.padRight("Date",20)+" Subject^?^.\n\r");
                    for(int num=0;num<msgs.size();num++)
                    {
                    	JournalsLibrary.JournalEntry thismsg=msgs.elementAt(num);
                        String to=(String)thismsg.to;
                        if(to.equalsIgnoreCase("ALL")
                        ||to.equalsIgnoreCase(mob.Name())
                        ||(to.toUpperCase().trim().startsWith("MASK=")&&CMLib.masking().maskCheck(to.trim().substring(5),mob,true)))
                        {
                            mymsgs.addElement(thismsg);
                            messages.append(CMStrings.padRight(""+mymsgs.size(),4)
                                    +CMStrings.padRight((thismsg.from),16)
                                    +CMStrings.padRight(CMLib.time().date2String(thismsg.date),21)
                                    +(thismsg.subj)
                                    +"\n\r");
                        }
                    }
                    if((mymsgs.size()==0)
                    ||(CMath.bset(metaFlags,Command.METAFLAG_POSSESSED))
                    ||(CMath.bset(metaFlags,Command.METAFLAG_AS)))
                    {
                        if(CMath.bset(mob.getBitmap(),MOB.ATT_AUTOFORWARD))
                            mob.tell("You have no email waiting, but then, it's probably been forwarded to you already.");
                        else
                            mob.tell("You have no email waiting.");
                        return false;
                    }
                    Session S=mob.session();
                    try {
                        if(S!=null) S.snoopSuspension(1);
                        mob.tell(messages.toString());
                    } finally {
                        if(S!=null) S.snoopSuspension(-1);
                    }
                    String s=mob.session().prompt("Enter a message #","");
                    if((!CMath.isInteger(s))||(mob.session().killFlag()))
                        return false;
                    int num=CMath.s_int(s);
                    if((num<=0)||(num>mymsgs.size()))
                        mob.tell("That is not a valid number.");
                    else
                    while((mob.session()!=null)&&(!mob.session().killFlag()))
                    {
                    	JournalsLibrary.JournalEntry thismsg=(JournalsLibrary.JournalEntry)mymsgs.elementAt(num-1);
                        String key=thismsg.key;
                        String from=thismsg.from;
                        String date=CMLib.time().date2String(thismsg.date);
                        String subj=thismsg.subj;
                        String message=thismsg.msg;
                        messages=new StringBuffer("");
                        messages.append("^XMessage :^?^."+num+"\n\r");
                        messages.append("^XFrom    :^?^."+from+"\n\r");
                        messages.append("^XDate    :^?^."+date+"\n\r");
                        messages.append("^XSubject :^?^."+subj+"\n\r");
                        messages.append("^X------------------------------------------------^?^.\n\r");
                        messages.append(message+"\n\r\n\r");
                        try {
                            if(S!=null) S.snoopSuspension(1);
                            mob.tell(messages.toString());
                        } finally {
                            if(S!=null) S.snoopSuspension(-1);
                        }
                        s=mob.session().choose("Would you like to D)elete, H)old, or R)eply (D/H/R)? ","DHR","H");
                        if(s.equalsIgnoreCase("H"))
                            break;
                        if(s.equalsIgnoreCase("R"))
                        {
                            if((from.length()>0)
                            &&(!from.equals(mob.Name()))
                            &&(!from.equalsIgnoreCase("BOX"))
                            &&(CMLib.players().getLoadPlayer(from)!=null))
                                execute(mob,CMParms.makeVector(getAccessWords()[0],from),metaFlags);
                            else
                                mob.tell("You can not reply to this email.");
                        }
                        else
                        if(s.equalsIgnoreCase("D"))
                        {
                            CMLib.database().DBDeleteJournal(journalName,key);
                            msgs.remove(thismsg);
                            mob.tell("Deleted.");
                            break;
                        }
                    }
                }
            }
            else
            {
                MOB M=CMLib.players().getLoadPlayer(name);
                if(M==null)
                {
                    mob.tell("There is no player called '"+name+"' to send email to.  If you were trying to read your mail, try EMAIL BOX.  If you were trying to change your email address, just enter EMAIL without any parameters.");
                    return false;
                }
                if(!CMath.bset(M.getBitmap(),MOB.ATT_AUTOFORWARD))
                {
                    if(!mob.session().confirm("Send email to '"+M.Name()+"' (Y/n)?","Y"))
                        return false;
                }
                else
                {
                    if(!mob.session().confirm("Send email to '"+M.Name()+"', even though their AUTOFORWARD is turned off (y/N)?","N"))
                        return false;
                }
                if(CMProps.getIntVar(CMProps.SYSTEMI_MAXMAILBOX)>0)
                {
                    int count=CMLib.database().DBCountJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX),null,M.Name());
                    if(count>=CMProps.getIntVar(CMProps.SYSTEMI_MAXMAILBOX))
                    {
                        mob.tell(M.Name()+"'s mailbox is full.");
                        return false;
                    }
                }
                String subject=mob.session().prompt("Email Subject: ","").trim();
                if(subject.length()==0)
                {
                    mob.tell("Aborted");
                    return false;
                }
                String message=mob.session().prompt("Enter your message\n\r: ","").trim();
                if(message.trim().length()==0)
                {
                    mob.tell("Aborted");
                    return false;
                }
                message+="\n\r\n\rThis message was sent through the "+CMProps.getVar(CMProps.SYSTEM_MUDNAME)+" mail server at "+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN)+", port"+CMProps.getVar(CMProps.SYSTEM_MUDPORTS)+".  Please contact the administrators regarding any abuse of this system.\n\r";
                CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX), mob.Name(), M.Name(), subject, message);
                mob.tell("Your email has been sent.");
                return true;
            }
        }
		if((pstats.getEmail()==null)||(pstats.getEmail().length()==0))
			mob.session().println("\n\rYou have no email address on file for this character.");
		else
		{
			if(commands==null) return true;
			String change=mob.session().prompt("You currently have '"+pstats.getEmail()+"' set as the email address for this character.\n\rChange it (y/N)?","N");
			if(change.toUpperCase().startsWith("N")) return false;
		}
        if((CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("PASS"))
        &&(commands!=null)
        &&(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0))
            mob.session().println("\n\r** Changing your email address will cause you to be logged off, and a new password to be generated and emailed to the new address. **\n\r");
		String newEmail=mob.session().prompt("New E-mail Address:");
		if(newEmail==null) return false;
		newEmail=newEmail.trim();
		if(!CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("OPTION"))
		{
			if(newEmail.length()<6) return false;
			if(newEmail.indexOf("@")<0) return false;
			String confirmEmail=mob.session().prompt("Confirm that '"+newEmail+"' is correct by re-entering.\n\rRe-enter:");
			if(confirmEmail==null) return false;
			confirmEmail=confirmEmail.trim();
			if(confirmEmail.length()==0) return false;
			if(!(newEmail.equalsIgnoreCase(confirmEmail))) return false;
		}
        pstats.setEmail(newEmail);
        CMLib.database().DBUpdateEmail(mob);
        if((commands!=null)
        &&(CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("PASS"))
        &&(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0))
        {
            String password="";
            for(int i=0;i<6;i++)
                password+=(char)('a'+CMLib.dice().roll(1,26,-1));
            pstats.setPassword(password);
            CMLib.database().DBUpdatePassword(mob.Name(),password);
            CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX),
                      mob.Name(),
                      mob.Name(),
                      "Password for "+mob.Name(),
                      "Your new password for "+mob.Name()+" is: "+pstats.password()+"\n\rYou can login by pointing your mud client at "+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN)+" port(s):"+CMProps.getVar(CMProps.SYSTEM_MUDPORTS)+".\n\rYou may use the PASSWORD command to change it once you are online.");
            mob.tell("You will receive an email with your new password shortly.  Goodbye.");
            if(mob.session()!=null)
            {
                try{Thread.sleep(1000);}catch(Exception e){}
                mob.session().kill(false,false,false);
            }
        }
		return true;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
