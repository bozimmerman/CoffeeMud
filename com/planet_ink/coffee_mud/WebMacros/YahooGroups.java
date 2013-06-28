package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.miniweb.interfaces.*;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
public class YahooGroups extends StdWebMacro
{
	public String name(){return "YahooGroups";}
	public boolean isAdminMacro()	{return true;}

	
	public String runMacro(HTTPRequest httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String command=parms.get("COMMAND");
		if(command==null)
			return " @break@";
		if(command.equalsIgnoreCase("LOGIN"))
		{
			HttpClient H=(HttpClient)CMClass.getCommon("DefaultHttpClient");
			String user=parms.get("USER");
			if(user==null)
				return " @break@";
			String password=parms.get("PASSWORD");
			if(password==null)
				return " @break@";
			try {
				final String url="http://login.yahoo.com?login="+URLEncoder.encode(user,"UTF8")+"&passwd="+URLEncoder.encode(password,"UTF8");
				Map<String,List<String>> M = H.getHeaders(url);
				if(M==null)
					return "Fail: Http error";
				StringBuilder cookieSet=new StringBuilder("");
				List<String> cookies=M.get("Set-Cookie");
				if(cookies!=null)
					for(String val : cookies)
					{
						if(cookieSet.length()>0)
							cookieSet.append(" ; ");
						int x=val.indexOf(';');
						cookieSet.append((x>=0)?val.substring(0,x).trim():val.trim());
					}
				return cookieSet.toString().replace('&','#');
			} catch (UnsupportedEncodingException e) {
				Log.errOut(Thread.currentThread().getName(),e);
			}
			return " @break@";
		}
		String url=parms.get("URL");
		if(url==null)
			return " @break@";
		if(command.equalsIgnoreCase("NUMMSGS"))
		{
			String token=parms.get("TOKEN");
			if(token==null)
				return " @break@";
			HttpClient H=(HttpClient)CMClass.getCommon("DefaultHttpClient");
			byte[] b=H.getRawUrl(url,token.replace('#','&'));
			if(b==null)
				return "Failed: Bad login token?";
			StringBuffer s=new StringBuffer(new String(b));
			CMStrings.convertHtmlToText(s);
			String txt=s.toString();
			int x=txt.indexOf(" of ");
			int num=-1;
			while((num<0)&&(x>=0))
			{
				if(Character.isDigit(txt.charAt(x+4)))
				{
					int y=4;
					while(Character.isDigit(txt.charAt(x+y)))
						y++;
					return Integer.toString(CMath.s_int(txt.substring(x+4,x+y)));
				}
				else
					x=txt.indexOf(" of ",x+1);
			}
			return "Fail: no numbers found";
		}
		return "";
	}
}
