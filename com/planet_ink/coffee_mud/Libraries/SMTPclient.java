package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import com.planet_ink.coffee_mud.core.exceptions.*;


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
public class SMTPclient extends StdLibrary implements SMTPLibrary, SMTPLibrary.SMTPClient
{
    public String ID(){return "SMTPclient";}

	/** Reply buffer */
    public BufferedReader reply = null;
	/** Send writer */
    public PrintWriter send = null;
	/** Socket to use */
    public Socket sock = null;
    
    private SMTPHostAuth auth = null;

	Attribute doMXLookup( String hostName ) 
	{
		try
		{
			Hashtable env = new Hashtable();
			env.put("java.naming.factory.initial",
			        "com.sun.jndi.dns.DnsContextFactory");
			DirContext ictx = new InitialDirContext( env );
			Attributes attrs = ictx.getAttributes( hostName, new String[] { "MX" });
			Attribute attr = attrs.get( "MX" );
			if( attr == null ) return( null );
			return( attr );
		}
		catch(javax.naming.NamingException x)
		{
		}
		return null;
	}
  
	public SMTPClient getClient(String SMTPServerInfo, int port) 
        throws UnknownHostException,IOException 
    {
        return new SMTPclient(SMTPServerInfo,port);
    }
    public SMTPClient getClient(String emailAddress) 
        throws IOException, BadEmailAddressException 
    {
        return new SMTPclient(emailAddress);
    }
    
    public SMTPclient()
    {
        super();
    }

    /**
     *   Create a SMTP object pointing to the specified host
     *   @param hostid The host to connect to.
     *   @exception UnknownHostException
     *   @exception IOException
     */
    public SMTPclient( String SMTPServerInfo, int port) throws UnknownHostException,IOException {
    	auth = new SMTPHostAuth(SMTPServerInfo);
        sock = new Socket( auth.getHost(), port );
		reply = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		sock.setSoTimeout(DEFAULT_TIMEOUT);
        send = new PrintWriter( sock.getOutputStream() );
        boolean debug = CMSecurity.isDebugging("SMTPCLIENT");
        String rstr = reply.readLine();
        if(debug) Log.debugOut("SMTPclient",rstr);
        if ((rstr==null)||(!rstr.startsWith("220"))) throw new ProtocolException(rstr);
        while (rstr.indexOf('-') == 3) {
            rstr = reply.readLine();
            if(debug) Log.debugOut("SMTPclient",rstr);
            if (!rstr.startsWith("220")) throw new ProtocolException(rstr);
        }
    }

	public SMTPclient (String emailAddress) throws IOException, 
												   BadEmailAddressException
	{
		int x=this.getEmailAddressError(emailAddress);
		if(x>=0) throw new BadEmailAddressException("Malformed email address");
		x=emailAddress.indexOf("@");
		String domain=emailAddress.substring(x+1).trim();
		Vector addys=new Vector();
		Attribute mx=doMXLookup(domain);
		boolean connected=false;
		try{
			if((mx!=null)&&(mx.size()>0))
			for(NamingEnumeration e=mx.getAll();e.hasMore();)
				addys.addElement(e.next());
		}
		catch(javax.naming.NamingException ne)
		{
		}
		if(addys.size()==0)
			addys.addElement(domain);
		for(Enumeration e=addys.elements();e.hasMoreElements();)
		{
			String hostid=(String)e.nextElement();
			int y=hostid.lastIndexOf(" ");
			if(y>=0) hostid=hostid.substring(y+1).trim();
			try
			{
				sock = new Socket( hostid, DEFAULT_PORT );
				reply = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				sock.setSoTimeout(DEFAULT_TIMEOUT);
				send = new PrintWriter( sock.getOutputStream() );
		        boolean debug = CMSecurity.isDebugging("SMTPCLIENT");
		        String rstr = reply.readLine();
		        if(debug) Log.debugOut("SMTPclient",rstr);
				if ((rstr==null)||(!rstr.startsWith("220"))) throw new ProtocolException(rstr);
				while (rstr.indexOf('-') == 3) {
				    rstr = reply.readLine();
			        if(debug) Log.debugOut("SMTPclient",rstr);
				    if (!rstr.startsWith("220")) throw new ProtocolException(rstr);
				}
				connected=true;
				break;
			}
			catch(Exception ex)
			{
				// just try the next one.
			}
		}
		if(!connected) throw new IOException("Unable to connect to '"+domain+"'.");
	}
	
	public boolean emailIfPossible(String from, MOB mob, String subj, String msg)
	{
		try
		{
	        SMTPLibrary.SMTPClient SC=null;
	        if(CMProps.getVar(CMProps.SYSTEM_SMTPSERVERNAME).length()>0)
	            SC=CMLib.smtp().getClient(CMProps.getVar(CMProps.SYSTEM_SMTPSERVERNAME),SMTPLibrary.DEFAULT_PORT);
	        else
	            SC=CMLib.smtp().getClient(mob.playerStats().getEmail());
	
	        String domain=CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase();
	        SC.sendMessage(from+"@"+domain,
	        			   from+"@"+domain,
	                       mob.playerStats().getEmail(),
	                       mob.playerStats().getEmail(),
	                       subj,
	                       CMLib.coffeeFilter().simpleOutFilter(msg));
	        return true;
		}
		catch(Exception e) {
			Log.errOut("SMTPClient",e.getMessage());
			return false;
		}
	}
	
	public boolean emailIfPossible(String SMTPServerInfo, 
    						       String from,
    						       String replyTo,
    						       String to,
    						       String subject,
    						       String message)
	{
		try
		{
			SMTPclient SC=null;
		    if(SMTPServerInfo.length()>0)
				SC=new SMTPclient(SMTPServerInfo,DEFAULT_PORT);
		    else
				SC=new SMTPclient(to);
		    
			SC.sendMessage(from,
						   replyTo,
						   to,
						   to,
						   subject,
						   message);
			return true;
		}
		catch(Exception ioe)
		{
		}
	    return false;
	}
	
	
	/** private constants for chars that are valid in email addy names */
	private final static String EMAIL_VALID_LOCAL_CHARS="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&'*+-/=?^_`{|}~.";
	/** private constants for chars that are valid in email addy domain names */
	private final static String EMAIL_VALID_DOMAIN_CHARS="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-.";

	/*
	 * Checks to see if the given string is a valid email address. 
	 * @param addy the email address
	 * @return true if all is good, false otherwise.
	 */
	public boolean isValidEmailAddress(String addy){ return getEmailAddressError(addy)<0;}
	
	/*
	 * Checks to see if the given string is a valid email address. 
	 * If it is not, it returns the location from 0-length+1 where
	 * the problem occurs.
	 * @param addy the email address
	 * @return -1, or the location of the error from 0-length+1
	 */
	private int getEmailAddressError(String addy)
	{
		if(addy==null) return 0;
		int x=addy.indexOf("@");
		if(x<0) return addy.length();
		String localPart=addy.substring(0,x).trim();
		String network=addy.substring(x+1);
		if(localPart.length()==0) return x;
		if(network.length()==0) return addy.length();
		if((localPart.startsWith("\"")&&localPart.endsWith("\"")))
		{
			int z=localPart.substring(1,localPart.length()-1).indexOf("\"");
			if(z>=0) return 1+z;
		}
		else
		for(int l=0;l<localPart.length();l++)
			if(EMAIL_VALID_LOCAL_CHARS.indexOf(localPart.charAt(l))<0)
				return l;
		if(localPart.startsWith(".")) return 0;
		if(localPart.endsWith(".")) return x-1;
		if(localPart.length()>64) return x;
		for(int l=0;l<network.length();l++)
			if(EMAIL_VALID_DOMAIN_CHARS.indexOf(network.charAt(l))<0)
				return x+1+l;
		if(network.startsWith("-")) return x+1;
		if(network.endsWith("-")) return addy.length();
		if(network.startsWith(".")) return x+1;
		if(network.length()>255) return addy.length();
		return -1;
	}
	
	/**
	* Send a message
	* 
	* <br><br><b>Usage:</b>  Mailer.sendmsg(S, From, To, Subject, Message);
	* @param froaddress  Address sending from
	* @param reply_address Address reply to 
	* @param to_address Address sending to 
	* @param mockto_address Address sending to 
	* @param subject Subject line
	* @param message Message content
	*/
    public synchronized void sendMessage(String froaddress, 
										 String reply_address,
										 String to_address, 
										 String mockto_address,
										 String subject, 
										 String message)
		throws IOException
	{
    	if(CMSecurity.isDisabled("SMTPCLIENT"))
    	{
    		Log.debugOut("SMTPclient", "Message not sent: "+froaddress+"/"+reply_address+"/"+to_address+"/"+mockto_address+"/"+subject+"/"+message);
    		return;
    	}
    	
		String rstr;
		String sstr;
		boolean debug = CMSecurity.isDebugging("SMTPCLIENT");
        StringBuffer fixMsg=new StringBuffer(message);
        for(int f=0;f<fixMsg.length();f++)
        {
            if((fixMsg.charAt(f)=='\n')
            &&(f>0)
            &&(fixMsg.charAt(f-1)!='\r'))
            {
                if((f<fixMsg.length()-1)&&(fixMsg.charAt(f+1)=='\r'))
                {
                    fixMsg.setCharAt(f,'\r');
                    fixMsg.setCharAt(f+1,'\n');
                }
                else
                {
                    fixMsg.insert(f,'\r');
                    f++;
                }
            }
            else
            if((fixMsg.charAt(f)=='\r')
            &&(f<fixMsg.length()-1)
            &&(fixMsg.charAt(f+1)!='\n'))
            {
                if((f>0)&&(fixMsg.charAt(f-1)=='\n'))
                {
                    fixMsg.setCharAt(f-1,'\r');
                    fixMsg.setCharAt(f,'\n');
                }
                else
                {
                    fixMsg.insert(f+1,'\n');
                    f++;
                }
            }
        }
        message=fixMsg.toString();

		InetAddress local;
		try {
		  local = InetAddress.getLocalHost();
		}
		catch (UnknownHostException ioe) {
		  System.err.println("No local IP address found - is your network up?");
		  throw ioe;
		}
		String host = local.getHostName();
		sstr="HELO " + host;
        if(debug) Log.debugOut("SMTPclient",sstr);
		send.print(sstr);
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
        if(debug) Log.debugOut("SMTPclient",rstr);
		if ((rstr==null)||(!rstr.startsWith("250"))) throw new ProtocolException(""+rstr);
		
		if((auth != null) && (auth.getAuthType().length()>0))
		{
			sstr="AUTH " + auth.getAuthType();
	        if(debug) Log.debugOut("SMTPclient",sstr);
			send.print(sstr);
			send.print(EOL);
			send.flush();
			rstr = reply.readLine();
	        if(debug) Log.debugOut("SMTPclient",rstr);
			if ((rstr==null)||(!rstr.startsWith("334"))) throw new ProtocolException(""+rstr);
			if(auth.getAuthType().equalsIgnoreCase("plain"))
			{
				sstr=auth.getPlainLogin();
		        if(debug) Log.debugOut("SMTPclient",sstr);
				send.print(sstr);
				send.print(EOL);
				send.flush();
			}
			else
			if(auth.getAuthType().equalsIgnoreCase("login"))
			{
				sstr=auth.getLogin();
		        if(debug) Log.debugOut("SMTPclient",sstr);
				send.print(sstr);
				send.print(EOL);
				send.flush();
				rstr = reply.readLine();
		        if(debug) Log.debugOut("SMTPclient",rstr);
				if ((rstr==null)||(!rstr.startsWith("334"))) throw new ProtocolException(""+rstr);
				sstr=auth.getPassword();
		        if(debug) Log.debugOut("SMTPclient",sstr);
				send.print(sstr);
				send.print(EOL);
				send.flush();
			}
			rstr = reply.readLine();
	        if(debug) Log.debugOut("SMTPclient",rstr);
			if ((rstr==null)||(!rstr.startsWith("235"))) throw new ProtocolException(""+rstr);
		}
		sstr = "MAIL FROM:<" + froaddress+">" ;
        if(debug) Log.debugOut("SMTPclient",sstr);
		send.print(sstr);
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
        if(debug) Log.debugOut("SMTPclient",rstr);
		if ((rstr==null)||(!rstr.startsWith("250"))) throw new ProtocolException(""+rstr);
		sstr = "RCPT TO:<" + to_address+">";
        if(debug) Log.debugOut("SMTPclient",sstr);
		send.print(sstr);
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
        if(debug) Log.debugOut("SMTPclient",rstr);
		if ((rstr==null)||(!rstr.startsWith("250"))) throw new ProtocolException(""+rstr);
		sstr="DATA";
        if(debug) Log.debugOut("SMTPclient",sstr);
		send.print(sstr);
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
        if(debug) Log.debugOut("SMTPclient",rstr);
		if ((rstr==null)||(!rstr.startsWith("354"))) throw new ProtocolException(""+rstr);
		sstr="MIME-Version: 1.0";
        if(debug) Log.debugOut("SMTPclient",sstr);
		send.print(sstr);
		send.print(EOL);
		sstr="Date: " + CMLib.time().date2SecondsString(System.currentTimeMillis());
        if(debug) Log.debugOut("SMTPclient",sstr);
		send.print(sstr);
		send.print(EOL);
		sstr="From: " + froaddress;
        if(debug) Log.debugOut("SMTPclient",sstr);
		send.print(sstr);
		send.print(EOL);
		sstr="Subject: " + subject;
        if(debug) Log.debugOut("SMTPclient",sstr);
		send.print(sstr);
		send.print(EOL);
		sstr="Sender: " + froaddress;
        if(debug) Log.debugOut("SMTPclient",sstr);
		send.print(sstr);
		send.print(EOL);
		sstr="Reply-To: " + reply_address;
        if(debug) Log.debugOut("SMTPclient",sstr);
		send.print(sstr);
		send.print(EOL);
		sstr="To: " + mockto_address;
		send.print(EOL);

		// Create Date - we'll cheat by assuming that local clock is right

		sstr="Date: " + CMLib.time().smtpDateFormat(System.currentTimeMillis());
        if(debug) Log.debugOut("SMTPclient",sstr);
		send.print(sstr);
		send.print(EOL);
		send.flush();

		// Warn the world that we are on the loose - with the comments header:
		//		send.print("Comment: Unauthenticated sender");
		//		send.print(EOL);
		//		send.print("X-Mailer: JNet SMTP");
		//		send.print(EOL);


		// Now send the message proper
		if(message!=null)
		{
			if((message.indexOf("<HTML>")>=0)&&(message.indexOf("</HTML>")>=0))
		    {
				//String BoundryString="---"+Math.random()+"_"+Math.random();
				//send.print("Content-Type: multipart/mixed; boundry="+BoundryString);
				//send.print(EOL);
				//send.print(BoundryString);
				//send.print(EOL);
				sstr="Content-Type: text/html";
		        if(debug) Log.debugOut("SMTPclient",sstr);
				send.print(sstr);
				send.print(EOL);
				
			}
			else
			{
				sstr="Content-Type: text/plain";
		        if(debug) Log.debugOut("SMTPclient",sstr);
				send.print(sstr);
				send.print(EOL);
			}
			// Sending a blank line ends the header part.
			send.print(EOL);
			send.print(message);
	        if(debug) Log.debugOut("SMTPclient",message);
		}
		send.print(EOL);
		sstr=".";
        if(debug) Log.debugOut("SMTPclient",sstr);
		send.print(sstr);
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
        if(debug) Log.debugOut("SMTPclient",rstr);
		if (!rstr.startsWith("250")) throw new ProtocolException(rstr);
    }

	/**
	* return members of a list on an email server.
	* 250-First Last <emailaddress>\r
	* 
	* <br><br><b>Usage:</b>  List=Mailer.getListMembers(List);
	* @param list member list
	* @return String List of members
	*/
    public synchronized String getListMembers( String list)
                         throws IOException, ProtocolException {

        String sendString;

        InetAddress local;
        try {
          local = InetAddress.getLocalHost();
        }
        catch (UnknownHostException ioe) {
          System.err.println("No local IP address found - is your network up?");
          throw ioe;
        }
        String host = local.getHostName();
        send.print("HELO " + host);
        send.print(EOL);
        send.flush();
        String rstr = reply.readLine();
        if (!rstr.startsWith("250")) throw new ProtocolException(rstr);
        sendString = "EXPN " + list ;
        send.print(sendString);
        send.print(EOL);
        send.flush();
		rstr="";
		try
		{
			while(true)
			{
				rstr+=reply.readLine();
				sock.setSoTimeout(1000);
			}
		}
		catch(java.io.InterruptedIOException x)
		{ // not really an error, just a control break			
		}
		sock.setSoTimeout(DEFAULT_TIMEOUT);
        if (!rstr.startsWith("250")) throw new ProtocolException(rstr);
		return rstr;
    }

	/**
	* close this socket
	* 
	* <br><br><b>Usage:</b>  this.close();
	*/
	public void close() {
      try {
        send.print("QUIT");
        send.print(EOL);
        send.flush();
        sock.close();
      }
      catch (IOException ioe) {
        // As though there's anything I can dof about it now...
      }
    }

    /**
	* close this socket
	* 
	* <br><br><b>Usage:</b>  finalize();
	*/
	public void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

	private class SMTPHostAuth
	{
		public SMTPHostAuth(String unparsedServerInfo)
		{
			Vector info=CMParms.parseCommas(unparsedServerInfo,false);
			if(info.size()==0) return;
			host = (String)info.remove(0);
			if((info.size()==0)||(host.length()==0)) return;
			String s=(String)info.elementAt(0);
			if(s.equalsIgnoreCase("plain")||s.equalsIgnoreCase("login"))
				authType=((String)info.remove(0)).toUpperCase().trim();
			else
				authType="PLAIN";
			if(info.size()==0){ authType=""; return;}
			login=(String)info.remove(0);
			if(info.size()==0) return;
			password=(String)info.remove(0);
		}
		private String host="";
		private String authType="";
		private String login="";
		private String password="";
		public String getHost(){ return host;}
		public String getAuthType(){ return authType;}
		
		public String getPlainLogin() 
		{
			byte[] buffer = new byte[2 + login.length() + password.length()];
			int bufDex=0;
			buffer[bufDex++]=0;
			for(int i=0;i<login.length();i++)
				buffer[bufDex++]=(byte)login.charAt(i);
			buffer[bufDex++]=0;
			for(int i=0;i<password.length();i++)
				buffer[bufDex++]=(byte)password.charAt(i);
			return B64Encoder.B64encodeBytes(buffer);
		}
		
		public String getLogin() 
		{
			byte[] buffer = new byte[login.length()];
			int bufDex=0;
			for(int i=0;i<login.length();i++)
				buffer[bufDex++]=(byte)login.charAt(i);
			return B64Encoder.B64encodeBytes(buffer);
		}
		
		public String getPassword() 
		{
			byte[] buffer = new byte[password.length()];
			int bufDex=0;
			for(int i=0;i<password.length();i++)
				buffer[bufDex++]=(byte)password.charAt(i);
			return B64Encoder.B64encodeBytes(buffer);
		}
	}
}
