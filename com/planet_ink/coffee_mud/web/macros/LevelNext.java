package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class LevelNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("LEVEL");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.getRequestParameters().remove("LEVEL");
			return "";
		}
		if((last==null)||(last.length()>0))
		{
			int level=0;
			if(last!=null) level=Util.s_int(last);
			level++;
			if(level<26)
			{
				httpReq.getRequestParameters().put("LEVEL",""+level);
				return "";
			}
		}
		httpReq.getRequestParameters().put("LEVEL","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}
}