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
				if(parms.containsKey("CLIMATES"))
				{
					int climate=A.climateType();
					if(httpReq.getRequestParameters().get("CLIMATE")!=null)
						climate=Util.s_int((String)httpReq.getRequestParameters().get("CLIMATE"));
					for(int i=1;i<Area.NUM_CLIMATES;i++)
					{
						String climstr=Area.CLIMATE_DESCS[i];
						int mask=Util.pow(2,i-1);
						str.append("<OPTION VALUE="+mask);
						if((climate&mask)>0) str.append(" SELECTED");
						str.append(">"+climstr);
					}
				}
				if(parms.containsKey("NAME"))
				{
					String name=(String)httpReq.getRequestParameters().get("NAME");
					if((name==null)||(name.length()==0))
						name=A.name();
					str.append(name);
				}
				if(parms.containsKey("CLASSES"))
				{
					String className=(String)httpReq.getRequestParameters().get("CLASS");
					if((className==null)||(className.length()==0))
						className=CMClass.className(A);
					for(int a=0;a<CMClass.areaTypes.size();a++)
					{
						Area cnam=(Area)CMClass.areaTypes.elementAt(a);
						str.append("<OPTION VALUE=\""+CMClass.className(cnam)+"\"");
						if(className.equalsIgnoreCase(CMClass.className(cnam)))
							str.append(" SELECTED");
						str.append(">"+CMClass.className(cnam));
					}
				}
				if(parms.containsKey("TESTSTUFF"))
					str.append(A.text());
				if(parms.containsKey("SUBOPS"))
				{
					String subOps=(String)httpReq.getRequestParameters().get("SUBOPS");
					if((subOps==null)||(subOps.length()==0))
						subOps=A.getSubOpList();
					Vector V=ExternalPlay.userList();
					for(int v=0;v<V.size();v++)
					{
						String cnam=(String)V.elementAt(v);
						str.append("<OPTION VALUE=\""+cnam+"\"");
						if(subOps.equals(cnam)
						   ||(subOps.indexOf(";"+cnam)>=0)
						   ||(subOps.startsWith(cnam+";")))
							str.append(" SELECTED");
						str.append(">"+cnam);
					}
				}
				if(parms.containsKey("DESCRIPTION"))
				{
					String desc=(String)httpReq.getRequestParameters().get("DESCRIPTION");
					if((desc==null)||(desc.length()==0))
						desc=A.description();
					str.append(desc);
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
