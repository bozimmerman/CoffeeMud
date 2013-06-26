package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.miniweb.http.MIMEType;
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
public class FileInfo extends StdWebMacro
{
	public String name() { return "FileInfo"; }
	public boolean isAdminMacro()	{return true;}

	public String runMacro(HTTPRequest httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String path=httpReq.getUrlParameter("PATH");
		if(path==null) path="";
		String file=httpReq.getUrlParameter("FILE");
		if(file==null) file="";
		MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(M==null) return "[authentication error]";
		try
		{
			CMFile F=new CMFile(path+"/"+file,M,false);
			if(parms.containsKey("ISDIRECTORY"))
				return ""+F.isDirectory();
			if(parms.containsKey("ISFILE"))
				return ""+F.isFile();
			if(parms.containsKey("ISLOCAL"))
				return ""+F.canLocalEquiv();
			if(parms.containsKey("ISBOTH"))
				return ""+(F.canLocalEquiv()&&(F.canVFSEquiv()));
			if(parms.containsKey("ISVFS"))
				return ""+F.canVFSEquiv();
			if(parms.containsKey("ISTEXT"))
			{
				int x=F.getName().lastIndexOf('.');
				if(x<0) return "false";
				String mime=MIMEType.getMIMEType(F.getName().substring(x)).getType();
				if(mime.toUpperCase().startsWith("TEXT"))
					return "true";
				return "false";
			}
			if(parms.containsKey("ISBINARY"))
			{
				int x=F.getName().lastIndexOf('.');
				if(x<0) return "true";
				String mime=MIMEType.getMIMEType(F.getName().substring(x)).getType();
				if(mime.toUpperCase().startsWith("TEXT"))
					return "false";
				return "true";
			}
			if(parms.containsKey("NAME"))
				return ""+F.getName();
			if(parms.containsKey("DATA"))
				return F.textUnformatted().toString();
			if(parms.containsKey("TEXTDATA"))
			{
				String s=F.text().toString();
				s=CMStrings.replaceAll(s,"\n\r","\n");
				s=CMStrings.replaceAll(s,"&","&amp;");
				s=CMStrings.replaceAll(s,"@","&#64;");
				s=CMStrings.replaceAll(s,"<","&lt;");
				return s;
			}
		}
		catch(Exception e)
		{
			return "[error]";
		}
		return "";
	}
}
