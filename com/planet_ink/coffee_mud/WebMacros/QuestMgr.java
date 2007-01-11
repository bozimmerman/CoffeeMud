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
   Copyright 2000-2007 Bo Zimmerman

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
public class QuestMgr extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		Quest Q=null;
		if(parms.containsKey("CREATE"))
		{
			Q=(Quest)CMClass.getCommon("DefaultQuest");
			String err=populateQuest(httpReq,Q);
			if(err.length()>0) return err;
			CMLib.quests().addQuest(Q);
			CMLib.quests().save();
			httpReq.addRequestParameters("QUEST",Q.name());
			return "Quest '"+Q.name()+"' created.";
		}
		
		String last=httpReq.getRequestParameter("QUEST");
		if(last==null) return "";
		if(last.length()>0)
		{
			Q=CMLib.quests().fetchQuest(last);
            if(Q==null)
                for(int q=0;q<CMLib.quests().numQuests();q++)
                    if((""+CMLib.quests().fetchQuest(q)).equals(last))
                    { Q=CMLib.quests().fetchQuest(q); break;}
			if(Q==null) return "";
			if(parms.containsKey("MODIFY"))
			{
				String err=populateQuest(httpReq,Q);
				if(err.length()>0) return err;
				httpReq.addRequestParameters("QUEST",Q.name());
				CMLib.quests().save();
			}
			if(parms.containsKey("DELETE"))
			{
				CMLib.quests().delQuest(Q);
				CMLib.quests().save();
				httpReq.addRequestParameters("QUEST","");
				return "Quest '"+Q.name()+"' deleted.";
			}
			if(parms.containsKey("START"))
			{
				if(Q.running())
					return "Quest '"+Q.name()+"' was already running.";
				Q.startQuest();
				return "Quest '"+Q.name()+"' started.";
			}
			if(parms.containsKey("STOP"))
			{
				if(!Q.running())
					return "Quest '"+Q.name()+"' was not running.";
				Q.stopQuest();
				return "Quest '"+Q.name()+"' stopped.";
			}
            if(parms.containsKey("STEP"))
            {
                if(!Q.running())
                    return "Quest '"+Q.name()+"' was not running.";
                Q.stepQuest();
                return "Quest '"+Q.name()+"' stepped.";
            }
		}
		return "";
	}
	
	public String populateQuest(ExternalHTTPRequests httpReq, Quest Q)
	{
		Q.script();
		String script=httpReq.getRequestParameter("SCRIPT");
		script=CMStrings.replaceAll(script,"'","`");
		script=CMStrings.replaceAll(script,"\n",";");
		script=CMStrings.replaceAll(script,"\r",";");
		script=CMStrings.replaceAll(script,";;",";");
		script=CMStrings.replaceAll(script,";;",";");
		script=script.trim();
		while(script.endsWith(";"))
			script=script.substring(0,script.length()-1);
		script=script.trim();
		if((script==null)||(script.trim().length()==0))
			return "No script was specified.";
		Q.setScript(script);
		if(Q.name().length()==0)
			return "You must specify a VALID quest string.  This one contained no name.";
		else
		if(Q.duration()<0)
			return "You must specify a VALID quest string.  This one contained no duration.";
		else
		for(int q=0;q<CMLib.quests().numQuests();q++)
		{
			Quest Q1=CMLib.quests().fetchQuest(q);
			if(Q1.name().equalsIgnoreCase(Q.name())&&(Q1!=Q))
				return "A quest with that name already exists.";
		}
		return "";
	}
}
