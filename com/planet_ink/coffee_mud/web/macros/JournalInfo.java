package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


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
			info=ExternalPlay.DBReadJournal(last);
			httpReq.getRequestObjects().put("JOURNAL: "+last,info);
		}
		if(parms.containsKey("COUNT"))
			return ""+info.size();
		String lastlast=(String)httpReq.getRequestParameter("JOURNALMESSAGE");
		int num=0;
		if(lastlast!=null) num=Util.s_int(lastlast);
		if((num<0)||(num>=info.size()))	return " @break@";
		
		MOB M=Authenticate.getMOB(Authenticate.getLogin(httpReq));
		if(M==null)	return " @break@";
		String to=((String)((Vector)info.elementAt(num)).elementAt(3));
		if(M.isASysOp(null)||(to.equalsIgnoreCase("all"))||(to.equalsIgnoreCase(M.Name())))
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
