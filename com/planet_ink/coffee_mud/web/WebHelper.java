package com.planet_ink.coffee_mud.web;
import java.io.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;

public class WebHelper
{
	// makes a simple error page if no error.cmvp exists
	//  does NOT imbed macros just inserts directly into page -
	//   this means no dependancies on pre-existing macros
	public static byte[] makeErrorPage(String s1, String s2)
	{
		StringBuffer s = new StringBuffer("<html><head><title>");
		s.append(s1);
		s.append("</title></head><body><p><h1>");
		s.append(s1);
		s.append("</h1>");

		if (s2 != null)
		{
			s.append(s2);
			s.append("<br>");
		}

		s.append("<br><hr><i>");
		s.append(HTTPserver.ServerVersionString);
		s.append("</i></body></html>");
		
		return s.toString().getBytes();
	}

	
	// jef: util functions for the web server
	
	// nb: modified game (commands/sysop/Import.java & Rooms.java)
	//  to erase this resource whenever it erases the in-game area list
	
	private static final int AT_MAX_COL = 3;
	public static String htmlAreaTbl(HTTPserver webServer)
	{
		// have to check, otherwise we'll be stuffing a blank string into resources
		if (!webServer.getMUD().isGameRunning())
		{
			return "<TR><TD colspan=\"" + AT_MAX_COL + "\" class=\"cmAreaTblEntry\"><I>Game is not running - unable to get area list!</I></TD></TR>";
		}
	
		StringBuffer areasList=(StringBuffer)Resources.getResource("areasListHTML");

		if (areasList == null || areasList.toString().trim().length() == 0)
		{
// OLD		Hashtable areasHash=new Hashtable();
			Vector areasVec=new Vector();

			for(int a=0;a<CMMap.AREAS.size();a++)
				areasVec.addElement(((Area)CMMap.AREAS.elementAt(a)).name());

/* OLD			for(int m=0;m<CMMap.map.size();m++)
			{
				Room room=(Room)CMMap.map.elementAt(m);
				if(areasHash.get(room.getAreaID())==null)
				{
					areasHash.put(room.getAreaID(),room.getAreaID());
					areasVec.addElement(room.getAreaID());
				}
			}
*/
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
/*
				if (i == areasVec.size() - 1)
					msg.append(" colspan=\"" + (5 - col) + "\"");	//last element is width of remainder
				else
					msg.append(" width=\"20%\"");
*/				
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
			
			Resources.submitResource("areasListHTML",msg);
			areasList=msg;
		}
		return areasList.toString();
	}

	public static String htmlPlayerList()
	{
//		StringBuffer s = new StringBuffer("\r\n");
		StringBuffer s = new StringBuffer("");
		for(int i=0;i<Sessions.size();i++)
		{
			Session session=Sessions.elementAt(i);
			// list entry with style sheet class
			s.append("<li class=\"cmPlayerListEntry");
			MOB m = session.mob();
			if ( (m!=null) && (m.name() != null) 
				&& (m.name().length() > 0) )
			{
				// jef: nb - only shows full sysops, not subops
				if ( m.isASysOp(null) )
					s.append("Archon");
				s.append("\">");
				s.append(m.name());
				s.append(" ");
				if (m.charStats().getMyRace()!= null && m.charStats().getMyRace().name()!=null 
					&& m.charStats().getMyRace().name().length() > 0
					&& !m.charStats().getMyRace().name().equals("MOB"))
				{
					s.append("(");
					s.append(m.charStats().getMyRace().name());
					s.append(" ");
					if (m.charStats().getMyClass()!= null && m.charStats().getMyClass().name()!=null
						&& m.charStats().getMyClass().name().length() > 0
						&& !m.charStats().getMyClass().name().equals("MOB"))
					{
						s.append(m.charStats().getMyClass().name());
						
						s.append(", level ");
						s.append(m.envStats().level());
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