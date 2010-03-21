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
public class JournalInfo extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("JOURNAL");
		if(last==null) return " @break@";
		
		Vector info=(Vector)httpReq.getRequestObjects().get("JOURNAL: "+last);
		if(info==null)
		{
			info=CMLib.database().DBReadJournalMsgs(last);
			httpReq.getRequestObjects().put("JOURNAL: "+last,info);
		}
		MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if((CMLib.journals().isArchonJournalName(last))&&((M==null)||(!CMSecurity.isASysOp(M))))
		    return " @break@";
		
		if(parms.containsKey("COUNT"))
			return ""+info.size();
		String lastlast=httpReq.getRequestParameter("JOURNALMESSAGE");
		int num=0;
		if(lastlast!=null) num=CMath.s_int(lastlast);
		if((num<0)||(num>=info.size()))	return " @break@";
        boolean priviledged=CMSecurity.isAllowedAnywhere(M,"JOURNALS")&&(!parms.contains("NOPRIV"));
        JournalsLibrary.JournalEntry entry = (JournalsLibrary.JournalEntry)info.elementAt(num);
		String to=entry.to;
		if(to.equalsIgnoreCase("all")
        ||((M!=null)
           &&(priviledged
               ||to.equalsIgnoreCase(M.Name())
               ||(to.toUpperCase().trim().startsWith("MASK=")&&(CMLib.masking().maskCheck(to.trim().substring(5),M,true))))))
		{
			if(parms.containsKey("KEY"))
                return clearWebMacros(entry.key);
			else
			if(parms.containsKey("FROM"))
                return clearWebMacros(entry.from);
			else
			if(parms.containsKey("DATE"))
				return CMLib.time().date2String(entry.date);
			else
			if(parms.containsKey("TO"))
            {
                if(to.toUpperCase().trim().startsWith("MASK="))
                    to=CMLib.masking().maskDesc(to.trim().substring(5),true);
                return clearWebMacros(to);
            }
			else
			if(parms.containsKey("SUBJECT"))
                return clearWebMacros(entry.subj);
			else
			if(parms.containsKey("MESSAGE"))
			{
				String s=entry.msg;
				if(parms.containsKey("NOREPLIES"))
				{
					int x=s.indexOf(JournalsLibrary.JOURNAL_BOUNDARY);
					if(x>=0) s=s.substring(0,x);
				}
				if(parms.containsKey("PLAIN"))
				{
					s=CMStrings.replaceAll(s,"%0D","\n");
	                s=CMStrings.replaceAll(s,"<BR>","\n");
	                s=CMStrings.removeColors(s);
				}
				else
				{
					s=CMStrings.replaceAll(s,JournalsLibrary.JOURNAL_BOUNDARY,"<HR>");
					s=CMStrings.replaceAll(s,"%0D","<BR>");
	                s=CMStrings.replaceAll(s,"\n","<BR>");
	                s=colorwebifyOnly(new StringBuffer(s)).toString();
	                s=clearWebMacros(s);
				}
                return s;
			}
            if(parms.containsKey("EMAILALLOWED"))
                return ""+((entry.from.length()>0)
                        &&(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0));
		}
		return "";
	}
}
