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
public class NetPeer implements java.io.Closeable
{
	public Socket			sock;
	public DataInputStream	sockIn;
	public DataOutputStream	sockOut;
	public final long		connectTime	= System.currentTimeMillis();

	public NetPeer(final Socket sock)
	{
		this.sock = sock;
		if(sock != null)
		{
			try
			{
				this.sockIn = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
				this.sockOut = new DataOutputStream(sock.getOutputStream());
			}
			catch (final IOException e)
			{
			}
		}
	}

	public boolean isConnected()
	{
		return (sock != null) && (sock.isConnected());
	}

	public DataInputStream getInputStream()
	{
		return (isConnected()) ? sockIn : null;
	}

	public DataOutputStream getOutputStream()
	{
		return (isConnected()) ? sockOut : null;
	}

	@Override
	public void close() throws IOException
	{
		if((sock != null)
		&&(isConnected()))
		{
			sockIn.close();
			sockOut.flush();
			sockOut.close();
			sock = null;
			sockIn = null;
			sockOut = null;
		}

	}
}
