package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;

public class NumPlayers extends StdWebMacro
{
	public String name()	{return "NumPlayers";}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		int numPlayers=0;
		for(int s=0;s<Sessions.size();s++)
		{
			Session S=Sessions.elementAt(s);
			if((S.mob()!=null)&&(Sense.isSeen(S.mob())))
			   numPlayers++;
		}
		return new Integer(numPlayers).toString();
	}

}