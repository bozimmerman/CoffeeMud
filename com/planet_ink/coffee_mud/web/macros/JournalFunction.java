package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class JournalFunction extends StdWebMacro
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
			info=ExternalPlay.DBReadJournal(last);
			httpReq.getRequestObjects().put("JOURNAL: "+last,info);
		}
		if(parms.containsKey("NEWPOST"))
		{
			String from=Authenticate.getLogin(httpReq);
			if((from==null)||(from.length()==0))
				return "Post not submitted -- no FROM name (Noone logged in?)";
			String to=httpReq.getRequestParameter("TO");
			String subject=httpReq.getRequestParameter("SUBJECT");
			String text=httpReq.getRequestParameter("NEWTEXT");
			ExternalPlay.DBWriteJournal(last,from,to,subject,text,-1);
			return "";
		}
		String lastlast=(String)httpReq.getRequestObjects().get("JOURNALMESSAGE");
		int num=0;
		if(lastlast!=null) num=Util.s_int(lastlast);
		if((num<0)||(num>=info.size()))
			return " @break@";
		return "";
	}
}
