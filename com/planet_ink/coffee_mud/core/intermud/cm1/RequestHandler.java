package com.planet_ink.coffee_mud.core.intermud.cm1;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class RequestHandler extends Thread
{
	private static AtomicInteger counter = new AtomicInteger();
	private BufferedReader br;
	private BufferedWriter bw;
	private Server myServer;
	private boolean shutdownRequested = false;
	private boolean isShutdown = false;
	private long idleTime = System.currentTimeMillis();
	public RequestHandler(Server server, Socket sock) throws IOException
	{
		super("CM1ReqHndler#"+counter.incrementAndGet());
		sock.setSoTimeout(30000);
		myServer=server;
		br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
	}
	
	public void sendGreeting(String msg) throws IOException 
	{
		bw.write(msg+"\n");
	}
	
	public void shutdown(Session S)
	{
		shutdownRequested = true;
		long time = System.currentTimeMillis();
		while((System.currentTimeMillis()-time<30000) && (!isShutdown))
		{
			this.interrupt();
			try {Thread.sleep(1000);}catch(Exception e){}
			if((br != null)&&(!isShutdown))
				try {br.close();}catch(Exception e){}
			if((bw != null)&&(!isShutdown))
				try {bw.close();}catch(Exception e){}
			try {Thread.sleep(1000);}catch(Exception e){}
		}
	}
	
	public boolean isShutdown()
	{
		return isShutdown;
	}
	
	public void run()
	{
		try
		{
			while(!shutdownRequested)
			{
				if((System.currentTimeMillis()-idleTime)> 300000)
					break;
				try
				{
					String s = br.readLine();
					bw.write("You said "+s);
				}
				catch(java.net.SocketTimeoutException se)
				{} // just eat it, our idle timer is our friend.
			}
		}
		catch(Throwable t)
		{
			Log.errOut("CM1ReqHndlr",t);
		}
		finally
		{
			isShutdown = true;
			try {br.close();}catch(Exception e){}
			try {bw.close();}catch(Exception e){}
			myServer.registerLostHandler(this);
		}
	}
}
