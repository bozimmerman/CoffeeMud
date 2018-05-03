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

public class QuestNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "QuestNext";
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
		String last=httpReq.getUrlParameter("QUEST");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("QUEST");
			return "";
		}
		if(last!=null)
			last=CMStrings.replaceAll(last,"*","@");
		String lastID="";

		final Vector<Quest> V=new Vector<Quest>();
		for(int q=0;q<CMLib.quests().numQuests();q++)
			V.addElement(CMLib.quests().fetchQuest(q));
		final Vector<Quest> sortedV=new Vector<Quest>();
		while(V.size()>0)
		{
			Quest lowQ=V.firstElement();
			for(int v=1;v<V.size();v++)
			{
				if(V.elementAt(v).name().toUpperCase().compareTo(lowQ.name().toUpperCase())<0)
					lowQ=V.elementAt(v);
			}
			V.remove(lowQ);
			sortedV.addElement(lowQ);
		}

		for(int q=0;q<sortedV.size();q++)
		{
			final Quest Q=sortedV.elementAt(q);
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!(""+Q).equals(lastID))))
			{
				httpReq.addFakeUrlParameter("QUEST",CMStrings.replaceAll(""+Q,"@","*"));
				return "";
			}
			lastID=""+Q;
		}
		httpReq.addFakeUrlParameter("QUEST","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}

}
