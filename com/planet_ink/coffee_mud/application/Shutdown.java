package com.planet_ink.coffee_mud.application;
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
import java.net.*;
import java.io.*;
/*
   Copyright 2004-2025 Bo Zimmerman

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
public class Shutdown
{

	public Shutdown()
	{
		super();
	}

	public static void main(final String a[])
	{
		final PrintStream outStream = System.out;
		if(a.length<4)
		{
			outStream.println("Command usage: Shutdown <host> <port> <username> <password> (<true/false for reboot> <external command>)");
			return;
		}
		Socket sock=null;
		try
		{
			final StringBuffer msg=new StringBuffer("\033[1z<SHUTDOWN "+a[2]+" "+a[3]);
			if(a.length>=5)
				msg.append(" "+!(CMath.s_bool(a[4])));
			if(a.length>=6)
				for(int i=5;i<a.length;i++)
				msg.append(" "+a[i]);
			sock=new Socket(a[0],CMath.s_int(a[1]));
			final OutputStream rawout=sock.getOutputStream();
			rawout.write(CMStrings.strToBytes((msg.toString()+">\n")));
			rawout.flush();
			final BufferedReader in=new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String read="";
			while(!read.startsWith("\033[1z<"))
				read=in.readLine();
			outStream.println(read.substring("\033[1z<".length()));
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (sock != null)
				try
				{
					sock.close();
				}
				catch (final IOException e)
				{
				}
		}
	}
}
