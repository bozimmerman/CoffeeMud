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
   Copyright 2003-2018 Bo Zimmerman

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

public class BaseCharClassNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "BaseCharClassNext";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("BASECLASS");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("BASECLASS");
			return "";
		}
		String lastID="";
		final Vector<String> baseClasses=new Vector<String>();
		boolean showAll=parms.containsKey("ALL");
		boolean includeSkillOnly=parms.containsKey("INCLUDESKILLONLY");
		for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
		{
			final CharClass C=c.nextElement();
			if(((CMProps.isTheme(C.availabilityCode()))||showAll)
			&&((!CMath.bset(C.availabilityCode(), Area.THEME_SKILLONLYMASK))||includeSkillOnly||showAll))
			{
				if(!baseClasses.contains(C.baseClass()))
					baseClasses.addElement(C.baseClass());
			}
		}
		for(int i=0;i<baseClasses.size();i++)
		{
			final String C=baseClasses.elementAt(i);
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!C.equals(lastID))))
			{
				httpReq.addFakeUrlParameter("BASECLASS",C);
				return "";
			}
			lastID=C;
		}
		httpReq.addFakeUrlParameter("BASECLASS","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
