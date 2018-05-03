package com.planet_ink.coffee_mud.Libraries.interfaces;

import java.io.IOException;
import java.net.UnknownHostException;

import com.planet_ink.coffee_mud.Libraries.SMTPclient;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.exceptions.BadEmailAddressException;
/*
   Copyright 2005-2018 Bo Zimmerman

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

public interface SMTPLibrary extends CMLibrary
{
	/** Default port number */
	public static final int DEFAULT_PORT = 25;
	/** network end of line */
	public static final String EOL = "\r\n";
	/** default timeout */
	public static final int DEFAULT_TIMEOUT=10000;

	public boolean isValidEmailAddress(String addy);
	public boolean emailIfPossible(String SMTPServerInfo, String from, String replyTo, String to, String subject, String message)
		throws IOException;
	public boolean emailIfPossible(String fromName, String toName, String subj, String msg);
	public void emailOrJournal(String SMTPServerInfo, String from, String replyTo, String to, String subject, String message);
	public SMTPClient getClient(String SMTPServerInfo, int port)  throws UnknownHostException, IOException;
	public SMTPClient getClient(String emailAddress) throws IOException, BadEmailAddressException;

	public static interface SMTPClient
	{
		public void sendMessage(String froaddress, String reply_address, String to_address, String mockto_address, String subject, String message)
			throws IOException;
	}
}
