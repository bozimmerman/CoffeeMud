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
public class JournalMessageNext extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String journal=httpReq.getRequestParameter("JOURNAL");
		if(journal==null) return " @break@";
		Vector info=(Vector)httpReq.getRequestObjects().get("JOURNAL: "+journal);
		if(info==null)
		{
			info=CMClass.DBEngine().DBReadJournal(journal);
			httpReq.getRequestObjects().put("JOURNAL: "+journal,info);
		}
		String last=httpReq.getRequestParameter("JOURNALMESSAGE");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("JOURNALMESSAGE");
			return "";
		}
		if(last==null) 
			last="0";
		else
		if(Util.s_int(last)>=info.size())
		{
			httpReq.addRequestParameters("JOURNALMESSAGE","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			else
				return " @break@";
		}
		else
			last=""+(Util.s_int(last)+1);
		
		httpReq.addRequestParameters("JOURNALMESSAGE",last);
		return "";
	}
}
