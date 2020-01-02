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
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.CommandJournal;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class PostOfficeBranchNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "PostOfficeBranchNext";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String chain=httpReq.getUrlParameter("POSTCHAIN");

		String last=httpReq.getUrlParameter("POSTBRANCH");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("POSTBRANCH");
			return "";
		}

		final Map<String,PostOffice> postalBranches;
		if(httpReq.getRequestObjects().containsKey("POSTAL_BRANCHES_"+chain))
			postalBranches = (TreeMap<String,PostOffice>)httpReq.getRequestObjects().get("POSTAL_BRANCHES_"+chain);
		else
		{
			postalBranches=new TreeMap<String,PostOffice>();
			for(final Enumeration<PostOffice> p=CMLib.map().postOffices();p.hasMoreElements();)
			{
				final PostOffice P2 = p.nextElement();
				if(!postalBranches.containsKey(P2.postalBranch())
				&&((chain==null)||(chain.length()==0)||(P2.postalChain().equalsIgnoreCase(chain))))
					postalBranches.put(P2.postalBranch(),P2);
			}
			httpReq.getRequestObjects().put("POSTAL_BRANCHES_"+chain,postalBranches);
		}
		String lastID="";
		for(final String postBranch : postalBranches.keySet())
		{
			if((last==null)
			||((last.length()>0)
				&&(last.equals(lastID))
				&&(!postBranch.equals(lastID))))
			{
				httpReq.addFakeUrlParameter("POSTBRANCH",postBranch);
				last=postBranch;
				return "";
			}
			lastID=postBranch;
		}
		httpReq.addFakeUrlParameter("POSTBRANCH","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}

}
