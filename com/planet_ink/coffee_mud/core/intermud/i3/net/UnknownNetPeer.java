package com.planet_ink.coffee_mud.core.intermud.i3.net;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public class UnknownNetPeer implements NetPeer
{
	public Socket			sock;
	public DataInputStream	in;
	public DataOutputStream	out;
	public final long		connectTime	= System.currentTimeMillis();
	final long[]			timeoutCtr 	= new long[] {0};

	public UnknownNetPeer(final Socket sock)
	{
		this.sock = sock;
		if(sock != null)
		{
			try
			{
				this.in = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
				this.out = new DataOutputStream(sock.getOutputStream());
			}
			catch (final IOException e)
			{
			}
		}
	}

	public UnknownNetPeer(final NetPeer other)
	{
		super();
		this.sock = other.getSocket();
		this.in = other.getInputStream();
		this.out = other.getOutputStream();
		other.clearSocket();
	}

	@Override
	public Socket getSocket()
	{
		return sock;
	}

	@Override
	public boolean isConnected()
	{
		return (sock != null) && (sock.isConnected());
	}

	@Override
	public DataInputStream getInputStream()
	{
		return (isConnected()) ? in : null;
	}

	@Override
	public DataOutputStream getOutputStream()
	{
		return (isConnected()) ? out : null;
	}

	@Override
	public void clearSocket()
	{
		sock = null;
		in = null;
		out = null;
	}

	@Override
	public void close() throws IOException
	{
		if((sock != null)
		&&(isConnected()))
		{
			in.close();
			out.flush();
			out.close();
			sock = null;
			in = null;
			out = null;
		}

	}

	/**
	 * Return the identifying name
	 * @return the name
	 */
	@Override
	public String getName()
	{
		if(sock != null)
		{
			final SocketAddress addr = sock.getRemoteSocketAddress();
			if(addr != null)
				return addr.toString();
		}
		return this+"";
	}

	@Override
	public long getConnectTime()
	{
		return this.connectTime;
	}

	@Override
	public long[] getSockTimeout()
	{
		synchronized(this)
		{
			return timeoutCtr;
		}
	}
}
