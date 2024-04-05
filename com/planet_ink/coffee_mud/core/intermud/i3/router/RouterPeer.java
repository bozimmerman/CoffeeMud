package com.planet_ink.coffee_mud.core.intermud.i3.router;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistentPeer;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
/**
 * com.planet_ink.coffee_mud.core.intermud.i3.router.RouterPeer
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
 * The RouterPeer interface prescribes behaviours which
 * must be defined by any peer connection.
 * @author Bo Zimmerman
 * @version 1.0
 */
public interface RouterPeer extends ServerObject, PersistentPeer
{
	/**
	 * This method is triggered by the server when the
	 * peer first connects.
	 */
	public abstract void connect();

	/**
	 * Check if the peer is still connected
	 */
	public abstract boolean isConnected();

	/**
	 * The server calls this method just after connection.
	 *
	 * @exception java.io.IOException thrown if a problem creating I/O streams occurs
	 * @param s the socket connected to the peer
	 * @see java.lang.Class#forName
	 * @see java.lang.Class#newInstance
	 */
	public abstract void setSocket(java.net.Socket s) throws java.io.IOException;
}
