package com.planet_ink.coffee_mud.Libraries.interfaces;
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
public interface JournalsLibrary extends CMLibrary, Runnable
{
	public HashSet<String> getArchonJournalNames();
	public boolean isArchonJournalName(String journal);
	
    public int loadCommandJournals(String list);
    public Enumeration<CommandJournal> commandJournals();
    public CommandJournal getCommandJournal(String named);
    public int getNumCommandJournals();
    public String getScriptValue(MOB mob, String journal, String oldValue);
    
    public boolean canReadMessage(JournalEntry entry, String srchMatch, MOB readerM, boolean ignorePrivileges);
    public int loadForumJournals(String list);
    public Enumeration<ForumJournal> forumJournals();
    public ForumJournal getForumJournal(String named);
    public int getNumForumJournals();
    
    public JournalSummaryStats getJournalStats(String journalName);
    public void clearJournalSummaryStats(String journalName);
    
	public static final String JOURNAL_BOUNDARY="%0D^w---------------------------------------------^N%0D";

	public static class JournalSummaryStats
	{
		public String name = "";
		public int threads =0;
		public int posts = 0;
		public String imagePath = "";
		public String shortIntro = "";
		public String longIntro = "";
		public String introKey = "";
		public String latestKey = "";
		public List<String> stuckyKeys=null;
	}
	
	public static class JournalEntry implements Comparable<JournalEntry>
	{
		public String key=null;
		public String from;
		public String to;
		public String subj;
		public String msg;
		public long date=0;
		public long update=0;
		public String parent="";
		public long attributes=0;
		public String data="";
		public int cardinal=0;
		public String msgIcon="";
		public int replies=0;
		public int views=0;
		public boolean isLastEntry=false;
		public int compareTo(JournalEntry o) {
			if(date < o.date) return -1;
			if(date > o.date) return 1;
			return 0;
		}
		public StringBuffer derivedBuildMessage=null;
		
		public final static long ATTRIBUTE_STUCKY=2;
		public final static long ATTRIBUTE_PROTECTED=1;
		
	}
	
    public static class CommandJournal
    {
    	private String name="";
    	private String mask="";
    	private Hashtable<CommandJournalFlags,String> flags=new Hashtable<CommandJournalFlags,String>(1);
    	
    	public CommandJournal(String name, String mask, Hashtable<CommandJournalFlags,String> flags)
    	{
    		this.name=name;
    		this.mask=mask;
    		this.flags=flags;
    	}
    	public String NAME(){return name;}
    	public String mask(){return mask;}
    	public String JOURNAL_NAME(){ return "SYSTEM_"+NAME().toUpperCase().trim()+"S";}
    	public String getFlag(CommandJournalFlags flag){return flags.get(flag);} 
    	public String getScriptFilename(){return flags.get(CommandJournalFlags.SCRIPT);}
    }
    
    public static enum CommandJournalFlags {
        CHANNEL,ADDROOM,EXPIRE,ADMINECHO,CONFIRM,SCRIPT;
    };
    
    public static class ForumJournal
    {
    	private String name="";
    	private String readMask="";
    	private String postMask="";
    	private String replyMask="";
    	private String adminMask="";
    	private Hashtable<ForumJournalFlags,String> flags=new Hashtable<ForumJournalFlags,String>(1);
    	
    	public ForumJournal(String name, Hashtable<ForumJournalFlags,String> flags)
    	{
    		this.name=name;
    		String mask;
    		
    		mask=flags.remove(ForumJournalFlags.READ);
    		this.readMask=(mask != null)?mask.trim():"";
    		mask=flags.remove(ForumJournalFlags.POST);
    		this.postMask=(mask != null)?mask.trim():"";
    		mask=flags.remove(ForumJournalFlags.REPLY);
    		this.replyMask=(mask != null)?mask.trim():"";
    		mask=flags.remove(ForumJournalFlags.ADMIN);
    		this.adminMask=(mask != null)?mask.trim():"";
    		this.flags=flags;
    	}
    	public String NAME(){return name;}
    	public String readMask(){return readMask;}
    	public String postMask(){return postMask;}
    	public String replyMask(){return replyMask;}
    	public String adminMask(){return adminMask;}
    	public String getFlag(CommandJournalFlags flag){return flags.get(flag);} 
    	public boolean maskCheck(MOB M, String mask)
    	{
    		if(mask.length()>0)
    		{
    			if(M==null) return false;
    			return CMLib.masking().maskCheck(mask, M, true);
    		}
    		return true;
    	}
    	public boolean authorizationCheck(MOB M, ForumJournalFlags fl)
    	{
    		if(!maskCheck(M,readMask))
    			return false;
    		if(fl==ForumJournalFlags.READ)
    			return true;
    		if(fl==ForumJournalFlags.POST)
    			return maskCheck(M,postMask);
    		else
    		if(fl==ForumJournalFlags.REPLY)
    			return maskCheck(M,replyMask);
    		else
    		if(fl==ForumJournalFlags.ADMIN)
    			return maskCheck(M,adminMask);
    		return false;
    	}
    }
    
    public static enum ForumJournalFlags {
        EXPIRE,READ,POST,REPLY,ADMIN;
    };
    
}
