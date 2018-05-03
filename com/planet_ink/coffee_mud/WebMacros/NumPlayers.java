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
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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
public class NumPlayers extends StdWebMacro
{
	@Override
	public String name()
	{
		return "NumPlayers";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		if(parms.containsKey("ALL"))
			return ""+CMLib.sessions().getCountLocalOnline();
		if(parms.containsKey("TOTALCACHED"))
			return ""+CMLib.players().numPlayers();
		if(parms.containsKey("TOTAL"))
		{
			final Enumeration<ThinPlayer> pe=CMLib.players().thinPlayers("",httpReq.getRequestObjects());
			int x=0;
			for(;pe.hasMoreElements();pe.nextElement()) 
				x++;
			return ""+x;
		}
		if(parms.containsKey("ACCOUNTS"))
		{
			final Enumeration<PlayerAccount> pe=CMLib.players().accounts("",httpReq.getRequestObjects());
			int x=0;
			for(;pe.hasMoreElements();pe.nextElement()) 
				x++;
			return ""+x;
		}

		int numPlayers=0;
		for(final Session S : CMLib.sessions().localOnlineIterable())
		{
			if((S.mob()!=null)&&(!CMLib.flags().isCloaked(S.mob())))
				numPlayers++;
		}
		return Integer.toString(numPlayers);
	}

}
