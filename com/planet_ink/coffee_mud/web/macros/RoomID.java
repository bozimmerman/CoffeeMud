package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class RoomID extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		String last=httpReq.getRequestParameter("ROOM");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Room R=CMMap.getRoom(last);
			if(R!=null)
				return R.roomID();
		}
		return "";
	}
}
