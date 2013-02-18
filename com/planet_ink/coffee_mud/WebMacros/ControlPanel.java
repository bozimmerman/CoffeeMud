package com.planet_ink.coffee_mud.WebMacros;
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
   Copyright 2000-2013 Bo Zimmerman

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
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()    {return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		
		String lastDisable=httpReq.getRequestParameter("DISABLEFLAG");
		if(parms.containsKey("DISABLERESET"))
		{
			if(lastDisable!=null) httpReq.removeRequestParameter("DISABLEFLAG");
			return "";
		}
		if(parms.containsKey("DISABLENEXT"))
		{
			String lastID="";
			for(CMSecurity.DisFlag flag : CMSecurity.DisFlag.values())
			{
				if((lastDisable==null)||((lastDisable.length()>0)&&(lastDisable.equals(lastID))&&(!flag.toString().equals(lastID))))
				{
					httpReq.addRequestParameters("DISABLEFLAG",flag.toString());
					return "";
				}
				lastID=flag.toString();
			}
			httpReq.addRequestParameters("DISABLEFLAG","");
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
		
		
		String query=(String)parms.get("QUERY");
		if((query==null)||(query.length()==0))
			return "";
		if(query.equalsIgnoreCase("DISABLE"))
		{
			String field=(String)parms.get("FIELD");
			if((field==null)||(field.length()==0))
				return "";
			CMSecurity.DisFlag flag = (CMSecurity.DisFlag)CMath.s_valueOf(CMSecurity.DisFlag.values(), field.toUpperCase().trim());
			if((flag!=null)&&(CMSecurity.isDisabled(flag)))
				return " CHECKED ";
			return "";
		}
		else
		if(query.equalsIgnoreCase("CHANGEDISABLE"))
		{
			String field=(String)parms.get("FIELD");
			if((field==null)||(field.length()==0))
				return "";
			String value=(String)parms.get("VALUE");
			CMSecurity.setDisableVar(field,((value!=null)&&(value.equalsIgnoreCase("on"))));
			return "";
		}
		else
		if(query.equalsIgnoreCase("QUERY"))
		{
			String field=(String)parms.get("FIELD");
			if((field==null)||(field.length()==0))
				return "";
			if(field.equalsIgnoreCase("DATABASE"))
				return "Database Status: "+CMLib.database().errorStatus();
			return "";
		}
		else
		if(query.equalsIgnoreCase("RESET"))
		{
			String field=(String)parms.get("FIELD");
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
