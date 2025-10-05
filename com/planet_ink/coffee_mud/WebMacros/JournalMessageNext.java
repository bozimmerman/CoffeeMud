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
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournalFlags;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2025 Bo Zimmerman

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
public class JournalMessageNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "JournalMessageNext";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String journalName=httpReq.getUrlParameter("JOURNAL");
		if(journalName==null)
			return " @break@";
		String last=httpReq.getUrlParameter("JOURNALMESSAGE");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
			{
				httpReq.removeUrlParameter("JOURNALMESSAGE");
				httpReq.removeUrlParameter("JOURNALCARDINAL");
			}
			return "";
		}

		final MOB M = Authenticate.getAuthenticatedMob(httpReq, httpResp);
		final Clan setClan=CMLib.clans().getClan(httpReq.getUrlParameter("CLAN"));
		final JournalsLibrary.ForumJournal journal=CMLib.journals().getForumJournal(journalName,setClan);
		if(journal==null)
			return " @break@";
		boolean authenticatedToRead = false;
		if((httpReq.getRequestObjects().get("AUTHENTICATED_JOURNAL")==null)
		||(!journalName.equals(httpReq.getRequestObjects().get("AUTHENTICATED_JOURNAL_NAME"))))
		{
			if (!journal.authorizationCheck(M, ForumJournalFlags.READ))
				httpReq.getRequestObjects().put("AUTHENTICATED_JOURNAL", new Object());
			else
			if(CMLib.journals().isArchonJournalName(journalName)
			&&((M==null)||(!CMSecurity.isASysOp(M))))
				httpReq.getRequestObjects().put("AUTHENTICATED_JOURNAL", new Object());
			else
				httpReq.getRequestObjects().put("AUTHENTICATED_JOURNAL", journal);
			httpReq.getRequestObjects().put("AUTHENTICATED_JOURNAL_NAME", journalName);
		}
		authenticatedToRead=httpReq.getRequestObjects().get("AUTHENTICATED_JOURNAL") == journal;
		if(parms.containsKey("AUTHCHECK"))
			return ""+authenticatedToRead;
		if (!authenticatedToRead)
			return " @break@";

		String srch=httpReq.getUrlParameter("JOURNALMESSAGESEARCH");
		if(srch!=null)
			srch=srch.toLowerCase();
		int cardinal=CMath.s_int(httpReq.getUrlParameter("JOURNALCARDINAL"));
		String page=httpReq.getUrlParameter("JOURNALPAGE");
		if(page==null)
			page=parms.get("JOURNALPAGE");
		if(page==null) // added to make linking to forums easier.  What will this break?
			page="0";
		int pageLimit;
		if(httpReq.isUrlParameter("JOURNALPAGELIMIT"))
			pageLimit = CMath.s_int(httpReq.getUrlParameter("JOURNALPAGELIMIT"));
		else
			pageLimit = CMProps.getIntVar(CMProps.Int.JOURNALLIMIT);
		if(parms.containsKey("PAGECARDINAL"))
		{
			if(pageLimit<=0)
				return "0";
			if((page!=null) && (page.length()>0))
			{
				final int x=CMStrings.countChars(page, ',');
				if(x <= 0)
					return "0";
				return Long.toString(x*pageLimit);
			}
			return "0";
		}
		if(parms.containsKey("LASTPAGE") || parms.containsKey("LASTPAGECARDINAL"))
		{
			if(pageLimit<=0)
				pageLimit=Integer.MAX_VALUE;
			@SuppressWarnings("unchecked")
			List<Long> pages = (List<Long>)httpReq.getRequestObjects().get("JOURNAL_"+journalName+"_ALL_PAGES_MINUS_ONE");
			if(pages == null)
			{
				pages = CMLib.database().DBReadJournalPages(journalName, null, null, pageLimit);
				for(int i=0;i<pages.size();i++)
					pages.set(i, Long.valueOf(pages.get(i).longValue()-1));
				httpReq.getRequestObjects().put("JOURNAL_"+journalName+"_ALL_PAGES_MINUS_ONE", pages);
			}
			if(pages.size()<2)
				return "";
			//pages.remove(pages.size()-1);
			if(parms.containsKey("LASTPAGE"))
			{
				final StringBuilder str=new StringBuilder("");
				for(final Long L : pages)
					str.append(L.longValue()).append(",");
				return str.substring(0,str.length()-1);
			}
			if(parms.containsKey("LASTPAGECARDINAL"))
				return Long.toString((pages.size()-1)*pageLimit);
			return "";
		}
		if(cardinal == 0)
		{
			String fMsgCardinal=httpReq.getUrlParameter("REPLYCARDINAL");
			if(fMsgCardinal != null)
			{
				if (fMsgCardinal.endsWith(","))
					fMsgCardinal = fMsgCardinal.substring(0, fMsgCardinal.length() - 1);
				final int x = fMsgCardinal.lastIndexOf(',');
				if (x > 0)
					fMsgCardinal = fMsgCardinal.substring(x + 1);
				cardinal = CMath.s_int(fMsgCardinal);
			}
		}
		cardinal++;
		JournalEntry entry = null;
		String mpage=httpReq.getUrlParameter("MESSAGEPAGE");
		if(mpage==null)
			mpage=parms.get("MESSAGEPAGE");
		if(mpage==null)
			mpage="0";
		final String parent=httpReq.getUrlParameter("JOURNALPARENT");
		final String dbsearch=httpReq.getUrlParameter("DBSEARCH");
		final List<JournalEntry> msgs=JournalInfo.getMessages(journalName,journal,page,mpage,parent,dbsearch,pageLimit, httpReq.getRequestObjects());
		if(parms.containsKey("REVERSE")
		&&(msgs.size()>1)
		&&(msgs.get(0).update()>msgs.get(msgs.size()-1).update()))
			Collections.reverse(msgs);
		while((entry==null)||(!CMLib.journals().canReadMessage(entry,srch,M,parms.containsKey("NOPRIV"))))
		{
			entry = JournalInfo.getNextEntry(msgs,last);
			if(entry==null)
			{
				httpReq.addFakeUrlParameter("JOURNALMESSAGE","");
				if(parms.containsKey("EMPTYOK"))
					return "<!--EMPTY-->";
				return " @break@";
			}
			last=entry.key();
		}
		entry.cardinal(cardinal);
		httpReq.addFakeUrlParameter("JOURNALCARDINAL",""+cardinal);
		httpReq.addFakeUrlParameter("JOURNALMESSAGE",last);
		return "";
	}
}
