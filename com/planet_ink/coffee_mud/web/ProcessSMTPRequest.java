package com.planet_ink.coffee_mud.web;
import java.io.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.exceptions.*;

public class ProcessSMTPRequest extends Thread
{
	private INI page;
	private Socket sock;
	private static long instanceCnt = 0;
	private SMTPserver server=null;
	private final static String cr = "\r\n";
	private final static String S_250 = "250 OK";
	private String from=null;
	private String to=null;
	private String data=null;

	public ProcessSMTPRequest(Socket a_sock,
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
	
	public void run()
	{
		BufferedReader sin = null;
		DataOutputStream sout = null;

		byte[] replyData = null;

		try
		{
			sout = new DataOutputStream(sock.getOutputStream());
			sin=new BufferedReader(new InputStreamReader(sock.getInputStream()));
			sout.write(("220 ESMTP "+server.domainName()+" "+server.ServerVersionString+"; "+new IQCalendar().d2String()+cr).getBytes());
			boolean quitFlag=false;
			while(!quitFlag)
			{
				String s=sin.readLine();
				String cmd=s.toUpperCase();
				String parm="";
				int cmdindex=s.indexOf(" ");
				if(cmdindex>0)
				{
					cmd=s.substring(0,cmdindex);
					parm=s.substring(cmdindex+1);
				}
				
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
					replyData=(S_250.getBytes()+cr).getBytes();
				else
				if(cmd.equals("HELO"))
				{
				}
				else
				if(cmd.equals("EHLO"))
				{
				}
				else
				if(cmd.equals("MAIL"))
				{
				}
				else
				if(cmd.equals("DATA"))
				{
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
				{
				}
				else
				if(cmd.equals("EXPN"))
				{
				}
				else
				if(cmd.equals("VERB"))
					replyData=("502 Verbose unavailable"+cr).getBytes();
				else
				if(cmd.equals("ETRN"))
					replyData=("502 ETRN not implemented"+cr).getBytes();
				else
				if(cmd.equals("RCPT"))
				{
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
