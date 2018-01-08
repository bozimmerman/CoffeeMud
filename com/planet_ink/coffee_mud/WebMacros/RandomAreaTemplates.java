package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.MultiPartData;
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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.regex.Pattern;

/*
   Copyright 2013-2018 Bo Zimmerman

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
public class RandomAreaTemplates extends StdWebMacro
{
	@Override
	public String name()
	{
		return "RandomAreaTemplates";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(M==null)
			return "[authentication error]";
		try
		{
			final String last=httpReq.getUrlParameter("RTEMPLATE");
			if(parms.containsKey("NEXT"))
			{
				if(parms.containsKey("RESET"))
				{
					if(last!=null)
						httpReq.removeUrlParameter("RTEMPLATE");
					return "";
				}
				if(last==null)
					return " @break@";
				List<String> fileList=(List<String>)httpReq.getRequestObjects().get("RANDOMAREATEMPLATESLIST");
				if(fileList==null)
				{
					fileList=new ArrayList<String>();
					final List<String> templateDirs=new LinkedList<String>();
					templateDirs.add("");
					while(templateDirs.size()>0)
					{
						final String templateDirPath=templateDirs.remove(0);
						final CMFile templateDir=new CMFile(Resources.buildResourcePath("randareas/"+templateDirPath),M);
						for(final CMFile file : templateDir.listFiles())
						{
							if(file.isDirectory() && file.canRead())
								templateDirs.add(templateDirPath+file.getName()+"/");
							else
								fileList.add(templateDirPath+file.getName());
						}
					}
					httpReq.getRequestObjects().put("RANDOMAREATEMPLATESLIST", fileList);
				}
				String lastID="";
				for (final String RC : fileList)
				{
					if((last.length()>0)&&(last.equals(lastID))&&(!RC.equals(lastID)))
					{
						httpReq.addFakeUrlParameter("RTEMPLATE",RC);
						return "";
					}
					lastID=RC;
				}
				httpReq.addFakeUrlParameter("RTEMPLATE","");
				if(parms.containsKey("EMPTYOK"))
					return "<!--EMPTY-->";
				return " @break@";
			}
		}
		catch(final Exception e)
		{
			return "[an error occurred performing the last operation]";
		}
		return "";
	}
}
