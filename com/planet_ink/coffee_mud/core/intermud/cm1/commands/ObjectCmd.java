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
public class ObjectCmd extends CM1Command
{
	@Override
	public String getCommandWord()
	{
		return "OBJECT";
	}

	public ObjectCmd(final RequestHandler req, final String parameters)
	{
		super(req, parameters);
	}

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
			final String startXML=xml.substring(0,10).toUpperCase();

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
		final String word=CMLib.english().getFirstWord(rest==null?"":rest).toUpperCase().trim();
		if (word.equals("READ"))
			return "USAGE: "+getCommandWord()+" READ \"<FILENAME>\": returns contents of the file or directory as a block.";
		else
		if (word.equals("DELETE"))
			return "USAGE: "+getCommandWord()+" DELETE \"<FILENAME>\": deletes the file or directory (if empty).";
		else
		if (word.equals("WRITE"))
			return "USAGE: "+getCommandWord()+" WRITE \"<FILENAME>\" <CONTENT>: creates or overwrites file.";
		else
		if (word.equals("READB64"))
			return "USAGE: "+getCommandWord()+" READB64 \"<FILENAME>\": returns contents of the file as a block of b64 encoded data.";
		else
		if (word.equals("WRITEB64"))
			return "USAGE: "+getCommandWord()+" WRITEB64 \"<FILENAME>\" <CONTENT>: creates or overwrites file with b64 encoded data.";
		else
		if (word.equals("LENGTH"))
			return "USAGE: "+getCommandWord()+" LENGTH \"<FILENAME>\": returns length of the file.";
		else
		if (word.equals("AUTHOR"))
			return "USAGE: "+getCommandWord()+" AUTHOR \"<FILENAME>\": returns author of the file.";
		else
		if (word.equals("LASTMODIFIED"))
			return "USAGE: "+getCommandWord()+" LASTMODIFIED \"<FILENAME>\": returns last modified date/time of the file.";
		else
			return "USAGE: "+getCommandWord()+" READ, READB64, WRITE, WRITEB64, LENGTH, AUTHOR, LASTMODIFIED";
	}
}
