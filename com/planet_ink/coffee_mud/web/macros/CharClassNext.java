package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class CharClassNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("CLASS");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.getRequestParameters().remove("CLASS");
			return "";
		}
		String lastID="";
		for(int c=0;c<CMClass.charClasses.size();c++)
		{
			CharClass C=(CharClass)CMClass.charClasses.elementAt(c);
			if((C.playerSelectable())||(parms.containsKey("ALL")))
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))))
				{
					httpReq.getRequestParameters().put("CLASS",C.ID());
					return "";
				}
				lastID=C.ID();
			}
		}
		httpReq.getRequestParameters().put("CLASS","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}
}