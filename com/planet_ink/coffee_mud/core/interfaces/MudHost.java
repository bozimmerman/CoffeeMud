package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
 * The interface implemented by the main mud application.  Includes several timing constants.
 * @author Bo Zimmerman
 *
 */
public interface MudHost
{
	/** the number of milliseconds between each savethread execution */
	public final static long TIME_SAVETHREAD_SLEEP=60*60000; // 60 minutes, right now.
	/** the number of milliseconds between each utilithread execution */
	public final static long TIME_UTILTHREAD_SLEEP=10 * 60000;
	/** for multi-host systems, the thread code denoting the main/first mud host */
	public final static char MAIN_HOST='0';

	/**
	 * the hostname of the mud server
	 * @return hostname or ip address
	 */
	public String getHost();

	/**
	 * the port a given MUD server instance is listening on
	 * @return the port numbered listened on by this mud instance
	 */
	public int getPort();

	/**
	 * An order to permanently shutdown the entire mud system
	 * @param S a player session to send status messages to.  May be null.
	 * @param keepItDown true to shutdown, false to restart
	 * @param externalCommand if keepItDown is false, an external command to execute
	 */
	public void shutdown(Session S, boolean keepItDown, String externalCommand);

	/**
	 * Retrieve a string telling the status of mud startup or shutdown
	 * @return status of mud startup or shutdown
	 */
	public String getStatus();

	/**
	 * Retrieve the number of seconds since startup
	 * @return number of seconds since startup
	 */
	public long getUptimeSecs();

	/**
	 * Return any internal threads that are a source of troubles
	 * @return a list of threads that need service or killing
	 */
	public List<Runnable> getOverdueThreads();

	/**
	 * Return the viewable name of the language supported by this host.
	 * @return the language supported by this host.
	 */
	public String getLanguage();

	/**
	 * Flexible interface for tinkering with mud-host settings.
	 * Commands to be defined later, or now, or whatever.
	 * @param cmd space-delimited (parsable) command/parm list
	 * @return any return variables
	 * @throws java.lang.Exception any exceptions
	 */
	public String executeCommand(String cmd)
		throws Exception;

	/**
	 * Because thread groups are used to track configurations,
	 * here is a way to get it.
	 * @return the thread group this host was created under.
	 */
	public ThreadGroup threadGroup();

	/**
	 * Sets whether this mud is accepting connections
	 * @see com.planet_ink.coffee_mud.core.interfaces.MudHost#isAcceptingConnections()
	 * @param truefalse whether it is accepting connections
	 */
	public void setAcceptConnections(boolean truefalse);

	/**
	 * Handles a connection from a user, and internal states
	 * @param sock the socket the connection was made on
	 * @throws java.net.SocketException socket exceptions
	 * @throws java.io.IOException io exceptions
	 */
	public void acceptConnection(Socket sock) throws SocketException, IOException;

	/**
	 * Sets whether this mud is accepting connections
	 * @see com.planet_ink.coffee_mud.core.interfaces.MudHost#setAcceptConnections(boolean)
	 * @return true/false whether it is accepting connections
	 */
	public boolean isAcceptingConnections();
}
