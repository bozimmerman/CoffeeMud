package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class JournalMessageNext extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public JournalsLibrary.JournalEntry getNextEntry(Vector<JournalsLibrary.JournalEntry> info, String key)
	{
		if(info==null)
			return null;
		for(Enumeration<JournalsLibrary.JournalEntry> e=info.elements();e.hasMoreElements();)
		{
			JournalsLibrary.JournalEntry entry = e.nextElement();
			if((key == null)||(key.length()==0))
				return entry;
			if(entry.key.equalsIgnoreCase(key))
			{
				if(e.hasMoreElements())
					return e.nextElement();
				return null;
			}
		}
		return null;
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String journalName=httpReq.getRequestParameter("JOURNAL");
		if(journalName==null) 
			return " @break@";
		
		if(CMLib.journals().isArchonJournalName(journalName))
		{
			MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if((M==null)||(!CMSecurity.isASysOp(M)))
			    return " @break@";
		}
		
		String page=httpReq.getRequestParameter("JOURNALPAGE");
		String parent=httpReq.getRequestParameter("JOURNALPARENT");
		if(parent==null) parent="";
		String dbsearch=httpReq.getRequestParameter("DBSEARCH");
		if(dbsearch==null) dbsearch="";
		
		Vector<JournalsLibrary.JournalEntry> info=(Vector<JournalsLibrary.JournalEntry>)httpReq.getRequestObjects().get("JOURNAL: "+journalName+": "+parent+": "+page);
		if(info==null)
		{
			if((page==null)||(page.length()==0))
				info=CMLib.database().DBReadJournalMsgs(journalName);
			else
			{
				int limit = CMProps.getIntVar(CMProps.SYSTEMI_JOURNALLIMIT);
				if(limit<=0) limit=Integer.MAX_VALUE;
				info=CMLib.database().DBReadJournalPageMsgs(journalName, parent, dbsearch, CMath.s_long(page), limit);
			}
			httpReq.getRequestObjects().put("JOURNAL: "+journalName+": "+parent+": "+page,info);
		}
        String srch=httpReq.getRequestParameter("JOURNALMESSAGESEARCH");
        if(srch!=null) 
        	srch=srch.toLowerCase();
		String last=httpReq.getRequestParameter("JOURNALMESSAGE");
		int cardinal=CMath.s_int(httpReq.getRequestParameter("JOURNALCARDINAL"));
		if(parms.containsKey("RESET"))
		{	
			if(last!=null)
			{
				httpReq.removeRequestParameter("JOURNALMESSAGE");
				httpReq.removeRequestParameter("JOURNALCARDINAL");
			}
			return "";
		}
		MOB M = Authenticate.getAuthenticatedMob(httpReq);
        cardinal++;
        JournalsLibrary.JournalEntry entry = null;
        while((entry==null)||(!CMLib.journals().canReadMessage(entry,srch,M,parms.contains("NOPRIV"))))
        {
        	entry = getNextEntry(info,last);
    		if(entry==null)
    		{
    			httpReq.addRequestParameters("JOURNALMESSAGE","");
    			if(parms.containsKey("EMPTYOK"))
    				return "<!--EMPTY-->";
    			return " @break@";
    		}
    		last=entry.key;
        }
        entry.cardinal=cardinal;
		httpReq.addRequestParameters("JOURNALCARDINAL",""+cardinal);
		httpReq.addRequestParameters("JOURNALMESSAGE",last);
		return "";
	}
}
