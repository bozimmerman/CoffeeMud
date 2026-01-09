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
   Copyright 2025-2026 Bo Zimmerman

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
public class StockAreaNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "StockAreaNext";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("AREA");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("AREA");
			return "";
		}
		Map<String,List<Map<String,String>>> stockInfo=StockNext.getStockInfo(httpReq);
		final List<String> areaNames=new ArrayList<String>(stockInfo.keySet());
		Collections.sort(areaNames,String.CASE_INSENSITIVE_ORDER);
		String lastID="";
		for(int s=0;s<areaNames.size();s++)
		{
			final String name=areaNames.get(s);
			if(name.length()==0)
				continue;
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!name.equalsIgnoreCase(lastID))))
			{
				httpReq.addFakeUrlParameter("AREA",name);
				return "";
			}
			lastID=name;
		}
		httpReq.addFakeUrlParameter("AREA","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
