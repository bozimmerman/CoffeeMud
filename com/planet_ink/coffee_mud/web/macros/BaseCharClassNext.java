package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class BaseCharClassNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("BASECLASS");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.getRequestParameters().remove("BASECLASS");
			return "";
		}
		String lastID="";
		Vector baseClasses=new Vector();
		for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
		{
			CharClass C=(CharClass)c.nextElement();
			if((C.playerSelectable())||(parms.containsKey("ALL")))
			{
				if(!baseClasses.contains(C.baseClass()))
				   baseClasses.addElement(C.baseClass());
			}
		}
		for(int i=0;i<baseClasses.size();i++)
		{
			String C=(String)baseClasses.elementAt(i);
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!C.equals(lastID))))
			{
				httpReq.getRequestParameters().put("BASECLASS",C);
				return "";
			}
			lastID=C;
		}
		httpReq.getRequestParameters().put("BASECLASS","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}
}