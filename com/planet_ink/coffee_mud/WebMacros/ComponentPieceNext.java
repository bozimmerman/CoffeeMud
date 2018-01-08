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
public class ComponentPieceNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "ComponentPieceNext";
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
		final String compID=httpReq.getUrlParameter("COMPONENT");
		if(compID==null)
			return " @break@";
		final String fixedCompID=compID.replace(' ','_').toUpperCase();
		if(!httpReq.isUrlParameter(fixedCompID+"_PIECE_CONNECTOR_1"))
		{
			final List<AbilityComponent> set=CMLib.ableComponents().getAbilityComponents(compID);
			if(set!=null)
			{
				int index=1;
				for(final AbilityComponent A : set)
				{
					httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_MASK_"+index, A.getMaskStr());
					if(A.getType()==AbilityComponent.CompType.STRING)
						httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_STRING_"+index, A.getStringType());
					else
						httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_STRING_"+index, Long.toString(A.getLongType()));
					httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_AMOUNT_"+index, Integer.toString(A.getAmount()));
					httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_CONNECTOR_"+index, A.getConnector().toString());
					httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_LOCATION_"+index, A.getLocation().toString());
					httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_TYPE_"+index, A.getType().toString());
					httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_CONSUMED_"+index, A.isConsumed()?"on":"");
					index++;
				}
			}
			else
			{
				httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_MASK_1", "");
				httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_STRING_1", "item name");
				httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_AMOUNT_1", "1");
				httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_CONNECTOR_1", "AND");
				httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_LOCATION_1", "INVENTORY");
				httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_TYPE_1", "STRING");
				httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_CONSUMED_1", "on");
			}
		}
		else
		{
			int oldIndex=1;
			int newIndex=1;
			while(httpReq.isUrlParameter(fixedCompID+"_PIECE_CONNECTOR_"+oldIndex))
			{
				final String type=httpReq.getUrlParameter(fixedCompID+"_PIECE_CONNECTOR_"+oldIndex);
				if((type.length()>0)&&(!type.equalsIgnoreCase("DELETE")))
				{
					if(newIndex != oldIndex)
					{
						httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_MASK_"+newIndex, httpReq.getUrlParameter(fixedCompID+"_PIECE_MASK_"+oldIndex));
						httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_STRING_"+newIndex, httpReq.getUrlParameter(fixedCompID+"_PIECE_STRING_"+oldIndex));
						httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_AMOUNT_"+newIndex, httpReq.getUrlParameter(fixedCompID+"_PIECE_AMOUNT_"+oldIndex));
						httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_CONNECTOR_"+newIndex, httpReq.getUrlParameter(fixedCompID+"_PIECE_CONNECTOR_"+oldIndex));
						httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_LOCATION_"+newIndex, httpReq.getUrlParameter(fixedCompID+"_PIECE_LOCATION_"+oldIndex));
						httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_TYPE_"+newIndex, httpReq.getUrlParameter(fixedCompID+"_PIECE_TYPE_"+oldIndex));
						httpReq.addFakeUrlParameter(fixedCompID+"_PIECE_CONSUMED_"+newIndex, httpReq.getUrlParameter(fixedCompID+"_PIECE_CONSUMED_"+oldIndex));
					}
					newIndex++;
				}
				oldIndex++;
			}
			httpReq.removeUrlParameter(fixedCompID+"_PIECE_MASK_"+newIndex);
			httpReq.removeUrlParameter(fixedCompID+"_PIECE_STRING_"+newIndex);
			httpReq.removeUrlParameter(fixedCompID+"_PIECE_AMOUNT_"+newIndex);
			httpReq.removeUrlParameter(fixedCompID+"_PIECE_CONNECTOR_"+newIndex);
			httpReq.removeUrlParameter(fixedCompID+"_PIECE_LOCATION_"+newIndex);
			httpReq.removeUrlParameter(fixedCompID+"_PIECE_TYPE_"+newIndex);
			httpReq.removeUrlParameter(fixedCompID+"_PIECE_CONSUMED_"+newIndex);
		}
		final String last=httpReq.getUrlParameter("COMPONENTPIECE");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("COMPONENTPIECE");
			return "";
		}
		String lastID="";
		for(int index=1;httpReq.isUrlParameter(fixedCompID+"_PIECE_CONNECTOR_"+index);index++)
		{
			final String id=Integer.toString(index);
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!id.equalsIgnoreCase(lastID))))
			{
				httpReq.addFakeUrlParameter("COMPONENTPIECE",id);
				return "";
			}
			lastID=id;
		}
		httpReq.addFakeUrlParameter("COMPONENTPIECE","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
