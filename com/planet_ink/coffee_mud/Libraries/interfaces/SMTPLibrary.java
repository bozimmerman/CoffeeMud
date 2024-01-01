package com.planet_ink.coffee_mud.Libraries.interfaces;

import java.io.IOException;
import java.net.UnknownHostException;

import com.planet_ink.coffee_mud.Libraries.SMTPclient;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.exceptions.BadEmailAddressException;
/*
   Copyright 2005-2024 Bo Zimmerman

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
/**
 * This library is an SMTP client, and probably won't remain a library
 * forever, as it really belongs in common.  Anyway, it handles the
 * sending of emails to far and exciting places.
 *
 * @author Bo Zimmerman
 *
 */
public interface SMTPLibrary extends CMLibrary
{
	/** Default port number */
	public static final int DEFAULT_PORT = 25;
	/** network end of line */
	public static final String EOL = "\r\n";
	/** default timeout */
	public static final int DEFAULT_TIMEOUT=10000;

	/**
	 * Checks the given string to see if it appears to be a valid email
	 * address.
	 *
	 * @param addy the string to check
	 * @return true if it looks emaily, false otherwise
	 */
	public boolean isValidEmailAddress(String addy);

	/**
	 * Attempts to connect to the given email server and send
	 * the given email to the given target address.
	 *
	 * @see SMTPLibrary#emailIfPossible(String, String, String, String)
	 * @see SMTPLibrary#emailOrJournal(String, String, String, String, String)
	 *
	 * @param SMTPServerInfo the server hostname to connect through, or null to attempt a direct transmission.
	 * @param from the from email address
	 * @param replyTo the reply-to email address
	 * @param to the target email address, which may be used as a direct-connect host also
	 * @param subject the subject of the message
	 * @param message the message itself
	 * @return true if the email was sent, false otherwise
	 * @throws IOException any I/O errors that occur
	 */
	public boolean emailIfPossible(String SMTPServerInfo, String from, String replyTo, String to, String subject, String message)
		throws IOException;

	/**
	 * Attempts to send an email to the given recipient, using the INI file smtp server
	 * if available, or directly if not.
	 *
	 * @see SMTPLibrary#emailIfPossible(String, String, String, String, String, String)
	 * @see SMTPLibrary#emailOrJournal(String, String, String, String, String)
	 *
	 * @param fromName the from email address
	 * @param toName the target email address, which may be used as a direct-connect host also
	 * @param subj the subject of the message
	 * @param msg the message itself
	 * @return if the email was sent, false otherwise
	 */
	public boolean emailIfPossible(String fromName, String toName, String subj, String msg);

	/**
	 * Sends an email or a journal message from a variety of argument formats.  Also appends
	 * the disclaimer, and the unsubscribe link if necessary.
	 *
	 * @see SMTPLibrary#emailIfPossible(String, String, String, String, String, String)
	 * @see SMTPLibrary#emailIfPossible(String, String, String, String)
	 *
	 * @param from from character name, or from email address
	 * @param replyTo reply to character name, or email address
	 * @param to target to character name, or email address
	 * @param subject the subject of the message
	 * @param message the main message
	 */
	public void emailOrJournal(String from, String replyTo, String to, String subject, String message);

	/**
	 * Builds an smtp client based on the given host/port info.
	 *
	 * @see SMTPLibrary#getClient(String)
	 *
	 * @param SMTPServerInfo the smtp server host with optional override port
	 * @param port the port for the smtp server
	 * @return the client object to send emails through
	 * @throws UnknownHostException the smtp host couldn't be found
	 * @throws IOException any I/O errors that occur
	 */
	public SMTPClient getClient(String SMTPServerInfo, int port)  throws UnknownHostException, IOException;

	/**
	 * Builds an smtp client based on the given email address for a target.
	 *
	 * @see SMTPLibrary#getClient(String, int)
	 *
	 * @param emailAddress the email address to extract the smtp server host from
	 * @return the client object to send emails through
	 * @throws BadEmailAddressException the email address was just bad
	 * @throws IOException any I/O errors that occur
	 */
	public SMTPClient getClient(String emailAddress) throws IOException, BadEmailAddressException;

	/**
	 * Interface for the underlying smtp client class, as opposed to tbe big bad library with
	 * its fancy abstract methods.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface SMTPClient
	{
		/**
		 * Sends the given email address through this client via its host.
		 *
		 * @param froaddress the from email address
		 * @param reply_address the reply-to email address or character name
		 * @param to_address the to target email address
		 * @param mockto_address the header-name target, maybe the charname@domain
		 * @param subject the subject of the message
		 * @param message the message itself
		 * @throws IOException any I/O errors that occur
		 */
		public void sendMessage(String froaddress, String reply_address, String to_address, String mockto_address, String subject, String message)
			throws IOException;
	}
}
