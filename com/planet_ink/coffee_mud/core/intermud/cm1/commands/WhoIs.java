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
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
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
   Copyright 2025-2025 Bo Zimmerman

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
public class WhoIs extends CM1Command
{
	@Override
	public String getCommandWord()
	{
		return "WHOIS";
	}

	public WhoIs(final RequestHandler req, final String parameters)
	{
		super(req, parameters);
	}

	@Override
	public void run()
	{
		try
		{
			if(!CMLib.players().playerExists(parameters))
				req.sendMsg("[FAIL UNKNOWN]");
			else
			{
				final ThinPlayer P =CMLib.players().getThinPlayer(parameters);
				if(P==null)
					req.sendMsg("[FAIL BROKWN]");
				else
				{
					final String response = P.level()+" "
							+P.gender()+" "
							+P.race()+" "
							+P.charClass();
					req.sendMsg("[OK " + response + "]");
				}
			}
		}
		catch (final Exception ioe)
		{
			Log.errOut(className, ioe);
			req.close();
		}
	}

	@Override
	public boolean passesSecurityCheck(final MOB user, final PhysicalAgent target)
	{
		return true;
	}

	@Override
	public String getHelp(final MOB user, final PhysicalAgent target, final String rest)
	{
		return "USAGE: WHOIS [NAME]: Basic character query.";
	}
}
