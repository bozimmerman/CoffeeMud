package com.planet_ink.coffee_mud.core.smtp;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.CMThreadFactory;
import com.planet_ink.coffee_mud.core.threads.CMThreadPoolExecutor;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.JournalEntry;

/*
   Copyright 2000-2014 Bo Zimmerman

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
public class SMTPserver extends Thread implements Tickable
{
	public static final float  HOST_VERSION_MAJOR=(float)1.1;
	public static final float  HOST_VERSION_MINOR=(float)1.0;
	public static final String ServerVersionString = "CoffeeMud SMTPserver/" + HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR;
	
	public String ID(){return "SMTPserver";}
	public String name(){return "SMTPserver";}
	public CMObject newInstance(){try{return getClass().newInstance();}catch(Exception e){return new SMTPserver(mud);}}
	public void initializeClass()
	{
	}
	public CMObject copyOf(){try{return (SMTPserver)this.clone();}catch(Exception e){return newInstance();}}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	
	public long 		tickStatus=STATUS_NOT;
	public boolean 		isOK = false;
	private MudHost 	mud;
	public CMProps 		page=null;
	public ServerSocket servsock=null;
	public CMProps 		iniPage=null;
	private boolean 	displayedBlurb=false;
	private String 		domain="coffeemud";
	private int			maxThreads = 3;
	private int			threadTimeoutMins = 10;
	private HashSet<String> 		 oldEmailComplaints=new HashSet<String>();
	private CMThreadPoolExecutor  	 threadPool;

	public SMTPserver()
	{
		super("SMTP"); 
		mud=null;
		isOK=false;
		threadPool = new CMThreadPoolExecutor("SMTP", 0, 3, 30, TimeUnit.SECONDS, 5, 256);
		threadPool.setThreadFactory(new CMThreadFactory("SMTP"));
		setDaemon(true);
	}
	
	public SMTPserver(MudHost a_mud)
	{
		super("SMTP");
		mud = a_mud;

		if (!initServer())
			isOK = false;
		else
			isOK = true;
		threadPool = new CMThreadPoolExecutor("SMTP", 0, maxThreads, 30, TimeUnit.SECONDS, threadTimeoutMins,256);
		setDaemon(true);
	}

	public long getTickStatus(){return tickStatus;}
	public MudHost getMUD()	{return mud;}
	public String domainName(){return domain;}
	public String mailboxName(){return CMProps.getVar(CMProps.Str.MAILBOX);}

	public Properties getCommonPropPage()
	{
		if (iniPage==null || !iniPage.isLoaded())
		{
			iniPage=new CMProps ("web/common.ini");
			if(!iniPage.isLoaded())
				Log.errOut("SMTPserver","Unable to load common.ini!");
		}
		return iniPage;
	}

	protected boolean initServer()
	{
		if (!loadPropPage())
		{
			Log.errOut(getName(),"SMTPserver unable to read ini file.");
			return false;
		}

		if (CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase().length()==0)
		{
			Log.errOut(getName(),"Set your coffeemud.ini parameter: DOMAIN");
			return false;
		}
		if (page.getStr("PORT").length()==0)
		{
			Log.errOut(getName(),"Set your coffeemud.ini parameter: PORT");
			return false;
		}
		if(CMath.isNumber(page.getStr("REQUESTTIMEOUTMINS")))
			threadTimeoutMins=CMath.s_int(page.getStr("REQUESTTIMEOUTMINS"));
		
		if(CMath.isNumber(page.getStr("MAXTHREADS")))
			maxThreads=CMath.s_int(page.getStr("MAXTHREADS"));

		domain=CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase();
		String mailbox=page.getStr("MAILBOX");
		if(mailbox==null) mailbox="";
		CMProps.setVar(CMProps.Str.MAILBOX,mailbox.trim());
		CMProps.setIntVar(CMProps.Int.MAXMAILBOX,getMaxMsgs());

		CMProps.setBoolVar(CMProps.Bool.EMAILFORWARDING,CMath.s_bool(page.getStr("FORWARD")));

		if((CMProps.getListVar(CMProps.StrList.SUBSCRIPTION_STRS)==null)
		||(CMProps.getListVar(CMProps.StrList.SUBSCRIPTION_STRS).length==0))
		{
			String[] msgs = new String[]{
					page.getStr("SUBSCRIBEDTITLE"),
					page.getStr("SUBSCRIBEDMSG"),
					page.getStr("UNSUBSCRIBEDTITLE"),
					page.getStr("UNSUBSCRIBEDMSG")
				};
			for(int i=0;i<msgs.length;i++)
				if(msgs[i]==null) 
					msgs[i]="";
			CMProps.setListVar(CMProps.StrList.SUBSCRIPTION_STRS, msgs);
		}
		
		if (!displayedBlurb)
		{
			displayedBlurb = true;
			//Log.sysOut(getName(),"SMTPserver (C)2005-2014 Bo Zimmerman");
		}
		if(mailbox.length()==0)
			Log.sysOut(getName(),"Player mail box system is disabled.");

		return true;
	}
	
	public TreeMap<String, JournalsLibrary.SMTPJournal> parseJournalList(String journalStr)
	{
		TreeMap<String, JournalsLibrary.SMTPJournal> set=new TreeMap<String, JournalsLibrary.SMTPJournal>(); 
		if((journalStr==null)||(journalStr.length()>0))
		{
			List<String> V=CMParms.parseCommas(journalStr,true);
			if(V.size()>0)
			{
				for(int v=0;v<V.size();v++)
				{
					String s=V.get(v).trim();
					String parm="";
					int x=s.indexOf('(');
					if((x>0)&&(s.endsWith(")")))
					{
						parm=s.substring(x+1,s.length()-1).trim();
						s=s.substring(0,x).trim();
					}
					List<String> PV=CMParms.parseSpaces(parm,true);
					StringBuffer crit=new StringBuffer("");
					boolean forward=false;
					boolean subscribeOnly=false;
					boolean keepAll=false;
					for(int pv=0;pv<PV.size();pv++)
					{
						String ps=PV.get(pv);
						if(ps.equalsIgnoreCase("forward"))
							forward=true;
						else
						if(ps.equalsIgnoreCase("subscribeonly"))
							subscribeOnly=true;
						else
						if(ps.equalsIgnoreCase("keepall"))
							keepAll=true;
						else
							crit.append(s+" ");
					}
					set.put(s.toUpperCase().trim(), 
							new JournalsLibrary.SMTPJournal(s, forward, subscribeOnly, keepAll, crit.toString().trim()));
				}
			}
		}
		return set;
	}

	public String getAnEmailJournal(String journal)
	{
		journal=CMStrings.replaceAll(journal,"_"," ");
		final JournalsLibrary.SMTPJournal jrnl=getAJournal(journal);
		return jrnl != null ? jrnl.name : null;
	}

	@SuppressWarnings("unchecked")
	public TreeMap<String, JournalsLibrary.SMTPJournal> getJournalSets()
	{
		TreeMap<String, JournalsLibrary.SMTPJournal> set=(TreeMap<String, JournalsLibrary.SMTPJournal>)
															Resources.getResource("SYSTEM_SMTP_JOURNALS");
		if(set==null)
		{
			set=parseJournalList(page.getStr("JOURNALS"));
			Resources.submitResource("SYSTEM_SMTP_JOURNALS", set);
		}
		return set;
	}
	
	public JournalsLibrary.SMTPJournal getAJournal(String journal)
	{
		TreeMap<String, JournalsLibrary.SMTPJournal> set=getJournalSets();
		if(set==null) return null;
		return set.get(journal.toUpperCase().trim());
	}
	public boolean isAForwardingJournal(String journal)
	{
		final JournalsLibrary.SMTPJournal jrnl=getAJournal(journal);
		return jrnl != null ? jrnl.forward : false;
	}
	public boolean isASubscribeOnlyJournal(String journal)
	{
		final JournalsLibrary.SMTPJournal jrnl=getAJournal(journal);
		return jrnl != null ? jrnl.subscribeOnly : false;
	}
	public boolean isAKeepAllJournal(String journal)
	{
		final JournalsLibrary.SMTPJournal jrnl=getAJournal(journal);
		return jrnl != null ? jrnl.keepAll : false;
	}
	public MaskingLibrary.CompiledZapperMask getJournalCriteria(String journal)
	{
		final JournalsLibrary.SMTPJournal jrnl=getAJournal(journal);
		return jrnl != null ? jrnl.criteria : null;
	}

	protected boolean loadPropPage()
	{
		if (page==null || !page.isLoaded())
		{
			String fn = "web/email.ini";
			page=new CMProps (getCommonPropPage(), fn);
			if(!page.isLoaded())
			{
				Log.errOut(getName(),"failed to load " + fn);
				return false;
			}
		}
		return true;
	}

	public void run()
	{
		int q_len = 6;
		Socket sock=null;
		boolean serverOK = false;

		if (!isOK)	return;
		if ((page == null) || (!page.isLoaded()))
		{
			Log.errOut(getName(),"ERROR: SMTPserver will not run with no properties. Shutting down.");
			isOK = false;
			return;
		}


		if (page.getInt("BACKLOG") > 0)
			q_len = page.getInt("BACKLOG");

		InetAddress bindAddr = null;


		if (page.getStr("BIND") != null && page.getStr("BIND").length() > 0)
		{
			try
			{
				bindAddr = InetAddress.getByName(page.getStr("BIND"));
			}
			catch (UnknownHostException e)
			{
				Log.errOut(getName(),"ERROR: Could not bind to address " + page.getStr("BIND"));
			}
		}

		try
		{
			setName(getName()+"@"+page.getInt("PORT"));
			servsock=new ServerSocket(page.getInt("PORT"), q_len, bindAddr);

			Log.sysOut(getName(),"Started on port: "+page.getInt("PORT"));
			if (bindAddr != null)
				Log.sysOut(getName(),"Bound to: "+bindAddr.toString());


			serverOK = true;

			while(true)
			{
				sock=servsock.accept();
				if(sock != null)
				{
					while(CMLib.threads().isAllSuspended())
						Thread.sleep(1000);
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SMTPSERVER))
						Log.debugOut("SMTPserver","Connection received: "+sock.getInetAddress().getHostAddress());
					if(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
						threadPool.execute(new ProcessSMTPrequest(sock,this));
					else
					{
						sock.getOutputStream().write(("421 Mud down.. try later.\r\n").getBytes());
						sock.getOutputStream().flush();
						sock.close();
					}
					sock=null;
				}
			}
		}
		catch(Exception e)
		{
			// if we've been interrupted, servsock will be null and serverOK will be true
			Log.errOut(getName(),e.getMessage());
			// this prevents initHost() from running if run() has failed (eg socket in use)
			if (!serverOK)
				isOK = false;
		}

		try
		{
			if(servsock!=null)
				servsock.close();
			if(sock!=null)
				sock.close();
		}
		catch(IOException e)
		{
		}
	}


	// sends shutdown message to both log and optional session
	// then just calls interrupt
	public void shutdown(Session S)
	{
		Log.sysOut(getName(),"Shutting down.");
		if (S != null)
			S.println( getName() + " shutting down.");
		try{servsock.close(); Thread.sleep(100);}catch(Exception e){}
		threadPool.shutdown();
		if(getTickStatus()==Tickable.STATUS_NOT)
			tick(this,Tickable.TICKID_READYTOSTOP);
		else
		{
			int att=0;
			while((att<100)&&(getTickStatus()!=Tickable.STATUS_NOT))
			{try{att++;Thread.sleep(100);}catch(Exception e){}}
		}
		threadPool.shutdownNow();
		CMLib.killThread(this,1000,30);
	}

	public void shutdown()	{shutdown(null);}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickStatus!=STATUS_NOT) return true;

		boolean updatedMailingLists=false;
		tickStatus=STATUS_START;
		if((tickID==Tickable.TICKID_READYTOSTOP)||(tickID==Tickable.TICKID_EMAIL))
		{
			MassMailer massMailer = new MassMailer(page,domain,oldEmailComplaints);
			
			TreeMap<String, JournalsLibrary.SMTPJournal> set=getJournalSets();
			long lastAllProcessing=0;
			if(Resources.isPropResource("SMTP","LASTALLPROCESING"))
				lastAllProcessing=CMath.s_long(Resources.getPropResource("SMTP","LASTALLPROCESING"));
			if(lastAllProcessing==0)
				lastAllProcessing=System.currentTimeMillis()-(10*60*1000);
			long nextAllProcessing=System.currentTimeMillis();
			
			// this is where it should attempt any mail forwarding
			// remember, a 5 day old private mail message is a goner
			// remember that new to all messages need to be parsed
			// for subscribe/unsubscribe and deleted, or then
			// forwarded to all members private boxes.  Lots of work to do!
			for(JournalsLibrary.SMTPJournal smtpJournal : set.values())
			{
				final String journalName=smtpJournal.name;
				if(smtpJournal.forward)
				{
					// Vector mailingList=?
					final List<JournalsLibrary.JournalEntry> msgs=CMLib.database().DBReadJournalMsgsNewerThan(journalName,"ALL",lastAllProcessing-1);
					for(final JournalsLibrary.JournalEntry msg : msgs)
					{
						//if(msg.to.equalsIgnoreCase("ALL")) // implied by the query
						final String subj=msg.subj;
						final String msgStr=msg.msg.trim();
						if((subj.equalsIgnoreCase("subscribe"))
						||(msgStr.equalsIgnoreCase("subscribe")))
						{
							// add to mailing list
							CMLib.database().DBDeleteJournal(journalName,msg.key);
							updatedMailingLists= CMLib.journals().subscribeToJournal(journalName, msg.from, false) || updatedMailingLists;
						}
						else
						if((subj.equalsIgnoreCase("unsubscribe"))
						||(msgStr.equalsIgnoreCase("unsubscribe")))
						{
							// remove from mailing list
							CMLib.database().DBDeleteJournal(journalName,msg.key);
							updatedMailingLists= CMLib.journals().unsubscribeFromJournal(journalName, msg.from, false) || updatedMailingLists;
						}
						else
						{
							if(CMProps.getBoolVar(CMProps.Bool.EMAILFORWARDING))
							{
								String jrnlSubj;
								if(msg.subj.indexOf("["+journalName+"]")<0)
								{
									if(msg.subj.startsWith("RE: "))
										jrnlSubj="RE: ["+journalName+"] "+msg.subj.substring(4);
									else
										jrnlSubj="["+journalName+"] "+msg.subj;
								}
								else
									jrnlSubj=msg.subj;
								String jrnlMessage=msg.msg;
								if(jrnlMessage.startsWith("<HTML><BODY>"))
								{
									
								}
								else
								{
									if(jrnlMessage.indexOf("<HTML>")>0)
									{
										jrnlMessage=CMStrings.replaceAll(jrnlMessage,"<HTML>","");
										jrnlMessage=CMStrings.replaceAll(jrnlMessage,"</HTML>","");
										jrnlMessage=CMStrings.replaceAll(jrnlMessage,"<BODY>","");
										jrnlMessage=CMStrings.replaceAll(jrnlMessage,"</BODY>","");
									}
									else
										jrnlMessage="<HTML><BODY>"+jrnlMessage+"</BODY></HTML>";
								}
								Map<String, List<String>> lists=Resources.getCachedMultiLists("mailinglists.txt",true);
								List<String> mylist=lists.get(journalName);
								if((mylist!=null)&&(mylist.contains(msg.from)))
								{
									for(int i=0;i<mylist.size();i++)
									{
										String emailToName=mylist.get(i);
										if(CMProps.getBoolVar(CMProps.Bool.EMAILFORWARDING))
											CMLib.database().DBWriteJournalEmail(mailboxName(),journalName,msg.from,emailToName,jrnlSubj,jrnlMessage);
									}
								}
							}
							if(!isAKeepAllJournal(journalName))
								CMLib.database().DBDeleteJournal(journalName,msg.key);
							else
							{
								Calendar IQE=Calendar.getInstance();
								IQE.setTimeInMillis(msg.update);
								IQE.add(Calendar.DATE,getJournalDays());
								if(IQE.getTimeInMillis()<System.currentTimeMillis())
									CMLib.database().DBDeleteJournal(journalName,msg.key);
							}
						}
					}
				}
			}
			Resources.setPropResource("SMTP", "LASTALLPROCESSING", Long.toString(nextAllProcessing));

			// here is where the mail is actually sent
			if((tickID==Tickable.TICKID_EMAIL)
			&&(CMProps.getBoolVar(CMProps.Bool.EMAILFORWARDING)))
			{
				if((mailboxName()!=null)&&(mailboxName().length()>0))
				{
					List<JournalsLibrary.JournalEntry> emails=CMLib.database().DBReadJournalMsgs(mailboxName());
					if(emails!=null)
					for(JournalsLibrary.JournalEntry mail : emails)
					{
						if((mail.data.length()>0)&&(isAForwardingJournal(mail.data)))
							massMailer.addMail(mail, mailboxName(), mail.data, true);
						else
							massMailer.addMail(mail, mailboxName(), null, true);
					}
				}
			}
			if(updatedMailingLists)
			{
				Resources.updateCachedMultiLists("mailinglists.txt");
				updatedMailingLists=false;
			}
			new Thread(Thread.currentThread().getThreadGroup(),massMailer,"MassMailer"+Thread.currentThread().getThreadGroup().getName().charAt(0)).start();
		}
		System.gc();
		try{Thread.sleep(1000);}catch(Exception ex){}
		tickStatus=STATUS_NOT;
		return true;
	}
	
	// interrupt does NOT interrupt the ServerSocket.accept() call...
	//  override it so it does
	public void interrupt()
	{
		if(servsock!=null)
		{
			try
			{
				servsock.close();
				//jef: we MUST set it to null
				// (so run() can tell it was interrupted & didn't have an error)
				servsock = null;
			}
			catch(IOException e)
			{
			}
		}
		super.interrupt();
	}

	public int getMaxMsgs()
	{
		String s=page.getStr("MAXMSGS");
		if(s==null) return Integer.MAX_VALUE;
		int x=CMath.s_int(s);
		if(x==0) return Integer.MAX_VALUE;
		return x;
	}
	public int getJournalDays()
	{
		String s=page.getStr("JOURNALDAYS");
		if(s==null) return (365*20);
		int x=CMath.s_int(s);
		if(x==0) return (365*20);
		return x;
	}
	public long getMaxMsgSize()
	{
		String s=page.getStr("MAXMSGSIZE");
		if(s==null) return Long.MAX_VALUE;
		long x=CMath.s_long(s);
		if(x==0) return Long.MAX_VALUE;
		return x;
	}
}
