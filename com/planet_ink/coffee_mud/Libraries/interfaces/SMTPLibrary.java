package com.planet_ink.coffee_mud.Libraries.interfaces;

import java.io.IOException;
import java.net.UnknownHostException;

import com.planet_ink.coffee_mud.Libraries.SMTPclient;
import com.planet_ink.coffee_mud.core.exceptions.BadEmailAddressException;

public interface SMTPLibrary
{
    /** Default port number */
    public static final int DEFAULT_PORT = 25;
    /** network end of line */
    public static final String EOL = "\r\n"; 
    /** default timeout */
    public static final int DEFAULT_TIMEOUT=10000;

	public boolean isValidEmailAddress(String addy);
    public boolean emailIfPossible(String SMTPServerName, String from, String replyTo, String to, String subject, String message)
        throws IOException;
    public SMTPClient getClient(String hostid, int port)  throws UnknownHostException, IOException;
    public SMTPClient getClient(String emailAddress) throws IOException, BadEmailAddressException;
    
    public static interface SMTPClient
    {
        public void sendMessage(String froaddress, String reply_address, String to_address, String mockto_address, String subject, String message)
            throws IOException;
    }
}
