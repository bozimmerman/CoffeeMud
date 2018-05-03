package com.planet_ink.coffee_mud.core.intermud.cm1;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.CMThreadFactory;
import com.planet_ink.coffee_mud.core.threads.CMThreadPoolExecutor;
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
import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/*
   Copyright 2010-2018 Bo Zimmerman

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
public class CM1Client
{
	private final String 	host;
	private final int 		port;
	private Socket 			sock	= null;
	private BufferedReader	br		= null;
	private BufferedWriter 	bw		= null;

	public CM1Client(String host, int port)
	{
		this.host=host;
		this.port=port;
	}

	public final static int s_int(final String INT)
	{
		try
		{ 
			return Integer.parseInt(INT); 
		}
		catch(final Exception e)
		{ 
			return 0;
		}
	}

	public synchronized List<String> transactMessages(String command)
	{
		final LinkedList<String> list=new LinkedList<String>();
		if(command.trim().length()==0)
			return list;
		try
		{
			if((command.indexOf('\n')<0)&&(command.indexOf('\r')<0))
				bw.write(command+"\n");
			else
			{
				bw.write("BLOCK\n");
				final String s=br.readLine();
				if(!s.startsWith("[OK /BLOCK:"))
					return list;
				final String eob=s.substring(11,s.length()-1);
				bw.write(command+eob);
			}
			String s=br.readLine();
			if(s==null)
				return list;
			if(s.startsWith("[OK "))
			{
				s=s.substring(4,s.length()-1);
				if(s.startsWith("/MESSAGES:"))
				{
					final int num=s_int(s.substring(9));
					for(int i=0;i<num;i++)
						list.add(br.readLine());
				}
				else
					list.add(s);
			}
			else
			if(s.startsWith("[FAIL "))
				list.add(s);
			else
			if(s.startsWith("[BLOCK "))
			{
				final String eob=s.substring(7,s.length()-1);
				final StringBuilder str=new StringBuilder("");
				while(!str.toString().endsWith(eob))
					str.append((char)br.read());
				list.add(str.toString().substring(0,str.length()-eob.length()));
			}
		}
		catch(final Exception e)
		{

		}
		return list;
	}

	public synchronized String transact(String command)
	{
		final List<String> list=transactMessages(command);
		if(list.size()==0)
			return "";
		return list.get(0);
	}

	public synchronized boolean connect()
	{
		try
		{
			sock=new Socket(host,port);
			br=new BufferedReader(new InputStreamReader(sock.getInputStream()));
			bw=new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			final long timeout=System.currentTimeMillis() + (3 * 1000);
			String s=br.readLine();
			while((System.currentTimeMillis()<timeout)
			&&(!s.startsWith("CONNECTED TO")))
				s=br.readLine();
			if(System.currentTimeMillis()>timeout)
				return true;
			bw.close();
			bw=null;
			br.close();
			br=null;
			sock.close();
			sock=null;
			return false;
		}
		catch(final Exception e)
		{
			return false;
		}
	}
}
