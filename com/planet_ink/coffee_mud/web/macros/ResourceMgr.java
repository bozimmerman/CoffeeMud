package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class ResourceMgr extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("RESOURCE");
		if(parms.containsKey("RESET"))
		{
			if(last!=null) httpReq.getRequestParameters().remove("RESOURCE");
			return "";
		}
		else
		if(parms.containsKey("NEXT"))
		{
			String lastID="";
			Vector V=Resources.findResourceKeys("");
			for(int i=0;i<V.size();i++)
			{
				String key=(String)V.elementAt(i);
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!key.equals(lastID))))
				{
					httpReq.getRequestParameters().put("RESOURCE",key);
					return "";
				}
				lastID=key;
			}
			httpReq.getRequestParameters().put("RESOURCE","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			else
				return " @break@";
		}
		else
		if(parms.containsKey("DELETE"))
		{
			String key=(String)httpReq.getRequestParameters().get("RESOURCE");
			if((key!=null)&&(Resources.getResource(key)!=null))
			{
				Resources.removeResource(key);
				return "Resource '"+key+"' deleted.";
			}
			return "<!--EMPTY-->";
		}
		else
		if(last!=null)
			return last;
		return "<!--EMPTY-->";
	}
	
}
