package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


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
			info=ExternalPlay.DBReadJournal(journal);
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
		
		String user=Authenticate.getLogin(httpReq);
		if((user==null)||(user.length()==0))
			return "Read Not Allowed -- no LOGIN name (Noone logged in?)";
		MOB M=Authenticate.getMOB(user);
		if(M==null)
			return "Read Not Allowed -- no LOGIN MOB (Noone logged in?)";
		
		httpReq.addRequestParameters("JOURNALMESSAGE",last);
		return "";
	}
}
