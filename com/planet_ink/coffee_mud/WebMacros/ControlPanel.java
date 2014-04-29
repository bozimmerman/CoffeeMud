package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.miniweb.interfaces.*;
import com.planet_ink.miniweb.util.MWThread;
import com.planet_ink.miniweb.util.MiniWebConfig;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
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
   Copyright 2000-2014 Bo Zimmerman

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
public class ControlPanel extends StdWebMacro
{
	@Override public String name() { return "ControlPanel"; }
	@Override public boolean isAdminMacro()    {return true;}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);

		String lastDisable=httpReq.getUrlParameter("DISABLEFLAG");
		if(parms.containsKey("DISABLERESET"))
		{
			if(lastDisable!=null) httpReq.removeUrlParameter("DISABLEFLAG");
			return "";
		}
		if(parms.containsKey("DISABLENEXT"))
		{
			String lastID="";
			for(CMSecurity.DisFlag flag : CMSecurity.DisFlag.values())
			{
				if((lastDisable==null)||((lastDisable.length()>0)&&(lastDisable.equals(lastID))&&(!flag.toString().equals(lastID))))
				{
					httpReq.addFakeUrlParameter("DISABLEFLAG",flag.toString());
					return "";
				}
				lastID=flag.toString();
			}
			httpReq.addFakeUrlParameter("DISABLEFLAG","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";

		}
		if(parms.containsKey("DISABLEID"))
		{
			if(lastDisable==null)
				return " @break@";
			return lastDisable;
		}
		if(parms.containsKey("DISABLEDESC"))
		{
			if(lastDisable==null)
				return " @break@";
			CMSecurity.DisFlag flag = (CMSecurity.DisFlag)CMath.s_valueOf(CMSecurity.DisFlag.values(), lastDisable);
			if(flag==null)
				return " @break@";
			return flag.description();
		}


		String lastDebug=httpReq.getUrlParameter("DEBUGFLAG");
		if(parms.containsKey("ISDEBUGGING"))
		{
			return Log.debugChannelOn()?"true":"false";
		}
		if(parms.containsKey("DEBUGRESET"))
		{
			if(lastDebug!=null) httpReq.removeUrlParameter("DEBUGFLAG");
			return "";
		}
		if(parms.containsKey("DEBUGNEXT"))
		{
			String lastID="";
			for(CMSecurity.DbgFlag flag : CMSecurity.DbgFlag.values())
			{
				if((lastDebug==null)||((lastDebug.length()>0)&&(lastDebug.equals(lastID))&&(!flag.toString().equals(lastID))))
				{
					httpReq.addFakeUrlParameter("DEBUGFLAG",flag.toString());
					return "";
				}
				lastID=flag.toString();
			}
			httpReq.addFakeUrlParameter("DEBUGFLAG","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";

		}
		if(parms.containsKey("DEBUGID"))
		{
			if(lastDebug==null)
				return " @break@";
			return lastDebug;
		}
		if(parms.containsKey("DEBUGDESC"))
		{
			if(lastDebug==null)
				return " @break@";
			CMSecurity.DbgFlag flag = (CMSecurity.DbgFlag)CMath.s_valueOf(CMSecurity.DbgFlag.values(), lastDebug);
			if(flag==null)
				return " @break@";
			return flag.description();
		}

		String query=parms.get("QUERY");
		if((query==null)||(query.length()==0))
			return "";
		if(query.equalsIgnoreCase("DISABLE"))
		{
			String field=parms.get("FIELD");
			if((field==null)||(field.length()==0))
				return "";
			CMSecurity.DisFlag flag = (CMSecurity.DisFlag)CMath.s_valueOf(CMSecurity.DisFlag.values(), field.toUpperCase().trim());
			if((flag!=null)&&(CMSecurity.isDisabled(flag)))
				return " CHECKED ";
			return "";
		}
		else
		if(query.equalsIgnoreCase("DEBUG"))
		{
			String field=parms.get("FIELD");
			if((field==null)||(field.length()==0))
				return "";
			CMSecurity.DbgFlag flag = (CMSecurity.DbgFlag)CMath.s_valueOf(CMSecurity.DbgFlag.values(), field.toUpperCase().trim());
			if((flag!=null)&&(CMSecurity.isDebugging(flag)))
				return " CHECKED ";
			return "";
		}
		else
		if(query.equalsIgnoreCase("CHANGEDISABLE"))
		{
			String field=parms.get("FIELD");
			if((field==null)||(field.length()==0))
				return "";
			String value=parms.get("VALUE");
			CMSecurity.setDisableVar(field,((value!=null)&&(value.equalsIgnoreCase("on"))));
			return "";
		}
		else
		if(query.equalsIgnoreCase("CHANGEDEBUG"))
		{
			String field=parms.get("FIELD");
			if((field==null)||(field.length()==0))
				return "";
			String value=parms.get("VALUE");
			DbgFlag flag = CMSecurity.setDebugVar(field,((value!=null)&&(value.equalsIgnoreCase("on"))));
			if((Thread.currentThread() instanceof MWThread)
			&&((flag==DbgFlag.HTTPACCESS)||(flag==DbgFlag.HTTPREQ)))
			{
				MiniWebConfig config=((MWThread)Thread.currentThread()).getConfig();
				if(CMSecurity.isDebugging(DbgFlag.HTTPREQ))
					config.setDebugFlag(CMProps.instance().getStr("DBGMSGS"));
				if(CMSecurity.isDebugging(DbgFlag.HTTPACCESS))
					config.setAccessLogFlag(CMProps.instance().getStr("ACCMSGS"));
			}
			return "";
		}
		else
		if(query.equalsIgnoreCase("QUERY"))
		{
			String field=parms.get("FIELD");
			if((field==null)||(field.length()==0))
				return "";
			if(field.equalsIgnoreCase("DATABASE"))
				return "Database Status: "+CMLib.database().errorStatus();
			return "";
		}
		else
		if(query.equalsIgnoreCase("RESET"))
		{
			String field=parms.get("FIELD");
			if((field==null)||(field.length()==0))
				return "";
			if(field.equalsIgnoreCase("DATABASE"))
			{
				CMLib.database().resetConnections();
				return "Database successfully reset";
			}
			else
			if(field.equalsIgnoreCase("SAVETHREAD"))
			{

			}
			return "";
		}
		return "";
	}
}
