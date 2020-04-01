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
public class AreaMobNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AreaMobNext";
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
		List<RoomContent> content=(List<RoomContent>)httpReq.getRequestObjects().get("AREA_"+area+"_MOBCONTENT");
		if(content == null)
		{
			content=Arrays.asList(CMLib.database().DBReadAreaMobs(A.Name()));
			httpReq.getRequestObjects().put("AREA_"+area+"_MOBCONTENT",content);
		}
		if(parms.containsKey("AMOBROOM"))
		{
			final String mobName=httpReq.getUrlParameter("MOBNAME");
			final String mobHash=httpReq.getUrlParameter("MOBHASH");
			final String roomID=httpReq.getUrlParameter("ROOM");
			if(((roomID!=null)&&(roomID.length()>0))
			&&((mobName!=null)&&(mobName.length()>0))
			&&((mobHash!=null)&&(mobHash.length()>0))
			&&(!httpReq.isUrlParameter("MOB")))
			{
				String mobID=null;
				for(final RoomContent C : content)
				{
					if((C.name().equalsIgnoreCase(mobName))
					&&(C.roomID().equalsIgnoreCase(roomID))
					&&(mobHash.equals(""+C.contentHash())))
					{
						mobID=C.dbKey();
						break;
					}
				}
				if(mobID!=null)
				{
					final MOB M=CMLib.database().DBReadRoomMOB(roomID, mobID);
					String s=RoomData.getMOBCode(RoomData.getMOBCache(),M);
					if(s.length()==0)
					{
						RoomData.contributeMOBs(new XVector<MOB>(M));
						s=RoomData.getMOBCode(RoomData.getMOBCache(),M);
					}
					if(s.length()>0)
					{
						httpReq.addFakeUrlParameter("MOB", s);
						return s;
					}
				}
			}
			return "";
		}
		final String mobHash=parms.get("AMOBHASH");
		final String mobFocus=parms.get("AMOBNAME");
		if((mobFocus!=null)&&(mobFocus.length()>0))
		{
			Map<Integer,Set<String>> mobSets;
			mobSets=(Map<Integer,Set<String>>)httpReq.getRequestObjects().get("AREA_"+area+"_MOBSET_"+mobFocus);
			if(mobSets == null)
			{
				mobSets = new TreeMap<Integer,Set<String>>();
				for(final RoomContent C : content)
				{
					if(C.name().equalsIgnoreCase(mobFocus))
					{
						final Integer h = Integer.valueOf(C.contentHash());
						if(!mobSets.containsKey(h))
							mobSets.put(h, new TreeSet<String>());
						mobSets.get(h).add(C.roomID());
					}
				}
				httpReq.getRequestObjects().put("AREA_"+area+"_MOBSET_"+mobFocus,mobSets);
			}
			if(mobSets.size()==1)
				httpReq.addFakeUrlParameter("MOBHASH",mobSets.keySet().iterator().next().toString());
			if((mobHash!=null)&&(mobHash.length()>0))
			{
				final Set<String> mobHashSets=mobSets.get(Integer.valueOf(mobHash));
				if(mobHashSets.size()==1)
					httpReq.addFakeUrlParameter("ROOM", mobHashSets.iterator().next());
				final String ret = spin("AMOBROOM", mobHashSets, httpReq, parms);
				if(ret != null)
					return ret;
			}
			else
			{
				final String ret = spin("AMOBHASH", mobSets.keySet(), httpReq, parms);
				if(ret != null)
					return ret;
			}
		}
		else
		{
			final List<String> mobNames;
			if(httpReq.getRequestObjects().containsKey("AREA_"+area+"_MOBNAMES"))
				mobNames=(List<String>)httpReq.getRequestObjects().get("AREA_"+area+"_MOBNAMES");
			else
			{
				mobNames=new ArrayList<String>();
				final Set<String> namesDone = new TreeSet<String>();
				for(final RoomContent roomContent : content)
				{
					if(!namesDone.contains(roomContent.name()))
						namesDone.add(roomContent.name());
				}
				mobNames.addAll(namesDone);
				httpReq.getRequestObjects().put("AREA_"+area+"_MOBNAMES",mobNames);
			}

			final String ret = spin("AMOBNAME", mobNames, httpReq, parms);
			if(ret != null)
				return ret;
		}
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
