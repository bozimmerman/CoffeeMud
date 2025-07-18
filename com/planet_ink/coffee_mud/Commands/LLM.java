package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ProtocolLibrary.LLMSession;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

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
public class LLM extends StdCommand
{
	public LLM()
	{
	}

	private final String[] access=I(new String[]{"LLM"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private final Map<String,LLMSession> sessions = new Hashtable<String,LLMSession>();
    @Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
    	if(mob.isMonster())
    		return false;
    	final String userText = CMParms.combineQuoted(commands,1);
    	final boolean reset = userText.equalsIgnoreCase("reset");
    	if(!sessions.containsKey(mob.Name()) || reset)
    	{
    		final LLMSession sess = CMLib.protocol().createLLMSession(Integer.valueOf(100));
    		if(sess != null)
    		{
    			sessions.put(mob.Name(), sess);
    			if(reset)
    			{
    				mob.tell(L("Reset done."));
    				return false;
    			}
    		}
    		else
    		{
    			mob.tell(L("Something went very wrong.  Check the mud.log file."));
    			return false;
    		}
    	}
    	mob.tell(sessions.get(mob.Name()).chat(userText));
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return (mob.playerStats()!=null) && (CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN));
	}

}
