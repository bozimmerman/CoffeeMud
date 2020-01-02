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
public class PostOfficeNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "PostOfficeNext";
	}

	@SuppressWarnings("unchecked")
	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		MOB playerM=null;
		boolean destroyPlayer=false;
		try
		{
			final java.util.Map<String,String> parms=parseParms(parm);
			String last=httpReq.getUrlParameter("POSTCHAIN");
			String player=httpReq.getUrlParameter("PLAYER");
			if((player==null)||(player.length()==0))
				player=httpReq.getUrlParameter("CLAN");
			if(parms.containsKey("RESET"))
			{
				if(last!=null)
					httpReq.removeUrlParameter("POSTCHAIN");
				return "";
			}
			String lastID="";
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return " @break@";
			if((player!=null)&&(player.length()>0))
			{
				if(((!M.Name().equalsIgnoreCase(player)))
				&&(!CMSecurity.isAllowedEverywhere(M,CMSecurity.SecFlag.CMDPLAYERS)))
					return "";
				final Clan C=CMLib.clans().getClanExact(player);
				if(C!=null)
				{
					playerM=CMClass.getFactoryMOB();
					playerM.setName(C.clanID());
					playerM.setLocation(M.location());
					playerM.setStartRoom(M.getStartRoom());
					playerM.setClan(C.clanID(),C.getTopRankedRoles(Function.WITHDRAW).get(0).intValue());
					destroyPlayer=true;
				}
				else
				{
					playerM=CMLib.players().getPlayerAllHosts(player);
					if(playerM==null)
					{
						playerM=CMClass.getFactoryMOB();
						playerM.setName(CMStrings.capitalizeAndLower(player));
						playerM.setLocation(M.location());
						playerM.setStartRoom(M.getStartRoom());
						destroyPlayer=true;
					}
				}
			}
			else
			if(!CMSecurity.isAllowedEverywhere(M,CMSecurity.SecFlag.CMDPLAYERS))
				return "";

			final Map<String,PostOffice> postalChains;
			if(httpReq.getRequestObjects().containsKey("POSTAL_CHAINS"))
				postalChains = (TreeMap<String,PostOffice>)httpReq.getRequestObjects().get("POSTAL_CHAINS");
			else
			{
				postalChains=new TreeMap<String,PostOffice>();
				for(final Enumeration<PostOffice> p=CMLib.map().postOffices();p.hasMoreElements();)
				{
					final PostOffice P = p.nextElement();
					if(!postalChains.containsKey(P.postalChain()))
						postalChains.put(P.postalChain(),P);
				}
				httpReq.getRequestObjects().put("POSTAL_CHAINS",postalChains);
			}
			for(final String postChain : postalChains.keySet())
			{
				if((last==null)
				||((last.length()>0)
					&&(last.equals(lastID))
					&&(!postChain.equals(lastID))))
				{
					httpReq.addFakeUrlParameter("POSTCHAIN",postChain);
					last=postChain;
					if(playerM!=null)
					{
						final PostOffice postM=postalChains.get(postChain);
						if((postM==null)
						||((postM.isSold(ShopKeeper.DEAL_CLANPOSTMAN))&&(playerM.getClanRole(playerM.Name())==null)))
						{
							lastID=postChain;
							continue;
						}
					}
					return "";
				}
				lastID=postChain;
			}
			httpReq.addFakeUrlParameter("POSTCHAIN","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}
		finally
		{
			if((destroyPlayer)&&(playerM!=null))
			{
				playerM.setLocation(null);
				playerM.destroy();
			}
		}
	}

}
