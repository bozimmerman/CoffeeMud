package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class PlayerList extends StdWebMacro
{
	public String name()	{return "PlayerList";}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		StringBuffer s = new StringBuffer("");
		for(int i=0;i<Sessions.size();i++)
		{
			Session session=Sessions.elementAt(i);
			// list entry with style sheet class
			s.append("<li class=\"cmPlayerListEntry");
			MOB m = session.mob();
			if((m!=null)&&(!Sense.isSeen(m))) continue;
			if((m!=null)&&(m.soulMate()!=null))
				m=m.soulMate();

			if ( (m!=null) && (m.name() != null)
				&& (m.name().length() > 0) )
			{
				// jef: nb - only shows full sysops, not subops
				if ( m.isASysOp(null) )
					s.append("Archon");
				s.append("\">");
				s.append(m.name());
				s.append(" ");
				if (m.charStats().getMyRace()!= null && m.charStats().raceName()!=null
					&& m.charStats().raceName().length() > 0
					&& !m.charStats().raceName().equals("MOB"))
				{
					s.append("(");
					s.append(m.charStats().raceName());
					s.append(" ");
					if ( m.charStats().displayClassName().length() > 0
						&& !m.charStats().displayClassName().equals("MOB"))
					{
						s.append(m.charStats().displayClassLevel(m,true));
					}
					else
						s.append("[new player]");
					s.append(")");
				}
				else
					s.append("[new player]");
			}
			else
			{
				s.append("\">");
				s.append("[logging in]");
			}
			s.append("\r\n");
		}
		return s.toString();
	}

}