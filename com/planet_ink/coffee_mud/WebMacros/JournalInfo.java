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
   Copyright 2000-2005 Bo Zimmerman

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
			info=CMLib.database().DBReadJournal(last);
			httpReq.getRequestObjects().put("JOURNAL: "+last,info);
		}
		MOB M=null;
		if(JournalMessageNext.isProtectedJournal(last))
		{
			M=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if((M==null)||(!CMSecurity.isASysOp(M)))
			    return " @break@";
		}
		if(parms.containsKey("COUNT"))
			return ""+info.size();
		String lastlast=httpReq.getRequestParameter("JOURNALMESSAGE");
		int num=0;
		if(lastlast!=null) num=CMath.s_int(lastlast);
		if((num<0)||(num>=info.size()))	return " @break@";
		
		if(M==null)
			M=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
        boolean priviledged=CMSecurity.isAllowedAnywhere(M,"JOURNALS")&&(!parms.contains("NOPRIV"));
		String to=((String)((Vector)info.elementAt(num)).elementAt(3));
		if(to.equalsIgnoreCase("all")||((M!=null)&&(priviledged||(to.equalsIgnoreCase(M.Name())))))
		{
			if(parms.containsKey("KEY"))
                return clearWebMacros(((String)((Vector)info.elementAt(num)).elementAt(0)));
			else
			if(parms.containsKey("FROM"))
                return clearWebMacros(((String)((Vector)info.elementAt(num)).elementAt(1)));
			else
			if(parms.containsKey("DATE"))
				return CMLib.time().date2String(CMath.s_long((String)((Vector)info.elementAt(num)).elementAt(2)));
			else
			if(parms.containsKey("TO"))
                return clearWebMacros(to);
			else
			if(parms.containsKey("SUBJECT"))
                return clearWebMacros(((String)((Vector)info.elementAt(num)).elementAt(4)));
			else
			if(parms.containsKey("MESSAGE"))
			{
				String s=((String)((Vector)info.elementAt(num)).elementAt(5));
				s=CMStrings.replaceAll(s,"%0D","<BR>");
                s=CMStrings.replaceAll(s,"\n","<BR>");
                return clearWebMacros(s);
			}
            if(parms.containsKey("EMAILALLOWED"))
                return ""+((((String)((Vector)info.elementAt(num)).elementAt(1)).length()>0)
                        &&(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0));
		}
		return "";
	}
}
