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
   Copyright 2005-2025 Bo Zimmerman

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
public class MUDInfo extends CM1Command
{
	@Override
	public String getCommandWord()
	{
		return "MUDINFO";
	}

	public MUDInfo(final RequestHandler req, final String parameters)
	{
		super(req, parameters);
	}

	@Override
	public void run()
	{
		try
		{
			if((parameters.length()==0)||(parameters.equalsIgnoreCase("STATUS")))
				req.sendMsg("[OK "+CMProps.getVar(CMProps.Str.MUDSTATUS)+"]");
			else
			if(parameters.equalsIgnoreCase("PORTS"))
				req.sendMsg("[OK "+CMProps.getVar(CMProps.Str.ALLMUDPORTS)+"]");
			else
			if(parameters.equalsIgnoreCase("PORT"))
				req.sendMsg("[OK "+CMLib.host().getPublicPort()+"]");
			else
			if(parameters.equalsIgnoreCase("VERSION"))
				req.sendMsg("[OK "+CMProps.getVar(CMProps.Str.MUDVER)+"]");
			else
			if(parameters.equalsIgnoreCase("DOMAIN"))
				req.sendMsg("[OK "+CMProps.getVar(CMProps.Str.MUDDOMAIN)+"]");
			else
			if(parameters.equalsIgnoreCase("NAME"))
				req.sendMsg("[OK "+CMProps.getVar(CMProps.Str.MUD_NAME)+"]");
			else
			if(parameters.equalsIgnoreCase("WHO"))
				req.sendMsg("[OK "+getWhos()+"]");
			else
			if(parameters.equalsIgnoreCase("HOSTS"))
				req.sendMsg("[OK "+getHosts()+"]");
			else
				req.sendMsg("[FAIL "+getHelp(req.getUser(), null, null)+"]");
		}
		catch(final java.io.IOException ioe)
		{
			Log.errOut(className,ioe);
			req.close();
		}
	}

	protected String getHosts()
	{
		final List<String> list = new ArrayList<String>();
		for(final MudHost host : CMLib.hosts())
		{
			final char groupC = host.threadGroup().getName().charAt(0);
			final String name = CMProps.instance(groupC).getStr(CMProps.Str.MUD_NAME);
			if(!list.contains(name))
				list.add(name);
		}
		return CMParms.toListString(list);
	}

	protected String getWhos()
	{
		final StringBuilder str = new StringBuilder("");
		for(final Session S : CMLib.sessions().localOnlineIterable())
		{
			MOB mob2=S.mob();
			if((mob2!=null)&&(mob2.soulMate()!=null))
				mob2=mob2.soulMate();
			if((mob2!=null)
			&&(((!CMLib.flags().isCloaked(mob2))||(CMSecurity.isAllowedAnywhere(mob2,CMSecurity.SecFlag.CLOAK))))
			&&(mob2.basePhyStats().level()>0))
				str.append(mob2.name()).append(" ");
		}
		return str.toString().trim();
	}

	@Override
	public boolean passesSecurityCheck(final MOB user, final PhysicalAgent target)
	{
		return true;
	}

	@Override
	public String getHelp(final MOB user, final PhysicalAgent target, final String rest)
	{
		return "USAGE: MUDINFO STATUS, PORT, PORTS, VERSION, DOMAIN, NAME, HOSTS, WHO";
	}
}
