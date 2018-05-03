package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2013-2018 Bo Zimmerman

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
public class CommandJournalInfo extends StdWebMacro
{
	@Override
	public String name()
	{
		return "CommandJournalInfo";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("COMMANDJOURNAL");
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("ALLFLAGS"))
		{
			for(final JournalsLibrary.CommandJournalFlags flag : JournalsLibrary.CommandJournalFlags.values())
				str.append("FLAG_"+flag.name()).append(", ");
		}
		else
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			final JournalsLibrary.CommandJournal C=CMLib.journals().getCommandJournal(last);
			if(C==null)
				return " @break@";
			if(parms.containsKey("ID"))
				str.append(C.NAME()).append(", ");
			if(parms.containsKey("NAME"))
				str.append(C.NAME()).append(", ");
			if(parms.containsKey("JOURNALNAME"))
				str.append(C.JOURNAL_NAME()).append(", ");
			if(parms.containsKey("MASK"))
				str.append(C.mask()).append(", ");
			if(parms.containsKey("FLAGSET"))
			{
				for(final JournalsLibrary.CommandJournalFlags flag : JournalsLibrary.CommandJournalFlags.values())
					httpReq.addFakeUrlParameter("FLAG_"+flag.name(), C.getFlag(flag)!=null?((C.getFlag(flag).length()==0)?"on":C.getFlag(flag)):"");
			}
			for(final JournalsLibrary.CommandJournalFlags flag : JournalsLibrary.CommandJournalFlags.values())
			{
				if(parms.containsKey("FLAG_"+flag.name().toUpperCase().trim()))
					str.append(C.getFlag(flag)!=null?((C.getFlag(flag).length()==0)?"on":C.getFlag(flag)):"").append(", ");
			}
		}
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return clearWebMacros(strstr);
	}
}
