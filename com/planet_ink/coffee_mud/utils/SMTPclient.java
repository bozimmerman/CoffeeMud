package com.planet_ink.coffee_mud.web;
import java.net.*;
import java.io.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;


public class SMTPclient implements SMTPInterface
{
	/** Default port number */
    static final int DEFAULT_PORT = 25;
	/** network end of line */
    static final String EOL = "\r\n"; 
	/** default timeout */
	static final int DEFAULT_TIMEOUT=10000;

	/** Reply buffer */
    protected BufferedReader reply = null;
	/** Send writer */
    protected PrintWriter send = null;
	/** Socket to use */
    protected Socket sock = null;

    /**
     *   Create a SMTP object pointing to the specified host
     *   @param hostid The host to connect to.
     *   @exception UnknownHostException
     *   @exception IOException
     */
    public SMTPclient( String hostid) throws UnknownHostException, IOException {
        this(hostid, DEFAULT_PORT);
    }

	/** Main constructor that initialized  internal structures*/
    public SMTPclient( String hostid, int port) throws UnknownHostException,IOException {
        sock = new Socket( hostid, port );
		reply = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		sock.setSoTimeout(DEFAULT_TIMEOUT);
        send = new PrintWriter( sock.getOutputStream() );
        String rstr = reply.readLine();
        if ((rstr==null)||(!rstr.startsWith("220"))) throw new ProtocolException(rstr);
        while (rstr.indexOf('-') == 3) {
            rstr = reply.readLine();
            if (!rstr.startsWith("220")) throw new ProtocolException(rstr);
        }
    }

	/** Main constructor that initialized  internal structures*/
    public SMTPclient( InetAddress address ) throws IOException {
        this(address, DEFAULT_PORT);
    }

	/** Main constructor that initialized  internal structures*/
    public SMTPclient( InetAddress address, int port ) throws IOException {
        sock = new Socket( address, port );
		sock.setSoTimeout(DEFAULT_TIMEOUT);
		reply = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        send = new PrintWriter( sock.getOutputStream() );
        String rstr = reply.readLine();
        if (!rstr.startsWith("220")) throw new ProtocolException(rstr);
        while (rstr.indexOf('-') == 3) {
            rstr = reply.readLine();
            if (!rstr.startsWith("220")) throw new ProtocolException(rstr);
        }
    }
	
	/**
	* Send a message
	* 
	* <br><br><b>Usage:</b>  Mailer.sendmsg(S, From, To, Subject, Message);
	* @param S Session object
	* @param froaddress  Address sending from
	* @param to_address Address sending to 
	* @param subject Subject line
	* @param message Message content
	* @return NA
	*/
    public synchronized void sendMessage(String froaddress, String to_address, String subject, String message)
	throws IOException
	{
		String rstr;
		String sstr;

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
		rstr = reply.readLine();
		if (!rstr.startsWith("250")) throw new ProtocolException(rstr);
		sstr = "MAIL FROM: " + froaddress ;
		send.print(sstr);
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
		if (!rstr.startsWith("250")) throw new ProtocolException(rstr);
		sstr = "RCPT TO: " + to_address;
		send.print(sstr);
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
		if (!rstr.startsWith("250")) throw new ProtocolException(rstr);
		send.print("DATA");
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
		if (!rstr.startsWith("354")) throw new ProtocolException(rstr);
		send.print("MIME-Version: 1.0");
		send.print(EOL);
		send.print("From: " + froaddress);
		send.print(EOL);
		send.print("To: " + to_address);
		send.print(EOL);
		send.print("Subject: " + subject);
		send.print(EOL);

		// Create Date - we'll cheat by assuming that local clock is right

		send.print("Date: " + msgDateFormat(IQCalendar.getIQInstance()));
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
//				String BoundryString="---"+Math.random()+"_"+Math.random();
//				send.print("Content-Type: multipart/mixed; boundry="+BoundryString);
//				send.print(EOL);
//				send.print(BoundryString);
//				send.print(EOL);
				send.print("Content-Type: text/html");
				send.print(EOL);
				
			}
			else
			{
				send.print("Content-Type: text/plain");
				send.print(EOL);
			}
			// Sending a blank line ends the header part.
			send.print(EOL);
			send.print(message);
		}
		send.print(EOL);
		send.print(".");
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
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

        String returnString;
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
	* @param NA
	* @return NA
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
	* @param NA
	* @return NA
	*/
	protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

	/**
	* format the date
	* 
	* <br><br><b>Usage:</b>  msgDateFormat(IQCalendar.getIQInstance())
	* @param NA
	* @return NA
	*/
    private String msgDateFormat(IQCalendar senddate) {
        String formatted = "hold";

        String Day[] = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String Month[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul","Aug", "Sep", "Oct", "Nov", "Dec"};
		int dow=senddate.get(IQCalendar.DAY_OF_WEEK)-1;
		int date=senddate.get(IQCalendar.DAY_OF_MONTH);
		int m=senddate.get(IQCalendar.MONTH);
		int y=senddate.get(IQCalendar.YEAR);
		int h=senddate.get(IQCalendar.HOUR_OF_DAY);
		int min=senddate.get(IQCalendar.MINUTE);
		int s=senddate.get(IQCalendar.SECOND);
		int zof=senddate.get(IQCalendar.ZONE_OFFSET);
		int dof=senddate.get(IQCalendar.DST_OFFSET);

        formatted = Day[dow] + ", ";
        formatted = formatted + String.valueOf(date) + " ";
        formatted = formatted + Month[m] + " ";
        formatted = formatted + String.valueOf(y) + " ";
        if (h < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(h) + ":";
        if (min < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(min) + ":";
        if (s < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(s) + " ";
        if ((zof + dof) < 0)
            formatted = formatted + "-";
        else
            formatted = formatted + "+";
		
		zof=Math.round(zof/1000); // now in seconds
		zof=Math.round(zof/60); // now in minutes
		
		dof=Math.round(dof/1000); // now in seconds
		dof=Math.round(dof/60); // now in minutes
		
        if ((Math.abs(zof + dof)/60) < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(Math.abs(zof + dof)/60);
        if ((Math.abs(zof + dof)%60) < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(Math.abs(zof + dof)%60);

        return formatted;
    }
}
