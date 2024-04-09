package com.planet_ink.coffee_mud.core.intermud.i3.net;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Copyright (c) 2024-2024 Bo Zimmerman
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
 */
public interface NetPeer extends java.io.Closeable
{
	/**
	 * Check if the peer is still connected
	 */
	public boolean isConnected();

	/**
	 * For IPR communication
	 * @return the input stream
	 */
	public DataInputStream getInputStream();

	/**
	 * For IPR communication
	 * @return the output stream
	 */
	public DataOutputStream getOutputStream();

	/**
	 * Returns the socket.
	 * @return the socket.
	 */
	public Socket getSocket();

	/**
	 * Zeroes out the socket without
	 * closing it.  Prevents this object
	 * from being used for other operations
	 */
	public void clearSocket();

	/**
	 * Close the sockets
	 * @throws IOException if an error occurs
	 */
	@Override
	public void close() throws IOException;

	/**
	 * Returns when this was created.
	 * @return the timestamp of creation
	 */
	public long getConnectTime();

	/**
	 * Returns 1 dimensional array to use for
	 * detecting timeouts.
	 * @return the long array
	 */
	public long[] getSockTimeout();
}
