package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class AreaScriptData extends AreaScriptNext 
{
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		
		String area=httpReq.getRequestParameter("AREA");
		if((area==null)||(area.length()==0)) return "@break@";
		String script=httpReq.getRequestParameter("AREASCRIPT");
		if((script==null)||(script.length()==0)) return "@break@";
		TreeMap<String,ArrayList<ArrayList<String>>> list = getAreaScripts(httpReq,area);
		ArrayList<ArrayList<String>> subList = list.get(script);
		if(subList == null) return " @break@";
		ArrayList<String> entry = null;
		String last=httpReq.getRequestParameter("AREASCRIPTHOST");
		if((last!=null)&&(last.length()>0))
		{
			for(ArrayList<String> hostList : subList)
			{
				String hostName = CMParms.combineWith(hostList, '.',2, hostList.size()-1);
				if(hostName.equalsIgnoreCase(last));
				{
					entry=hostList;
					break;
				}
			}
		}
		else
			entry=(subList.size()>0)?subList.get(0):null;
			
		if(parms.containsKey("NEXT")||parms.containsKey("RESET"))
		{
			if(parms.containsKey("RESET"))
			{
				if(last!=null) httpReq.removeRequestParameter("AREASCRIPTHOST");
				return "";
			}
			String lastID="";
			for(ArrayList<String> hostList : subList)
			{
				String hostName = CMParms.combineWith(hostList, '.',2, hostList.size()-1);
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!hostName.equals(lastID))))
				{
					httpReq.addRequestParameters("AREASCRIPTHOST",hostName);
					last=hostName;
				}
				lastID=hostName;
			}
			httpReq.addRequestParameters("AREASCRIPTHOST","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}
		
		StringBuilder str = new StringBuilder("");
		
		if(parms.containsKey("NUMHOSTS"))
			str.append(subList.size()+", ");
		
		if(parms.containsKey("FILE") && (entry != null))
			str.append(entry.get(entry.size()-1)+", ");
		
		if(parms.containsKey("ROOM") && (entry != null))
			str.append(entry.get(2)+", ");
		
		if(parms.containsKey("AREA") && (entry != null))
			str.append(entry.get(1)+", ");
		
		if(parms.containsKey("SCRIPTKEY") && (entry != null))
			str.append(entry.get(0)+", ");
		
		if(parms.containsKey("CLEARRESOURCE") && (entry != null))
			Resources.removeResource(entry.get(0));
		
		if(parms.containsKey("ISCUSTOM") && (entry != null))
			str.append(entry.get(entry.size()-2).equalsIgnoreCase("Custom")+", ");
		
		if(parms.containsKey("ISFILE") && (entry != null))
			str.append(!entry.get(entry.size()-2).equalsIgnoreCase("Custom")+", ");
		
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
        return clearWebMacros(strstr);
	}
}
