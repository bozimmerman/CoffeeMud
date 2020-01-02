package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.CommandJournalFlags;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournal;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournalFlags;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;

/*
   Copyright 2005-2020 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "CMJournals";
	}

	public final int QUEUE_SIZE=100;
	protected final SHashtable<String,CommandJournal>	commandJournals		= new SHashtable<String,CommandJournal>();
	protected final Vector<ForumJournal> 				forumJournalsSorted	= new Vector<ForumJournal>();
	protected final SHashtable<String,ForumJournal>	 	forumJournals		= new SHashtable<String,ForumJournal>();
	protected final SHashtable<String,List<ForumJournal>>clanForums			= new SHashtable<String,List<ForumJournal>>();

	protected final static List<ForumJournal> emptyForums = new ReadOnlyVector<ForumJournal>(0);

	@SuppressWarnings("unchecked")
	protected Hashtable<String,JournalMetaData> getSummaryStats()
	{
		Hashtable<String,JournalMetaData> journalSummaryStats;
		journalSummaryStats= (Hashtable<String,JournalMetaData>)Resources.getResource("FORUM_JOURNAL_STATS");
		if(journalSummaryStats == null)
		{
			synchronized("FORUM_JOURNAL_STATS".intern())
			{
				journalSummaryStats= (Hashtable<String,JournalMetaData>)Resources.getResource("FORUM_JOURNAL_STATS");
				if(journalSummaryStats==null)
				{
					journalSummaryStats=new Hashtable<String,JournalMetaData>();
					Resources.submitResource("FORUM_JOURNAL_STATS", journalSummaryStats);
				}
			}
		}
		return journalSummaryStats;
	}

	@Override
	public JournalMetaData getJournalStats(final ForumJournal journal)
	{
		if(journal == null)
			return null;
		final Hashtable<String,JournalMetaData> journalSummaryStats=getSummaryStats();
		JournalMetaData metaData = journalSummaryStats.get(journal.NAME().toUpperCase().trim());
		if(metaData == null)
		{
			synchronized(journal.NAME().intern())
			{
				metaData = journalSummaryStats.get(journal.NAME().toUpperCase().trim());
				if(metaData == null)
				{
					metaData = new JournalMetaData()
					{
						private String			name		= "";
						private int				threads		= 0;
						private int				posts		= 0;
						private String			imagePath	= "";
						private String			shortIntro	= "";
						private String			longIntro	= "";
						private String			introKey	= "";
						private String			latestKey	= "";
						private List<String>	stuckyKeys	= null;

						@Override
						public String name()
						{
							return name;
						}

						@Override
						public JournalMetaData name(final String intro)
						{
							name = intro;
							return this;
						}

						@Override
						public int threads()
						{
							return threads;
						}

						@Override
						public JournalMetaData threads(final int num)
						{
							this.threads = num;
							return this;
						}

						@Override
						public int posts()
						{
							return posts;
						}

						@Override
						public JournalMetaData posts(final int num)
						{
							this.posts = num;
							return this;
						}

						@Override
						public String imagePath()
						{
							return imagePath;
						}

						@Override
						public JournalMetaData imagePath(final String intro)
						{
							imagePath = intro;
							return this;
						}

						@Override
						public String shortIntro()
						{
							return shortIntro;
						}

						@Override
						public JournalMetaData shortIntro(final String intro)
						{
							shortIntro = intro;
							return this;
						}

						@Override
						public String longIntro()
						{
							return longIntro;
						}

						@Override
						public JournalMetaData longIntro(final String intro)
						{
							longIntro = intro;
							return this;
						}

						@Override
						public String introKey()
						{
							return introKey;
						}

						@Override
						public JournalMetaData introKey(final String key)
						{
							introKey = key;
							return this;
						}

						@Override
						public String latestKey()
						{
							return latestKey;
						}

						@Override
						public JournalMetaData latestKey(final String key)
						{
							latestKey = key;
							return this;
						}

						@Override
						public List<String> stuckyKeys()
						{
							return stuckyKeys;
						}

						@Override
						public JournalMetaData stuckyKeys(final List<String> keys)
						{
							stuckyKeys = keys;
							return this;
						}
					};
					CMLib.database().DBReadJournalMetaData(journal.NAME(),metaData);
					journalSummaryStats.put(journal.NAME().toUpperCase().trim(), metaData);
				}
			}
		}
		return metaData;
	}

	@Override
	public void clearJournalSummaryStats(final ForumJournal journal)
	{
		if(journal == null)
			return;
		final Hashtable<String,JournalMetaData> journalSummaryStats=getSummaryStats();
		synchronized(journal.NAME().intern())
		{
			journalSummaryStats.remove(journal.NAME().toUpperCase().trim());
		}
	}

	@Override
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
			final Hashtable<CommandJournalFlags,String> flags=new Hashtable<CommandJournalFlags,String>();
			String mask="";
			if(x>0)
			{
				mask=item.substring(x+1).trim();
				for(int pf=0;pf<CommandJournalFlags.values().length;pf++)
				{
					final String flag = CommandJournalFlags.values()[pf].toString();
					final int keyx=mask.toUpperCase().indexOf(flag);
					if(keyx>=0)
					{
						int keyy=mask.indexOf(' ',keyx+1);
						if(keyy<0)
							keyy=mask.length();
						if((keyx==0)||(Character.isWhitespace(mask.charAt(keyx-1))))
						{
							String parm=mask.substring(keyx+flag.length(),keyy).trim();
							if((parm.length()==0)||(parm.startsWith("=")))
							{
								if(parm.startsWith("="))
									parm=parm.substring(1);
								flags.put(CommandJournalFlags.values()[pf],parm);
								mask=mask.substring(0,keyx).trim()+" "+mask.substring(keyy).trim();
							}
						}
					}
				}
				item=item.substring(0,x);
			}
			final String name=item.toUpperCase().trim();
			CMSecurity.registerJournal(name);
			final String flagVal = flags.get(CommandJournalFlags.ASSIGN);
			if(flagVal == null)
				flags.put(CommandJournalFlags.ASSIGN, "ALL");
			else
			{
				final List<String> flagValL=CMParms.parseAny(flagVal.toUpperCase().trim(), ':', true);
				if(!flagValL.contains("ALL"))
					flagValL.add("ALL");
				final StringBuilder newFlags=new StringBuilder("");
				for(final String flag : flagValL)
					newFlags.append(flag).append(':');
				flags.put(CommandJournalFlags.ASSIGN, newFlags.toString());
			}
			final String journalAdminMask = mask;
			commandJournals.put(name,new CommandJournal()
			{
				@Override
				public String NAME()
				{
					return name;
				}

				@Override
				public String mask()
				{
					return journalAdminMask;
				}

				@Override
				public String JOURNAL_NAME()
				{
					return "SYSTEM_" + NAME().toUpperCase().trim() + "S";
				}

				@Override
				public String getFlag(final CommandJournalFlags flag)
				{
					return flags.get(flag);
				}

				@Override
				public String getScriptFilename()
				{
					return flags.get(CommandJournalFlags.SCRIPT);
				}
			});
		}
		return commandJournals.size();
	}

	@Override
	public boolean canReadMessage(final JournalEntry entry, final String srchMatch, final MOB readerM, final boolean ignorePrivileges)
	{
		if(entry==null)
			return false;
		final String to=entry.to();
		if((srchMatch!=null)
		&&(srchMatch.length()>0)
		&&((to.toLowerCase().indexOf(srchMatch)<0)
		&&(entry.from().toLowerCase().indexOf(srchMatch)<0)
		&&(entry.subj().toLowerCase().indexOf(srchMatch)<0)
		&&(entry.msg().toLowerCase().indexOf(srchMatch)<0)))
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

	@Override
	public int loadForumJournals(final String list)
	{
		clearForumJournals();
		final List<ForumJournal> journals = parseForumJournals(list);
		final List<String> catList=new ArrayList<String>();
		catList.add("");
		for(final ForumJournal CJ : journals)
		{
			if(!catList.contains(CJ.category()))
				catList.add(CJ.category());
		}
		Collections.sort(journals, new Comparator<ForumJournal>()
		{
			@Override
			public int compare(final ForumJournal j1, final ForumJournal j2)
			{
				if(j1==null)
					return (j2==null)?0:-1;
				else
				if(j2==null)
					return 1;
				final int idx1;
				final int idx2;
				if(j1.category().equalsIgnoreCase(j2.category()))
				{
					idx1=journals.indexOf(j1);
					idx2=journals.indexOf(j2);
				}
				else
				{
					idx1=catList.indexOf(j1.category());
					idx2=catList.indexOf(j2.category());
				}
				return (idx1==idx2)?0:((idx1<idx2)?-1:1);
			}
		});
		for(final ForumJournal F : journals)
		{
			forumJournals.put(F.NAME().toUpperCase().trim(), F);
			forumJournalsSorted.add(F);
			CMSecurity.registerJournal(F.NAME().toUpperCase().trim());
		}
		return forumJournals.size();
	}

	@Override
	public List<ForumJournal> getClanForums(final Clan clan)
	{
		if(clan == null)
			return null;
		return this.clanForums.get(clan.clanID());
	}

	@Override
	public void registerClanForum(final Clan clan, final String allClanForumDefs)
	{
		if(clan==null)
			return;
		this.clanForums.remove(clan.clanID());
		if(allClanForumDefs==null)
			return;
		final List<String> set=CMParms.parseCommas(allClanForumDefs,true);
		final StringBuilder myForumList=new StringBuilder("");
		for(String s : set)
		{
			s=s.trim();
			if(s.startsWith("["))
			{
				final int x=s.indexOf(']');
				final String cat=s.substring(1,x).trim();
				if(clan.getGovernment().getCategory().equalsIgnoreCase(cat))
				{
					s=s.substring(x+1).trim();
					s=CMStrings.replaceAll(s, "<CLANTYPE>", clan.getGovernmentName());
					s=CMStrings.replaceAll(s, "<CLANNAME>", clan.getName());
					s=CMStrings.replaceAll(s, ",", ".");
					if(myForumList.length()>0)
						myForumList.append(',');
					myForumList.append(s);
				}
			}
		}
		final List<ForumJournal> journals = parseForumJournals(myForumList.toString());
		if((journals!=null)&&(journals.size()>0))
			this.clanForums.put(clan.clanID(), journals);
	}

	public List<ForumJournal> parseForumJournals(String list)
	{
		final List<ForumJournal> journals = new Vector<ForumJournal>(1);
		while(list.length()>0)
		{
			int x=list.indexOf(',');
			String item;
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
			final Hashtable<ForumJournalFlags,String> flags=new Hashtable<ForumJournalFlags,String>();
			x=item.indexOf('=');
			if(x > 0)
			{
				int y=x;
				while((y>0)&&(!Character.isWhitespace(item.charAt(y))))
					y--;
				final String rest = item.substring(y+1).trim();
				item=item.substring(0,y);
				final Vector<Integer> flagDexes = new Vector<Integer>();
				x=rest.indexOf('=');
				while(x > 0)
				{
					y=x;
					while((y>0)&&(!Character.isWhitespace(rest.charAt(y))))
						y--;
					if(y>0)
					{
						try
						{
							ForumJournalFlags.valueOf(rest.substring(y,x).toUpperCase().trim());
							flagDexes.addElement(Integer.valueOf(y));
						}
						catch(final Exception e)
						{
						}
					}
					x=rest.indexOf('=',x+1);
				}
				flagDexes.addElement(Integer.valueOf(rest.length()));
				int lastStart=0;
				for(final Integer flagDex : flagDexes)
				{
					final String piece = rest.substring(lastStart,flagDex.intValue());
					lastStart=flagDex.intValue();
					x=piece.indexOf('=');
					try
					{
						final ForumJournalFlags flagVar = ForumJournalFlags.valueOf(piece.substring(0,x).toUpperCase().trim());
						final String flagVal = piece.substring(x+1);
						if(flagVar==ForumJournalFlags.CATEGORY)
							flags.put(flagVar, flagVal);
						else
							flags.put(flagVar, flagVal.toUpperCase());
					}
					catch(final Exception e)
					{
					}
				}
			}
			final String forumName = item.trim();
			journals.add(new ForumJournal()
			{
				final String name = forumName;
				final Map<ForumJournalFlags,String> flagMap = new XHashtable<ForumJournalFlags,String>(flags);
				final String readMask=flagMap.containsKey(ForumJournalFlags.READ)?flagMap.get(ForumJournalFlags.READ).trim():"";
				final String postMask=flagMap.containsKey(ForumJournalFlags.POST)?flagMap.get(ForumJournalFlags.POST).trim():"";
				final String replyMask=flagMap.containsKey(ForumJournalFlags.REPLY)?flagMap.get(ForumJournalFlags.REPLY).trim():"";
				final String adminMask=flagMap.containsKey(ForumJournalFlags.ADMIN)?flagMap.get(ForumJournalFlags.ADMIN).trim():"";
				final String category =flagMap.containsKey(ForumJournalFlags.CATEGORY)?flagMap.get(ForumJournalFlags.CATEGORY).trim():"";
				
				@Override
				public String NAME()
				{
					return name;
				}

				@Override
				public String readMask()
				{
					return readMask;
				}

				@Override
				public String postMask()
				{
					return postMask;
				}

				@Override
				public String replyMask()
				{
					return replyMask;
				}

				@Override
				public String adminMask()
				{
					return adminMask;
				}

				@Override
				public String getFlag(final ForumJournalFlags flag)
				{
					return flagMap.get(flag);
				}

				@Override
				public boolean maskCheck(final MOB M, final String mask)
				{
					if(mask.length()>0)
					{
						if(M==null)
							return false;
						return CMLib.masking().maskCheck(mask, M, true);
					}
					return true;
				}

				@Override
				public boolean authorizationCheck(final MOB M, final ForumJournalFlags fl)
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

				@Override
				public String category()
				{
					return category;
				}
			});
		}
		return journals;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getArchonJournalNames()
	{
		HashSet<String> H = (HashSet<String>)Resources.getResource("ARCHON_ONLY_JOURNALS");
		if(H == null)
		{
			Item I=null;
			H=new HashSet<String>();
			for(final Enumeration<Item> e=CMClass.basicItems();e.hasMoreElements();)
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

	@Override
	public boolean isArchonJournalName(final String journal)
	{
		if(getArchonJournalNames().contains(journal.toUpperCase().trim()))
			return true;
		return false;
	}

	@Override
	public String getScriptValue(final MOB mob, final String journal, final String oldValue)
	{
		final CommandJournal CMJ=getCommandJournal(journal);
		if(CMJ==null)
			return oldValue;
		final String scriptFilename=CMJ.getScriptFilename();
		if((scriptFilename==null)||(scriptFilename.trim().length()==0))
			return oldValue;
		final ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
		S.setSavable(false);
		S.setVarScope("*");
		S.setScript("LOAD="+scriptFilename);
		S.setVar(mob.Name(),"VALUE", oldValue);
		final CMMsg msg2=CMClass.getMsg(mob,mob,null,CMMsg.MSG_OK_VISUAL,null,null,L("COMMANDJOURNAL_@x1",CMJ.NAME()));
		S.executeMsg(mob, msg2);
		S.dequeResponses();
		S.tick(mob,Tickable.TICKID_MOB);
		final String response=S.getVar("*","VALUE");
		if(response!=null)
			return response;
		return oldValue;
	}

	@Override
	public int getNumCommandJournals()
	{
		return commandJournals.size();
	}

	@Override
	public Enumeration<CommandJournal> commandJournals()
	{
		return commandJournals.elements();
	}

	@Override
	public CommandJournal getCommandJournal(final String named)
	{
		return commandJournals.get(named.toUpperCase().trim());
	}

	public void expirationJournalSweep()
	{
		setThreadStatus(serviceClient,"expiration journal sweeping");
		try
		{
			for(final Enumeration<CommandJournal> e=commandJournals();e.hasMoreElements();)
			{
				final CommandJournal CMJ=e.nextElement();
				final String num=CMJ.getFlag(CommandJournalFlags.EXPIRE);
				if((num!=null)&&(CMath.isNumber(num))&&(CMath.s_double(num)>0.0))
				{
					setThreadStatus(serviceClient,"updating journal "+CMJ.NAME());
					final long expirationDate = System.currentTimeMillis() - Math.round(CMath.mul(TimeManager.MILI_DAY,CMath.s_double(num)));
					final List<JournalEntry> items=CMLib.database().DBReadJournalMsgsOlderThan(CMJ.JOURNAL_NAME(),null,expirationDate);
					for(int i=items.size()-1;i>=0;i--)
					{
						final JournalEntry entry=items.get(i);
						final String from=entry.from();
						final String message=entry.msg();
						Log.sysOut(Thread.currentThread().getName(),"Expired "+CMJ.NAME()+" from "+from+": "+message);
						CMLib.database().DBDeleteJournal(CMJ.JOURNAL_NAME(),entry.key());
					}
					setThreadStatus(serviceClient,"command journal sweeping");
				}
			}
		}
		catch(final NoSuchElementException nse)
		{
		}
		try
		{
			final List<Pair<String,String>> deleteThese = new LinkedList<Pair<String,String>>();
			for(final Enumeration<ForumJournal> e=forumJournals();e.hasMoreElements();)
			{
				final ForumJournal FMJ=e.nextElement();
				final String num=FMJ.getFlag(ForumJournalFlags.EXPIRE);
				if((num!=null)&&(CMath.isNumber(num))&&(CMath.s_double(num)>0.0))
				{
					setThreadStatus(serviceClient,"updating journal "+FMJ.NAME());
					final long expirationDate = System.currentTimeMillis() - Math.round(CMath.mul(TimeManager.MILI_DAY,CMath.s_double(num)));
					final List<JournalEntry> items=CMLib.database().DBReadJournalMsgsOlderThan(FMJ.NAME(),null,expirationDate);
					for(int i=items.size()-1;i>=0;i--)
					{
						final JournalEntry entry=items.get(i);
						if(!CMath.bset(entry.attributes(), JournalEntry.ATTRIBUTE_PROTECTED))
						{
							final String from=entry.from();
							final String message=entry.msg();
							Log.debugOut(Thread.currentThread().getName(),"Expired "+FMJ.NAME()+" from "+from+": "+message);
							deleteThese.add(new Pair<String,String>(FMJ.NAME(),entry.key()));
						}
					}
					setThreadStatus(serviceClient,"forum journal sweeping");
				}
			}
			for(final Pair<String,String> p : deleteThese)
				CMLib.database().DBDeleteJournal(p.first,p.second);
		}
		catch(final NoSuchElementException nse)
		{
		}
	}

	@Override
	public boolean activate()
	{
		if(serviceClient==null)
		{
			name="THJournals"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, MudHost.TIME_SAVETHREAD_SLEEP, 1);
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		tickStatus=Tickable.STATUS_ALIVE;
		try
		{
			if((!CMSecurity.isDisabled(CMSecurity.DisFlag.SAVETHREAD))
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.JOURNALTHREAD)))
			{
				isDebugging=CMSecurity.isDebugging(DbgFlag.JOURNALTHREAD);
				tickStatus=Tickable.STATUS_ALIVE;
				expirationJournalSweep();
				setThreadStatus(serviceClient,"sleeping");
			}
		}
		finally
		{
			tickStatus=Tickable.STATUS_NOT;
		}
		return true;
	}

	private void clearCommandJournals()
	{
		commandJournals.clear();
	}

	@Override
	public int getNumForumJournals()
	{
		return forumJournals.size();
	}

	@Override
	public Enumeration<ForumJournal> forumJournals()
	{
		return forumJournals.elements();
	}

	@Override
	public Enumeration<ForumJournal> forumJournalsSorted()
	{
		return forumJournalsSorted.elements();
	}

	@Override
	public ForumJournal getForumJournal(final String named)
	{
		return forumJournals.get(named.toUpperCase().trim());
	}

	@Override
	public ForumJournal getForumJournal(String named, final Clan clan)
	{
		if(named==null)
			return null;

		named=named.toUpperCase().trim();
		if(forumJournals.containsKey(named))
			return forumJournals.get(named);

		if(clan!=null)
		{
			final List<ForumJournal> clanJournals=this.clanForums.get(clan.clanID());
			if(clanJournals!=null)
			{
				for (final ForumJournal CJ : clanJournals)
				{
					if(CJ.NAME().equalsIgnoreCase(named))
						return CJ;
				}
			}
		}
		return null;
	}

	private void clearForumJournals()
	{
		forumJournals.clear();
		forumJournalsSorted.clear();
		Resources.removeResource("FORUM_JOURNAL_STATS");
	}

	@Override
	public boolean shutdown()
	{
		clearCommandJournals();
		clearForumJournals();
		if(CMLib.threads().isTicking(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}

	private String unsafePrompt(final Session sess, final String prompt, final String defaultMsg) throws IOException
	{
		sess.promptPrint(prompt);
		final String line=sess.blockingIn(-1, false);
		if(line == null)
			return defaultMsg;
		return line;
	}

	protected String getMsgMkrHelp(final Session sess)
	{
		final boolean canExtEdit=((sess!=null)&&(sess.getClientTelnetMode(Session.TELNET_GMCP)));
		final String help=
			L("^HCoffeeMud Message Maker Options:^N\n\r"+
			"^XA)^.^Wdd new lines (go into ADD mode)\n\r"+
			"^XD)^.^Welete one or more lines\n\r"+
			"^XL)^.^Wist the entire text file\n\r"+
			"^XI)^.^Wnsert a line\n\r"+
			"^XE)^.^Wdit a line\n\r"+
			"^XR)^.^Weplace text in the file\n\r"+
			"^XS)^.^Wave the file\n\r"+
			(canExtEdit?"^XW)^.^Write over using GMCP\n\r":"")+
			"^XQ)^.^Wuit without saving");
		return help;
	}

	private enum MsgMkrState
	{
		INPUT,
		MENU,
		SAVECONFIRM,
		QUITCONFIRM,
		SRPROMPT,
		EDITPROMPT,
		DELPROMPT,
		GMCPWAIT,
		INSPROMPT
	}

	@Override
	public void notifyPosting(final String journal, final String from, final String to, final String subject)
	{
		final String notifyName = " P :"+journal.toUpperCase().trim();
		for(final Session S : CMLib.sessions().allIterableAllHosts())
		{
			if(S!=null)
			{
				final MOB M=S.mob();
				if((M!=null)
				&&(M.playerStats()!=null)
				&&(M.playerStats().getSubscriptions().contains(notifyName))
				&&((from==null)
					||(!from.equalsIgnoreCase(M.Name())))
				&&((to==null)
					||(to.equalsIgnoreCase("ALL"))
					||(to.equalsIgnoreCase(M.Name()))))
				{
					M.tell(L("^w@x1 Notification: @x2 just added a new message.^?",journal,from));
				}
			}
		}
	}

	@Override
	public void notifyReplying(final String journal, final String tpAuthor, final String reAuthor, final String subject)
	{
		final String notifyName = " P :"+journal.toUpperCase().trim();
		for(final Session S : CMLib.sessions().allIterableAllHosts())
		{
			if(S!=null)
			{
				final MOB M=S.mob();
				if((M!=null)
				&&(M.playerStats()!=null)
				&&(M.playerStats().getSubscriptions().contains(notifyName))
				&&(M.Name().equalsIgnoreCase(tpAuthor)))
				{
					M.tell(L("^w@x1 Notification: @x2 just replied to your message '@x3'.^?",journal,reAuthor,subject));
				}
			}
		}
	}

	@Override
	public void makeMessageASync(final MOB M, final String messageTitle, final List<String> vbuf, final boolean autoAdd, final MsgMkrCallback back)
	{
		final Session sess=M.session();
		if((sess == null )||(sess.isStopped()))
		{
			back.callBack(M,sess,MsgMkrResolution.CANCELFILE);
			return;
		}
		final String addModeMessage=L("^ZYou are now in Add Text mode.\n\r^ZEnter an empty line to exit.^.^N");
		sess.println(L("^HCoffeeMud Message Maker^N"));
		if(autoAdd)
			sess.println(addModeMessage);
		sess.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
		{
			final MOB mob=M;
			final Session sess=mob.session();
			MsgMkrState state=!autoAdd?MsgMkrState.MENU:MsgMkrState.INPUT;
			final boolean canExtEdit=((mob.session()!=null)&&(mob.session().getClientTelnetMode(Session.TELNET_GMCP)));
			final LinkedList<String> paramsOut=new LinkedList<String>();
			String paramAll=null;
			String param1=null;
			String param2=null;

			@Override
			public void showPrompt()
			{
				this.noTrim=false;
				switch(state)
				{
				case INPUT:
					sess.promptPrint(L("^X"+CMStrings.padRight(""+vbuf.size(),3)+")^.^N "));
					this.noTrim=true;
					break;
				case MENU:
					sess.promptPrint(L("^HMenu ^N(?/A/D/L/I/E/R/S/Q@x1)^H: ^N",(canExtEdit?"/W":"")));
					break;
				case SAVECONFIRM:
					sess.promptPrint(L("Save and exit, are you sure (N/y)? "));
					break;
				case QUITCONFIRM:
					sess.promptPrint(L("Quit without saving (N/y)? "));
					break;
				case SRPROMPT:
					if(param1==null)
						sess.promptPrint(L("Text to search for (case sensitive): "));
					else
					if(param2==null)
						sess.promptPrint(L("Text to replace it with: "));
					break;
				case EDITPROMPT:
					if(param1==null)
						sess.promptPrint(L("Line to edit (0-@x1): ",""+(vbuf.size()-1)));
					else
					if(param2==null)
					{
						final int ln=CMath.s_int(param1.trim());
						final StringBuilder str=new StringBuilder("");
						str.append(L("Current: \n\r@x1) @x2",CMStrings.padRight(""+ln,3),vbuf.get(ln)));
						str.append(L("\n\rRewrite: \n\r"));
						sess.promptPrint(str.toString());
					}
					break;
				case DELPROMPT:
					sess.promptPrint(L("Line to delete (0-@x1): ",""+(vbuf.size()-1)));
					break;
				case GMCPWAIT:
					sess.promptPrint(L("Re-Enter the whole doc using your GMCP editor.\n\rIf the editor has not popped up, just hit enter and QUIT Without Saving immediately.\n\rProceed: "));
					break;
				case INSPROMPT:
					if(param1==null)
						sess.promptPrint(L("Line to insert before (0-@x1): ",""+(vbuf.size()-1)));
					else
					if(param2==null)
						sess.promptPrint(L("Enter text to insert here.\n\r: "));
					break;
				}
			}

			@Override
			public void timedOut()
			{
				back.callBack(mob,sess,MsgMkrResolution.CANCELFILE);
				waiting=false;
			}

			@Override
			public void callBack()
			{
				if((mob.session()==null)||(sess.isStopped()))
				{
					back.callBack(mob,sess,MsgMkrResolution.CANCELFILE);
					return;
				}
				if(this.input==null)
				{
					mob.session().prompt(this);
					return;
				}
				switch(state)
				{
				case INPUT:
				{
					if((this.input.length()==0)||(this.input.equals(".")))
						state=MsgMkrState.MENU;
					else
						vbuf.add(this.input);
					break;
				}
				case SAVECONFIRM:
				{
					if((this.input.length()==0)||(!this.input.toUpperCase().startsWith("Y")))
						state=MsgMkrState.MENU;
					else
					{
						back.callBack(mob,sess,MsgMkrResolution.SAVEFILE);
						return;
					}
					break;
				}
				case QUITCONFIRM:
				{
					if((this.input.length()==0)||(!this.input.toUpperCase().startsWith("Y")))
						state=MsgMkrState.MENU;
					else
					{
						back.callBack(mob,sess,MsgMkrResolution.CANCELFILE);
						return;
					}
					break;
				}
				case SRPROMPT:
				{
					if(param1==null)
					{
						if((this.input==null)||(this.input.trim().length()==0))
						{
							sess.println(L("(aborted)"));
							state=MsgMkrState.MENU;
							break;
						}
						else
							param1=this.input;
					}
					else
					if(param2==null)
					{
						param2=this.input;
					}
					if((param1!=null) && (param2!=null))
					{
						for(int i=0;i<vbuf.size();i++)
							vbuf.set(i,CMStrings.replaceAll(vbuf.get(i),param1,param2));
						state=MsgMkrState.MENU;
					}
					else
						state=MsgMkrState.SRPROMPT;
					break;
				}
				case EDITPROMPT:
				{
					if(param1==null)
					{
						if((this.input==null)||(this.input.trim().length()==0))
						{
							sess.println(L("(aborted)"));
							state=MsgMkrState.MENU;
							break;
						}
						else
						{
							this.input=this.input.trim();
							final int ln=CMath.isInteger(this.input)?CMath.s_int(this.input):-1;
							if((ln<0)||(ln>=vbuf.size()))
							{
								sess.println(L("'@x1' is not a valid line number.",this.input));
								state=MsgMkrState.MENU;
							}
							else
								param1=this.input;
						}
					}
					else
					if(param2==null)
					{
						param2=this.input;
					}
					if((param1!=null) && (param2!=null))
					{
						final int ln=CMath.isInteger(param1)?CMath.s_int(param1):-1;
						if((ln<0)||(ln>=vbuf.size()))
							sess.println(L("'@x1' is not a valid line number.",param1));
						else
							vbuf.set(ln,param2);
						state=MsgMkrState.MENU;
					}
					else
						state=MsgMkrState.EDITPROMPT;
					break;
				}
				case DELPROMPT:
				{
					if(paramAll==null)
					{
						final String line=this.input;
						if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
						{
							final int ln=CMath.s_int(line);
							vbuf.remove(ln);
							sess.println(L("Line @x1 deleted.",""+ln));
						}
						else
							sess.println(L("'@x1' is not a valid line number.",""+line));
						state=MsgMkrState.MENU;
					}
					else
						state=MsgMkrState.DELPROMPT;
					break;
				}
				case GMCPWAIT:
				{
					vbuf.clear();
					String newText=this.input;
					if((newText.length()>0)&&(newText.charAt(newText.length()-1)=='\\'))
						newText = newText.substring(0,newText.length()-1);
					final String[] newDoc=newText.split("\\\\n");
					for(final String s : newDoc)
						vbuf.add(s);
					if(newDoc.length>1)
					{
						sess.println(L("\n\r^HNew text successfully imported.^N"));
					}
					state=MsgMkrState.MENU;
					break;
				}
				case INSPROMPT:
				{
					if(param1==null)
					{
						if((this.input==null)||(this.input.trim().length()==0))
						{
							sess.println(L("(aborted)"));
							state=MsgMkrState.MENU;
							break;
						}
						else
						{
							this.input=this.input.trim();
							final int ln=CMath.isInteger(this.input)?CMath.s_int(this.input):-1;
							if((ln<0)||(ln>=vbuf.size()))
							{
								sess.println(L("'@x1' is not a valid line number.",this.input));
								state=MsgMkrState.MENU;
							}
							else
								param1=this.input;
						}
					}
					else
					if(param2==null)
					{
						param2=this.input;
					}
					if((param1!=null) && (param2!=null))
					{
						final int ln=CMath.isInteger(param1)?CMath.s_int(param1):-1;
						if((ln<0)||(ln>=vbuf.size()))
							sess.println(L("'@x1' is not a valid line number.",param1));
						else
							vbuf.add(ln,param2);
						state=MsgMkrState.MENU;
					}
					else
					if(param1 != null)
					{
						final int ln=CMath.isInteger(param1)?CMath.s_int(param1):-1;
						if((ln<0)||(ln>=vbuf.size()))
							sess.println(L("'@x1' is not a valid line number.",param1));
						else
							vbuf.add(ln,"");
						state=MsgMkrState.EDITPROMPT;
					}
					else
						state=MsgMkrState.EDITPROMPT;
					break;
				}
				case MENU:
				{
					final String options=L("ADLIERSQ?@x1",(canExtEdit?"W":"")).trim();
					this.input=this.input.trim();
					if(this.input.length()==0)
						this.input="?";
					final char cmdChar=this.input.toUpperCase().charAt(0);
					if(options.indexOf(cmdChar)>=0)
					{
						paramsOut.clear();
						if(this.input.length()>1)
							paramsOut.addAll(CMParms.cleanParameterList(this.input.substring(1)));
						paramAll=(paramsOut.size()>0)?CMParms.combine(paramsOut,0):null;
						param1=(paramsOut.size()>0)?paramsOut.getFirst():null;
						param2=(paramsOut.size()>1)?CMParms.combine(paramsOut,1):null;
						switch(cmdChar)
						{
						case 'S':
							if((paramAll!=null)&&(paramAll.equalsIgnoreCase("Y")))
							{
								back.callBack(mob,sess,MsgMkrResolution.SAVEFILE);
								return;
							}
							else
								state=MsgMkrState.SAVECONFIRM;
							break;
						case 'Q':
							if((paramAll!=null)&&(paramAll.equalsIgnoreCase("Y")))
							{
								back.callBack(mob,sess,MsgMkrResolution.CANCELFILE);
								return;
							}
							else
								state=MsgMkrState.QUITCONFIRM;
							break;
						case 'R':
						{
							if(vbuf.size()==0)
								sess.println(L("The file is empty!"));
							else
							{
								if((param1!=null) && (param2!=null))
								{
									if(param1.length()==0)
										sess.println(L("(aborted)"));
									else
									for(int i=0;i<vbuf.size();i++)
										vbuf.set(i,CMStrings.replaceAll(vbuf.get(i),param1,param2));
								}
								else
									state=MsgMkrState.SRPROMPT;
							}
							break;
						}
						case 'E':
						{
							if(vbuf.size()==0)
								sess.println(L("The file is empty!"));
							else
							{
								if((param1!=null) && (param2!=null))
								{
									final int ln=CMath.isInteger(param1)?CMath.s_int(param1):-1;
									if((ln<0)||(ln>=vbuf.size()))
										sess.println(L("'@x1' is not a valid line number.",param1));
									else
										vbuf.set(ln,param2);
								}
								else
									state=MsgMkrState.EDITPROMPT;
							}
							break;
						}
						case 'D':
						{
							if(vbuf.size()==0)
								sess.println(L("The file is empty!"));
							else
							{
								if(paramAll!=null)
								{
									final String line=paramAll;
									if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
									{
										final int ln=CMath.s_int(line);
										vbuf.remove(ln);
										sess.println(L("Line @x1 deleted.",""+ln));
									}
									else
										sess.println(L("'@x1' is not a valid line number.",""+line));
								}
								else
									state=MsgMkrState.DELPROMPT;
							}
							break;
						}
						case '?':
							sess.println(getMsgMkrHelp(sess));
							break;
						case 'A':
							sess.println(addModeMessage);
							state=MsgMkrState.INPUT;
							break;
						case 'W':
						{
							final StringBuilder oldDoc=new StringBuilder();
							for(final String s : vbuf)
								oldDoc.append(s).append("\n");
							sess.sendGMCPEvent("IRE.Composer.Edit", "{\"title\":\""+MiniJSON.toJSONString(messageTitle)+"\",\"text\":\""+MiniJSON.toJSONString(oldDoc.toString())+"\"}");
							state=MsgMkrState.GMCPWAIT;
							break;
						}
						case 'L':
						{
							final StringBuffer list=new StringBuffer(messageTitle+"\n\r");
							for(int v=0;v<vbuf.size();v++)
								list.append(CMLib.coffeeFilter().colorOnlyFilter("^X"+CMStrings.padRight(""+v,3)+")^.^N ",sess)+vbuf.get(v)+"\n\r");
							sess.rawPrint(list.toString());
							break;
						}
						case 'I':
						{
							if(vbuf.size()==0)
								sess.println(L("The file is empty!"));
							else
							{
								if((param1!=null) && (param2!=null))
								{
									final int ln=CMath.isInteger(param1)?CMath.s_int(param1):-1;
									if((ln<0)||(ln>=vbuf.size()))
										sess.println(L("'@x1' is not a valid line number.",param1));
									else
										vbuf.add(ln,param2);
								}
								else
									state=MsgMkrState.INSPROMPT;
							}
							break;
						}
						}
					}
				}
				}
				this.waiting=true;
				mob.session().prompt(this);
				return;
			}
		});
	}

	@Override
	public MsgMkrResolution makeMessage(final MOB mob, final String messageTitle, final List<String> vbuf, final boolean autoAdd) throws IOException
	{
		final Session sess=mob.session();
		if((sess == null )||(sess.isStopped()))
			return MsgMkrResolution.CANCELFILE;

		final String addModeMessage=L("^ZYou are now in Add Text mode.\n\r^ZEnter . on a blank line to exit.^.^N");
		mob.tell(L("^HCoffeeMud Message Maker^N"));
		boolean menuMode=!autoAdd;
		if(autoAdd)
			sess.println(addModeMessage);
		while((mob.session()!=null)&&(!sess.isStopped()))
		{
			sess.setAfkFlag(false);
			if(!menuMode)
			{
				final String line =unsafePrompt(sess,"^X"+CMStrings.padRight(""+vbuf.size(),3)+")^.^N ",".");
				if(line.trim().equals("."))
					menuMode=true;
				else
					vbuf.add(line);
			}
			else
			{
				final boolean canExtEdit=((sess.getClientTelnetMode(Session.TELNET_GMCP)));
				final LinkedList<String> paramsOut=new LinkedList<String>();
				final String option=sess.choose(L("^HMenu ^N(?/A/D/L/I/E/R/S/Q@x1)^H: ^N",(canExtEdit?"/W":"")),L("ADLIERSQ?@x1",(canExtEdit?"W":"")),"?",-1,paramsOut);
				final String paramAll=(paramsOut.size()>0)?CMParms.combine(paramsOut,0):null;
				final String param1=(paramsOut.size()>0)?paramsOut.getFirst():null;
				final String param2=(paramsOut.size()>1)?CMParms.combine(paramsOut,1):null;
				switch(option.charAt(0))
				{
				case 'S':
					if(((paramAll!=null)&&(paramAll.equalsIgnoreCase("Y")))
					||(sess.confirm(L("Save and exit, are you sure (N/y)? "),"N")))
					{
						return MsgMkrResolution.SAVEFILE;
					}
					break;
				case 'Q':
					if(((paramAll!=null)&&(paramAll.equalsIgnoreCase("Y")))
					||(sess.confirm(L("Quit without saving (N/y)? "),"N")))
						return MsgMkrResolution.CANCELFILE;
					break;
				case 'R':
				{
					if(vbuf.size()==0)
						mob.tell(L("The file is empty!"));
					else
					{
						String line=param1;
						if(line==null)
							line=unsafePrompt(sess,L("Text to search for (case sensitive): "),"");
						if(line.length()>0)
						{
							String str=param2;
							if(str==null)
								str=unsafePrompt(sess,L("Text to replace it with: "),"");
							for(int i=0;i<vbuf.size();i++)
								vbuf.set(i,CMStrings.replaceAll(vbuf.get(i),line,str));
						}
						else
							mob.tell(L("(aborted)"));
					}
					break;
				}
				case 'E':
				{
					if(vbuf.size()==0)
						mob.tell(L("The file is empty!"));
					else
					{
						String line=param1;
						if(line==null)
							line=sess.prompt(L("Line to edit (0-@x1): ",""+(vbuf.size()-1)),"");
						if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
						{
							final int ln=CMath.s_int(line);
							mob.tell(L("Current: \n\r@x1) @x2",CMStrings.padRight(""+ln,3),vbuf.get(ln)));
							String str=param2;
							if(str==null)
								str=unsafePrompt(sess,L("Rewrite: \n\r"),"");
							if(str.length()==0)
								mob.tell(L("(no change)"));
							else
								vbuf.set(ln,str);
						}
						else
							mob.tell(L("'@x1' is not a valid line number.",line));
					}
					break;
				}
				case 'D':
				{
					if(vbuf.size()==0)
						mob.tell(L("The file is empty!"));
					else
					{
						String line=paramAll;
						if(line==null)
							line=sess.prompt(L("Line to delete (0-@x1): ",""+(vbuf.size()-1)),"");
						if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
						{
							final int ln=CMath.s_int(line);
							vbuf.remove(ln);
							mob.tell(L("Line @x1 deleted.",""+ln));
						}
						else
							mob.tell(L("'@x1' is not a valid line number.",""+line));
					}
					break;
				}
				case '?':
					mob.tell(getMsgMkrHelp(sess));
					break;
				case 'A':
					mob.tell(addModeMessage);
					menuMode = false;
					break;
				case 'W':
				{
					StringBuilder oldDoc=new StringBuilder();
					for(final String s : vbuf)
						oldDoc.append(s).append("\n");
					vbuf.clear();
					sess.sendGMCPEvent("IRE.Composer.Edit", "{\"title\":\""+MiniJSON.toJSONString(messageTitle)+"\",\"text\":\""+MiniJSON.toJSONString(oldDoc.toString())+"\"}");
					oldDoc=null;
					String newText=unsafePrompt(sess,L("Re-Enter the whole doc using your GMCP editor.\n\rIf the editor has not popped up, just hit enter and QUIT Without Saving immediately.\n\rProceed: "),"");
					if((newText.length()>0)&&(newText.charAt(newText.length()-1)=='\\'))
						newText = newText.substring(0,newText.length()-1);
					final String[] newDoc=newText.split("\\\\n");
					for(final String s : newDoc)
						vbuf.add(s);
					if(newDoc.length>1)
					{
						mob.tell(L("\n\r^HNew text successfully imported.^N"));
					}
					break;
				}
				case 'L':
				{
					final StringBuffer list=new StringBuffer(messageTitle+"\n\r");
					for(int v=0;v<vbuf.size();v++)
						list.append(CMLib.coffeeFilter().colorOnlyFilter("^X"+CMStrings.padRight(""+v,3)+")^.^N ",sess)+vbuf.get(v)+"\n\r");
					sess.rawPrint(list.toString());
					break;
				}
				case 'I':
				{
					if(vbuf.size()==0)
						mob.tell(L("The file is empty!"));
					else
					{
						String line=param1;
						if(line==null)
							line=unsafePrompt(sess,L("Line to insert before (0-@x1): ",""+(vbuf.size()-1)),"");
						if((CMath.isInteger(line))&&(CMath.s_int(line)>=0)&&(CMath.s_int(line)<(vbuf.size())))
						{
							final int ln=CMath.s_int(line);
							String str=param2;
							if(str==null)
								str=unsafePrompt(sess,L("Enter text to insert here.\n\r: "),"");
							vbuf.add(ln,str);
						}
						else
							mob.tell(L("'@x1' is not a valid line number.",""+line));
					}
					break;
				}
				}
			}
		}
		return MsgMkrResolution.CANCELFILE;
	}

	@Override
	public boolean subscribeToJournal(final String journalName, final String userName, final boolean saveMailingList)
	{
		boolean updateMailingLists=false;
		if((CMProps.getVar(CMProps.Str.MAILBOX).length()>0)
		&&(CMLib.players().playerExistsAllHosts(userName)||CMLib.players().accountExistsAllHosts(userName)))
		{
			final Map<String, List<String>> lists=Resources.getCachedMultiLists("mailinglists.txt",true);
			List<String> mylist=lists.get(journalName);
			if(mylist==null)
			{
				mylist=new Vector<String>();
				lists.put(journalName,mylist);
			}
			boolean found=false;
			for(int l=0;l<mylist.size();l++)
			{
				if(mylist.get(l).equalsIgnoreCase(userName))
					found=true;
			}
			if(!found)
			{
				mylist.add(userName);
				updateMailingLists=true;
				if(CMProps.getBoolVar(CMProps.Bool.EMAILFORWARDING))
				{
					String subscribeTitle="Subscribed";
					String subscribedMsg="You are now subscribed to "+journalName+". To unsubscribe, send an email with a subject of unsubscribe.";
					final String[] msgs =CMProps.getListVar(CMProps.StrList.SUBSCRIPTION_STRS);
					if((msgs!=null)&&(msgs.length>0))
					{
						if(msgs[0].length()>0)
							subscribeTitle = CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(msgs[0],"<NAME>",journalName));
						if((msgs.length>0) && (msgs[1].length()>0))
							subscribedMsg = CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(msgs[1],"<NAME>",journalName));
					}
					CMLib.smtp().emailOrJournal(journalName,journalName,userName,subscribeTitle,subscribedMsg);
				}
			}
		}
		if(updateMailingLists && saveMailingList)
		{
			Resources.updateCachedMultiLists("mailinglists.txt");
		}
		return updateMailingLists;
	}

	@Override
	public boolean unsubscribeFromJournal(final String journalName, final String userName, final boolean saveMailingList)
	{
		boolean updateMailingLists = false;
		if(CMProps.getVar(CMProps.Str.MAILBOX).length()==0)
			return false;

		final Map<String, List<String>> lists=Resources.getCachedMultiLists("mailinglists.txt",true);
		final List<String> mylist=lists.get(journalName);
		if(mylist==null)
			return false;
		for(int l=mylist.size()-1;l>=0;l--)
		{
			if(mylist.get(l).equalsIgnoreCase(userName))
			{
				mylist.remove(l);
				updateMailingLists=true;
				if(CMProps.getBoolVar(CMProps.Bool.EMAILFORWARDING))
				{
					String unsubscribeTitle="Un-Subscribed";
					String unsubscribedMsg="You are no longer subscribed to "+journalName+". To subscribe again, send an email with a subject of subscribe.";
					final String[] msgs =CMProps.getListVar(CMProps.StrList.SUBSCRIPTION_STRS);
					if((msgs!=null)&&(msgs.length>2))
					{
						if(msgs[2].length()>0)
							unsubscribeTitle = CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(msgs[2],"<NAME>",journalName));
						if((msgs.length>3) && (msgs[1].length()>0))
							unsubscribedMsg = CMLib.coffeeFilter().fullInFilter(CMStrings.replaceAll(msgs[3],"<NAME>",journalName));
					}
					CMLib.smtp().emailOrJournal(journalName,journalName,userName,unsubscribeTitle,unsubscribedMsg);
				}
			}
		}
		if(updateMailingLists && saveMailingList)
		{
			Resources.updateCachedMultiLists("mailinglists.txt");
		}
		return updateMailingLists;
	}
}
