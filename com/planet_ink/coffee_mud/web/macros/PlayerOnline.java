package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class PlayerOnline extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String ban(String banMe)
	{
		Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini"));
		if((banned!=null)&&(banned.size()>0))
		for(int b=0;b<banned.size();b++)
		{
			String B=(String)banned.elementAt(b);
			if(B.equals(banMe))
				return "false";
		}
		StringBuffer str=Resources.getFileResource("banned.ini");
		str.append(banMe+"\n\r");
		Resources.saveFileResource("banned.ini");
		return "true";
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		String last=httpReq.getRequestParameter("PLAYER");
		Hashtable parms=parseParms(parm);
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			MOB M=PlayerNext.getMOB(last);
			if(M!=null)
			{
				if(parms.containsKey("BANBYNAME"))
					ban(last);
				
				if(M.session()!=null)
				{
					if(parms.containsKey("BOOT"))
					{
						M.session().setKillFlag(true);
						return "false";
					}
					else
					if(parms.containsKey("BANBYIP"))
						ban(M.session().getAddress());
					return "true";
				}
			}
		}
		return "false";
	}
}
