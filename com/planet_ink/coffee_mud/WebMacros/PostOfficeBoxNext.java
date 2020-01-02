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
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
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
public class PostOfficeBoxNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "PostOfficeBoxNext";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String chain=httpReq.getUrlParameter("POSTCHAIN");
		if(chain == null)
			return " @break@";

		String last=httpReq.getUrlParameter("POSTBOX");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("POSTBOX");
			return "";
		}

		final MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(M==null)
			return " @break@";

		final List<String> postalBoxes;
		if(httpReq.getRequestObjects().containsKey("POSTAL_BOXES_"+chain))
			postalBoxes = (List<String>)httpReq.getRequestObjects().get("POSTAL_BOXES_"+chain);
		else
		if(CMSecurity.isAllowedEverywhere(M, CMSecurity.SecFlag.CMDPLAYERS))
		{
			postalBoxes=CMLib.database().DBReadPlayerDataAuthorsBySection(chain);
			postalBoxes.remove(chain);
			postalBoxes.add(0, chain);
			httpReq.getRequestObjects().put("POSTAL_BOXES_"+chain, postalBoxes);
		}
		else
		{
			postalBoxes=new ArrayList<String>();
			postalBoxes.add(M.Name());
			for(final Pair<Clan,Integer> C : M.clans())
			{
				if(C.first.getAuthority(C.second.intValue(),Function.WITHDRAW)!=Authority.CAN_NOT_DO)
				{
					if(!postalBoxes.contains(C.first.name()))
						postalBoxes.add(C.first.name());
				}
			}
			httpReq.getRequestObjects().put("POSTAL_BOXES_"+chain, postalBoxes);
		}
		String lastID="";
		for(final String postalBox : postalBoxes)
		{
			if((last==null)
			||((last.length()>0)
				&&(last.equals(lastID))))
			{
				httpReq.addFakeUrlParameter("POSTBOX",postalBox);
				last=postalBox;
				return "";
			}
			lastID=postalBox;
		}
		httpReq.addFakeUrlParameter("POSTBOX","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}

}
