package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AreaNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("AREA");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.getRequestParameters().remove("AREA");
			return "";
		}
		String lastID="";
		for(int a=0;a<CMMap.numAreas();a++)
		{
			Area A=(Area)CMMap.getArea(a);
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!A.name().equals(lastID))))
			{
				httpReq.getRequestParameters().put("AREA",A.name());
				if(!Sense.isHidden(A))
					return "";
				else
					last=A.name();
			}
			lastID=A.name();
		}
		httpReq.getRequestParameters().put("AREA","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}

}