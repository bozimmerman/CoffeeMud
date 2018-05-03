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
public class PlayerList extends StdWebMacro
{
	@Override
	public String name()
	{
		return "PlayerList";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final StringBuffer s = new StringBuffer("");
		for(final Session S : CMLib.sessions().allIterable())
		{
			MOB m = S.mob();
			if((m!=null)&&(CMLib.flags().isCloaked(m)))
				continue;

			if((m!=null)&&(m.soulMate()!=null))
				m=m.soulMate();

			if ((m!=null) 
			&& (m.name() != null)
			&& (m.name().length() > 0)
			&& (!S.getStatus().toString().startsWith("LOGOUT")))
			{
				s.append("<li class=\"cmPlayerListEntry");

				// jef: nb - only shows full sysops, not subops
				if ( CMSecurity.isASysOp(m) )
					s.append("Archon");
				s.append("\">");
				s.append(CMStrings.removeColors(m.Name().equals(m.name())?m.titledName():m.name()));
				s.append(" ");
				if (m.charStats().getMyRace()!= null 
				&& m.charStats().raceName()!=null
				&& m.charStats().raceName().length() > 0
				&& !m.charStats().raceName().equals("MOB")
				&& ((S.getStatus())==Session.SessionStatus.MAINLOOP))
				{
					s.append("(");
					if(!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
					{
						if(!m.charStats().getCurrentClass().raceless())
							s.append(m.charStats().raceName());
						s.append(" ");
					}
					if ( m.charStats().displayClassName().length() > 0
					&& ((!m.charStats().displayClassName().equals("MOB"))
						||CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES)
						||m.charStats().getMyRace().classless()))
					{
						if((!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
						&&(!m.charStats().getMyRace().classless())
						&&(!m.charStats().getMyRace().leveless())
						&&(!m.charStats().getCurrentClass().leveless())
						&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS)))
							s.append(m.charStats().displayClassLevel(m,true));
						else
						if((!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
						&&(!m.charStats().getMyRace().classless()))
							s.append(""+m.charStats().displayClassName());
						else
						if((!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
						&&(!m.charStats().getMyRace().leveless())
						&&(!m.charStats().getCurrentClass().leveless()))
							s.append(""+m.charStats().getClassLevel(m.charStats().getCurrentClass()));
					}
					else
					if (( m.charStats().displayClassName().length() == 0)
					|| (m.charStats().displayClassName().equals("MOB")))
						s.append("[new player]");
					s.append(")");
				}
				else
					s.append("[new player]");
			}
			s.append("\r\n");
		}
		return clearWebMacros(s);
	}

}
