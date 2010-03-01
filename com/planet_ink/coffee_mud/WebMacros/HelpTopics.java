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
@SuppressWarnings("unchecked")
public class HelpTopics extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("HELPTOPIC");
		if(parms.containsKey("RESET"))
		{
			if(last!=null) httpReq.removeRequestParameter("HELPTOPIC");
			httpReq.removeRequestParameter("HELPFIRSTLETTER");
			return "";
		}
		else
		if(parms.containsKey("DATA"))
		{
			int limit=70;
			if(parms.containsKey("LIMIT")) limit=CMath.s_int((String)parms.get("LIMIT"));
			if((last!=null)&&(last.length()>0))
			{
				StringBuilder s=CMLib.help().getHelpText(last,null,parms.containsKey("AHELP"));
				if(s!=null)
                    return clearWebMacros(helpHelp(s,limit).toString());
			}
			return "";
		}
		else
		if(parms.containsKey("NEXTLETTER"))
		{
			String fletter=httpReq.getRequestParameter("HELPFIRSTLETTER");
			if((fletter==null)||(fletter.length()==0))
				fletter="A";
			else
			if(fletter.charAt(0)>='Z')
			{
				httpReq.addRequestParameters("HELPFIRSTLETTER","");
				return " @break@";
			}
			else
				fletter=Character.toString((char)(fletter.charAt(0)+1));
			httpReq.addRequestParameters("HELPFIRSTLETTER",fletter);
		}
		else
		if(parms.containsKey("NEXT"))
		{
			Vector topics=null;
			if(parms.containsKey("ARCHON"))
				topics=CMLib.help().getTopics(true,false);
			else
			if(parms.containsKey("BOTH"))
				topics=CMLib.help().getTopics(true,true);
			else
				topics=CMLib.help().getTopics(false,true);

			boolean noables=parms.containsKey("SHORT");
			String fletter=(String)parms.get("FIRSTLETTER");
			if(fletter==null) fletter=httpReq.getRequestParameter("FIRSTLETTER");
			if(fletter==null) fletter="";

			String lastID="";
			for(int h=0;h<topics.size();h++)
			{
				String topic=(String)topics.elementAt(h);
				if(noables&&CMLib.help().isPlayerSkill(topic))
				   continue;
				if(topic.startsWith(fletter)||(fletter.length()==0))
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!topic.equals(lastID))))
				{
					httpReq.addRequestParameters("HELPTOPIC",topic);
					return "";
				}
				lastID=topic;
			}
			httpReq.addRequestParameters("HELPTOPIC","");
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
