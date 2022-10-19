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
		String lastConn = "&";
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
				if(V.size()>0)
				{
					String cmd=V.firstElement().toUpperCase().trim();
					TriggerCode T;
					if(cmd.endsWith("+"))
						T=(TriggerCode)CMath.s_valueOf(TriggerCode.class, cmd.substring(0,cmd.length()-1));
					else
						T=(TriggerCode)CMath.s_valueOf(TriggerCode.class, cmd);
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
						t.add(lastConn);
						lastConn=((div==div1)?"&":"|");
						t.add(cmd);
						for(int i=1;i<T.parmTypes.length;i++)
							t.add(2,V.remove(V.size()-1));
						if(T.parmTypes.length>0)
							t.add(2,CMParms.combine(V,1));
						triggers.add(t.toArray(new String[t.size()]));
					}
				}
			}
		}
		return triggers;
	}

	protected static List<String[]> getTrigDefs(final HTTPRequest httpReq, final String triggerDefStr)
	{
		return parseTriggerDef(triggerDefStr);
	}

	protected static enum TrigMGField
	{
		PARMCOUNT("CT"),
		CONNECTOR("CONN"),
		TRIGTYPE("TRIG"),
		VALUE("VAL"),
		PARMTYPE("PARM"),
		ARG("ARG"),
		;
		public String str;
		private TrigMGField(final String fieldPostdex)
		{
			str = fieldPostdex;
		}
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
		@SuppressWarnings("unchecked")
		List<String[]> trigDefs = (List<String[]>)httpReq.getRequestObjects().get("TRIGGERDEFSTR");
		if(trigDefs == null)
		{
			String triggerDefStr = httpReq.getUrlParameter(field);
			if(triggerDefStr == null)
				return " @break@";
			final StringBuilder buildStr = new StringBuilder("");
			int trigDex=0;
			while(httpReq.isUrlParameter("TRIG"+trigDex+TrigMGField.PARMCOUNT.str))
			{
				final String cmd = httpReq.getUrlParameter("TRIG"+trigDex+TrigMGField.TRIGTYPE.str);
				if(!cmd.equalsIgnoreCase("DELETE"))
				{
					final int ct = CMath.s_int(httpReq.getUrlParameter("TRIG"+trigDex+TrigMGField.PARMCOUNT.str));
					if(trigDex>0)
					{
						final String connStr=httpReq.getUrlParameter("TRIG"+trigDex+TrigMGField.CONNECTOR.str);
						buildStr.append(connStr);
					}
					buildStr.append(cmd);
					if("on".equalsIgnoreCase(httpReq.getUrlParameter("TRIG"+trigDex+TrigMGField.ARG.str)))
						buildStr.append("+");
					buildStr.append(" ");
					for(int x=0;x<ct;x++)
					{
						String p = httpReq.getUrlParameter("TRIG"+trigDex+TrigMGField.VALUE.str+x);
						if(p == null)
							p="";
						if(p.indexOf(' ')>0)
							p="\""+CMStrings.replaceAll(CMStrings.replaceAll(p,"\\","\\\\"),"\"","\\\"")+"\"";
						buildStr.append(p).append(" ");
					}
				}
				trigDex++;
			}
			final String newConn = httpReq.getUrlParameter("TRIGCONN");
			if((newConn != null) && (newConn.length()==1))
			{
				if(buildStr.length()>0)
					buildStr.append(newConn);
				buildStr.append("WAIT 0");
			}
			if(buildStr.length()>0)
			{
				triggerDefStr = buildStr.toString();
			}
			trigDefs = getTrigDefs(httpReq, triggerDefStr);
			httpReq.getRequestObjects().put("TRIGGERDEFSTR",trigDefs);
		}
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
				if(td[1].equalsIgnoreCase("DELETE"))
				{
					trigDex--;
					continue;
				}
				TriggerCode T;
				if(td[1].endsWith("+"))
					T=(TriggerCode)CMath.s_valueOf(TriggerCode.class, td[1].substring(0,td[1].length()-1));
				else
					T=(TriggerCode)CMath.s_valueOf(TriggerCode.class, td[1]);
				final StringBuilder newSubTrigger = new StringBuilder("");
				httpReq.addFakeUrlParameter("TRIG"+trigDex+TrigMGField.ARG.str, td[1].endsWith("+")?"on":"");
				httpReq.addFakeUrlParameter("TRIG"+trigDex+TrigMGField.PARMCOUNT.str, Integer.toString(T.parmTypes.length));
				httpReq.addFakeUrlParameter("TRIG"+trigDex+TrigMGField.CONNECTOR.str, td[0]);
				newSubTrigger.append(httpReq.getUrlParameter("TRIG"+trigDex+TrigMGField.CONNECTOR.str));
				httpReq.addFakeUrlParameter("TRIG"+trigDex+TrigMGField.TRIGTYPE.str, T.name());
				newSubTrigger.append(td[1]);
				newSubTrigger.append(" ");
				for(int x=2;x<td.length;x++)
				{
					httpReq.addFakeUrlParameter("TRIG"+trigDex+TrigMGField.VALUE.str+(x-2), td[x]);
					String p = httpReq.getUrlParameter("TRIG"+trigDex+TrigMGField.VALUE.str+(x-2));
					if(p.indexOf(' ')>0)
						p="\""+CMStrings.replaceAll(CMStrings.replaceAll(p,"\\","\\\\"),"\"","\\\"")+"\"";
					newSubTrigger.append(p).append(" ");
				}
				for(int x=0;x<T.parmTypes.length;x++)
					httpReq.addFakeUrlParameter("TRIG"+trigDex+TrigMGField.PARMTYPE.str+x, T.parmTypes[x].getSimpleName());
				httpReq.addFakeUrlParameter("TRIG"+trigDex+"BUILT", newSubTrigger.toString());
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
