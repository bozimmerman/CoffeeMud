package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.CMSupportThread;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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

import java.io.IOException;
import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
	protected SHashtable<String,CommandJournal> commandJournals=new SHashtable<String,CommandJournal>();
	protected SHashtable<String,ForumJournal> 	forumJournals=new SHashtable<String,ForumJournal>();
	
	private CMSupportThread thread=null;
	public CMSupportThread getSupportThread() { return thread;}
	
	@SuppressWarnings("unchecked")
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
		JournalSummaryStats stats = journalSummaryStats.get(journalName.toUpperCase().trim());
		if(stats == null)
		{
			synchronized(journal.NAME().intern())
			{
				stats = journalSummaryStats.get(journalName.toUpperCase().trim());
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
			int x=list.indexOf(',');

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
			x=item.indexOf(' ');
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
						int keyy=mask.indexOf(' ',keyx+1);
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
			CMSecurity.registerJournal(item.toUpperCase().trim());
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
			priviledged=CMSecurity.isAllowedAnywhere(readerM,CMSecurity.SecFlag.JOURNALS)&&(!ignorePrivileges);
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
			int x=list.indexOf(',');
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
			CMSecurity.registerJournal(item.toUpperCase().trim());
			forumJournals.put(item.toUpperCase().trim(),new ForumJournal(item.trim(),flags));
		}
		return forumJournals.size();
	}
	
	@SuppressWarnings("unchecked")
    public HashSet<String> getArchonJournalNames()
	{
		HashSet<String> H = (HashSet<String>)Resources.getResource("ARCHON_ONLY_JOURNALS");
		if(H == null)
		{
			Item I=null;
			H=new HashSet<String>();
			for(Enumeration<Item> e=CMClass.basicItems();e.hasMoreElements();)
			{
				I=e.nextElement();
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
	
	public Enumeration<CommandJournal> commandJournals(){ return commandJournals.elements();}
	
	public CommandJournal getCommandJournal(String named) { return commandJournals.get(named.toUpperCase().trim());}
	
	public void expirationJournalSweep()
	{
		thread.setStatus("expiration journal sweeping");
		try
		{
			for(Enumeration<CommandJournal> e=commandJournals();e.hasMoreElements();)
			{
				CommandJournal CMJ=e.nextElement();
				String num=CMJ.getFlag(CommandJournalFlags.EXPIRE);
				if((num!=null)&&(CMath.isNumber(num))&&(CMath.s_double(num)>0.0))
				{
					thread.setStatus("updating journal "+CMJ.NAME());
					List<JournalsLibrary.JournalEntry> items=CMLib.database().DBReadJournalMsgs(CMJ.JOURNAL_NAME());
					if(items!=null)
					for(int i=items.size()-1;i>=0;i--)
					{
						JournalEntry entry=items.get(i);
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
					thread.setStatus("command journal sweeping");
				}
			}
		}catch(NoSuchElementException nse){}
		try
		{
			for(Enumeration<ForumJournal> e=forumJournals();e.hasMoreElements();)
			{
				ForumJournal FMJ=e.nextElement();
				String num=FMJ.getFlag(CommandJournalFlags.EXPIRE);
				if((num!=null)&&(CMath.isNumber(num))&&(CMath.s_double(num)>0.0))
				{
					thread.setStatus("updating journal "+FMJ.NAME());
					List<JournalsLibrary.JournalEntry> items=CMLib.database().DBReadJournalMsgs(FMJ.NAME());
					if(items!=null)
					for(int i=items.size()-1;i>=0;i--)
					{
						JournalEntry entry=items.get(i);
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
					thread.setStatus("forum journal sweeping");
				}
			}
		}catch(NoSuchElementException nse){}
	}
	
	public boolean activate() {
		if(thread==null)
			thread=new CMSupportThread("THJournals"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
					MudHost.TIME_SAVETHREAD_SLEEP, this, CMSecurity.isDebugging(CMSecurity.DbgFlag.JOURNALTHREAD), CMSecurity.DisFlag.JOURNALTHREAD);
		if(!thread.isStarted())
			thread.start();
		return true;
	}
	
	private void clearCommandJournals() {
		commandJournals=new SHashtable<String,CommandJournal>();
	}
	
	public int getNumForumJournals() { return forumJournals.size();    }
	
	public Enumeration<ForumJournal> forumJournals(){ return forumJournals.elements();}
	
	public ForumJournal getForumJournal(String named) { return forumJournals.get(named.toUpperCase().trim());}
	
	private void clearForumJournals() {
		forumJournals=new SHashtable<String,ForumJournal>();
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
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.SAVETHREAD))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.JOURNALTHREAD)))
		{
			expirationJournalSweep();
		}
	}

	public MsgMkrResolution makeMessage(final MOB mob, final String messageTitle, final List<String> vbuf, boolean autoAdd) throws IOException
	{
		final Session sess=mob.session();
		if((sess == null )||(sess.isStopped())) 
			return MsgMkrResolution.CANCELFILE;
		final String help=
			"^HCoffeeMud Message Maker Options:^N\n\r"+
			"^XA)^.^Wdd new lines (go into ADD mode)\n\r"+
			"^XD)^.^Welete one or more lines\n\r"+
			"^XL)^.^Wist the entire text file\n\r"+
			"^XI)^.^Wnsert a line\n\r"+
			"^XE)^.^Wdit a line\n\r"+
			"^XR)^.^Weplace text in the file\n\r"+
			"^XS)^.^Wave the file\n\r"+
			"^XQ)^.^Wuit without saving";
		final String addModeMessage="^ZYou are now in Add Text mode.\n\r^ZEnter . on a blank line to exit.^.^N";
		mob.tell("^HCoffeeMud Message Maker^N");
		boolean menuMode=!autoAdd;
		if(autoAdd) sess.println(addModeMessage);
		while((mob.session()!=null)&&(!sess.isStopped()))
		{
			sess.setAfkFlag(false);
			if(!menuMode)
			{
				String line=sess.prompt("^X"+CMStrings.padRight(""+vbuf.size(),3)+")^.^N ","");
				if(line.trim().equals("."))
					menuMode=true;
				else
					vbuf.add(line);
			}
			else
			{
				LinkedList<String> paramsOut=new LinkedList<String>();
				String option=sess.choose("^HMenu ^N(?/A/D/L/I/E/R/S/Q)^H: ^N","ADLIERSQ?","?",-1,paramsOut);
				String paramAll=(paramsOut.size()>0)?CMParms.combine(paramsOut,0):null;
				String param1=(paramsOut.size()>0)?paramsOut.getFirst():null;
				String param2=(paramsOut.size()>1)?CMParms.combine(paramsOut,1):null;
				switch(option.charAt(0))
				{
				case 'S':
					if(((paramAll!=null)&&(paramAll.equalsIgnoreCase("Y")))
					||(sess.confirm("Save and exit, are you sure (N/y)? ","N")))
					{
						return MsgMkrResolution.SAVEFILE;
					}
					break;
				case 'Q':
					if(((paramAll!=null)&&(paramAll.equalsIgnoreCase("Y")))
					||(sess.confirm("Quit without saving (N/y)? ","N")))
						return MsgMkrResolution.CANCELFILE;
					break;
				case 'R':
				{
					if(vbuf.size()==0)
						mob.tell("The file is empty!");
					else
					{
						String line=param1;
						if(line==null)
							line=sess.prompt("Text to search for (case sensitive): ","");
						if(line.length()>0)
						{
							String str=param2;
							if(str==null)
								str=sess.prompt("Text to replace it with: ","");
							for(int i=0;i<vbuf.size();i++)
								vbuf.set(i,CMStrings.replaceAll(vbuf.get(i),line,str));
						}
						else
							mob.tell("(aborted)");
					}
					break;
				}
				case 'E':
				{
					if(vbuf.size()==0)
						mob.tell("The file is empty!");
					else
					{
						String line=param1;
						if(line==null)
							line=sess.prompt("Line to edit (0-"+(vbuf.size()-1)+"): ","");
						if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
						{
							int ln=CMath.s_int(line);
							mob.tell("Current: \n\r"+CMStrings.padRight(""+ln,3)+") "+vbuf.get(ln));
							String str=param2;
							if(str==null)
								str=sess.prompt("Rewrite: \n\r");
							if(str.length()==0)
								mob.tell("(no change)");
							else
								vbuf.set(ln,str);
						}
						else
							mob.tell("'"+line+"' is not a valid line number.");
					}
					break;
				}
				case 'D':
				{
					if(vbuf.size()==0)
						mob.tell("The file is empty!");
					else
					{
						String line=paramAll;
						if(line==null)
							line=sess.prompt("Line to delete (0-"+(vbuf.size()-1)+"): ","");
						if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
						{
							int ln=CMath.s_int(line);
							vbuf.remove(ln);
							mob.tell("Line "+ln+" deleted.");
						}
						else
							mob.tell("'"+line+"' is not a valid line number.");
					}
					break;
				}
				case '?': mob.tell(help); break;
				case 'A': mob.tell(addModeMessage); 
						  menuMode=false;
						  break;
				case 'L':
				{
					StringBuffer list=new StringBuffer(messageTitle+"\n\r");
					for(int v=0;v<vbuf.size();v++)
						list.append(CMLib.coffeeFilter().colorOnlyFilter("^X"+CMStrings.padRight(""+v,3)+")^.^N ",sess)+vbuf.get(v)+"\n\r");
					sess.rawPrint(list.toString());
					break;
				}
				case 'I':
				{
					if(vbuf.size()==0)
						mob.tell("The file is empty!");
					else
					{
						String line=param1;
						if(line==null)
							line=sess.prompt("Line to insert before (0-"+(vbuf.size()-1)+"): ","");
						if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
						{
							int ln=CMath.s_int(line);
							String str=param2;
							if(str==null)
								str=sess.prompt("Enter text to insert here.\n\r: ");
							vbuf.add(ln,str);
						}
						else
							mob.tell("'"+line+"' is not a valid line number.");
					}
					break;
				}
				}
			}
				
		}
		return MsgMkrResolution.CANCELFILE;
	}
	
	public boolean subscribeToJournal(String journalName, String userName, boolean saveMailingList)
	{
		boolean updateMailingLists=false;
		if((CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0)
		&&(CMLib.players().playerExists(userName)||CMLib.players().accountExists(userName)))
		{
			Map<String, List<String>> lists=Resources.getCachedMultiLists("mailinglists.txt",true);
			List<String> mylist=lists.get(journalName);
			if(mylist==null)
			{
				mylist=new Vector<String>();
				lists.put(journalName,mylist);
			}
			boolean found=false;
			for(int l=0;l<mylist.size();l++)
				if(mylist.get(l).equalsIgnoreCase(userName))
					found=true;
			if(!found)
			{
				mylist.add(userName);
				updateMailingLists=true;
				if(CMProps.getBoolVar(CMProps.SYSTEMB_EMAILFORWARDING))
				{
					String subscribeTitle="Subscribed";
					String subscribedMsg="You are now subscribed to "+journalName+". To unsubscribe, send an email with a subject of unsubscribe.";
					String[] msgs =CMProps.getListVar(CMProps.StrList.SUBSCRIPTION_STRS);
					if((msgs!=null)&&(msgs.length>0))
					{
						if(msgs[0].length()>0)
							subscribeTitle = CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(msgs[0],"<NAME>",journalName),false);
						if((msgs.length>0) && (msgs[1].length()>0))
							subscribedMsg = CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(msgs[1],"<NAME>",journalName),false);
					}
					CMLib.database().DBWriteJournalEmail(CMProps.getVar(CMProps.SYSTEM_MAILBOX),journalName,journalName,userName,subscribeTitle,subscribedMsg);
				}
			}
		}
		if(updateMailingLists && saveMailingList)
		{
			Resources.updateCachedMultiLists("mailinglists.txt");
		}
		return updateMailingLists;
	}
	
	public boolean unsubscribeFromJournal(String journalName, String userName, boolean saveMailingList)
	{
		boolean updateMailingLists = false;
		if(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()==0)
			return false;
		
		Map<String, List<String>> lists=Resources.getCachedMultiLists("mailinglists.txt",true);
		List<String> mylist=lists.get(journalName);
		if(mylist==null) return false;
		for(int l=mylist.size()-1;l>=0;l--)
			if(mylist.get(l).equalsIgnoreCase(userName))
			{
				mylist.remove(l);
				updateMailingLists=true;
				if(CMProps.getBoolVar(CMProps.SYSTEMB_EMAILFORWARDING))
				{
					String unsubscribeTitle="Un-Subscribed";
					String unsubscribedMsg="You are no longer subscribed to "+journalName+". To subscribe again, send an email with a subject of subscribe.";
					String[] msgs =CMProps.getListVar(CMProps.StrList.SUBSCRIPTION_STRS);
					if((msgs!=null)&&(msgs.length>2))
					{
						if(msgs[2].length()>0)
							unsubscribeTitle = CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(msgs[2],"<NAME>",journalName),false);
						if((msgs.length>3) && (msgs[1].length()>0))
							unsubscribedMsg = CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(msgs[3],"<NAME>",journalName),false);
					}
					CMLib.database().DBWriteJournalEmail(CMProps.getVar(CMProps.SYSTEM_MAILBOX),journalName,journalName,userName,unsubscribeTitle,unsubscribedMsg);
				}
			}
		if(updateMailingLists && saveMailingList)
		{
			Resources.updateCachedMultiLists("mailinglists.txt");
		}
		return updateMailingLists;
	}
}