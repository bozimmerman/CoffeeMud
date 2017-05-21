package com.planet_ink.coffee_mud.core.intermud.i3.server;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
/**
 * com.planet_ink.coffee_mud.core.intermud.i3.server.ServerUser
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * The interface prescribing behaviour for a mudlib
 * user connection object.
 */

/**
 * The ServerUser interface prescribes behaviours which
 * must be defined by any user connection used with
 * the Imaginary JavaMud Server.  Specifically, it
 * requires that the user connection be able to handle
 * user input somehow.
 * Created: 27 September 1996
 * Last modified: 27 Septembet 1996
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 */
public interface ServerUser extends ServerObject {
	/**
	 * This method is triggered by the server when the
	 * user first connects.
	 */
	public abstract void connect();

	/**
	 * The server calls this method every server cycle.  The
	 * mudlib implementation is expected to be queueing up user
	 * input as it gets it (as opposed to processing it immediately
	 * as it comes across the net) for synchronicity's sake.
	 * The mudlib implementation therefore should use this method
	 * to pull a command off the queue and process it.
	 */
	public abstract void processInput();

	/**
	 * The server calls this method just after creating an instance
	 * of the mudlib user connection object that implements this
	 * interface.  Normally, the socket would be passed as an
	 * argument to the constructor.  Because the Server class does
	 * not know the name of the user connection implementation class
	 * at compile time, it has to use the Class.forName.newInstance()
	 * construct, which means the default constructor must be used.
	 * This method thus allows the server to pass the mudlib
	 * implementation the socket to use for communication with the
	 * client.
	 * @exception java.io.IOException thrown if a problem creating I/O streams occurs
	 * @param s the socket connected to the user's machine
	 * @see java.lang.Class#forName
	 * @see java.lang.Class#newInstance
	 */
	public abstract void setSocket(java.net.Socket s) throws java.io.IOException;
}
