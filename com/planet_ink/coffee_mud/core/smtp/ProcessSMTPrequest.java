package com.planet_ink.coffee_mud.core.smtp;
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

import java.net.*;
import java.util.*;

import com.planet_ink.coffee_mud.core.exceptions.*;
import java.io.*;

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
public class ProcessSMTPrequest extends Thread
{
	private CMProps page;
	private Socket sock;
	private static long instanceCnt = 0;
	private SMTPserver server=null;
	private final static String cr = "\r\n";
	private final static String S_250 = "250 OK";
	protected String from=null;
	protected Vector to=null;
	private StringBuffer data=null;
	protected String domain=null;

	public ProcessSMTPrequest(Socket a_sock,
							  SMTPserver a_Server,
							  CMProps a_page)
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
					name=CMLib.database().DBEmailSearch(s);
					if(name!=null) return name;
				}
				return null;
			}
		}
		if(server.getAnEmailJournal(name)!=null)
			return server.getAnEmailJournal(name);
		if(server.mailboxName().length()>0)
		{
			if(CMLib.players().playerExists(name))
				return CMStrings.capitalizeAndLower(name);
		}
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
			sout.write(("220 ESMTP "+server.domainName()+" "+SMTPserver.ServerVersionString+"; "+CMLib.time().date2String(System.currentTimeMillis())+cr).getBytes());
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
					input.append(c);
					if(input.length()>server.getMaxMsgSize())
					{
						replyData=("552 String exceeds size limit. You are very bad!"+cr).getBytes();
						//Log.errOut("SMTPR","Long request from "+sock.getInetAddress());
						sout.write(replyData);
						sout.flush();
						quitFlag=true;
						s="";
					}
				}
				if(s!=null)
				{
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
	                    boolean translateEqualSigns=false;
						/*When the SMTP server accepts a message either for relaying or for final delivery, it inserts a trace record (also referred to interchangeably as a "time stamp line" or "Received" line) at the top of the mail data. This trace record indicates the identity of the host that sent the message, the identity of the host that received the message (and is inserting this time stamp), and the date and time the message was received.*/
						if(data.length()>=server.getMaxMsgSize())
							replyData=("552 Message exceeds size limit."+cr).getBytes();
						else
						{
							replyData=("250 Message accepted for delivery."+cr).getBytes();
							boolean startBuffering=false;
							StringBuffer finalData=new StringBuffer("");
							String subject=null;
	                        String boundry=null;
	                        // -1=waitForHeaderDone, 
	                        // 0=waitForFirstHeaderDone, 
	                        // 1=waitForBoundry, 
	                        // 2=waitForContentTypeConfirmation
	                        int boundryState=-1; 
							try
							{
								BufferedReader lineR=new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data.toString().getBytes())));
								while(true)
								{
									String s2=lineR.readLine();
									if(s2==null) break;
									String s2u=s2.toUpperCase();
									
									if((startBuffering)&&(boundry!=null)&&(s2.indexOf(boundry)>=0))
	                                    break; // we're done with a multipart!
	                                else
	                                if(startBuffering)
	                                {
	                                    boolean nextAppended=false;
	                                    if(translateEqualSigns)
	                                    {
	                                        if(s2.indexOf("=")>=0)
	                                        {
	                                            StringBuffer newStr=new StringBuffer(s2);
	                                            for(int c1=0;c1<newStr.length()-2;c1++)
	                                                if(newStr.charAt(c1)=='=')
	                                                {
	                                                    if(("0123456789ABCDEF".indexOf(Character.toUpperCase(newStr.charAt(c1+1)))>=0)
	                                                    &&("0123456789ABCDEF".indexOf(Character.toUpperCase(newStr.charAt(c1+2)))>=0))
	                                                    {
	                                                        int x=(16*("0123456789ABCDEF".indexOf(Character.toUpperCase(newStr.charAt(c1+1)))))
	                                                                 +"0123456789ABCDEF".indexOf(Character.toUpperCase(newStr.charAt(c1+2)));
	                                                        newStr.replace(c1,c1+3,""+((char)x));
	                                                    }
	                                                }
	                                            s2=newStr.toString();
	                                        }
	                                        if(s2.endsWith("="))
	                                        {
	                                            nextAppended=true;
	                                            s2=s2.substring(0,s2.length()-1);
	                                        }
	                                        else
	                                        if(s2.endsWith("=<BR>"))
	                                        {
	                                            nextAppended=true;
	                                            s2=s2.substring(0,s2.length()-5);
	                                        }
	                                    }
	                                    if(nextAppended)
	                                        finalData.append(s2);
	                                    else
	                                        finalData.append(s2+cr);
	                                }
									else
									if((s2.length()==0)&&(boundryState<0))
										startBuffering=true;
	                                else
	                                if((s2.length()==0)&&(boundryState==0))
	                                    boundryState=1;
	                                else
	                                if(boundryState==1)
	                                {
	                                    if(s2.indexOf(boundry)>=0)
	                                        boundryState=2;
	                                    continue;
	                                }
	                                else
									if((s2u.startsWith("SUBJECT: "))&&(boundryState<2))
										subject=s2.substring(9).trim();
									else
	                                if((s2u.startsWith("CONTENT-TRANSFER-ENCODING: "))
	                                ||(s2u.startsWith("CONTENT TRANSFER ENCODING: ")))
	                                {
	                                    if(s2u.substring(27).trim().startsWith("QUOTED-PRINTABLE")
	                                     ||s2u.substring(27).trim().startsWith("QUOTED PRINTABLE"))
	                                        translateEqualSigns=true;
	                                    else
	                                        translateEqualSigns=false;
	                                }
	                                else
									if((s2u.startsWith("CONTENT TYPE: ")||s2u.startsWith("CONTENT-TYPE: ")))
	                                {
	                                    if((boundryState<0)&&(s2u.substring(14).trim().startsWith("MULTIPART/")))
	                                    {
	                                        String contentType=s2.substring(14).trim();
	                                        int y=s2u.indexOf(";");
	                                        if(y>=0) contentType=s2.substring(14,y).trim();
	                                        int x=s2u.indexOf("BOUNDARY=");
	                                        for(int z=0;(z<5)&&(x<0);z++)
	                                        {
	                                            s2=lineR.readLine();
	                                            if(s2==null) break;
	                                            s2u=s2.toUpperCase();
	                                            x=s2u.indexOf("BOUNDARY=");
	                                        }
	                                        if(x<0)
	                                        {
	                                            replyData=("552 Message content type '"+contentType+"' not accepted without boundry."+cr).getBytes();
	                                            subject=null;
	                                            break;
	                                        }
	                                        if(s2!=null)
	                                        {
		                                        boundry=s2.substring(x+9).trim();
		                                        y=s2.indexOf(";");
		                                        if(y>=0) s2=s2.substring(0,y).trim();
		                                        if(boundry.startsWith("\"")&&boundry.endsWith("\""))
		                                            boundry=boundry.substring(1,boundry.length()-1).trim();
		                                        boundryState=0;
	                                        }
	                                    }
	                                    else
	    								if(!s2u.substring(14).trim().startsWith("TEXT/PLAIN"))
	    								{
	                                        if(boundryState==2)
	                                            boundryState=0;
	                                        else
	                                        {
	        									replyData=("552 Message content type '"+s2u.substring(14).trim()+"' not accepted."+cr).getBytes();
	        									subject=null;
	        									break;
	                                        }
	    								}
	                                    else
	                                    if(boundryState==2)
	                                        boundryState=-1;
	                                }
								}
							}
							catch(IOException e){}
								
							if((replyData!=null)&&(new String(replyData).startsWith("250")))
							{
								if((finalData.length()==0)&&(!startBuffering))
								{
									finalData=new StringBuffer(data.toString());
									if(subject==null) subject="";
								}
								
								if((finalData.length()>0) && (subject!=null))
								{
									if(subject.toUpperCase().startsWith("MOTD")
									||subject.toUpperCase().startsWith("MOTM")
									||subject.toUpperCase().startsWith("MOTY"))
									{
										MOB M=CMLib.players().getLoadPlayer(from);
										if((M==null)||(!CMSecurity.isAllowedAnywhere(M,"JOURNALS")))
											subject=subject.substring(4);
									}
									
									for(int i=0;i<to.size();i++)
									{
										String journal=server.getAnEmailJournal((String)to.elementAt(i));
										if(journal!=null)
										{
											String fdat=finalData.toString().trim();
											if((subject!=null)
											&&(!subject.trim().equalsIgnoreCase("subscribe"))
											&&(!subject.trim().equalsIgnoreCase("unsubscribe"))
											&&(!fdat.trim().equalsIgnoreCase("subscribe"))
											&&(!fdat.trim().equalsIgnoreCase("unsubscribe"))
											&&(server.isAForwardingJournal(journal)))
											{
												if(server.isASubscribeOnlyJournal(journal))
												{
													MOB M=CMLib.players().getLoadPlayer(from);
													if((M==null)||(!CMSecurity.isAllowedAnywhere(M,"JOURNALS")))
													{
														replyData=("552 Mailbox '"+journal+"' only accepts subscribe/unsubscribe."+cr).getBytes();
														break;
													}
												}
												else
												{
													Hashtable lists=server.getMailingLists(null);
													Vector mylist=null;
													if(lists!=null)	mylist=(Vector)lists.get(journal);
													if((mylist==null)||(!mylist.contains(from)))
													{
														replyData=("552 Mailbox '"+journal+"' only accepts messages from subscribers.  Send an email with 'subscribe' as the subject."+cr).getBytes();
														break;
													}
												}
											}
											   
											CMLib.database().DBWriteJournal(journal,
																			  from,
																			  "ALL",
																			  CMLib.coffeeFilter().fullInFilter(subject,false),
																			  CMLib.coffeeFilter().fullInFilter(fdat,false));
										}
										else
										{
											CMLib.database().DBWriteJournal(server.mailboxName(),
																			  from,
																			  (String)to.elementAt(i),
	                                                                          CMLib.coffeeFilter().fullInFilter(subject,false),
																			  CMLib.coffeeFilter().fullInFilter(finalData.toString(),false));
										}
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
							"214-This is "+SMTPserver.ServerVersionString+cr+
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
						if((domain!=null)&&(parm.trim().length()==0))
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
	                            {
	                                if((parmparms.trim().toUpperCase().startsWith("SIZE="))
	                                ||(!CMath.isNumber(parmparms.trim().toUpperCase().substring(5))))
	                                {
	                                    int size=CMath.s_int(parmparms.trim().toUpperCase().substring(5));
	                                    if(size>server.getMaxMsgSize())
	                                        replyData=("552 String exceeds size limit. But you were nice to tell me!"+cr).getBytes();
	                                }
	                                else
	    								replyData=("502 Parameters not supported... \""+parmparms+"\""+cr).getBytes();
	                            }
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
	                                {
	                                    if((parmparms.trim().toUpperCase().startsWith("SIZE="))
	                                    ||(!CMath.isNumber(parmparms.trim().toUpperCase().substring(5))))
	                                    {
	                                        int size=CMath.s_int(parmparms.trim().toUpperCase().substring(5));
	                                        if(size>server.getMaxMsgSize())
	                                            replyData=("552 String exceeds size limit. But you were nice to tell me!"+cr).getBytes();
	                                    }
	                                    else
	                                        replyData=("502 Parameters not supported... \""+parmparms+"\""+cr).getBytes();
	                                }
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
													MOB M=CMLib.players().getPlayer(from);
													if((M==null)
													||(!CMLib.masking().maskCheck(server.getJournalCriteria(name),M,false)))
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
											if(CMLib.database().DBCountJournal(server.mailboxName(),null,name)>=server.getMaxMsgs())
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
		}
		catch (java.net.SocketTimeoutException e2)
		{
			try
			{
				if (sout != null)
				{
					sout.write(("421 You're taking too long.  I'm outa here."+cr).getBytes());
					sout.flush();
				}
			}
			catch(Exception e)
			{
				Log.errOut(getName(),"Exception2: " + e.getMessage() );
			}
		}
		catch (Exception e)
		{
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
