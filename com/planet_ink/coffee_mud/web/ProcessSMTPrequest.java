package com.planet_ink.coffee_mud.web;
import java.io.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.exceptions.*;

public class ProcessSMTPrequest extends Thread
{
	private INI page;
	private Socket sock;
	private static long instanceCnt = 0;
	private SMTPserver server=null;
	private final static String cr = "\r\n";
	private final static String S_250 = "250 OK";
	private String from=null;
	private Vector to=null;
	private StringBuffer data=null;
	private String domain=null;

	public ProcessSMTPrequest(Socket a_sock,
							  SMTPserver a_Server,
							  INI a_page)
	{
		super( "SMTPrq"+(instanceCnt++));
		page = a_page;
		server = a_Server;
		sock = a_sock;

		if (page != null && sock != null)
			this.start();
	}
	
	public String validLocalAccount(String s)
	{
		int x=s.indexOf("@");
		String name=s;
		if(x>0)
		{
			name=s.substring(0,x).trim();
			String domain=s.substring(x+1).trim();
			if(!domain.toUpperCase().endsWith(server.domainName().toUpperCase()))
			{
				if(server.mailboxName().length()>0)
				{
					name=CMClass.DBEngine().DBEmailSearch(s);
					if(name!=null) return name;
				}
				return null;
			}
		}
		if(server.getAnEmailJournal(name)!=null)
			return server.getAnEmailJournal(name);
		if((server.mailboxName().length()>0)
		&&(CMClass.DBEngine().DBUserSearch(null,name)))
			return name;
		return null;
	}
	
	
	public void run()
	{
		DataInputStream sin = null;
		DataOutputStream sout = null;
		int failures=0;

		byte[] replyData = null;

		try
		{
			sout = new DataOutputStream(sock.getOutputStream());
			sin=new DataInputStream(sock.getInputStream());
			sout.write(("220 ESMTP "+server.domainName()+" "+server.ServerVersionString+"; "+new IQCalendar().d2String()+cr).getBytes());
			boolean quitFlag=false;
			boolean dataMode=false;
			while(!quitFlag)
			{
				sock.setSoTimeout(5*60*1000);
				String s=null;
				char lastc=(char)-1;
				char c=(char)-1;
				StringBuffer input=new StringBuffer("");
				while(!quitFlag)
				{
					lastc=c;
					c=(char)sin.read();
					if(c<0)	throw new IOException("reset by peer");
					if((lastc==cr.charAt(0))&&(c==cr.charAt(1)))
					{	s=input.substring(0,input.length()-1); break;}
					input.append((char)c);
				}
				String cmd=s.toUpperCase();
				String parm="";
				int cmdindex=s.indexOf(" ");
				if(cmdindex>0)
				{
					cmd=s.substring(0,cmdindex).toUpperCase();
					parm=s.substring(cmdindex+1);
				}
				
				
				if((dataMode)&&(s.equals(".")))
				{
					dataMode=false;
					/*When the SMTP server accepts a message either for relaying or for final delivery, it inserts a trace record (also referred to interchangeably as a "time stamp line" or "Received" line) at the top of the mail data. This trace record indicates the identity of the host that sent the message, the identity of the host that received the message (and is inserting this time stamp), and the date and time the message was received.*/
					if(data.length()>=server.getMaxMsgSize())
						replyData=("552 Message exceeds size limit."+cr).getBytes();
					else
					{
						replyData=("250 Message accepted for delivery."+cr).getBytes();
						boolean startBuffering=false;
						StringBuffer finalData=new StringBuffer("");
						String subject=null;
						try
						{
							BufferedReader lineR=new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data.toString().getBytes())));
							while(true)
							{
								String s2=lineR.readLine();
								if(s2==null) break;
								if(startBuffering)
									finalData.append(s2+cr);
								else
								if(s2.length()==0)
									startBuffering=true;
								else
								if(s2.startsWith("Subject: "))
									subject=s2.substring(9).trim();
								else
								if(s2.startsWith("Content Type: "))
								{
									if(!s2.substring(14).toUpperCase().startsWith("TEXT/PLAIN"));
									{
										replyData=("552 Message content type '"+s2.substring(14)+"' not accepted."+cr).getBytes();
										subject=null;
										break;
									}
								}
							}
						}
						catch(IOException e){}
							
						if((finalData.length()==0)
						&&(!startBuffering))
						{
							finalData=new StringBuffer(data.toString());
							subject="";
						}
							
						if((finalData.length()>0)&&(subject!=null))
						{
							for(int i=0;i<to.size();i++)
							{
								String journal=server.getAnEmailJournal((String)to.elementAt(i));
								if(journal!=null)
								{
									String fdat=finalData.toString().trim();
									if(server.isASubscribeOnlyJournal(journal))
									{
										if(!subject.trim().equalsIgnoreCase("subscribe")
										&&(!subject.trim().equalsIgnoreCase("unsubscribe"))
										&&(!fdat.trim().equalsIgnoreCase("subscribe"))
										&&(!fdat.trim().equalsIgnoreCase("unsubscribe")))
										{
											MOB M=CMMap.getLoadPlayer(from);
											if((M==null)||(!M.isASysOp(null)))
											{
												replyData=("552 Mailbox '"+journal+"' only accepts subscribe/unsubscribe."+cr).getBytes();
												break;
											}
										}
									}
									CMClass.DBEngine().DBWriteJournal(journal,
																	  from,
																	  "ALL",
																	  subject,
																	  fdat,-1);
								}
								else
								{
									CMClass.DBEngine().DBWriteJournal(server.mailboxName(),
																	  from,
																	  (String)to.elementAt(i),
																	  subject,
																	  finalData.toString(),-1);
								}
							}
						}
					}
				}
				else
				if(dataMode)
				{
					if(data==null) data=new StringBuffer("");
					if(data.length()<server.getMaxMsgSize())
						data.append(s+cr);
				}
				else
				if(cmd.equals("HELP"))
				{
					parm=parm.toUpperCase();
					if(parm.length()==0)
					{
						replyData=(
						"214-This is "+server.ServerVersionString+cr+
						"214-Topics:"+cr+
						"214-    HELO    EHLO    MAIL    RCPT    DATA"+cr+
						"214-    RSET    NOOP    QUIT    HELP    VRFY"+cr+
						"214-    EXPN    VERB    ETRN    DSN"+cr+
						"214-For more info use \"HELP <topic>\"."+cr+
						"214-For local information send email to your local Archon."+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
					if(parm.equals("NOOP"))
					{
						replyData=(
						"214-NOOP"+cr+
						"214-    Do nothing."+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
					if(parm.equals("HELO"))
					{
						replyData=(
						"214-HELO <hostname>"+cr+
						"214-    Introduce yourself."+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
					if(parm.equals("EHLO"))
					{
						replyData=(
						"214-EHLO"+cr+
						"214-    Introduce yourself, and request extended SMTP mode."+cr+
						"214-Possible replies include:"+cr+
						"214-    SEND            Send as mail                    [RFC821]"+cr+
						"214-    SOML            Send as mail or terminal        [RFC821]"+cr+
						"214-    SAML            Send as mail and terminal       [RFC821]"+cr+
						"214-    EXPN            Expand the mailing list         [RFC821]"+cr+
						"214-    HELP            Supply helpful information      [RFC821]"+cr+
						"214-    TURN            Turn the operation around       [RFC821]"+cr+
						"214-    8BITMIME        Use 8-bit data                  [RFC1652]"+cr+
						"214-    SIZE            Message size declaration        [RFC1870]"+cr+
						"214-    VERB            Verbose                         [Allman]"+cr+
						"214-    ONEX            One message transaction only    [Allman]"+cr+
						"214-    CHUNKING        Chunking                        [RFC1830]"+cr+
						"214-    BINARYMIME      Binary MIME                     [RFC1830]"+cr+
						"214-    PIPELINING      Command Pipelining              [RFC1854]"+cr+
						"214-    DSN             Delivery Status Notification    [RFC1891]"+cr+
						"214-    ETRN            Remote Message Queue Starting   [RFC1985]"+cr+
						"214-    XUSR            Initial (user) submission       [Allman]"+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
					if(parm.equals("MAIL"))
					{
						replyData=(
						"214-MAIL FROM: <sender> [ <parameters> ]"+cr+
						"214-    Specifies the sender.  Parameters are ESMTP extensions."+cr+
						"214-    See \"HELP DSN\" for details."+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
					if(parm.equals("DATA"))
					{
						replyData=(
						"214-DATA"+cr+
						"214-    Following text is collected as the message."+cr+
						"214-    End with a single dot."+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
					if(parm.equals("RSET"))
					{
						replyData=(
						"214-RSET"+cr+
						"214-    Resets the system."+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
					if(parm.equals("QUIT"))
					{
						replyData=(
						"214-QUIT"+cr+
						"214-    Exit SMTP."+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
					if(parm.equals("VRFY"))
					{
						replyData=(
						"214-VRFY <recipient>"+cr+
						"214-    Verify an address.  If you want to see what it aliases"+cr+
						"214-    to, use EXPN instead."+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
					if(parm.equals("EXPN"))
					{
						replyData=(
						"214-EXPN <recipient>"+cr+
						"214-    Expand an address.  If the address indicates a mailing"+cr+
						"214-    list, return the contents of that list."+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
					if(parm.equals("VERB"))
					{
						replyData=(
						"214-VERB"+cr+
						"214-    Not implemented in this server."+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
					if(parm.equals("ETRN"))
					{
						replyData=(
						"214-ETRN [ <hostname> | @<domain> | #<queuename> ]"+cr+
						"214-    Not implemented in this server."+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
					if(parm.equals("DSN"))
					{
						replyData=(
						"214-MAIL FROM: <sender> [ RET={ FULL | HDRS} ] [ ENVID=<envid> ]"+cr+
						"214-RCPT TO: <recipient> [ NOTIFY={NEVER,SUCCESS,FAILURE,DELAY} ]"+cr+
						"214-                     [ ORCPT=<recipient> ]"+cr+
						"214-    SMTP Delivery Status Notifications."+cr+
						"214-Descriptions:"+cr+
						"214-    RET     Return either the full message or only headers."+cr+
						"214-    ENVID   Sender's \"envelope identifier\" for tracking."+cr+
						"214-    NOTIFY  When to send a DSN. Multiple options are OK, comma-"+cr+
						"214-            delimited. NEVER must appear by itself."+cr+
						"214-    ORCPT   Original recipient."+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
					if(parm.equals("RCPT"))
					{
						replyData=(
						"214-RCPT TO: <recipient> [ <parameters> ]"+cr+
						"214-    Specifies the recipient.  Can be used any number of times."+cr+
						"214-    Parameters are ESMTP extensions.  See \"HELP DSN\" for details."+cr+
						"214 End of HELP info"+cr).getBytes();
					}
					else
						replyData=("504 Help topic \""+parm+"\" unknown"+cr).getBytes();
				}
				else
				if(cmd.equals("NOOP"))
					replyData=(S_250+cr).getBytes();
				else
				if(cmd.equals("HELO")
				||cmd.equals("EHLO"))
				{
					if(domain!=null)
						replyData=("503 "+sock.getLocalAddress().getHostName()+" Duplicate HELO/EHLO"+cr).getBytes();
					else	
					if(parm.trim().length()==0)
						replyData=("501 "+cmd+" requires domain address"+cr).getBytes();
					else
					{
						domain=parm;
						replyData=("250 "+sock.getLocalAddress().getHostName()+" Hello "+sock.getInetAddress().getHostName()+" ["+sock.getInetAddress().getHostAddress()+"], pleased to meet you"+cr).getBytes();
						if(cmd.equals("EHLO"))
						{
							replyData=(replyData.toString()
									  +"250-8BITMIME"+cr
									  +"250-SIZE 2000"+cr
									  +"250-DSN"+cr
									  +"250-ONEX"+cr
									  +"250-XUSR"+cr
									  +"250 HELP"+cr).getBytes();
						}
					}
				}
				else
				if(cmd.equals("MAIL"))
				{
					int x=parm.indexOf(":");
					if(x<0)
						replyData=("501 Syntax error in \""+parm+"\""+cr).getBytes();
					else
					{
						String to2=parm.substring(0,x).trim();
						if(!to2.equalsIgnoreCase("from"))
							replyData=("500 Unrecognized command \""+cmd+"\""+cr).getBytes();
						else
						{
							parm=parm.substring(x+1).trim();
							String parmparms="";
							boolean error=false;
							if(parm.startsWith("<"))
							{
								x=parm.indexOf(">");
								if(x<0)
								{
									replyData=("501 Syntax error in \""+parm+"\""+cr).getBytes();
									error=true;
								}
								else
								{
									parmparms=parm.substring(x+1).trim();
									parm=parm.substring(1,x);
								}
							}
							else
							if(parm.indexOf(" ")>=0)
							{
								replyData=("501 Syntax error in \""+parm+"\""+cr).getBytes();
								error=true;
							}
							if(parmparms.trim().length()>0)
								replyData=("502 Parameters not supported... \""+parmparms+"\""+cr).getBytes();
							else
							if(!error)
							{
								String name=validLocalAccount(parm);
								if(name==null)
								{
									if((++failures)==3)
									{
										replyData=("421 Quit Fishing!"+cr).getBytes();
										quitFlag=true;
									}
									else
										replyData=("551 Requested action not taken: User is not local."+cr).getBytes();
								}
								else
								{
									replyData=("250 OK "+name+cr).getBytes();
									from=name;
								}
							}
						}
					}
				}
				else
				if(cmd.equals("DATA"))
				{
					if(from==null)
						replyData=("503 Need MAIL command"+cr).getBytes();
					else
					if(to==null)
						replyData=("503 Need RCPT (recipient)"+cr).getBytes();
					else
					{
						replyData=("354 Enter mail, end with \".\" on a line by itself"+cr).getBytes();
						dataMode=true;
					}
				}
				else
				if(cmd.equals("RSET"))
				{
					replyData=("250 Reset state"+cr).getBytes();
					from=null;
					to=null;
					data=null;
				}
				else
				if(cmd.equals("QUIT"))
				{
					replyData=("221 "+server.domainName()+" closing connection"+cr).getBytes();
					quitFlag=true;
				}
				else
				if(cmd.equals("VRFY"))
					replyData=("252 Cannot VRFY user; try RCPT to attempt delivery (or try finger)"+cr).getBytes();
				else
				if(cmd.equals("EXPN"))
					replyData=("502 Sorry, we don't allow mailing lists"+cr).getBytes();
				else
				if(cmd.equals("VERB"))
					replyData=("502 Verbose unavailable"+cr).getBytes();
				else
				if(cmd.equals("ETRN"))
					replyData=("502 ETRN not implemented"+cr).getBytes();
				else
				if(cmd.equals("RCPT"))
				{
					if(from==null)
						replyData=("503 Need MAIL before RCPT"+cr).getBytes();
					else
					{
						int x=parm.indexOf(":");
						if(x<0)
							replyData=("501 Syntax error in \""+parm+"\""+cr).getBytes();
						else
						{
							String to2=parm.substring(0,x).trim();
							if(!to2.equalsIgnoreCase("to"))
								replyData=("500 Unrecognized command \""+cmd+"\""+cr).getBytes();
							else
							{
								parm=parm.substring(x+1).trim();
								String parmparms="";
								boolean error=false;
								if(parm.startsWith("<"))
								{
									x=parm.indexOf(">");
									if(x<0)
									{
										replyData=("501 Syntax error in \""+parm+"\""+cr).getBytes();
										error=true;
									}
									else
									{
										parmparms=parm.substring(x+1).trim();
										parm=parm.substring(1,x);
									}
								}
								else
								if(parm.indexOf(" ")>=0)
								{
									replyData=("501 Syntax error in \""+parm+"\""+cr).getBytes();
									error=true;
								}
								if(parmparms.trim().length()>0)
									replyData=("502 Parameters not supported... \""+parmparms+"\""+cr).getBytes();
								else
								if(parm.indexOf("@")<0)
									replyData=("550 "+parm+" user unknown."+cr).getBytes();
								else
								if(!error)
								{
									String name=validLocalAccount(parm);
									if(name==null)
									{
										if((++failures)==3)
										{
											replyData=("421 Quit Fishing!"+cr).getBytes();
											quitFlag=true;
										}
										else
											replyData=("553 Requested action not taken: User is not local."+cr).getBytes();
									}
									else
									{
										if(server.getAnEmailJournal(name)!=null)
										{
											boolean jerror=false;
											if(server.getJournalCriteria(name).length()>0)
											{
												MOB M=CMMap.getPlayer(from);
												if((M==null)||(!MUDZapper.zapperCheck(server.getJournalCriteria(name),M)))
												{
													replyData=("552 User '"+from+"' may not send emails to '"+name+"'."+cr).getBytes();
													jerror=true;
												}
											}
											
											if(!jerror)
											{
												replyData=("250 OK "+name+cr).getBytes();
												if(to==null) to=new Vector();
												if(!to.contains(name))
													to.addElement(name);
											}
										}
										else
										if(CMClass.DBEngine().DBCountJournal(server.mailboxName(),null,name)>=server.getMaxMsgs())
											replyData=("552 Mailbox '"+name+"' is full."+cr).getBytes();
										else
										{
											replyData=("250 OK "+name+cr).getBytes();
											if(to==null) to=new Vector();
											if(!to.contains(name))
												to.addElement(name);
										}
									}
								}
							}
						}
					}
				}
				else
					replyData=("500 Command Unrecognized: \""+cmd+"\""+cr).getBytes();
				
				
				if ((replyData != null))
				{
					// must insert a blank line before message body
					sout.write(replyData);
					sout.flush();
					replyData=null;
				}
			}

		}
		catch (java.net.SocketTimeoutException e2)
		{
			try
			{
				sout.write(("421 You're taking too long.  I'm outa here."+cr).getBytes());
				sout.flush();
			}
			catch(Exception e)
			{
				Log.errOut(getName(),"Exception2: " + e.getMessage() );
			}
		}
		catch (Exception e)
		{
Log.errOut(getName(),e);
			Log.errOut(getName(),"Exception: " + e.getMessage() );
		}
		
		try
		{
			if (sout != null)
			{
				sout.flush();
				sout.close();
				sout = null;
			}
		}
		catch (Exception e)	{}

		try
		{
			if (sin != null)
			{
				sin.close();
				sin = null;
			}
		}
		catch (Exception e)	{}

		try
		{
			if (sock != null)
			{
				sock.close();
				sock = null;
			}
		}
		catch (Exception e)	{}
	}

}
