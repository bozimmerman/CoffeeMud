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
public class HelpTopics extends StdWebMacro
{
	@Override
	public String name()
	{
		return "HelpTopics";
	}

	@Override @SuppressWarnings({ "unchecked", "rawtypes" })
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("HELPTOPIC");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("HELPTOPIC");
			httpReq.removeUrlParameter("HELPFIRSTLETTER");
			return "";
		}
		else
		if(parms.containsKey("DATA"))
		{
			int limit=78;
			if(parms.containsKey("LIMIT"))
				limit=CMath.s_int(parms.get("LIMIT"));
			if((last!=null)&&(last.length()>0))
			{
				final StringBuilder s=CMLib.help().getHelpText(last,null,parms.containsKey("AHELP"));
				if(s!=null)
					return clearWebMacros(helpHelp(s,limit).toString());
			}
			return "";
		}
		else
		if(parms.containsKey("NEXTLETTER"))
		{
			String fletter=httpReq.getUrlParameter("HELPFIRSTLETTER");
			if((fletter==null)||(fletter.length()==0))
				fletter="A";
			else
			if(fletter.charAt(0)>='Z')
			{
				httpReq.addFakeUrlParameter("HELPFIRSTLETTER","");
				return " @break@";
			}
			else
				fletter=Character.toString((char)(fletter.charAt(0)+1));
			httpReq.addFakeUrlParameter("HELPFIRSTLETTER",fletter);
		}
		else
		if(parms.containsKey("NEXT"))
		{
			List<String> topics=null;
			if(parms.containsKey("ARCHON"))
			{
				topics=(List)httpReq.getRequestObjects().get("HELP_ARCHONTOPICS");
				if(topics==null)
				{
					topics=CMLib.help().getTopics(true,false);
					httpReq.getRequestObjects().put("HELP_ARCHONTOPICS", topics);
				}
			}
			else
			if(parms.containsKey("BOTH"))
			{
				topics=(List)httpReq.getRequestObjects().get("HELP_BOTHTOPICS");
				if(topics==null)
				{
					topics=CMLib.help().getTopics(true,true);
					httpReq.getRequestObjects().put("HELP_BOTHTOPICS", topics);
				}
			}
			else
			{
				topics=(List)httpReq.getRequestObjects().get("HELP_HELPTOPICS");
				if(topics==null)
				{
					topics=CMLib.help().getTopics(false,true);
					httpReq.getRequestObjects().put("HELP_HELPTOPICS", topics);
				}
			}

			final boolean noables=parms.containsKey("SHORT");
			String fletter=parms.get("FIRSTLETTER");
			if(fletter==null)
				fletter=httpReq.getUrlParameter("FIRSTLETTER");
			if(fletter==null)
				fletter="";
			String lastID="";
			for(int h=0;h<topics.size();h++)
			{
				final String topic=topics.get(h);
				if(noables&&CMLib.help().isPlayerSkill(topic))
					continue;
				if(topic.startsWith(fletter)||(fletter.length()==0))
				{
					if((last==null)
					||((last.length()>0)&&(last.equals(lastID))&&(!topic.equals(lastID))&&(topic.length()>0)))
					{
						httpReq.addFakeUrlParameter("HELPTOPIC",topic);
						return "";
					}
				}
				lastID=topic;
			}
			httpReq.addFakeUrlParameter("HELPTOPIC","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}
		else
		if(last!=null)
			return last;
		return "<!--EMPTY-->";
	}

}
