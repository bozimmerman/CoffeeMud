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

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class HolidayNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "HolidayNext";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("HOLIDAY");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("HOLIDAY");
			return "";
		}
		final Object resp=CMLib.quests().getHolidayFile();
		List<String> steps=null;
		if(resp instanceof List)
			steps=(List<String>)resp;
		else
		if(resp instanceof String)
			return (String)resp;
		else
			return "[Unknown error.]";
		final Vector<String> holidays=new Vector<String>();
		List<String> line=null;
		String var=null;
		List<String> V=null;
		for(int s=1;s<steps.size();s++)
		{
			final String step=steps.get(s);
			V=Resources.getFileLineVector(new StringBuffer(step));
			final List<List<String>> cmds=CMLib.quests().parseQuestCommandLines(V,"SET",0);
			//Vector<String> areaLine=null;
			List<String> nameLine=null;
			for(int v=0;v<cmds.size();v++)
			{
				line=cmds.get(v);
				if(line.size()>1)
				{
					var=line.get(1).toUpperCase();
					//if(var.equals("AREAGROUP"))
					//{ areaLine=line;}
					if(var.equals("NAME"))
					{
						nameLine=line;
					}
				}
			}
			if(nameLine!=null)
			{
				/*String areaName=null;
				if(areaLine==null)
					areaName="*special*";
				else
					areaName=CMParms.combineWithQuotes(areaLine,2);*/
				final String name=CMParms.combine(nameLine,2);
				holidays.addElement(name);
			}
		}
		String lastID="";
		for(final Enumeration q=holidays.elements();q.hasMoreElements();)
		{
			final String holidayID=(String)q.nextElement();
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!holidayID.equalsIgnoreCase(lastID))))
			{
				httpReq.addFakeUrlParameter("HOLIDAY",holidayID);
				return "";
			}
			lastID=holidayID;
		}
		httpReq.addFakeUrlParameter("HOLIDAY","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
