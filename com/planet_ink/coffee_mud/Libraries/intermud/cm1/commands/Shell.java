package com.planet_ink.coffee_mud.Libraries.intermud.cm1.commands;
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
import com.planet_ink.coffee_mud.Libraries.intermud.cm1.RequestHandler;
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
   Copyright 2026-2026 Bo Zimmerman

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
public class Shell extends CM1Command
{
	@Override
	public String getCommandWord()
	{
		return "SHELL";
	}

	public Shell(final RequestHandler req, final String parameters)
	{
		super(req, parameters);
	}

	@Override
	public void run()
	{
		try
		{
			Command shellC = CMClass.getCommand("Shell");
			String eob="/BLOCK:"+Math.random();
			String data = (String)shellC.executeInternal(req.getUser(), MUDCmdProcessor.METAFLAG_FORCED, CMParms.parse("SHELL "+parameters).toArray());
			data = CMStrings.removeColors(data);
			while(data.indexOf(eob)>=0)
				eob="/BLOCK:"+Math.random();
			req.sendMsg("[BLOCK "+eob+"]");
			req.sendMsg(data+eob);
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
		return "USAGE: "+getCommandWord()+" -shell command-";
	}
}
