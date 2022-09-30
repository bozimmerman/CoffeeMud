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
import com.planet_ink.coffee_mud.Common.interfaces.Triggerer.TriggerCode;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2022-2022 Bo Zimmerman

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
public class TriggerNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "TriggerNext";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	public static List<String[]> parseTriggerDef(String trigger)
	{
		final List<String[]> triggers = new Vector<String[]>();
		while(trigger.length()>0)
		{
			final int div1=trigger.indexOf('&');
			final int div2=trigger.indexOf('|');
			int div=div1;

			if((div2>=0)&&((div<0)||(div2<div)))
				div=div2;
			String trig=null;
			if(div<0)
			{
				trig=trigger;
				trigger="";
			}
			else
			{
				trig=trigger.substring(0,div).trim();
				trigger=trigger.substring(div+1);
			}
			if(trig.length()>0)
			{
				final Vector<String> V=CMParms.parse(trig);
				if(V.size()>1)
				{
					String cmd=V.firstElement();
					TriggerCode T;
					if(cmd.endsWith("+"))
						T=(TriggerCode)CMath.s_valueOf(TriggerCode.class, cmd.substring(0,cmd.length()-1));
					else
						T = (TriggerCode)CMath.s_valueOf(TriggerCode.class, cmd);
					if(T==null)
					{
						for(final TriggerCode RT : TriggerCode.values())
						{
							if(RT.name().startsWith(cmd))
							{
								T=RT;
								cmd = RT.name();
								break;
							}
						}
					}
					if(T!=null)
					{
						final List<String> t = new ArrayList<String>();
						t.add((div==div1)?"&":"|");
						t.add(cmd);
						for(int i=1;i<T.parmTypes.length;i++)
							t.add(1,V.remove(V.size()-1));
						t.add(1,CMParms.combine(V,1));
						triggers.add(t.toArray(new String[t.size()]));
					}
				}
			}
		}
		return triggers;
	}

	protected static List<String[]> getTrigDefs(final HTTPRequest httpReq, final String triggerDefStr)
	{
		@SuppressWarnings("unchecked")
		List<String[]> trigDefs = (List<String[]>)httpReq.getRequestObjects().get("ALLTRIGGERS");
		if(trigDefs == null)
		{
			trigDefs = parseTriggerDef(triggerDefStr);
			httpReq.getRequestObjects().put("ALLTRIGGERS", trigDefs);
		}
		return trigDefs;
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("TRIGGER");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("TRIGGER");
			return "";
		}
		final String field = httpReq.getUrlParameter("FIELD");
		if(field == null)
			return " @break@";
		final String triggerDefStr = httpReq.getUrlParameter(field);
		if(triggerDefStr == null)
			return " @break@";
		final List<String[]> trigDefs = getTrigDefs(httpReq, triggerDefStr);
		String lastID="";
		String componentID;
		int trigDex = 0;
		for(final Iterator<String[]> i=trigDefs.iterator();i.hasNext();i.next())
		{
			componentID=Integer.toString(trigDex);
			if((last==null)
			||((last.length()>0)
				&&(last.equals(lastID))
				&&(!componentID.equalsIgnoreCase(lastID))))
			{
				httpReq.addFakeUrlParameter("TRIGGER",componentID);
				final String[] td = trigDefs.get(trigDex);
				final TriggerCode T = (TriggerCode)CMath.s_valueOf(TriggerCode.class, td[1]);
				if(!httpReq.isUrlParameter("TRIG"+trigDex+"CT"))
					httpReq.addFakeUrlParameter("TRIG"+trigDex+"CT", Integer.toString(T.parmTypes.length));
				if(!httpReq.isUrlParameter("TRIG"+trigDex+"CONN"))
					httpReq.addFakeUrlParameter("TRIG"+trigDex+"CONN", td[0]);
				if(!httpReq.isUrlParameter("TRIG"+trigDex+"TYP"))
					httpReq.addFakeUrlParameter("TRIG"+trigDex+"TYP", td[1]);
				for(int x=2;x<td.length;x++)
				{
					if(!httpReq.isUrlParameter("TRIG"+trigDex+"VAL"))
						httpReq.addFakeUrlParameter("TRIG"+trigDex+"VAL"+(x-2), td[x]);
				}
				for(int x=0;x<T.parmTypes.length;x++)
				{
					if(!httpReq.isUrlParameter("TRIG"+trigDex+"TYP"))
						httpReq.addFakeUrlParameter("TRIG"+trigDex+"TYP"+x, T.parmTypes[x].getSimpleName());
				}
				return "";
			}
			trigDex++;
			lastID=componentID;
		}
		httpReq.addFakeUrlParameter("TRIGGER","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
