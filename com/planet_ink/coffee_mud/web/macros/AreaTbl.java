package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AreaTbl extends StdWebMacro
{
	public String name()	{return "AreaTbl";}
	
	private static final int AT_MAX_COL = 3;

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		// have to check, otherwise we'll be stuffing a blank string into resources
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
		{
			return "<TR><TD colspan=\"" + AT_MAX_COL + "\" class=\"cmAreaTblEntry\"><I>Game is not running - unable to get area list!</I></TD></TR>";
		}

		Vector areasVec=new Vector();

		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if(!Sense.isHidden(A))
				areasVec.addElement(A.name());
		}

		Collections.sort((List)areasVec);
		StringBuffer msg=new StringBuffer("\n\r");
		int col=0;
		int percent = 100/AT_MAX_COL;
		for(int i=0;i<areasVec.size();i++)
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
				if (i > areasVec.size() - AT_MAX_COL)
					percent = 100;
			}

			msg.append("<td");

			if (percent == 100)
				msg.append(" colspan=\"" + AT_MAX_COL + "\"");	//last element is width of remainder
			else
				msg.append(" width=\"" + percent + "%\"");

			msg.append(" class=\"cmAreaTblEntry\">");
			msg.append((String)areasVec.elementAt(i));
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
		return msg.toString();
	}

}