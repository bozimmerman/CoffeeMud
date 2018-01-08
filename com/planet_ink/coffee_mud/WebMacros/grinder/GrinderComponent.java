package com.planet_ink.coffee_mud.WebMacros.grinder;

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
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/*
   Copyright 2011-2018 Bo Zimmerman

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
public class GrinderComponent
{
	public String name()
	{
		return "GrinderComponent";
	}

	public String runMacro(HTTPRequest httpReq, String parm)
	{
		final String last=httpReq.getUrlParameter("COMPONENT");
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			final String fixedCompID=last.replace(' ','_').toUpperCase();
			final List<AbilityComponent> set=new Vector<AbilityComponent>();
			int posDex=1;
			while(httpReq.isUrlParameter(fixedCompID+"_PIECE_CONNECTOR_"+posDex) && httpReq.getUrlParameter(fixedCompID+"_PIECE_CONNECTOR_"+posDex).trim().length()>0)
			{
				final String mask=httpReq.getUrlParameter(fixedCompID+"_PIECE_MASK_"+posDex);
				final String str=httpReq.getUrlParameter(fixedCompID+"_PIECE_STRING_"+posDex);
				final String amt=httpReq.getUrlParameter(fixedCompID+"_PIECE_AMOUNT_"+posDex);
				final String conn=httpReq.getUrlParameter(fixedCompID+"_PIECE_CONNECTOR_"+posDex);
				final String loc=httpReq.getUrlParameter(fixedCompID+"_PIECE_LOCATION_"+posDex);
				final String type=httpReq.getUrlParameter(fixedCompID+"_PIECE_TYPE_"+posDex);
				final String consumed=httpReq.getUrlParameter(fixedCompID+"_PIECE_CONSUMED_"+posDex);
				if(!conn.equalsIgnoreCase("DELETE"))
				{
					final AbilityComponent able=(AbilityComponent)CMClass.getCommon("DefaultAbilityComponent");
					able.setAmount(CMath.s_int(amt));
					if(posDex==1)
						able.setConnector(AbilityComponent.CompConnector.AND);
					else
						able.setConnector(AbilityComponent.CompConnector.valueOf(conn));
					able.setConsumed((consumed!=null)&&(consumed.equalsIgnoreCase("on")||consumed.equalsIgnoreCase("checked")));
					able.setLocation(AbilityComponent.CompLocation.valueOf(loc));
					able.setMask(mask);
					able.setType(AbilityComponent.CompType.valueOf(type), str);
					set.add(able);
				}
				posDex++;
			}

			if(CMLib.ableComponents().getAbilityComponentMap().containsKey(last.toUpperCase().trim()))
			{
				final List<AbilityComponent> xset=CMLib.ableComponents().getAbilityComponentMap().get(last.toUpperCase().trim());
				xset.clear();
				xset.addAll(set);
			}
			else
				CMLib.ableComponents().getAbilityComponentMap().put(last.toUpperCase().trim(),set);
			CMLib.ableComponents().alterAbilityComponentFile(last.toUpperCase().trim(),false);
		}
		return "";
	}
}
