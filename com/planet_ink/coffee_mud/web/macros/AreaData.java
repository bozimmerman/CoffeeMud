package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AreaData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	// accepts season, todcode, weather, moon, stats, and help
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("AREA");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Area A=CMMap.getArea(last);
			if(A!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("HELP"))
				{
					StringBuffer s=ExternalPlay.getHelpText("AREA_"+A.name());
					if(s==null)	s=ExternalPlay.getHelpText(A.name());
					str.append(helpHelp(s));
				}
				if(parms.containsKey("SEASON"))
					str.append(Area.SEASON_DESCS[A.getSeasonCode()]+", ");
				if(parms.containsKey("TODCODE"))
					str.append(Area.TOD_DESC[A.getTODCode()]+", ");
				if(parms.containsKey("WEATHER"))
					str.append(A.getWeatherDescription()+", ");
				if(parms.containsKey("MOON"))
					str.append(Area.MOON_PHASES[A.getMoonPhase()]+", ");
				if(parms.containsKey("STATS"))
					str.append(A.getAreaStats()+", ");
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return strstr;
			}
		}
		return "";
	}
}
