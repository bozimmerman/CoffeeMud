package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_web.util.CWThread;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.CommandJournal;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournal;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2010-2018 Bo Zimmerman

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

public class ForumJournalNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "ForumJournalNext";
	}

	public static MOB guestM = null;

	@SuppressWarnings("unchecked")

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
		MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(M==null)
		{
			if(guestM==null)
			{
				guestM=CMClass.getFactoryMOB();
				guestM.basePhyStats().setLevel(0);
				guestM.phyStats().setLevel(0);
				guestM.setName(("guest"));
			}
			M=guestM;
		}

		final Clan setClan=CMLib.clans().getClan(httpReq.getUrlParameter("CLAN"));
		List<String> journals;
		if((setClan!=null)&&(CMLib.journals().getClanForums(setClan)!=null))
		{
			journals=(List<String>)httpReq.getRequestObjects().get("JOURNALLIST_FOR_"+setClan.clanID());
			if(journals==null)
			{
				journals=new Vector<String>();
				final List<JournalsLibrary.ForumJournal> clanForumJournals=CMLib.journals().getClanForums(setClan);
				for (final ForumJournal CJ : clanForumJournals)
				{
					if((!journals.contains(CJ.NAME().toUpperCase()))
					&&(CMLib.masking().maskCheck(CJ.readMask(), M, true)))
						journals.add(CJ.NAME());
				}
				httpReq.getRequestObjects().put("JOURNALLIST_FOR_"+setClan.clanID(),journals);
			}
		}
		else
		{
			journals=(List<String>)httpReq.getRequestObjects().get("JOURNALLIST");
			if(journals==null)
			{
				journals=new Vector<String>();
				for(final Enumeration<JournalsLibrary.ForumJournal> e=CMLib.journals().forumJournals();e.hasMoreElements();)
				{
					final JournalsLibrary.ForumJournal CJ=e.nextElement();
					if((!journals.contains(CJ.NAME().toUpperCase()))
					&&(CMLib.masking().maskCheck(CJ.readMask(), M, true)))
						journals.add(CJ.NAME());
				}
				httpReq.getRequestObjects().put("JOURNALLIST",journals);
			}
		}
		String lastID="";
		final Set<String> H=CMLib.journals().getArchonJournalNames();
		boolean allForumJournals=false;
		if((Thread.currentThread() instanceof CWThread)
		&&CMath.s_bool(((CWThread)Thread.currentThread()).getConfig().getMiscProp("ADMIN"))
		&&parms.containsKey("ALLFORUMJOURNALS"))
			allForumJournals=true;

		for(int j=0;j<journals.size();j++)
		{
			final String B=journals.get(j);
			if((!allForumJournals)&&(H.contains(B.toUpperCase().trim()))&&((M==null)||(!CMSecurity.isASysOp(M))))
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
