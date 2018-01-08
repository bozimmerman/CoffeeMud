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
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.CommandJournal;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class JournalNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "JournalNext";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("JOURNAL");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("JOURNAL");
			httpReq.getRequestObjects().remove("JOURNALLIST");
			return "";
		}

		List<String> journals=(List<String>)httpReq.getRequestObjects().get("JOURNALLIST");
		if(journals==null)
		{
			final List<String> rawJournals=CMLib.database().DBReadJournals();
			if(!rawJournals.contains("SYSTEM_NEWS"))
				rawJournals.add("SYSTEM_NEWS");
			for(final Enumeration e=CMLib.journals().commandJournals();e.hasMoreElements();)
			{
				final CommandJournal CJ=(CommandJournal)e.nextElement();
				if((!rawJournals.contains(CJ.NAME().toUpperCase()))
				&&(!rawJournals.contains(CJ.JOURNAL_NAME())))
					rawJournals.add(CJ.JOURNAL_NAME());
			}
			Collections.sort(rawJournals);
			journals=new Vector<String>();
			String s;
			for(final Iterator<String> i=rawJournals.iterator();i.hasNext();)
			{
				s=i.next();
				if(s.startsWith("SYSTEM_"))
				{
					journals.add(s);
					i.remove();
				}
			}
			journals.addAll(rawJournals);
			httpReq.getRequestObjects().put("JOURNALLIST",journals);
		}
		String lastID="";
		final Set<String> H=CMLib.journals().getArchonJournalNames();
		final MOB M = Authenticate.getAuthenticatedMob(httpReq);
		for(int j=0;j<journals.size();j++)
		{
			final String B=journals.get(j);
			if((H.contains(B.toUpperCase().trim()))&&((M==null)||(!CMSecurity.isASysOp(M))))
				continue;
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!B.equals(lastID))))
			{
				httpReq.addFakeUrlParameter("JOURNAL",B);
				return "";
			}
			lastID=B;
		}
		httpReq.addFakeUrlParameter("JOURNAL","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}

}
