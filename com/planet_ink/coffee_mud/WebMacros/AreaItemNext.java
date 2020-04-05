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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.RoomContent;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class AreaItemNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AreaItemNext";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	protected String spin(final String tag, final Collection<?> set, final HTTPRequest httpReq, final Map<String,String> parms)
	{
		String last=httpReq.getUrlParameter(tag);
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter(tag);
			return "";
		}
		String lastID="";
		for(final Object name : set)
		{
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!(""+name).equals(lastID))))
			{
				httpReq.addFakeUrlParameter(tag,""+name);
				last=""+name;
				return "";
			}
			lastID=""+name;
		}
		httpReq.addFakeUrlParameter(tag,"");
		return null;
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final Map<String,String> parms=parseParms(parm);
		final String area=httpReq.getUrlParameter("AREA");
		if((area==null)||(area.length()==0))
			return "@break@";
		final Area A=CMLib.map().getArea(area);
		if(A==null)
			return "@break@";
		List<RoomContent> content=(List<RoomContent>)httpReq.getRequestObjects().get("AREA_"+area+"_ITEMCONTENT");
		if(content == null)
		{
			content=Arrays.asList(CMLib.database().DBReadAreaItems(A.Name()));
			httpReq.getRequestObjects().put("AREA_"+area+"_ITEMCONTENT",content);
		}
		if(parms.containsKey("AITEMROOM"))
		{
			final String itemName=httpReq.getUrlParameter("ITEMNAME");
			final String itemHash=httpReq.getUrlParameter("ITEMHASH");
			final String roomID=httpReq.getUrlParameter("ROOM");
			if(((roomID!=null)&&(roomID.length()>0))
			&&((itemName!=null)&&(itemName.length()>0))
			&&((itemHash!=null)&&(itemHash.length()>0))
			&&(!httpReq.isUrlParameter("ITEM")))
			{
				String itemID=null;
				for(final RoomContent C : content)
				{
					if((C.name().equalsIgnoreCase(itemName))
					&&(C.roomID().equalsIgnoreCase(roomID))
					&&(itemHash.equals(""+C.contentHash())))
					{
						itemID=C.dbKey();
						break;
					}
				}
				if(itemID!=null)
				{
					final Item I=CMLib.database().DBReadRoomItem(roomID, itemID);
					String s=RoomData.getItemCode(RoomData.getItemCache(),I);
					if(s.length()==0)
					{
						RoomData.contributeItems(new XVector<Item>(I));
						s=RoomData.getItemCode(RoomData.getItemCache(),I);
					}
					if(s.length()>0)
					{
						httpReq.addFakeUrlParameter("ITEM", s);
						return s;
					}
				}
			}
			return "";
		}
		final String itemHash=parms.get("AITEMHASH");
		final String itemFocus=parms.get("AITEMNAME");
		if((itemFocus!=null)&&(itemFocus.length()>0))
		{
			Map<Integer,Set<String>> itemSets;
			itemSets=(Map<Integer,Set<String>>)httpReq.getRequestObjects().get("AREA_"+area+"_ITEMSET_"+itemFocus);
			if(itemSets == null)
			{
				itemSets = new TreeMap<Integer,Set<String>>();
				for(final RoomContent C : content)
				{
					if(C.name().equalsIgnoreCase(itemFocus))
					{
						final Integer h = Integer.valueOf(C.contentHash());
						if(!itemSets.containsKey(h))
							itemSets.put(h, new TreeSet<String>());
						itemSets.get(h).add(C.roomID());
					}
				}
				httpReq.getRequestObjects().put("AREA_"+area+"_ITEMSET_"+itemFocus,itemSets);
			}
			if(itemSets.size()==1)
				httpReq.addFakeUrlParameter("ITEMHASH",itemSets.keySet().iterator().next().toString());
			if((itemHash!=null)&&(itemHash.length()>0))
			{
				final Set<String> itemHashSets=itemSets.get(Integer.valueOf(itemHash));
				if(itemHashSets.size()==1)
					httpReq.addFakeUrlParameter("ROOM", itemHashSets.iterator().next());
				final String ret = spin("AITEMROOM", itemHashSets, httpReq, parms);
				if(ret != null)
					return ret;
			}
			else
			{
				final String ret = spin("AITEMHASH", itemSets.keySet(), httpReq, parms);
				if(ret != null)
					return ret;
			}
		}
		else
		{
			final List<String> itemNames;
			if(httpReq.getRequestObjects().containsKey("AREA_"+area+"_ITEMNAMES"))
				itemNames=(List<String>)httpReq.getRequestObjects().get("AREA_"+area+"_ITEMNAMES");
			else
			{
				itemNames=new ArrayList<String>();
				final Set<String> namesDone = new TreeSet<String>();
				for(final RoomContent roomContent : content)
				{
					if(!namesDone.contains(roomContent.name()))
						namesDone.add(roomContent.name());
				}
				itemNames.addAll(namesDone);
				httpReq.getRequestObjects().put("AREA_"+area+"_ITEMNAMES",itemNames);
			}

			final String ret = spin("AITEMNAME", itemNames, httpReq, parms);
			if(ret != null)
				return ret;
		}
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
