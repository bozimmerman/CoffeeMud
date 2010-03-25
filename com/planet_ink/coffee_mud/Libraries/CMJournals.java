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
    protected Hashtable<String,CommandJournal> commandJournals=new Hashtable<String,CommandJournal>();
    protected Hashtable<String,ForumJournal> forumJournals=new Hashtable<String,ForumJournal>();
    public final Vector emptyVector=new Vector(1);
    
    private ThreadEngine.SupportThread thread=null;
    public ThreadEngine.SupportThread getSupportThread() { return thread;}
    
    protected Hashtable<String,JournalSummaryStats> getSummaryStats()
    {
		Hashtable<String,JournalSummaryStats> journalSummaryStats;
		journalSummaryStats= (Hashtable<String,JournalSummaryStats>)Resources.getResource("FORUM_JOURNAL_STATS");
    	if(journalSummaryStats == null)
    	{
    		synchronized("FORUM_JOURNAL_STATS".intern())
    		{
    			journalSummaryStats= (Hashtable<String,JournalSummaryStats>)Resources.getResource("FORUM_JOURNAL_STATS");
				if(journalSummaryStats==null)
				{
					journalSummaryStats=new Hashtable<String,JournalSummaryStats>();
					Resources.submitResource("FORUM_JOURNAL_STATS", journalSummaryStats);
				}
    		}
    	}
    	return journalSummaryStats;
    }
    
    public JournalSummaryStats getJournalStats(String journalName)
    {
    	ForumJournal journal = getForumJournal(journalName);
    	if(journal == null)
    		return null;
		Hashtable<String,JournalSummaryStats> journalSummaryStats=getSummaryStats();
		JournalSummaryStats stats = (JournalSummaryStats)journalSummaryStats.get(journalName.toUpperCase().trim());
    	if(stats == null)
    	{
    		synchronized(journal.NAME().intern())
    		{
    	    	stats = (JournalSummaryStats)journalSummaryStats.get(journalName.toUpperCase().trim());
    	    	if(stats == null)
    	    	{
	    			stats = new JournalSummaryStats();
	    			stats.name = journal.NAME();
	    			CMLib.database().DBReadJournalSummaryStats(stats);
	    			journalSummaryStats.put(journalName.toUpperCase().trim(), stats);
    	    	}
    		}
    	}
    	return stats;
    }
    
    public void clearJournalSummaryStats(String journalName)
    {
    	ForumJournal journal = getForumJournal(journalName);
    	if(journal == null)
    		return;
		Hashtable<String,JournalSummaryStats> journalSummaryStats=getSummaryStats();
		synchronized(journal.NAME().intern())
		{
			journalSummaryStats.remove(journalName.toUpperCase().trim());
		}
    }
    
    public int loadCommandJournals(String list)
    {
        clearCommandJournals();
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
            Hashtable<CommandJournalFlags,String> flags=new Hashtable<CommandJournalFlags,String>();
            String mask="";
            if(x>0)
            {
                mask=item.substring(x+1).trim();
                for(int pf=0;pf<CommandJournalFlags.values().length;pf++)
                {
                	String flag = CommandJournalFlags.values()[pf].toString();
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
                                flags.put(CommandJournalFlags.values()[pf],parm);
                                mask=mask.substring(0,keyx).trim()+" "+mask.substring(keyy).trim();
                            }
                        }
                    }
                }
                item=item.substring(0,x);
            }
            commandJournals.put(item.toUpperCase().trim(),new CommandJournal(item.toUpperCase().trim(),mask,flags));
        }
        return commandJournals.size();
    }
    
    public boolean canReadMessage(JournalEntry entry, String srchMatch, MOB readerM, boolean ignorePrivileges)
    {
    	if(entry==null)
    		return false;
        String to=entry.to;
        if((srchMatch!=null)
        &&(srchMatch.length()>0)
        &&((to.toLowerCase().indexOf(srchMatch)<0)
        &&(entry.from.toLowerCase().indexOf(srchMatch)<0)
        &&(entry.subj.toLowerCase().indexOf(srchMatch)<0)
        &&(entry.msg.toLowerCase().indexOf(srchMatch)<0)))
        	return false;
        boolean priviledged=false;
        if(readerM!=null)
        	priviledged=CMSecurity.isAllowedAnywhere(readerM,"JOURNALS")&&(!ignorePrivileges);
        if(to.equalsIgnoreCase("all")
        ||((readerM!=null)
            &&(priviledged
                ||to.equalsIgnoreCase(readerM.Name())
                ||(to.toUpperCase().trim().startsWith("MASK=")&&(CMLib.masking().maskCheck(to.trim().substring(5),readerM,true))))))
        	return true;
        return false;
    }
    
    public int loadForumJournals(String list)
    {
        clearForumJournals();
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
            Hashtable<ForumJournalFlags,String> flags=new Hashtable<ForumJournalFlags,String>();
            x=item.indexOf('=');
            if(x > 0)
            {
            	int y=x;
            	while((y>0)&&(!Character.isWhitespace(item.charAt(y))))
            		y--;
            	String rest = item.toUpperCase().substring(y+1).trim();
            	item=item.substring(0,y);
            	Vector<Integer> flagDexes = new Vector<Integer>();
                x=rest.indexOf('=');
                while(x > 0)
                {
                	y=x;
                	while((y>0)&&(!Character.isWhitespace(rest.charAt(y))))
                		y--;
                	if(y>0)
                	{
                		try {
	                		ForumJournalFlags.valueOf(rest.substring(y,x).toUpperCase().trim());
	                		flagDexes.addElement(Integer.valueOf(y));
                		} catch(Exception e){}
                	}
                	x=rest.indexOf('=',x+1);
                }
                flagDexes.addElement(Integer.valueOf(rest.length()));
                int lastStart=0;
                for(Integer flagDex : flagDexes)
                {
                	String piece = rest.substring(lastStart,flagDex.intValue());
                	lastStart=flagDex.intValue();
                	x=piece.indexOf('=');
                	try {
	            		ForumJournalFlags flagVar = ForumJournalFlags.valueOf(piece.substring(0,x).toUpperCase().trim());
	            		String flagVal = piece.substring(x+1);
	                    flags.put(flagVar, flagVal);
                	}
                	catch(Exception e){}
                }
            }
            forumJournals.put(item.toUpperCase().trim(),new ForumJournal(item.trim(),flags));
        }
        return forumJournals.size();
    }
    
	public HashSet<String> getArchonJournalNames()
	{
		HashSet<String> H = (HashSet<String>)Resources.getResource("ARCHON_ONLY_JOURNALS");
		if(H == null)
		{
		    Item I=null;
		    H=new HashSet<String>();
		    for(Enumeration e=CMClass.basicItems();e.hasMoreElements();)
		    {
		        I=(Item)e.nextElement();
		        if((I instanceof ArchonOnly)
		        &&(!I.isGeneric()))
		            H.add(I.Name().toUpperCase().trim());
		    }
		    Resources.submitResource("ARCHON_ONLY_JOURNALS", H);
		}
	    return H;
	}
	
	public boolean isArchonJournalName(String journal)
	{
	    if(getArchonJournalNames().contains(journal.toUpperCase().trim()))
	        return true;
	    return false;
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
    
    public int getNumCommandJournals() { return commandJournals.size();    }
    
    public Enumeration<CommandJournal> commandJournals(){ return (Enumeration<CommandJournal>)DVector.s_enum(commandJournals,false);}
    
    public CommandJournal getCommandJournal(String named) { return commandJournals.get(named.toUpperCase().trim());}
    
    public void expirationJournalSweep()
    {
        thread.status("expiration journal sweeping");
        try
        {
            for(Enumeration<CommandJournal> e=commandJournals();e.hasMoreElements();)
            {
            	CommandJournal CMJ=e.nextElement();
                String num=CMJ.getFlag(CommandJournalFlags.EXPIRE);
                if((num!=null)&&(CMath.isNumber(num)))
                {
                    thread.status("updating journal "+CMJ.NAME());
                    Vector items=CMLib.database().DBReadJournalMsgs(CMJ.JOURNAL_NAME());
                    if(items!=null)
                    for(int i=items.size()-1;i>=0;i--)
                    {
                    	JournalEntry entry=(JournalEntry)items.elementAt(i);
                        long compdate=entry.update;
                        compdate=compdate+Math.round(CMath.mul(TimeManager.MILI_DAY,CMath.s_double(num)));
                        if(System.currentTimeMillis()>compdate)
                        {
                            String from=entry.from;
                            String message=entry.msg;
                            Log.sysOut(Thread.currentThread().getName(),"Expired "+CMJ.NAME()+" from "+from+": "+message);
                            CMLib.database().DBDeleteJournal(CMJ.JOURNAL_NAME(),entry.key);
                        }
                    }
                    thread.status("command journal sweeping");
                }
            }
        }catch(NoSuchElementException nse){}
        try
        {
            for(Enumeration<ForumJournal> e=forumJournals();e.hasMoreElements();)
            {
            	ForumJournal FMJ=e.nextElement();
                String num=FMJ.getFlag(CommandJournalFlags.EXPIRE);
                if((num!=null)&&(CMath.isNumber(num)))
                {
                    thread.status("updating journal "+FMJ.NAME());
                    Vector items=CMLib.database().DBReadJournalMsgs(FMJ.NAME());
                    if(items!=null)
                    for(int i=items.size()-1;i>=0;i--)
                    {
                    	JournalEntry entry=(JournalEntry)items.elementAt(i);
                    	if(!CMath.bset(entry.attributes, JournalEntry.ATTRIBUTE_PROTECTED))
                    	{
	                        long compdate=entry.update;
	                        compdate=compdate+Math.round(CMath.mul(TimeManager.MILI_DAY,CMath.s_double(num)));
	                        if(System.currentTimeMillis()>compdate)
	                        {
	                            String from=entry.from;
	                            String message=entry.msg;
	                            Log.sysOut(Thread.currentThread().getName(),"Expired "+FMJ.NAME()+" from "+from+": "+message);
	                            CMLib.database().DBDeleteJournal(FMJ.NAME(),entry.key);
	                        }
                    	}
                    }
                    thread.status("forum journal sweeping");
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
    
    private void clearCommandJournals() {
    	commandJournals=new Hashtable<String,CommandJournal>();
    }
    
    public int getNumForumJournals() { return forumJournals.size();    }
    
    public Enumeration<ForumJournal> forumJournals(){ return (Enumeration<ForumJournal>)DVector.s_enum(forumJournals,false);}
    
    public ForumJournal getForumJournal(String named) { return forumJournals.get(named.toUpperCase().trim());}
    
    private void clearForumJournals() {
    	forumJournals=new Hashtable<String,ForumJournal>();
    	Resources.removeResource("FORUM_JOURNAL_STATS");
    }
    
    public boolean shutdown() {
    	clearCommandJournals();
    	clearForumJournals();
        thread.shutdown();
        return true;
    }
    
    public void run()
    {
        if((!CMSecurity.isDisabled("SAVETHREAD"))
        &&(!CMSecurity.isDisabled("JOURNALTHREAD")))
        {
            expirationJournalSweep();
        }
    }
}
