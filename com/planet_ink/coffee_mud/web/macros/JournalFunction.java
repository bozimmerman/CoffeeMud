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
public class JournalFunction extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("JOURNAL");
		if(last==null) return "Function not performed -- no Journal specified.";
		Vector info=(Vector)httpReq.getRequestObjects().get("JOURNAL: "+last);
		if(info==null)
		{
			info=CMClass.DBEngine().DBReadJournal(last);
			httpReq.getRequestObjects().put("JOURNAL: "+last,info);
		}
		MOB M=CMMap.getLoadPlayer(Authenticate.getLogin(httpReq));
		String from="Unknown";
		if(M!=null) from=M.Name();
		if(parms.containsKey("NEWPOST"))
		{
			String to=httpReq.getRequestParameter("TO");
			if((to==null)||(M==null)||(to.equalsIgnoreCase("all"))) to="ALL";
			if(!to.equals("ALL"))
			{
				if(!CMClass.DBEngine().DBUserSearch(null,to))
					return "Post not submitted -- TO user does not exist.  Try 'All'.";
			}
			String subject=httpReq.getRequestParameter("SUBJECT");
			if(subject.length()==0)
				return "Post not submitted -- No subject!";
			String text=httpReq.getRequestParameter("NEWTEXT");
			if(text.length()==0)
				return "Post not submitted -- No text!";
			CMClass.DBEngine().DBWriteJournal(last,from,to,subject,text,-1);
			httpReq.getRequestObjects().remove("JOURNAL: "+last);
			return "Post submitted.";
		}
		String lastlast=httpReq.getRequestParameter("JOURNALMESSAGE");
		int num=0;
		if(lastlast!=null) num=Util.s_int(lastlast);
		if((num<0)||(num>=info.size()))
			return "Function not performed -- illegal journal message specified.";
		String to= ((String)((Vector)info.elementAt(num)).elementAt(3));
		if(CMSecurity.isAllowedAnywhere(M,"JOURNALS")||(to.equalsIgnoreCase(M.Name())))
		{
			if(parms.containsKey("DELETE"))
			{
				if(M==null)	return "Can not delete -- required logged in user.";
				CMClass.DBEngine().DBDeleteJournal(last,num);
				httpReq.addRequestParameters("JOURNALMESSAGE","");
				httpReq.getRequestObjects().remove("JOURNAL: "+last);
				return "Message #"+num+" deleted.";
			}
			else
			if(parms.containsKey("REPLY"))
			{
				String text=httpReq.getRequestParameter("NEWTEXT");
				if(text.length()==0)
					return "Reply not submitted -- No text!";
				CMClass.DBEngine().DBWriteJournal(last,from,"","",text,num);
				httpReq.getRequestObjects().remove("JOURNAL: "+last);
				return "Reply submitted";
			}
			else
				return "";
		}
		else
			return "You are not allowed to perform this function.";
	}
}
