package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import java.net.URLEncoder;



/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class RequestParameter extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	private static enum MODIFIER {UPPERCASE,LOWERCASE,LEFT,RIGHT,ELLIPSE,TRIM};
	private static HashSet<String> modifiers=new HashSet<String>();
	static
	{
		for(MODIFIER M : MODIFIER.values())
			modifiers.add(M.name());
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		String str="";
		Hashtable parms=parseParms(parm);
		for(Enumeration e=parms.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if(!modifiers.contains(key))
				if(httpReq.isRequestParameter(key))
					str+=httpReq.getRequestParameter(key);
		}
		for(Enumeration e=parms.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if(modifiers.contains(key))
			{
				int num = 0;
				if(key.equals(MODIFIER.UPPERCASE.name()))
					str=str.toUpperCase();
				else
				if(key.equals(MODIFIER.LOWERCASE.name()))
					str=str.toLowerCase();
				else
				if(key.equals(MODIFIER.TRIM.name()))
					str=str.trim();
				else
				if(key.equals(MODIFIER.LEFT.name()))
				{
					num = CMath.s_int((String)parms.get(MODIFIER.LEFT.name()));
					if((num >0)&& (num < str.length()))
						str=str.substring(0,num);
				}
				else
				if(key.equals(MODIFIER.RIGHT.name()))
				{
					num = CMath.s_int((String)parms.get(MODIFIER.RIGHT.name()));
					if((num >0)&& (num < str.length()))
						str=str.substring(str.length()-num);
				}
				else
				if(key.equals(MODIFIER.ELLIPSE.name()))
				{
					num = CMath.s_int((String)parms.get(MODIFIER.ELLIPSE.name()));
					if((num >0)&& (num < str.length()))
						str=str.substring(0,num)+"...";
				}
			}
		}
		str=clearWebMacros(str);
		return str;
	}
}
