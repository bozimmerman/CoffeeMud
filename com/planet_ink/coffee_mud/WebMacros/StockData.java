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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PAData;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
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
public class StockData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "StockData";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("STOCK");
		if(last==null)
			return " @break@";
		final Map<String,List<Map<String,String>>> stockList=StockNext.getStockInfo(httpReq);
		String area=httpReq.getUrlParameter("AREA");
		if(area==null)
			area="";
		final List<Map<String,String>> areaStocks=stockList.get(area);
		if(areaStocks==null)
			return " @break@";
		@SuppressWarnings("unchecked")
		Map<String,String> stockData=(Map<String,String>)httpReq.getRequestObjects().get("COFFEE_CURRENTSTOCK");
		if((stockData==null)||(!stockData.get("ID").equals(last)))
		{
			stockData=null;
			for(final Map<String,String> data : areaStocks)
			{
				if(data.get("ID").equals(last))
				{
					stockData=data;
					break;
				}
			}
			if(stockData==null)
				return " @break@";
			httpReq.getRequestObjects().put("COFFEE_CURRENTSTOCK", stockData);
		}
		final StringBuilder str=new StringBuilder("");
		for(final String key : parms.keySet())
		{
			final String val=stockData.get(key.toUpperCase());
			if(val!=null)
				str.append(val).append(", ");
		}
		if(str.length()==0)
			return "";
		if(str.toString().endsWith(", "))
			str.setLength(str.length()-2);
		return str.toString();
	}
}
