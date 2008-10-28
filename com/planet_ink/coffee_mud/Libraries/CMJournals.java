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
   Copyright 2000-2008 Bo Zimmerman

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
public class CMJournals extends StdLibrary implements JournalsLibrary
{
    public String ID(){return "CMJournals";}
    public final int QUEUE_SIZE=100;
    
    public int numCommandJournalsLoaded=0;
    public Vector commandJournalNames=new Vector();
    public Vector commandJournalMasks=new Vector();
    public Vector commandJournalFlags=new Vector();
    public final Vector emptyVector=new Vector();
    
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
            numCommandJournalsLoaded++;
            x=item.indexOf(" ");
            Hashtable flags=new Hashtable();
            if(x>0)
            {
                String mask=item.substring(x+1).trim();
                String[] possflags={"CHANNEL=","ADDROOM","EXPIRE=","ADMINECHO"};
                for(int pf=0;pf<possflags.length;pf++)
                {
                    int keyx=mask.toUpperCase().indexOf(possflags[pf]);
                    if(keyx>=0)
                    {
                        int keyy=mask.indexOf(" ",keyx+1);
                        if(keyy<0) keyy=mask.length();
                        if((keyx==0)||(Character.isWhitespace(mask.charAt(keyx-1))))
                        {
                            String parm=mask.substring(keyx+possflags[pf].length(),keyy).trim();
                            if((parm.length()==0)||(possflags[pf].endsWith("=")))
                            {
                                flags.put(possflags[pf],parm);
                                mask=mask.substring(0,keyx).trim()+" "+mask.substring(keyy).trim();
                            }
                        }
                    }
                }
                commandJournalMasks.addElement(mask);
                item=item.substring(0,x);
            }
            else
                commandJournalMasks.addElement("");
            commandJournalFlags.addElement(flags);
            commandJournalNames.addElement(item.toUpperCase().trim());
        }
        return numCommandJournalsLoaded;
    }
    
    public int getNumCommandJournals()
    {
        return commandJournalNames.size();
    }
    
    public String getCommandJournalMask(int i)
    {
        if((i>=0)&&(i<commandJournalMasks.size()))
            return (String)commandJournalMasks.elementAt(i);
        return "";
    }

    public String getCommandJournalName(int i)
    {
        if((i>=0)&&(i<commandJournalNames.size()))
            return (String)commandJournalNames.elementAt(i);
        return "";
    }

    public Hashtable getCommandJournalFlags(int i)
    {
        if((i>=0)&&(i<commandJournalFlags.size()))
            return (Hashtable)commandJournalFlags.elementAt(i);
        return new Hashtable();
    }
    public String[] getCommandJournalNames()
    {
        if(commandJournalNames.size()==0) return null;
        return CMParms.toStringArray(commandJournalNames);
    }
    
    public void commandJournalSweep()
    {
        thread.status("command journal sweeping");
        try
        {
            for(int j=0;j<getNumCommandJournals();j++)
            {
                String num=(String)getCommandJournalFlags(j).get("EXPIRE=");
                if((num!=null)&&(CMath.isNumber(num)))
                {
                    thread.status("updating journal "+getCommandJournalName(j));
                    Vector<JournalEntry> items=CMLib.database().DBReadJournalMsgs("SYSTEM_"+getCommandJournalName(j)+"S");
                    if(items!=null)
                    for(int i=items.size()-1;i>=0;i--)
                    {
                    	JournalEntry entry=items.elementAt(i);
                        long compdate=CMath.s_long(entry.update);
                        compdate=compdate+Math.round(CMath.mul(TimeManager.MILI_DAY,CMath.s_double(num)));
                        if(System.currentTimeMillis()>compdate)
                        {
                            String from=entry.from;
                            String message=entry.msg;
                            Log.sysOut(Thread.currentThread().getName(),"Expired "+getCommandJournalName(j)+" from "+from+": "+message);
                            CMLib.database().DBDeleteJournal("SYSTEM_"+getCommandJournalName(j)+"S",i);
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
        numCommandJournalsLoaded=0;
        commandJournalMasks=new Vector();
        commandJournalFlags=new Vector();
        commandJournalNames=new Vector();
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
