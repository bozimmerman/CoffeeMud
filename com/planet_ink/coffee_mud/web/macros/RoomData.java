package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class RoomData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	// valid parms include help, ranges, quality, target, alignment, domain, 
	// qualifyQ, auto
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("ROOM");
		if(last==null) return " @break@";

		if(last.length()>0)
		{
			Room R=CMMap.getRoom(last);
			if(R!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("NAME"))
				{
					String name=(String)httpReq.getRequestParameters().get("NAME");
					if((name==null)||(name.length()==0))
						name=R.displayText();
					str.append(name);
				}
				if(parms.containsKey("CLASSES"))
				{
					String className=(String)httpReq.getRequestParameters().get("CLASS");
					if((className==null)||(className.length()==0))
						className=CMClass.className(R);
					for(int r=0;r<CMClass.locales.size();r++)
					{
						Room cnam=(Room)CMClass.locales.elementAt(r);
						str.append("<OPTION VALUE=\""+CMClass.className(cnam)+"\"");
						if(className.equalsIgnoreCase(CMClass.className(cnam)))
							str.append(" SELECTED");
						str.append(">"+CMClass.className(cnam));
					}
				}
				
				str.append(AreaData.affectsNBehaves(R,httpReq,parms));
				
				if(parms.containsKey("DESCRIPTION"))
				{
					String desc=(String)httpReq.getRequestParameters().get("DESCRIPTION");
					if((desc==null)||(desc.length()==0))
						desc=R.description();
					str.append(desc);
				}
									 
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return strstr;
			}
		}
		return "";
	}
}
