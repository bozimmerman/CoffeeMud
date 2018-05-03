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
public class QuestData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "QuestData";
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
		final String last=httpReq.getUrlParameter("QUEST");
		if(last==null)
			return "";
		if(last.length()>0)
		{
			Quest Q=CMLib.quests().fetchQuest(last);
			if(Q==null)
			{
				final String newLast=CMStrings.replaceAll(last,"*","@");
				for(int q=0;q<CMLib.quests().numQuests();q++)
				{
					if((""+CMLib.quests().fetchQuest(q)).equals(newLast))
					{
						Q=CMLib.quests().fetchQuest(q);
						break;
					}
				}
			}
			if(Q==null)
				return "";
			if(parms.containsKey("NAME"))
				return clearWebMacros(Q.name());
			if(parms.containsKey("ID"))
				return clearWebMacros(CMStrings.replaceAll(""+Q,"@","*"));
			if(parms.containsKey("DURATION"))
				return ""+Q.duration();
			if(parms.containsKey("WAIT"))
				return ""+Q.minWait();
			if(parms.containsKey("INTERVAL"))
				return ""+Q.waitInterval();
			if(parms.containsKey("RUNNING"))
				return ""+Q.running();
			if(parms.containsKey("WAITING"))
				return ""+Q.waiting();
			if(parms.containsKey("SUSPENDED"))
				return ""+Q.suspended();
			if(parms.containsKey("REMAINING"))
				return ""+Q.minsRemaining();
			if(parms.containsKey("REMAININGLEFT"))
			{
				if(Q.duration()==0)
					return "eternity";
				return Q.minsRemaining()+" minutes";
			}
			if(parms.containsKey("WAITLEFT"))
				return ""+Q.waitRemaining();
			if(parms.containsKey("WAITMINSLEFT"))
			{
				long min=Q.waitRemaining();
				if(min>0)
				{
					min=min*CMProps.getTickMillis();
					if(min>60000)
						return (min/60000)+" minutes";
					return (min/1000)+" seconds";
				}
				return min+" minutes";
			}
			if(parms.containsKey("WINNERS"))
				return ""+Q.getWinnerStr();
			if(parms.containsKey("RAWTEXT"))
			{
				StringBuffer script=new StringBuffer(Q.script());
				if((parms.containsKey("REDIRECT"))
				&&(script.toString().toUpperCase().trim().startsWith("LOAD=")))
				{
					final String fileName=script.toString().trim().substring(5);
					final CMFile F=new CMFile(Resources.makeFileResourceName(fileName),null,CMFile.FLAG_LOGERRORS);
					if((F.exists())&&(F.canRead()))
						script=F.text();
					script=new StringBuffer(script.toString().replaceAll("\n\r","\n"));
				}
				script=new StringBuffer(CMStrings.replaceAll(script.toString(),"&","&amp;"));
				String postFix="";
				final int limit=script.toString().toUpperCase().indexOf("<?XML");
				if(limit>=0)
				{
					postFix=script.toString().substring(limit);
					script=new StringBuffer(script.toString().substring(0,limit));
				}
				for(int i=0;i<script.length();i++)
				{
					if((script.charAt(i)==';')
					&&((i==0)||(script.charAt(i-1)!='\\')))
						script.setCharAt(i,'\n');
				}
				script=new StringBuffer(CMStrings.replaceAll(script.toString(),"\\;",";"));
				return clearWebMacros(script+postFix);
 			}
		}
		return "";
	}
}
