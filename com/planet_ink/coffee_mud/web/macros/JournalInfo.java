package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


/* 
   Copyright 2000-2004 Bo Zimmerman

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
			info=CMClass.DBEngine().DBReadJournal(last);
			httpReq.getRequestObjects().put("JOURNAL: "+last,info);
		}
		if(parms.containsKey("COUNT"))
			return ""+info.size();
		String lastlast=httpReq.getRequestParameter("JOURNALMESSAGE");
		int num=0;
		if(lastlast!=null) num=Util.s_int(lastlast);
		if((num<0)||(num>=info.size()))	return " @break@";
		
		MOB M=CMMap.getLoadPlayer(Authenticate.getLogin(httpReq));
		
		String to=((String)((Vector)info.elementAt(num)).elementAt(3));
		if(to.equalsIgnoreCase("all")||((M!=null)&&(CMSecurity.isAllowedAnywhere(M,"JOURNALS")||(to.equalsIgnoreCase(M.Name())))))
		{
			if(parms.containsKey("KEY"))
				return ((String)((Vector)info.elementAt(num)).elementAt(0));
			else
			if(parms.containsKey("FROM"))
				return ((String)((Vector)info.elementAt(num)).elementAt(1));
			else
			if(parms.containsKey("DATE"))
				return IQCalendar.d2String(Util.s_long((String)((Vector)info.elementAt(num)).elementAt(2)));
			else
			if(parms.containsKey("TO"))
				return to;
			else
			if(parms.containsKey("SUBJECT"))
				return ((String)((Vector)info.elementAt(num)).elementAt(4));
			else
			if(parms.containsKey("MESSAGE"))
			{
				String s=((String)((Vector)info.elementAt(num)).elementAt(5));
				s=Util.replaceAll(s,"%0D","<BR>");
				return s;
			}
		}
		return "";
	}
}
