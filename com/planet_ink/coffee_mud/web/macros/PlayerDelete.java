package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class PlayerDelete extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		if(!httpReq.getMUD().gameStatusStr().equalsIgnoreCase("OK"))
			return httpReq.getMUD().gameStatusStr();

		String last=httpReq.getRequestParameter("PLAYER");
		if(last==null) return " @break@";
		MOB M=CMMap.getLoadPlayer(last);
		if(M==null) return " @break@";

		ExternalPlay.destroyUser(M);
		Log.sysOut("PlayerDelete","Someone destroyed the user "+M.Name()+".");
		return "";
	}
}
