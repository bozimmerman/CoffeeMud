package com.planet_ink.coffee_web.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/*
   Copyright 2012-2018 Bo Zimmerman

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
 * A wrapper for a foreign host, port, and context
 * @author Bo Zimmerman
 *
 */
public class WebAddress
{
	private final String				host;
	private final int					port;
	private final InetSocketAddress		address;
	private final String				context;
	
	public WebAddress(String host, int port, String context) throws UnknownHostException
	{
		this.host=host;
		this.port=port;
		this.address=new InetSocketAddress(InetAddress.getByName(host), port);
		this.context=context;
	}

	/**
	 * @return the host
	 */
	public InetSocketAddress getAddress()
	{
		return address;
	}

	/**
	 * @return the context
	 */
	public String getContext()
	{
		return context;
	}
	
	/**
	 * 
	 * @return the host
	 */
	public String getHost()
	{
		return host;
	}
	
	/**
	 * 
	 * @return the port
	 */
	public int getPort()
	{
		return port;
	}
	
	/**
	 * 
	 * @return the host + port
	 */
	public String getAddressStr() 
	{ 
		return getHost()+":"+getPort();
	}
}
