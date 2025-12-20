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
   Copyright 2025-2025 Bo Zimmerman

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
public class StockNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "StockNext";
	}

	public static Map<String,List<Map<String,String>>> getStockInfo(final HTTPRequest httpReq)
	{
		final String player = httpReq.getUrlParameter("PLAYER");
		@SuppressWarnings("unchecked")
		Map<String,List<Map<String,String>>> stockList=(Map<String,List<Map<String,String>>>)httpReq.getRequestObjects().get("COFFEE_STOCKLIST_"+player);
		if(stockList == null)
		{
			stockList = new TreeMap<String,List<Map<String,String>>>();
			final List<Map<String,String>> globalList = new ArrayList<Map<String,String>>();
			stockList.put("", globalList);
			Map<String,Integer> playerAmounts = null;
			if((player!=null)&&(player.length()>0))
			{
				final List<PAData> playerStockList = CMLib.database().DBReadPlayerData(player, "STOCKMARKET_STOCKS");
				if((playerStockList!=null)&&(playerStockList.size()>0))
				{
					playerAmounts = new TreeMap<String,Integer>();
					for(final PAData pdat : playerStockList)
					{
						final String xml = pdat.xml().trim();
						if(xml.startsWith("<AMT>") && xml.endsWith("</AMT>"))
						{
							final int newAmt = CMath.s_int(xml.substring(5,xml.length()-6));
							playerAmounts.put(pdat.key(), Integer.valueOf(newAmt));
						}
					}
				}
			}

			final List<PAData> dat = CMLib.database().DBReadAreaSectionData("CMKTSTOCKS");
			if((dat != null)&&(dat.size()>0))
			{
				for(final PAData pdat : dat)
				{
					final List<XMLTag> tags = CMLib.xml().parseAllXML(pdat.xml());
					for(final XMLTag tag : tags)
					{
						if(tag.tag().equals("S"))
						{
							final String bankruptUntil = tag.getParmValue("U");
							if((bankruptUntil != null)&&(bankruptUntil.trim().length()>0))
								continue;
							final String ID = tag.getParmValue("ID");
							final String name  = CMLib.xml().restoreAngleBrackets(tag.getParmValue("NAME"));
							final double price  = CMath.s_double(tag.getParmValue("PRICE"));
							final Area hostA = CMLib.map().getArea(CMLib.xml().restoreAngleBrackets(tag.getParmValue("A")));
							if(hostA != null)
							{
								List<Map<String,String>> areaStocks = stockList.get(hostA.Name());
								if(areaStocks == null)
								{
									areaStocks = new ArrayList<Map<String,String>>();
									stockList.put(hostA.Name(), areaStocks);
								}
								final String currency = CMLib.beanCounter().getCurrency(hostA);
								Map<String,String> stock = new TreeMap<String,String>();
								stock.put("ID", ID);
								stock.put("NAME", name);
								stock.put("PRICE", CMLib.beanCounter().abbreviatedPrice(currency, price));
								if((playerAmounts != null)&&(playerAmounts.containsKey(ID)))
								{
									stock.put("AMT", playerAmounts.get(ID).toString());
									stock.put("OWNED", "true");
								}
								areaStocks.add(stock);
								globalList.add(stock);
							}
						}
					}
				}
			}
			for(final String key : stockList.keySet())
			{
				final List<Map<String,String>> stocks = stockList.get(key);
				stocks.sort(new Comparator<Map<String,String>>()
				{
					@Override
					public int compare(final Map<String,String> o1, final Map<String,String> o2)
					{
						return o1.get("NAME").compareTo(o2.get("NAME"));
					}
				});
			}
			httpReq.getRequestObjects().put("COFFEE_STOCKLIST_"+player, stockList);
		}
		return stockList;
	}
	
	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("STOCK");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("STOCK");
			return "";
		}
		final Map<String,List<Map<String,String>>> stockList=getStockInfo(httpReq);
		List<Map<String,String>> listStocks=stockList.get("");
		if(parms.containsKey("AREA"))
		{
			final String areaName=parms.get("AREA");
			listStocks=stockList.get(areaName);
			if(listStocks==null)
				return " @break@";
		}

		String lastID="";
		for(int s=0;s<listStocks.size();s++)
		{
			final String ID=listStocks.get(s).get("ID");
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!ID.equalsIgnoreCase(lastID))))
			{
				httpReq.getRequestObjects().put("COFFEE_CURRENTSTOCK", listStocks.get(s));
				httpReq.addFakeUrlParameter("STOCK",ID);
				return "";
			}
			lastID=ID;
		}
		httpReq.getRequestObjects().remove("COFFEE_CURRENTSTOCK");
		httpReq.addFakeUrlParameter("STOCK","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
