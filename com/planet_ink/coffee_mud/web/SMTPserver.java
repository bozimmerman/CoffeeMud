package com.planet_ink.coffee_mud.web;
import java.io.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.exceptions.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
	public String ID(){return "SMTPserver";}
	public String name(){return "SMTPserver";}
	public long tickStatus=STATUS_NOT;
	public long getTickStatus(){return tickStatus;}
	public long lastAllProcessing=System.currentTimeMillis();
	
	public INI page=null;

	public static final float HOST_VERSION_MAJOR=(float)1.0;
	public static final float HOST_VERSION_MINOR=(float)0.0;
	public static Hashtable webMacros=null;
	public static INI iniPage=null;
	public ServerSocket servsock=null;
	public boolean isOK = false;
	private MudHost mud;
	private static boolean displayedBlurb=false;
	private static String domain="coffeemud";
	private static DVector journals=null;
	
	private HashSet oldEmailComplaints=new HashSet();
											 
	public final static String ServerVersionString = "CoffeeMud SMTPserver/" + HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR;

	public SMTPserver(MudHost a_mud)
	{
		super("SMTP");
		mud = a_mud;

		if (!initServer())
			isOK = false;
		else
			isOK = true;
	}

	public MudHost getMUD()	{return mud;}
	public String domainName(){return domain;}
	public String mailboxName(){return CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX);}

	public Properties getCommonPropPage()
	{
		if (iniPage==null || !iniPage.loaded)
		{
			iniPage=new INI("web" + File.separatorChar + "common.ini");
			if(!iniPage.loaded)
				Log.errOut("SMTPserver","Unable to load common.ini!");
		}
		return iniPage;
	}

	private boolean initServer()
	{
		if (!loadPropPage())
		{
			Log.errOut(getName(),"ERROR: SMTPserver unable to read ini file.");
			return false;
		}

		if (CommonStrings.getVar(CommonStrings.SYSTEM_MUDDOMAIN).toLowerCase().length()==0)
		{
			Log.errOut(getName(),"ERROR: required parameter missing: DOMAIN");
			return false;
		}
		if (page.getStr("PORT").length()==0)
		{
			Log.errOut(getName(),"ERROR: required parameter missing: PORT");
			return false;
		}
		
		domain=CommonStrings.getVar(CommonStrings.SYSTEM_MUDDOMAIN).toLowerCase();
		String mailbox=page.getStr("MAILBOX");
		if(mailbox==null) mailbox="";
		CommonStrings.setVar(CommonStrings.SYSTEM_MAILBOX,mailbox.trim());
		
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_EMAILFORWARDING,Util.s_bool(page.getStr("FORWARD")));
		
		String journalStr=page.getStr("JOURNALS");
		if((journalStr==null)||(journalStr.length()>0))
		{
			Vector V=Util.parseCommas(journalStr,true);
			if(V.size()>0)
			{
				journals=new DVector(5);
				for(int v=0;v<V.size();v++)
				{
					String s=((String)V.elementAt(v)).trim();
					String parm="";
					int x=s.indexOf("(");
					if((x>0)&&(s.endsWith(")")))
					{
						parm=s.substring(x+1,s.length()-1).trim();
						s=s.substring(0,x).trim();
					}
					if(!journals.contains(s))
					{
						Vector PV=Util.parseSpaces(parm,true);
						StringBuffer crit=new StringBuffer("");
						boolean forward=false;
						boolean subscribeOnly=false;
						boolean keepAll=false;
						for(int pv=0;pv<PV.size();pv++)
						{
							String ps=(String)PV.elementAt(pv);
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
						journals.addElement(s,new Boolean(forward),new Boolean(subscribeOnly),new Boolean(keepAll),crit.toString().trim());
					}
				}
			}
		}

		if (!displayedBlurb)
		{
			displayedBlurb = true;
			//Log.sysOut(getName(),"SMTPserver (C)2004 Bo Zimmerman");
		}
		if(mailbox.length()==0)
			Log.sysOut(getName(),"Player mail box system is disabled.");

		return true;
	}
	
	public String getAnEmailJournal(String journal)
	{
		if(journals==null) return null;
		journal=Util.replaceAll(journal,"_"," ");
		for(int i=0;i<journals.size();i++)
		{
			if(journal.equalsIgnoreCase((String)journals.elementAt(i,1)))
				return (String)journals.elementAt(i,1);
		}
		return null;
	}
	public boolean isAForwardingJournal(String journal)
	{
		if(journals==null) return false;
		for(int i=0;i<journals.size();i++)
		{
			if(journal.equalsIgnoreCase((String)journals.elementAt(i,1)))
				return ((Boolean)journals.elementAt(i,2)).booleanValue();
		}
		return false;
	}
	public boolean isASubscribeOnlyJournal(String journal)
	{
		if(journals==null) return false;
		for(int i=0;i<journals.size();i++)
		{
			if(journal.equalsIgnoreCase((String)journals.elementAt(i,1)))
				return ((Boolean)journals.elementAt(i,3)).booleanValue();
		}
		return false;
	}
	public boolean isAKeepAllJournal(String journal)
	{
		if(journals==null) return false;
		for(int i=0;i<journals.size();i++)
		{
			if(journal.equalsIgnoreCase((String)journals.elementAt(i,1)))
				return ((Boolean)journals.elementAt(i,4)).booleanValue();
		}
		return false;
	}
	public String getJournalCriteria(String journal)
	{
		if(journals==null) return "";
		for(int i=0;i<journals.size();i++)
		{
			if(journal.equalsIgnoreCase((String)journals.elementAt(i,1)))
				return (String)journals.elementAt(i,5);
		}
		return "";
	}

	private boolean loadPropPage()
	{
		if (page==null || !page.loaded)
		{
			String fn = "web" + File.separatorChar + "email.ini";
			page=new INI(getCommonPropPage(), fn);
			if(!page.loaded)
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
		if ((page == null) || (!page.loaded))
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
				bindAddr = null;
			}
		}

		try
		{
			servsock=new ServerSocket(page.getInt("PORT"), q_len, bindAddr);

			Log.sysOut(getName(),"Started on port: "+page.getInt("PORT"));
			if (bindAddr != null)
				Log.sysOut(getName(),"Bound to: "+bindAddr.toString());


			serverOK = true;

			while(true)
			{
				sock=servsock.accept();
				if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
				{
					ProcessSMTPrequest W=new ProcessSMTPrequest(sock,this,page);
					W.equals(W); // this prevents an initialized by never used error
					// nb - ProcessSMTPrequest is a Thread, but it .start()s in the constructor
					//  if succeeds - no need to .start() it here
				}
				else
				{
					sock.getOutputStream().write(("421 Mud down.. try later.\r\n").getBytes());
					sock.getOutputStream().flush();
					sock.close();
				}
				sock=null;
			}
		}
		catch(Throwable t)
		{
			// jef: if we've been interrupted, servsock will be null
			//   and serverOK will be true
			if((t!=null)&&(t instanceof Exception))
				Log.errOut(getName(),((Exception)t).getMessage());


			// jef: this prevents initHost() from running if run() has failed (eg socket in use)
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

		//Log.sysOut(getName(),"Thread stopped!");
	}


	// sends shutdown message to both log and optional session
	// then just calls interrupt

	public void shutdown(Session S)
	{
		Log.sysOut(getName(),"Shutting down.");
		if (S != null)
			S.println( getName() + " shutting down.");
		if(getTickStatus()==Tickable.STATUS_NOT)
			tick(this,MudHost.TICK_READYTOSTOP);
		else
		while(getTickStatus()!=Tickable.STATUS_NOT)
		{try{Thread.sleep(100);}catch(Exception e){}}
		this.interrupt();
	}

	public void shutdown()	{shutdown(null);}
	
	
	private boolean rightTimeToSendEmail(long email)
	{
		long curr=System.currentTimeMillis();
		IQCalendar IQE=new IQCalendar(email);
		IQCalendar IQC=new IQCalendar(curr);
		if(Util.absDiff(email,curr)<(30*60*1000)) return true;
		while(IQE.before(IQC))
		{
			if(Util.absDiff(IQE.getTimeInMillis(),IQC.getTimeInMillis())<(30*60*1000)) 
				return true;
			IQE.add(Calendar.DATE,1);
		}
		return false;
	}

	
	public Hashtable getMailingLists(Hashtable oldH)
	{
		if(oldH!=null) return oldH;
		return Resources.getMultiLists("mailinglists.txt");
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickStatus!=STATUS_NOT) return true;
		
		boolean updatedMailingLists=false;
		Hashtable lists=null;
		
		tickStatus=STATUS_START;
		if((tickID==MudHost.TICK_READYTOSTOP)||(tickID==MudHost.TICK_EMAIL))
		{
			// this is where it should attempt any mail forwarding
			// remember, a 5 day old private mail message is a goner
			// remember that new to all messages need to be parsed
			// for subscribe/unsubscribe and deleted, or then 
			// forwarded to all members private boxes.  Lots of work to do!
			if(journals!=null)
			for(int j=0;j<journals.size();j++)
			{
				String name=(String)journals.elementAt(j,1);
				if(isAForwardingJournal(name))
				{
					boolean keepall=isAKeepAllJournal(name);
					// Vector mailingList=?
					Vector msgs=CMClass.DBEngine().DBReadJournal(name);
					for(int m=0;m<msgs.size();m++)
					{
						Vector msg=(Vector)msgs.elementAt(m);
						String to=(String)msg.elementAt(DatabaseEngine.JOURNAL_TO);
						if(to.equalsIgnoreCase("ALL"))
						{
							long date=Util.s_long((String)msg.elementAt(DatabaseEngine.JOURNAL_DATE2));
							String from=(String)msg.elementAt(DatabaseEngine.JOURNAL_FROM);
							String key=(String)msg.elementAt(DatabaseEngine.JOURNAL_KEY);
							String subj=((String)msg.elementAt(DatabaseEngine.JOURNAL_SUBJ)).trim();
							String s=((String)msg.elementAt(DatabaseEngine.JOURNAL_MSG)).trim();
							if((subj.equalsIgnoreCase("subscribe"))
							||(s.equalsIgnoreCase("subscribe")))
							{
								// add to mailing list
								CMClass.DBEngine().DBDeleteJournal(key);
								if(CMClass.DBEngine().DBUserSearch(null,from))
								{
									lists=getMailingLists(lists);
									if(lists==null) lists=new Hashtable();
									Vector mylist=(Vector)lists.get(name);
									if(mylist==null)
									{
										mylist=new Vector();
										lists.put(name,mylist);
									}
									boolean found=false;
									for(int l=0;l<mylist.size();l++)
										if(((String)mylist.elementAt(l)).equalsIgnoreCase(from))
											found=true;
									if(!found)
									{
										mylist.addElement(from);
										updatedMailingLists=true;
										if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_EMAILFORWARDING))
										{
											String subscribeTitle=page.getStr("SUBSCRIBEDTITLE");
											if((subscribeTitle==null)||(subscribeTitle.length()==0))
												subscribeTitle="Subscribed";
											String subscribedMsg=page.getStr("SUBSCRIBEDMSG");
											if((subscribedMsg==null)||(subscribedMsg.length()==0))
												subscribedMsg="You are now subscribed to "+name+". To unsubscribe, send an email with a subject of unsubscribe.";
											subscribeTitle=CoffeeFilter.fullInFilter(Util.replaceAll(subscribeTitle,"<NAME>",name));
											subscribedMsg=CoffeeFilter.fullInFilter(Util.replaceAll(subscribedMsg,"<NAME>",name));
											CMClass.DBEngine().DBWriteJournal(name,name,from,subscribeTitle,subscribedMsg,-1);
										}
									}
								}
							}
							else
							if((subj.equalsIgnoreCase("unsubscribe"))
							||(s.equalsIgnoreCase("unsubscribe")))
							{
								// remove from mailing list
								CMClass.DBEngine().DBDeleteJournal(key);
								lists=getMailingLists(lists);
								if(lists==null) continue;
								Vector mylist=(Vector)lists.get(name);
								if(mylist==null) continue;
								for(int l=mylist.size()-1;l>=0;l--)
									if(((String)mylist.elementAt(l)).equalsIgnoreCase(from))
									{
										mylist.removeElementAt(l);
										updatedMailingLists=true;
										if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_EMAILFORWARDING))
										{
											String unsubscribeTitle=page.getStr("UNSUBSCRIBEDTITLE");
											if((unsubscribeTitle==null)||(unsubscribeTitle.length()==0))
												unsubscribeTitle="Subscribed";
											String unsubscribedMsg=page.getStr("UNSUBSCRIBEDMSG");
											if((unsubscribedMsg==null)||(unsubscribedMsg.length()==0))
												unsubscribedMsg="You are no longer subscribed to "+name+". To subscribe again, send an email with a subject of subscribe.";
											unsubscribeTitle=CoffeeFilter.fullInFilter(Util.replaceAll(unsubscribeTitle,"<NAME>",name));
											unsubscribedMsg=CoffeeFilter.fullInFilter(Util.replaceAll(unsubscribedMsg,"<NAME>",name));
											CMClass.DBEngine().DBWriteJournal(name,name,from,unsubscribeTitle,unsubscribedMsg,-1);
										}
									}
							}
							else
							{
								if(date>lastAllProcessing)
								{
									lists=getMailingLists(lists);
									if(lists!=null)
									{
										Vector mylist=(Vector)lists.get(name);
										if((mylist!=null)&&(mylist.contains(from)))
										{
											for(int i=0;i<mylist.size();i++)
											{
												String to2=(String)mylist.elementAt(i);
												if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_EMAILFORWARDING))
													CMClass.DBEngine().DBWriteJournal(name,from,to2,subj,s,-1);
											}
										}
										else
											CMClass.DBEngine().DBDeleteJournal(key);
									}
								}
								if(!keepall)
									CMClass.DBEngine().DBDeleteJournal(key);
								else
								{
									IQCalendar IQE=new IQCalendar(date);
									IQE.add(IQCalendar.DATE,getJournalDays());
									if(IQE.getTimeInMillis()<System.currentTimeMillis())
										CMClass.DBEngine().DBDeleteJournal((String)msg.elementAt(DatabaseEngine.JOURNAL_KEY));
								}
							}
						}
					}
				}
			}
		
			// here is where the mail is actually sent
			if((tickID==MudHost.TICK_EMAIL)
			&&(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_EMAILFORWARDING)))
			{
				if((mailboxName()!=null)&&(mailboxName().length()>0))
				{
					Vector emails=CMClass.DBEngine().DBReadJournal(mailboxName());
					processEmails(emails,null,true);
				}
				if(journals!=null)
					for(int j=0;j<journals.size();j++)
					{
						String name=(String)journals.elementAt(j,1);
						if(isAForwardingJournal(name))
						{
							Vector emails=CMClass.DBEngine().DBReadJournal(name);
							processEmails(emails,name,false);
						}
					}
			}
			lastAllProcessing=System.currentTimeMillis();
			if((updatedMailingLists)&&(lists!=null))
			{
				Resources.updateMultiList("mailinglists.txt",lists);
				updatedMailingLists=false;
			}
		}
		System.gc();
		try{Thread.sleep(1000);}catch(Exception ex){}
		tickStatus=STATUS_NOT;
		return true;
	}

	public void processEmails(Vector emails, 
							  String overrideReplyTo,
							  boolean usePrivateRules)
	{
		if(emails!=null)
		for(int e=0;e<emails.size();e++)
		{
			Vector mail=(Vector)emails.elementAt(e);
			String key=(String)mail.elementAt(DatabaseEngine.JOURNAL_KEY);
			String from=(String)mail.elementAt(DatabaseEngine.JOURNAL_FROM);
			String to=(String)mail.elementAt(DatabaseEngine.JOURNAL_TO);
			long date=Util.s_long((String)mail.elementAt(DatabaseEngine.JOURNAL_DATE2));
			String subj=((String)mail.elementAt(DatabaseEngine.JOURNAL_SUBJ)).trim();
			String msg=((String)mail.elementAt(DatabaseEngine.JOURNAL_MSG)).trim();
			
			if(to.equalsIgnoreCase("ALL")) continue;
			
			if(!rightTimeToSendEmail(date)) continue;
			
			// check for valid recipient
			MOB toM=CMMap.getLoadPlayer(to);
			if(toM==null)
			{ 
				Log.errOut("SMTPServer","Invalid to address '"+to+"' in email: "+msg);
				CMClass.DBEngine().DBDeleteJournal(key);
				continue;
			}
			
			// check to see if the sender is ignored
			if((toM.playerStats()!=null)
			&&(toM.playerStats().getIgnored().contains(from)))
			{
				// email is ignored
				CMClass.DBEngine().DBDeleteJournal(key);
				continue;
			}
			
			// check email age
			if(usePrivateRules)
			{
				IQCalendar IQE=new IQCalendar(date);
				IQE.add(IQCalendar.DATE,getEmailDays());
				if(IQE.getTimeInMillis()<System.currentTimeMillis())
				{
					// email is a goner
					CMClass.DBEngine().DBDeleteJournal(key);
					continue;
				}
			}
			
			if(Util.bset(toM.getBitmap(),MOB.ATT_AUTOFORWARD)) // forwarding OFF
				continue;

			if((toM.playerStats()==null)
			||(toM.playerStats().getEmail().length()==0)) // no email addy to forward TO
				continue;
			
			SMTPclient SC=null;
			try
			{
				SC=new SMTPclient(toM.playerStats().getEmail());
			}
			catch(BadEmailAddressException be)
			{
				if(!usePrivateRules)
				{
					// email is a goner if its a list
					CMClass.DBEngine().DBDeleteJournal(key);
					continue;
				}
				else
				{
					// otherwise it has its n days
					continue;
				}
			}
			catch(java.io.IOException ioe)
			{
				if(!oldEmailComplaints.contains(toM.Name()))
				{
					oldEmailComplaints.add(toM.Name());
					Log.errOut("SMTPServer","Unable to find '"+toM.playerStats().getEmail()+"' for '"+toM.name()+"'.");
				}
				// it has 5 days to get better.
				IQCalendar IQE=new IQCalendar(date);
				IQE.add(IQCalendar.DATE,getFailureDays());
				if(IQE.getTimeInMillis()<System.currentTimeMillis())
				{
					// email is a goner
					CMClass.DBEngine().DBDeleteJournal(key);
				}
				continue;
			}
			
			// one way or another, this email is HISTORY!
			CMClass.DBEngine().DBDeleteJournal(key);
			
			String replyTo=(overrideReplyTo!=null)?(overrideReplyTo):from;
			try
			{
				SC.sendMessage(from+"@"+domainName(),
							   replyTo+"@"+domainName(),
							   toM.playerStats().getEmail(),
							   usePrivateRules?toM.playerStats().getEmail():replyTo+"@"+domainName(),
							   subj,
							   CoffeeFilter.simpleOutFilter(msg));
			}
			catch(java.io.IOException ioe)
			{
				Log.errOut("SMTPServer","Unable to send to '"+toM.playerStats().getEmail()+"' for user '"+toM.name()+"': "+ioe.getMessage()+".");
			}
			
			// kaplah! On to next...
		}
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
		int x=Util.s_int(s);
		if(x==0) return Integer.MAX_VALUE;
		return x;
	}
	public int getEmailDays()
	{
		String s=page.getStr("EMAILDAYS");
		if(s==null) return (365*20);
		int x=Util.s_int(s);
		if(x==0) return (365*20);
		return x;
	}
	public int getJournalDays()
	{
		String s=page.getStr("JOURNALDAYS");
		if(s==null) return (365*20);
		int x=Util.s_int(s);
		if(x==0) return (365*20);
		return x;
	}
	public int getFailureDays()
	{
		String s=page.getStr("FAILUREDAYS");
		if(s==null) return (365*20);
		int x=Util.s_int(s);
		if(x==0) return (365*20);
		return x;
	}
	public long getMaxMsgSize()
	{
		String s=page.getStr("MAXMSGSIZE");
		if(s==null) return Long.MAX_VALUE;
		long x=Util.s_long(s);
		if(x==0) return Long.MAX_VALUE;
		return x;
	}
}