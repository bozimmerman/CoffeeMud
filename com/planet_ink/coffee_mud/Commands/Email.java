package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

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
public class Email extends StdCommand
{
	public Email(){}

	private String[] access={"EMAIL"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob.session()==null)	return true;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return true;

        if((commands!=null)
        &&(commands.size()>1)
        &&(commands.elementAt(1) instanceof String)
        &&(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX).length()>0))
        {
            String name=Util.combine(commands,1);
            if(name.equalsIgnoreCase("BOX"))
            {
                Vector msgs=CMClass.DBEngine().DBReadJournal(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX));
                while((mob.session()!=null)&&(!mob.session().killFlag()))
                {
                    Vector mymsgs=new Vector();
                    StringBuffer messages=new StringBuffer("^X"+Util.padCenter(mob.Name()+"'s MailBox",48)+"^?^.\n\r");
                    messages.append("^X### "+Util.padRight("From",15)+" "+Util.padRight("Date",20)+" Subject^?^.\n\r");
                    for(int num=0;num<msgs.size();num++)
                    {
                        Vector thismsg=(Vector)msgs.elementAt(num);
                        String to=((String)thismsg.elementAt(3));
                        if(to.equalsIgnoreCase("all")
                        ||to.equalsIgnoreCase(mob.Name()))
                        {
                            mymsgs.addElement(thismsg);
                            messages.append(Util.padRight(""+mymsgs.size(),4)
                                    +Util.padRight(((String)thismsg.elementAt(1)),16)
                                    +Util.padRight(IQCalendar.d2String(Util.s_long((String)thismsg.elementAt(2))),21)
                                    +((String)thismsg.elementAt(4))
                                    +"\n\r");
                        }
                    }
                    if(mymsgs.size()==0)
                    {
                        if(Util.bset(mob.getBitmap(),MOB.ATT_AUTOFORWARD))
                            mob.tell("You have no email waiting, but then, it's probably been forwarded to you already.");
                        else
                            mob.tell("You have no email waiting.");
                        return false;
                    }
                    mob.tell(messages.toString());
                    String s=mob.session().prompt("Enter a message #","");
                    if((!Util.isInteger(s))||(mob.session().killFlag()))
                        return false;
                    int num=Util.s_int(s);
                    if((num<=0)||(num>mymsgs.size()))
                        mob.tell("That is not a valid number.");
                    else
                    while((mob.session()!=null)&&(!mob.session().killFlag()))
                    {
                        Vector thismsg=(Vector)mymsgs.elementAt(num-1);
                        String key=(String)thismsg.elementAt(0);
                        String from=(String)thismsg.elementAt(1);
                        String date=(String)thismsg.elementAt(2);
                        date=IQCalendar.d2String(Util.s_long(date));
                        String subj=(String)thismsg.elementAt(4);
                        String message=(String)thismsg.elementAt(5);
                        messages=new StringBuffer("");
                        messages.append("^XMessage :^?^."+num+"\n\r");
                        messages.append("^XFrom    :^?^."+from+"\n\r");
                        messages.append("^XDate    :^?^."+date+"\n\r");
                        messages.append("^XSubject :^?^."+subj+"\n\r");
                        messages.append("^X------------------------------------------------^?^.\n\r");
                        messages.append(message+"\n\r\n\r");
                        mob.tell(messages.toString());
                        s=mob.session().choose("Would you like to D)elete, H)old, or R)eply (D/H/R)? ","DHR","H");
                        if(s.equalsIgnoreCase("H"))
                            break;
                        if(s.equalsIgnoreCase("R"))
                        {
                            if((from.length()>0)
                            &&(!from.equals(mob.Name()))
                            &&(!from.equalsIgnoreCase("BOX"))
                            &&(CMMap.getLoadPlayer(from)!=null))
                                execute(mob,Util.makeVector(getAccessWords()[0],from));
                            else
                                mob.tell("You can not reply to this email.");
                        }
                        else
                        if(s.equalsIgnoreCase("D"))
                        {
                            CMClass.DBEngine().DBDeleteJournal(key);
                            msgs.remove(thismsg);
                            mob.tell("Deleted.");
                            break;
                        }
                    }
                }
            }
            else
            {
                MOB M=CMMap.getLoadPlayer(name);
                if(M==null)
                {
                    mob.tell("There is no player called '"+name+"' to send email to.  If you were trying to read your mail, try EMAIL BOX.  If you were trying to change your email address, just enter EMAIL without any parameters.");
                    return false;
                }
                if(Util.bset(M.getBitmap(),MOB.ATT_AUTOFORWARD))
                {
                    if(!mob.session().confirm("Send email to '"+M.Name()+"' (Y/n)?","Y"))
                        return false;
                }
                else
                {
                    if(!mob.session().confirm("Send email to '"+M.Name()+"', even though their AUTOFORWARD is turned off (y/N)?","N"))
                        return false;
                }
                if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_MAXMAILBOX)>0)
                {
                    int count=CMClass.DBEngine().DBCountJournal(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX),null,M.Name());
                    if(count>=CommonStrings.getIntVar(CommonStrings.SYSTEMI_MAXMAILBOX))
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
                if(subject.length()==0)
                {
                    mob.tell("Aborted");
                    return false;
                }
                message+="\n\r\n\rThis message was sent through the "+CommonStrings.getVar(CommonStrings.SYSTEM_MUDNAME)+" mail server at "+CommonStrings.getVar(CommonStrings.SYSTEM_MUDDOMAIN)+", port"+CommonStrings.getVar(CommonStrings.SYSTEM_MUDPORTS)+".  Please contact the administrators regarding any abuse of this system.\n\r";
                CMClass.DBEngine().DBWriteJournal(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX),
                          mob.Name(),
                          M.Name(),
                          subject,
                          message,-1);
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
        if((CommonStrings.getVar(CommonStrings.SYSTEM_EMAILREQ).toUpperCase().startsWith("PASS"))
        &&(commands!=null)
        &&(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX).length()>0))
            mob.session().println("\n\r** Changing your email address will cause you to be logged off, and a new password to be generated and emailed to the new address. **\n\r");
		String newEmail=mob.session().prompt("New E-mail Address:");
		if(newEmail==null) return false;
		newEmail=newEmail.trim();
		if(!CommonStrings.getVar(CommonStrings.SYSTEM_EMAILREQ).toUpperCase().startsWith("OPTION"))
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
        CMClass.DBEngine().DBUpdateEmail(mob);
        if((commands!=null)
        &&(CommonStrings.getVar(CommonStrings.SYSTEM_EMAILREQ).toUpperCase().startsWith("PASS"))
        &&(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX).length()>0))
        {
            String password="";
            for(int i=0;i<6;i++)
                password+=(char)('a'+Dice.roll(1,26,-1));
            pstats.setPassword(password);
            CMClass.DBEngine().DBUpdatePassword(mob);
            CMClass.DBEngine().DBWriteJournal(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX),
                      mob.Name(),
                      mob.Name(),
                      "Password for "+mob.Name(),
                      "Your new password for "+mob.Name()+" is: "+pstats.password()+"\n\rYou can login by pointing your mud client at "+CommonStrings.getVar(CommonStrings.SYSTEM_MUDDOMAIN)+" port(s):"+CommonStrings.getVar(CommonStrings.SYSTEM_MUDPORTS)+".\n\rYou may use the PASSWORD command to change it once you are online.",-1);
            mob.tell("You will receive an email with your new password shortly.  Goodbye.");
            if(mob.session()!=null)
            {
                try{Thread.sleep(1000);}catch(Exception e){}
                mob.session().setKillFlag(true);
            }
        }
		return true;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
