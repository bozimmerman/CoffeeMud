package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AddRandomFile extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if((parms==null)||(parms.size()==0)) return "";
		StringBuffer buf=new StringBuffer("");
		int d=Dice.roll(1,parms.size(),0);
		String file=null;
		int i=0;
		boolean LINKONLY=false;
		for(Enumeration e=parms.elements();e.hasMoreElements();)
			if(((String)e.nextElement()).equalsIgnoreCase("LINKONLY"))
				LINKONLY=true;
		for(Enumeration e=parms.elements();e.hasMoreElements();)
		{
			file=(String)e.nextElement();
			if(file.equalsIgnoreCase("LINKONLY")) continue;
			if((++i)==d) break;
		}
		if((file!=null)&&(file.length()>0))
		{
			if(LINKONLY)
				buf.append(file);
			else
				buf.append(httpReq.getPageContent(file));
		}
		return buf.toString();
	}
}
