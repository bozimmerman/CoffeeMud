package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class SocialTbl extends StdWebMacro
{
	public String name()	{return "SocialTbl";}
	
	private static final int AT_MAX_COL = 6;

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		StringBuffer TBL=(StringBuffer)Resources.getResource("WEB SOCIALS TBL");
		if(TBL!=null) return TBL.toString();

		Vector socialVec=new Vector();
		for(int s=0;s<Socials.num();s++)
		{
			Social S=Socials.enum(s);
			String name=null;
			int x=S.name().trim().indexOf(" ");
			if(x>=0)
				name=S.name().trim().substring(0,x);
			else
				name=S.name();
			if(!socialVec.contains(name))
				socialVec.addElement(name);
		}

		Collections.sort((List)socialVec);
		StringBuffer msg=new StringBuffer("\n\r");
		int col=0;
		int percent = 100/AT_MAX_COL;
		for(int i=0;i<socialVec.size();i++)
		{
			if (col == 0)
			{
				msg.append("<tr>");
				// the bottom elements can be full width if there's
				//  not enough to fill one row
				// ie.   -X- -X- -X-
				//       -X- -X- -X-
				//       -----X-----
				//       -----X-----
				if (i > socialVec.size() - AT_MAX_COL)
					percent = 100;
			}

			msg.append("<td");

			if (percent == 100)
				msg.append(" colspan=\"" + AT_MAX_COL + "\"");	//last element is width of remainder
			else
				msg.append(" width=\"" + percent + "%\"");

			msg.append(">");
			msg.append((String)socialVec.elementAt(i));
			msg.append("</td>");
			// finish the row
			if((percent == 100) || (++col)> (AT_MAX_COL-1 ))
			{
				msg.append("</tr>\n\r");
				col=0;
			}
		}
		if (!msg.toString().endsWith("</tr>\n\r"))
			msg.append("</tr>\n\r");
		Resources.submitResource("WEB SOCIALS TBL",msg);
		return msg.toString();
	}

}