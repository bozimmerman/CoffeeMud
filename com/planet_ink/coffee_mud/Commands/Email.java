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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2006 Bo Zimmerman

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

	private String[] access={getScr("Email","cmd1")};
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
        &&(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0))
        {
            String name=CMParms.combine(commands,1);
            if(name.equalsIgnoreCase(getScr("Email","cmdbox")))
            {
                Vector msgs=CMLib.database().DBReadJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX));
                while((mob.session()!=null)&&(!mob.session().killFlag()))
                {
                    Vector mymsgs=new Vector();
                    StringBuffer messages=new StringBuffer("^X"+CMStrings.padCenter(mob.Name()+getScr("Email","ownerbox"),48)+"^?^.\n\r");
                    messages.append("^X### "+CMStrings.padRight(getScr("Email","from"),15)+" "+CMStrings.padRight(getScr("Email","date"),20)+getScr("Email","subject"));
                    for(int num=0;num<msgs.size();num++)
                    {
                        Vector thismsg=(Vector)msgs.elementAt(num);
                        String to=((String)thismsg.elementAt(3));
                        if(to.equalsIgnoreCase(getScr("Email","all"))
                        ||to.equalsIgnoreCase(mob.Name()))
                        {
                            mymsgs.addElement(thismsg);
                            messages.append(CMStrings.padRight(""+mymsgs.size(),4)
                                    +CMStrings.padRight(((String)thismsg.elementAt(1)),16)
                                    +CMStrings.padRight(CMLib.time().date2String(CMath.s_long((String)thismsg.elementAt(2))),21)
                                    +((String)thismsg.elementAt(4))
                                    +"\n\r");
                        }
                    }
                    if(mymsgs.size()==0)
                    {
                        if(CMath.bset(mob.getBitmap(),MOB.ATT_AUTOFORWARD))
                            mob.tell(getScr("Email","nowait"));
                        else
                            mob.tell(getScr("Email","nowait2"));
                        return false;
                    }
                    mob.tell(messages.toString());
                    String s=mob.session().prompt(getScr("Email","msgnum"),"");
                    if((!CMath.isInteger(s))||(mob.session().killFlag()))
                        return false;
                    int num=CMath.s_int(s);
                    if((num<=0)||(num>mymsgs.size()))
                        mob.tell(getScr("Email","nonum"));
                    else
                    while((mob.session()!=null)&&(!mob.session().killFlag()))
                    {
                        Vector thismsg=(Vector)mymsgs.elementAt(num-1);
                        String key=(String)thismsg.elementAt(0);
                        String from=(String)thismsg.elementAt(1);
                        String date=(String)thismsg.elementAt(2);
                        date=CMLib.time().date2String(CMath.s_long(date));
                        String subj=(String)thismsg.elementAt(4);
                        String message=(String)thismsg.elementAt(5);
                        messages=new StringBuffer("");
                        messages.append(getScr("Email","message")+num+"\n\r");
                        messages.append(getScr("Email","msgfrom")+from+"\n\r");
                        messages.append(getScr("Email","msgdate")+date+"\n\r");
                        messages.append(getScr("Email","msgsubj")+subj+"\n\r");
                        messages.append("^X------------------------------------------------^?^.\n\r");
                        messages.append(message+"\n\r\n\r");
                        mob.tell(messages.toString());
                        s=mob.session().choose(getScr("Email","prompt"),getScr("Email","opts"),getScr("Email","optdef"));
                        if(s.equalsIgnoreCase(getScr("Email","optdef")))
                            break;
                        if(s.equalsIgnoreCase(getScr("Email","optreply")))
                        {
                            if((from.length()>0)
                            &&(!from.equals(mob.Name()))
                            &&(!from.equalsIgnoreCase(getScr("Email","cmdbox")))
                            &&(CMLib.map().getLoadPlayer(from)!=null))
                                execute(mob,CMParms.makeVector(getAccessWords()[0],from));
                            else
                                mob.tell(getScr("Email","noreply"));
                        }
                        else
                        if(s.equalsIgnoreCase(getScr("Email","optdel")))
                        {
                            CMLib.database().DBDeleteJournal(key);
                            msgs.remove(thismsg);
                            mob.tell(getScr("Email","deleted"));
                            break;
                        }
                    }
                }
            }
            else
            {
                MOB M=CMLib.map().getLoadPlayer(name);
                if(M==null)
                {
                    mob.tell(getScr("Email","noplayer",name));
                    return false;
                }
                if(!CMath.bset(M.getBitmap(),MOB.ATT_AUTOFORWARD))
                {
                    if(!mob.session().confirm(getScr("Email","sendto",M.Name()),"Y"))
                        return false;
                }
                else
                {
                    if(!mob.session().confirm(getScr("Email","sendanyway",M.Name()),"N"))
                        return false;
                }
                if(CMProps.getIntVar(CMProps.SYSTEMI_MAXMAILBOX)>0)
                {
                    int count=CMLib.database().DBCountJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX),null,M.Name());
                    if(count>=CMProps.getIntVar(CMProps.SYSTEMI_MAXMAILBOX))
                    {
                        mob.tell(M.Name()+getScr("Email","full"));
                        return false;
                    }
                }
                String subject=mob.session().prompt(getScr("Email","emailsubj"),"").trim();
                if(subject.length()==0)
                {
                    mob.tell(getScr("Email","emailaborted"));
                    return false;
                }
                String message=mob.session().prompt(getScr("Email","entermsg"),"").trim();
                if(message.trim().length()==0)
                {
                    mob.tell(getScr("Email","emailaborted"));
                    return false;
                }
                message+=getScr("Email","disclaimer",CMProps.getVar(CMProps.SYSTEM_MUDNAME),CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN),CMProps.getVar(CMProps.SYSTEM_MUDPORTS));
                CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX),
                          mob.Name(),
                          M.Name(),
                          subject,
                          message,-1);
                mob.tell(getScr("Email","emailsent"));
                return true;
            }
        }
		if((pstats.getEmail()==null)||(pstats.getEmail().length()==0))
			mob.session().println(getScr("Email","noaddress"));
		else
		{
			if(commands==null) return true;
			String change=mob.session().prompt(getScr("Email","changeaddr",pstats.getEmail()),"N");
			if(change.toUpperCase().startsWith("N")) return false;
		}
        if((CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("PASS"))
        &&(commands!=null)
        &&(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0))
            mob.session().println(getScr("Email","logoff"));
		String newEmail=mob.session().prompt(getScr("Email","newemail"));
		if(newEmail==null) return false;
		newEmail=newEmail.trim();
		if(!CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("OPTION"))
		{
			if(newEmail.length()<6) return false;
			if(newEmail.indexOf("@")<0) return false;
			String confirmEmail=mob.session().prompt(getScr("Email","confirm",newEmail));
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
            CMLib.database().DBUpdatePassword(mob);
            CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX),
                      mob.Name(),
                      mob.Name(),
                      getScr("Email","password")+mob.Name(),
                      getScr("Email","newpass",mob.Name(),pstats.password(),CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN),CMProps.getVar(CMProps.SYSTEM_MUDPORTS)),-1);
            mob.tell(getScr("Email","waitemail"));
            if(mob.session()!=null)
            {
                try{Thread.sleep(1000);}catch(Exception e){}
                mob.session().setKillFlag(true);
            }
        }
		return true;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
