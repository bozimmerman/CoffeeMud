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
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
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
import java.util.Map.Entry;
import java.util.concurrent.atomic.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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
public class GetSys extends CM1Command
{
	@Override 
	public String getCommandWord()
	{ 
		return "GETSYS";
	}
	
	public GetSys(RequestHandler req, String parameters)
	{
		super(req, parameters);
	}

	@Override
	public void run()
	{
		try
		{
			String type = parameters.toUpperCase().trim();
			if(type.startsWith("PROP "))
			{
				type = type.substring(5).trim();
				if(CMProps.isPropName(type))
					req.sendMsg("[OK "+CMProps.getProp(type)+"]");
				else
					req.sendMsg("[FAIL NO PROP "+type+"]");
			}
			else
			if(type.startsWith("DEBUG "))
			{
				type = type.substring(6).trim();
				if(CMParms.indexOfIgnoreCase(CMSecurity.DbgFlag.values(), type)>=0)
					req.sendMsg("[OK "+CMSecurity.isDebuggingSearch(type)+"]");
				else
					req.sendMsg("[FAIL NO DEBUG "+type+"]");
			}
			else
			if(type.startsWith("DISABLE "))
			{
				type = type.substring(8).trim();
				if(CMParms.indexOfIgnoreCase(CMSecurity.DisFlag.values(), type)>=0)
					req.sendMsg("[OK "+CMSecurity.isAnyFlagDisabled(type)+"]");
				else
					req.sendMsg("[FAIL NO DISABLE "+type+"]");
			}
			else
			if(type.startsWith("ENABLE "))
			{
				req.sendMsg("[FAIL NO ENABLE "+type+"]");
			}
			else
			{
				if(CMProps.isPropName(type))
					req.sendMsg("[OK "+CMProps.getProp(type)+"]");
				else
				if(CMParms.indexOfIgnoreCase(CMSecurity.DbgFlag.values(), type)>=0)
					req.sendMsg("[OK "+CMSecurity.isDebuggingSearch(type)+"]");
				else
				if(CMParms.indexOfIgnoreCase(CMSecurity.DisFlag.values(), type)>=0)
					req.sendMsg("[OK "+CMSecurity.isAnyFlagDisabled(type)+"]");
				else
					req.sendMsg("[FAIL NO SYS "+type+"]");
			}
		}
		catch(final Exception ioe)
		{
			Log.errOut(className,ioe);
			req.close();
		}
	}

	@Override
	public boolean passesSecurityCheck(MOB user, PhysicalAgent target)
	{
		return ((user != null) && (CMSecurity.isASysOp(user)));
	}
	
	@SuppressWarnings("rawtypes")
	protected String getAllProps()
	{
		StringBuilder str= new StringBuilder("PROP [?], DEBUG [?], DISABLE [?], ENABLE [?] ");
		for(Class<? extends Enum> c : CMProps.PROP_CLASSES)
		{
			str.append(CMParms.toListString(c.getEnumConstants()));
			str.append(", ");
		}
		str.append(CMParms.toListString(CMSecurity.DbgFlag.class.getEnumConstants()));
		str.append(", ");
		str.append(CMParms.toListString(CMSecurity.DisFlag.class.getEnumConstants()));
		str.append(", ");
		return str.toString().substring(0,str.length()-2);
	}
	
	@Override
	public String getHelp(MOB user, PhysicalAgent target, String rest)
	{
		return "USAGE: "+getCommandWord()+" "+getAllProps();
	}
}
