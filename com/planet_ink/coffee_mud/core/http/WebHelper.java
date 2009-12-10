package com.planet_ink.coffee_mud.core.http;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.net.*;
import java.util.*;


/* 
   Portions Copyright 2002 Jeff Kamenek
   Portions Copyright 2002-2010 Bo Zimmerman

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
public class WebHelper
{
	// makes a simple error page if no error.cmvp exists
	//  does NOT imbed macros just inserts directly into page -
	//   this means no dependancies on pre-existing macros
	public static byte[] makeErrorPage(String s1, String s2)
	{
		StringBuffer s = new StringBuffer("<html><head><title>");
		s.append(s1);
		s.append("</title></head><body><p><h1>");
		s.append(s1);
		s.append("</h1>");

		if (s2 != null)
		{
			s.append(s2);
			s.append("<br>");
		}

		s.append("<br><hr><i>");
		s.append(HTTPserver.ServerVersionString);
		s.append("</i></body></html>");

		return s.toString().getBytes();
	}
}
