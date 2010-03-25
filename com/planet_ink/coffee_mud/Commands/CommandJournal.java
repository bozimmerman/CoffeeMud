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
public class CommandJournal extends StdCommand
{
    public CommandJournal(){}

    public static String[] access=null;
    public String[] getAccessWords()
    {
        if(access!=null) return access;
        synchronized(this)
        {
            if(access!=null) return access;
            
	        access=new String[CMLib.journals().getNumCommandJournals()];
	        int x=0;
	        for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
	        {
	        	JournalsLibrary.CommandJournal CMJ=e.nextElement();
	        	access[x]=CMJ.NAME();
	        	x++;
	        }
        }
        return access;
    }

    public boolean transfer(MOB mob,
                            String journalID,
                            String journalWord,
                            Vector commands,
                            String security)
    {
        String first=(String)commands.elementAt(1);
        String second=(commands.size()>2)?(String)commands.elementAt(2):"";
        String rest=(commands.size()>3)?CMParms.combine(commands,3):"";
        if(!("TRANSFER".startsWith(first.toUpperCase().trim())))
           return false;
        if((!CMSecurity.isAllowed(mob,mob.location(),security))
        &&(!CMSecurity.isAllowed(mob,mob.location(),"KILL"+security+"S")))
        {
            mob.tell("Transfer not allowed.");
            return true;
        }
        if((second.length()>0)&&(!CMath.isNumber(second)))
        {
            mob.tell(second+" is not a number");
            return true;
        }
        int count=CMath.s_int(second);
        Vector journal=CMLib.database().DBReadJournalMsgs(journalID);
        int size=0;
        if(journal!=null) size=journal.size();
        if(size<=0)
        {
            mob.tell("There are no "+journalWord+" listed at this time.");
            return true;
        }
        if(count>size)
        {
            mob.tell("Maximum count of "+journalWord+" is "+size+".");
            return true;
        }
        String realName=null;
        for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
        {
        	JournalsLibrary.CommandJournal CMJ=e.nextElement();
            if(rest.equalsIgnoreCase(CMJ.NAME())
            ||rest.equalsIgnoreCase(CMJ.NAME()+"s"))
            {
                realName=CMJ.JOURNAL_NAME();
                break;
            }
        }
        if(realName==null)
            realName=CMLib.database().DBGetRealJournalName(rest);
        if(realName==null)
            realName=CMLib.database().DBGetRealJournalName(rest.toUpperCase());
        if(realName==null)
        {
            mob.tell(rest+" is not a journal");
            return true;
        }
        Vector journal2=CMLib.database().DBReadJournalMsgs(journalID);
        JournalsLibrary.JournalEntry entry2=(JournalsLibrary.JournalEntry)journal2.elementAt(count-1);
        String from2=entry2.from;
        String to=(String)entry2.to;
        String subject=(String)entry2.subj;
        String message=(String)entry2.msg;
        CMLib.database().DBDeleteJournal(journalID,entry2.key);
        CMLib.database().DBWriteJournal(realName,
                                          from2,
                                          to,
                                          subject,
                                          message);
        mob.tell("Message transferred.");
        return true;
    }
    
    public boolean review(MOB mob,
                          String journalID, 
                          String journalWord,
                          Vector commands,
                          String security)
    {
        String first=(String)commands.elementAt(1);
        String second=(commands.size()>2)?CMParms.combine(commands,2):"";
        if(!("REVIEW".startsWith(first.toUpperCase().trim())))
           return false;
        if((!CMSecurity.isAllowed(mob,mob.location(),security))
        &&(!CMSecurity.isAllowed(mob,mob.location(),"KILL"+security+"S")))
            return false;
        if((second.length()>0)&&(!CMath.isNumber(second)))
            return false;
        int count=CMath.s_int(second);
            
        Item journalItem=CMClass.getItem("StdJournal");
        if(journalItem==null)
            mob.tell("This feature has been disabled.");
        else
        {
            Vector journal=CMLib.database().DBReadJournalMsgs(journalID);
            int size=0;
            if(journal!=null) size=journal.size();
            if(size<=0)
                mob.tell("There are no "+journalWord+" listed at this time.");
            else
            {
                journalItem.setName(journalID);                     
                if(count>size)
                    mob.tell("Maximum count of "+journalWord+" is "+size+".");
                else
                while(count<=size)
                {
                    CMMsg msg=CMClass.getMsg(mob,journalItem,null,CMMsg.MSG_READ,null,CMMsg.MSG_READ,""+count,CMMsg.MSG_READ,null);
                    msg.setValue(1);
                    journalItem.executeMsg(mob,msg);
                    if(msg.value()==0)
                        break;
                    else
                    if(msg.value()<0)
                        size--;
                    else
                        count++;
                }
            }
        }
        return true;
    }
    
    public boolean execute(MOB mob, Vector commands, int metaFlags)
        throws java.io.IOException
    {
        if((commands==null)||(commands.size()==1))
        {
            mob.tell("!!!!!");
            return false;
        }
        JournalsLibrary.CommandJournal journal=null;
        for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
        {
        	JournalsLibrary.CommandJournal CMJ=e.nextElement();
            if(CMJ.NAME().equals(((String)commands.firstElement()).toUpperCase().trim()))
            {
            	journal=CMJ;
                break;
            }
        }
        if(journal==null)
        for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
        {
        	JournalsLibrary.CommandJournal CMJ=e.nextElement();
            if(CMJ.NAME().startsWith(((String)commands.firstElement()).toUpperCase().trim()))
            {
            	journal=CMJ;
                break;
            }
        }
        if(journal==null)
        {
            mob.tell("!!!!!");
            return false;
        }
        if((journal.mask().length()>0)
        &&(!CMLib.masking().maskCheck(journal.mask(),mob,true)))
        {
            mob.tell("This command is not available to you.");
            return false;
        }
        if((!review(mob,journal.JOURNAL_NAME(),journal.NAME().toLowerCase()+"s",commands,journal.NAME()))
        &&(!transfer(mob,journal.JOURNAL_NAME(),journal.NAME().toLowerCase()+"s",commands,journal.NAME())))
        {
	        String msgString=CMParms.combine(commands,1);
	        if((mob.session()!=null)&&(!mob.session().killFlag()))
	        	msgString=CMLib.journals().getScriptValue(mob,journal.NAME(),msgString);
	        if(msgString.trim().length()>0)
	        {
		        if(journal.getFlag(JournalsLibrary.CommandJournalFlags.CONFIRM)!=null)
		        {
		        	if(!mob.session().confirm("\n\r^HSubmit this "+journal.NAME().toLowerCase()+": '^N"+msgString+"^H' (Y/n)?^.^N","Y"))
			            return false;
		        }
	            String prePend="";
	            if(journal.getFlag(JournalsLibrary.CommandJournalFlags.ADDROOM)!=null)
	                prePend="(^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(mob.location())+"^</LSTROOMID^>) ";
	            CMLib.database().DBWriteJournal(journal.JOURNAL_NAME(),mob.Name(),"ALL",
	            		CMStrings.padRight("^.^N"+msgString+"^.^N",20),
	                    prePend+msgString);
	            mob.tell("Your "+journal.NAME().toLowerCase()+" message has been sent.  Thank you.");
	            if(journal.getFlag(JournalsLibrary.CommandJournalFlags.CHANNEL)!=null)
	                CMLib.commands().postChannel(journal.getFlag(JournalsLibrary.CommandJournalFlags.CHANNEL).toUpperCase().trim(),"",mob.Name()+" posted to "+journal.NAME()+": "+CMParms.combine(commands,1),true);
	        }
	        else
	        {
	            mob.tell("What's the "+journal.NAME().toLowerCase()+"? Be Specific!");
	            return false;
	        }
	        
        }
    	return true;
    }
    
    public boolean canBeOrdered(){return false;}

    
}
