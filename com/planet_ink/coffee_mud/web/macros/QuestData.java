package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


/* 
   Copyright 2000-2004 Bo Zimmerman

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
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("QUEST");
		if(last==null) return "";
		if(last.length()>0)
		{
			Quest Q=Quests.fetchQuest(last);
			if(Q==null) return "";
			if(parms.containsKey("NAME"))
				return Q.name();
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
			if(parms.containsKey("REMAINING"))
				return ""+Q.minsRemaining();
			if(parms.containsKey("WAITLEFT"))
				return ""+Q.waitRemaining();
			if(parms.containsKey("WINNERS"))
				return ""+Q.getWinnerStr();
			if(parms.containsKey("SCRIPT"))
			{
				String script=Q.script();
				script=Util.replaceAll(script,";","\n");
				return script;
			}
		}
		return "";
	}
}
