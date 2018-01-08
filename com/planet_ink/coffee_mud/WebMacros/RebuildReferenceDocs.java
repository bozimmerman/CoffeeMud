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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.core.exceptions.HTTPServerException;

/*
   Copyright 2008-2018 Bo Zimmerman

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
public class RebuildReferenceDocs extends StdWebMacro
{
	@Override
	public String name()
	{
		return "RebuildReferenceDocs";
	}

	@Override
	public boolean isAWebPath()
	{
		return true;
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) throws HTTPServerException
	{
		final MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(M==null)
			return "[Unauthorized]";
		if(!CMSecurity.isASysOp(M))
			return "[Unallowed]";
		final CMFile sourcesF = new CMFile("/web/admin/work",M,CMFile.FLAG_LOGERRORS);
		if((!sourcesF.canRead())||(!sourcesF.isDirectory())||(sourcesF.list().length==0))
			return "[Unsourced]";
		final CMFile[] sourceFiles = sourcesF.listFiles();
		final long[] processStartTime=new long[]{System.currentTimeMillis()};
		final String[] lastFoundMacro=new String[]{""};
		for (final CMFile sf : sourceFiles)
		{
			if(sf.getName().endsWith(".cmvp"))
			{
				final int sfLen=sf.getName().length();
				final CMFile df=new CMFile("/guides/refs/"+sf.getName().substring(0,sfLen-5)+".html",M);
				if(!df.canWrite())
					return "[Unwrittable: "+df.getName()+"]";
				final byte[] savable = CMLib.webMacroFilter().virtualPageFilter(httpReq, httpReq.getRequestObjects(), processStartTime, lastFoundMacro, new StringBuffer(new String(sf.raw()))).toString().getBytes();
				for(int b=0;b<savable.length-5;b++)
				{
					if((savable[b]=='.') &&(savable[b+1]=='c') &&(savable[b+2]=='m') &&(savable[b+3]=='v') &&(savable[b+4]=='p'))
					{
						savable[b+1]='h';
						savable[b+2]='t';
						savable[b+3]='m';
						savable[b+4]='l';
						b+=4;
					}
				}
				df.saveRaw(savable);
			}
		}
		return "[Done!]";
	}
}
