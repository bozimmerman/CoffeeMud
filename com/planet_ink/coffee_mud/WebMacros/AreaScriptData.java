package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
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

import java.net.URLEncoder;
import java.util.*;

/*
   Copyright 2010-2018 Bo Zimmerman

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
public class AreaScriptData extends AreaScriptNext
{
	@Override
	public String name()
	{
		return "AreaScriptData";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);

		final String area=httpReq.getUrlParameter("AREA");
		if((area==null)||(area.length()==0))
			return "@break@";
		final String script=httpReq.getUrlParameter("AREASCRIPT");
		if((script==null)||(script.length()==0))
			return "@break@";
		final TreeMap<String,ArrayList<AreaScriptInstance>> list = getAreaScripts(httpReq,area);
		final ArrayList<AreaScriptInstance> subList = list.get(script);
		if(subList == null)
			return " @break@";
		AreaScriptInstance entry = null;
		String last=httpReq.getUrlParameter("AREASCRIPTHOST");
		if((last!=null)&&(last.length()>0))
		{
			for(final AreaScriptInstance inst : subList)
			{
				final String hostName = CMParms.combineWith(inst.path, '.',0, inst.path.size()) + "." + inst.fileName;
				if(hostName.equalsIgnoreCase(last))
				{
					entry=inst;
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
				if(last!=null)
					httpReq.removeUrlParameter("AREASCRIPTHOST");
				return "";
			}
			String lastID="";
			for(final AreaScriptInstance inst : subList)
			{
				final String hostName = CMParms.combineWith(inst.path, '.',0, inst.path.size()) + "." + inst.fileName;
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!hostName.equals(lastID))))
				{
					httpReq.addFakeUrlParameter("AREASCRIPTHOST",hostName);
					last=hostName;
					return "";
				}
				lastID=hostName;
			}
			httpReq.addFakeUrlParameter("AREASCRIPTHOST","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}

		final StringBuilder str = new StringBuilder("");

		if(parms.containsKey("NUMHOSTS"))
			str.append(subList.size()+", ");

		if(parms.containsKey("FILE") && (entry != null))
			str.append(Resources.makeFileResourceName(entry.fileName)+", ");

		if(parms.containsKey("RESOURCEKEY") && (entry != null))
			str.append(entry.instanceKey+", ");

		if(parms.containsKey("RESOURCEKEYENCODED") && (entry != null))
		{
			try
			{
				str.append(URLEncoder.encode(entry.instanceKey,"UTF-8")+", ");
			}
			catch (final Exception e)
			{
			}
		}

		if(parms.containsKey("PATH") && (entry != null))
		{
			final String path=Resources.makeFileResourceName(entry.fileName);
			final int x=path.lastIndexOf('/');
			str.append(((x<0)?"":path.substring(0,x))+", ");
		}

		if(parms.containsKey("ENTRYPATH") && (entry != null))
			str.append(CMParms.combineWith(entry.path, '.',0, entry.path.size())+", ");

		if(parms.containsKey("CUSTOMSCRIPT") && (entry != null))
		{
			try
			{
				String s = entry.customScript;
				if(parms.containsKey("PARSELED") && (s.trim().length()>0))
				{
					final StringBuffer st=new StringBuffer("");
					final List<List<String>> V = CMParms.parseDoubleDelimited(s,'~',';');
					for(final List<String> LV : V)
					{
						for(final String L : LV)
							st.append(L+"\n\r");
						st.append("\n\r");
					}
					s=st.toString();
				}
				str.append(webify(new StringBuffer(s))+", ");
			}
			catch (final Exception e)
			{
			}
		}

		if(parms.containsKey("FILENAME") && (entry != null))
		{
			final String path=Resources.makeFileResourceName(entry.fileName);
			final int x=path.lastIndexOf('/');
			str.append(((x<0)?path:path.substring(x+1))+", ");
		}

		if(parms.containsKey("ENCODEDPATH") && (entry != null))
		{
			final String path=Resources.makeFileResourceName(entry.fileName);
			final int x=path.lastIndexOf('/');
			try
			{
				str.append(URLEncoder.encode(((x<0)?"":path.substring(0,x)),"UTF-8")+", ");
			}
			catch (final Exception e)
			{
			}
		}

		if(parms.containsKey("ENCODEDFILENAME") && (entry != null))
		{
			final String path=Resources.makeFileResourceName(entry.fileName);
			final int x=path.lastIndexOf('/');
			try
			{
				str.append(URLEncoder.encode(((x<0)?path:path.substring(x+1)),"UTF-8")+", ");
			}
			catch (final Exception e)
			{
			}
		}

		if(parms.containsKey("ROOM") && (entry != null) && (entry.path.size()>1))
			str.append(entry.path.get(1)+", ");

		if(parms.containsKey("AREA") && (entry != null))
			str.append(entry.path.get(0)+", ");

		if(parms.containsKey("SCRIPTKEY") && (entry != null))
			str.append(entry.instanceKey+", ");

		if(parms.containsKey("CLEARRESOURCE") && (entry != null))
			Resources.removeResource(entry.instanceKey);

		if(parms.containsKey("ISCUSTOM") && (entry != null))
			str.append(entry.key.equalsIgnoreCase("Custom")+", ");

		if(parms.containsKey("ISFILE") && (entry != null))
			str.append(!entry.key.equalsIgnoreCase("Custom")+", ");

		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return clearWebMacros(strstr);
	}
}
