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
public class CommandJournal extends StdCommand
{
    public CommandJournal(){}

    public static String[] access=null;
    public String[] getAccessWords()
    {
        if(access!=null) return access;
        access=CMLib.journals().getCommandJournalNames();
        return access;
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
            mob.tell(getScr("CommandJournal","featdis"));
        else
        {
            Vector journal=CMLib.database().DBReadJournal(journalID);
            int size=0;
            if(journal!=null) size=journal.size();
            if(size<=0)
                mob.tell(getScr("CommandJournal","nolisted",journalWord));
            else
            {
                journalItem.setName(journalID);                     
                if(count>size)
                    mob.tell(getScr("CommandJournal","maxcount",journalWord,""+size));
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
    
    public boolean execute(MOB mob, Vector commands)
        throws java.io.IOException
    {
        if((commands==null)||(commands.size()==1))
        {
            mob.tell("!!!!!");
            return false;
        }
        String journalWord=null;
        int journalNum=-1;
        if(journalWord==null)
        for(int i=0;i<CMLib.journals().getNumCommandJournals();i++)
            if(CMLib.journals().getCommandJournalName(i).equals(((String)commands.firstElement()).toUpperCase().trim()))
            {
                journalNum=i;
                journalWord=CMLib.journals().getCommandJournalName(i).toUpperCase().trim();
                break;
            }
        if(journalWord==null)
        for(int i=0;i<CMLib.journals().getNumCommandJournals();i++)
            if(CMLib.journals().getCommandJournalName(i).startsWith(((String)commands.firstElement()).toUpperCase().trim()))
            {
                journalNum=i;
                journalWord=CMLib.journals().getCommandJournalName(i).toUpperCase().trim();
                break;
            }
        if(journalWord==null)
        {
            mob.tell("!!!!!");
            return false;
        }
        if((journalNum>=0)&&(!CMLib.masking().maskCheck(CMLib.channels().getChannelMask(journalNum),mob)))
        {
            mob.tell(getScr("CommandJournal","notava"));
            return false;
        }
        if(CMParms.combine(commands,1).length()>0)
        {
            if(!review(mob,"SYSTEM_"+journalWord+"S",journalWord.toLowerCase()+"s",commands,journalWord))
            {
                String prePend="";
                if((journalNum>=0)&&(CMLib.journals().getCommandJournalFlags(journalNum).containsKey("ADDROOM")))
                    prePend="(^<LSTROOMID^>"+CMLib.map().getExtendedRoomID(mob.location())+"^</LSTROOMID^>) ";
                CMLib.database().DBWriteJournal("SYSTEM_"+journalWord+"S",mob.Name(),"ALL",
                        journalWord+": "+CMStrings.padRight(CMParms.combine(commands,1),15),
                        prePend+CMParms.combine(commands,1),
                        -1);
                mob.tell(getScr("CommandJournal","thankyou",journalWord.toLowerCase()));
                if((journalNum>=0)&&(CMLib.journals().getCommandJournalFlags(journalNum).get("CHANNEL=")!=null))
                    CMLib.commands().postChannel(((String)CMLib.journals().getCommandJournalFlags(journalNum).get("CHANNEL=")).toUpperCase().trim(),"",getScr("CommandJournal","customline",mob.Name(),journalWord,CMParms.combine(commands,1)),true);
            }
        }
        else
            mob.tell(getScr("CommandJournal","whats",journalWord.toLowerCase()));
        return false;
    }
    public int actionsCost(){return 0;}
    public boolean canBeOrdered(){return false;}

    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
