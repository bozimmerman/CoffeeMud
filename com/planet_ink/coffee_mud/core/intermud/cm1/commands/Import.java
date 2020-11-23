package com.planet_ink.coffee_mud.core.intermud.cm1.commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.intermud.cm1.RequestHandler;
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
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.io.*;
import java.util.concurrent.atomic.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class Import extends CM1Command
{
	@Override
	public String getCommandWord()
	{
		return "IMPORT";
	}

	public Import(final RequestHandler req, final String parameters)
	{
		super(req, parameters);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run()
	{
		try
		{
			final PhysicalAgent P=req.getTarget();
			if(P==null)
			{
				req.sendMsg("[FAIL NO TARGET]");
				return;
			}
			if(!isAuthorized(req.getUser(),P))
			{
				req.sendMsg("[FAIL UNAUTHORIZED]");
				return;
			}
			final String xml = parameters.trim();
			if((xml.length()<10)||(xml.indexOf('<')<0))
			{
				req.sendMsg("[FAIL NO XML]");
				return;
			}
			Vector<Object> V=new Vector<Object>();
			V.addAll(CMParms.parse("IMPORT NODELETE NOPROMPT"));
			V.add(new StringBuffer(xml));
			V.add(req.getTarget());
			final Command C=CMClass.getCommand("Import");
			try
			{
				V = (Vector<Object>)C.executeInternal(req.getUser(), 0, V.toArray(new Object[0]));
				final StringBuilder errors=new StringBuilder("");
				for(final Object O : V)
				{
					if(O instanceof String)
						errors.append((String)O).append("---");
				}
				if(errors.length()>0)
					req.sendMsg("[FAIL "+CMStrings.flatten(errors.toString().toUpperCase().replace('[', '.').replace(']', '.')+"]"));
				else
					req.sendMsg("[OK]");
			}
			catch (final Exception e)
			{
				req.sendMsg("[FAIL "+CMStrings.flatten(""+e.getMessage().toUpperCase().replace('[', '.').replace(']', '.')+"]"));
			}
		}
		catch(final java.io.IOException ioe)
		{
			Log.errOut(className,ioe);
			req.close();
		}
	}

	@Override
	public boolean passesSecurityCheck(final MOB user, final PhysicalAgent target)
	{
		if(user == null)
			return false;
		return CMSecurity.hasAccessibleDir(user,null);
	}

	@Override
	public String getHelp(final MOB user, final PhysicalAgent target, final String rest)
	{
		return "USAGE: "+getCommandWord()+" <XML>";
	}
}
