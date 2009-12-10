package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
	public String name()	{return "PlayerList";}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		StringBuffer s = new StringBuffer("");
		for(int i=0;i<CMLib.sessions().size();i++)
		{
			Session session=CMLib.sessions().elementAt(i);
			// list entry with style sheet class
			
			MOB m = session.mob();
			if((m!=null)&&(CMLib.flags().isCloaked(m))) continue;
			
			s.append("<li class=\"cmPlayerListEntry");
			
			if((m!=null)&&(m.soulMate()!=null))
				m=m.soulMate();

			if ( (m!=null) && (m.name() != null)
				&& (m.name().length() > 0) )
			{
				// jef: nb - only shows full sysops, not subops
				if ( CMSecurity.isASysOp(m) )
					s.append("Archon");
				s.append("\">");
				s.append(CMStrings.removeColors(m.Name().equals(m.name())?m.titledName():m.name()));
				s.append(" ");
				if (m.charStats().getMyRace()!= null && m.charStats().raceName()!=null
					&& m.charStats().raceName().length() > 0
					&& !m.charStats().raceName().equals("MOB"))
				{
					s.append("(");
					if(!CMSecurity.isDisabled("RACES"))
					{
						if(!m.charStats().getCurrentClass().raceless())
							s.append(m.charStats().raceName());
						s.append(" ");
					}
					if ( m.charStats().displayClassName().length() > 0
					&& ((!m.charStats().displayClassName().equals("MOB"))
				        ||CMSecurity.isDisabled("CLASSES")
						||m.charStats().getMyRace().classless()))
					{
						if((!CMSecurity.isDisabled("CLASSES"))
						&&(!m.charStats().getMyRace().classless())
						&&(!m.charStats().getMyRace().leveless())
						&&(!m.charStats().getCurrentClass().leveless())
						&&(!CMSecurity.isDisabled("LEVELS")))
							s.append(m.charStats().displayClassLevel(m,true));
						else
						if((!CMSecurity.isDisabled("CLASSES"))
						&&(!m.charStats().getMyRace().classless()))
						    s.append(""+m.charStats().displayClassName());
						else
						if((!CMSecurity.isDisabled("LEVELS"))
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
			else
			{
				s.append("\">");
				s.append("[logging in]");
			}
			s.append("\r\n");
		}
        return clearWebMacros(s);
	}

}
