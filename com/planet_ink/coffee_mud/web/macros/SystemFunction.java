package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class SystemFunction extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if(parms.get("ANNOUNCE")!=null)
		{
			String s=httpReq.getRequestParameter("TEXT");
			if((s!=null)&&(s.length()>0))
			{
				MOB M=((MOB)CMClass.sampleMOB().copyOf());
				Command C=CMClass.getCommand("Announce");
				try{
					C.execute(M,Util.parse("all "+s.trim()));
				}catch(Exception e){}
			}
		}
		if(parms.get("SHUTDOWN")!=null)
		{
com.planet_ink.coffee_mud.application.MUD.globalShutdown(null,(parms.get("RESTART")==null),null);
			return "";
		}
		return "";
	}
}
