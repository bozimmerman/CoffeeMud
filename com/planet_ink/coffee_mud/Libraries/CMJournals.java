package com.planet_ink.coffee_mud.Libraries;
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
public class CMJournals extends StdLibrary implements JournalsLibrary
{
    public String ID(){return "CMJournals";}
    public final int QUEUE_SIZE=100;
    protected Hashtable<String,CommandJournal> journals=new Hashtable<String,CommandJournal>();
    public final Vector emptyVector=new Vector(1);
    
    private ThreadEngine.SupportThread thread=null;
    public ThreadEngine.SupportThread getSupportThread() { return thread;}
    
    public int loadCommandJournals(String list)
    {
        clearJournals();
        while(list.length()>0)
        {
            int x=list.indexOf(",");

            String item=null;
            if(x<0)
            {
                item=list.trim();
                list="";
            }
            else
            {
                item=list.substring(0,x).trim();
                list=list.substring(x+1);
            }
            x=item.indexOf(" ");
            Hashtable<JournalFlag,String> flags=new Hashtable<JournalFlag,String>();
            String mask="";
            if(x>0)
            {
                mask=item.substring(x+1).trim();
                for(int pf=0;pf<JournalFlag.values().length;pf++)
                {
                	String flag = JournalFlag.values()[pf].toString();
                    int keyx=mask.toUpperCase().indexOf(flag);
                    if(keyx>=0)
                    {
                        int keyy=mask.indexOf(" ",keyx+1);
                        if(keyy<0) keyy=mask.length();
                        if((keyx==0)||(Character.isWhitespace(mask.charAt(keyx-1))))
                        {
                            String parm=mask.substring(keyx+flag.length(),keyy).trim();
                            if((parm.length()==0)||(parm.startsWith("=")))
                            {
                            	if(parm.startsWith("=")) parm=parm.substring(1);
                                flags.put(JournalFlag.values()[pf],parm);
                                mask=mask.substring(0,keyx).trim()+" "+mask.substring(keyy).trim();
                            }
                        }
                    }
                }
                item=item.substring(0,x);
            }
            journals.put(item.toUpperCase().trim(),new CommandJournal(item.toUpperCase().trim(),mask,flags));
        }
        return journals.size();
    }
    
    public String getScriptValue(MOB mob, String journal, String oldValue) 
    {
    	CommandJournal CMJ=getCommandJournal(journal);
    	if(CMJ==null) return oldValue;
        String scriptFilename=CMJ.getScriptFilename();
        if((scriptFilename==null)||(scriptFilename.trim().length()==0)) return oldValue;
        ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
        S.setSavable(false);
        S.setVarScope("*");
        S.setScript("LOAD="+scriptFilename);
        S.setVar(mob.Name(),"VALUE", oldValue);
        CMMsg msg2=CMClass.getMsg(mob,mob,null,CMMsg.MSG_OK_VISUAL,null,null,"COMMANDJOURNAL_"+CMJ.NAME());
        S.executeMsg(mob, msg2);
        S.dequeResponses();
        S.tick(mob,Tickable.TICKID_MOB);
        String response=S.getVar("*","VALUE");
        if(response!=null) return response;
        return oldValue;
    }
    
    public int getNumCommandJournals() { return journals.size();    }
    
    public Enumeration<CommandJournal> journals(){ return (Enumeration<CommandJournal>)DVector.s_enum(journals,false);}
    
    public CommandJournal getCommandJournal(String named) { return journals.get(named.toUpperCase().trim());}
    
    public void commandJournalSweep()
    {
        thread.status("command journal sweeping");
        try
        {
            for(Enumeration<CommandJournal> e=journals();e.hasMoreElements();)
            {
            	CommandJournal CMJ=e.nextElement();
                String num=CMJ.getFlag(JournalFlag.EXPIRE);
                if((num!=null)&&(CMath.isNumber(num)))
                {
                    thread.status("updating journal "+CMJ.NAME());
                    Vector items=CMLib.database().DBReadJournalMsgs(CMJ.JOURNAL_NAME());
                    if(items!=null)
                    for(int i=items.size()-1;i>=0;i--)
                    {
                    	JournalEntry entry=(JournalEntry)items.elementAt(i);
                        long compdate=CMath.s_long(entry.update);
                        compdate=compdate+Math.round(CMath.mul(TimeManager.MILI_DAY,CMath.s_double(num)));
                        if(System.currentTimeMillis()>compdate)
                        {
                            String from=entry.from;
                            String message=entry.msg;
                            Log.sysOut(Thread.currentThread().getName(),"Expired "+CMJ.NAME()+" from "+from+": "+message);
                            CMLib.database().DBDeleteJournal(CMJ.JOURNAL_NAME(),i);
                        }
                    }
                    thread.status("command journal sweeping");
                }
            }
        }catch(NoSuchElementException nse){}
    }
    
    public boolean activate() {
        if(thread==null)
            thread=new ThreadEngine.SupportThread("THJournals"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
                    MudHost.TIME_SAVETHREAD_SLEEP, this, CMSecurity.isDebugging("SAVETHREAD"));
        if(!thread.started)
            thread.start();
        return true;
    }
    
    private void clearJournals() {
    	journals=new Hashtable<String,CommandJournal>();
    }
    
    public boolean shutdown() {
        clearJournals();
        thread.shutdown();
        return true;
    }
    
    public void run()
    {
        if((!CMSecurity.isDisabled("SAVETHREAD"))
        &&(!CMSecurity.isDisabled("JOURNALTHREAD")))
        {
            commandJournalSweep();
        }
    }
}
