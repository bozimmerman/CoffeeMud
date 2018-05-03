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
public class QuestMgr extends StdWebMacro
{
	@Override
	public String name()
	{
		return "QuestMgr";
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
		Quest Q=null;
		final MOB M = Authenticate.getAuthenticatedMob(httpReq);
		final String name=(M==null)?"Someone":M.Name();
		if(parms.containsKey("CREATE"))
		{
			Q=(Quest)CMClass.getCommon("DefaultQuest");
			final String err=populateQuest(httpReq,Q,false);
			if(err.length()>0)
				return err;
			CMLib.quests().addQuest(Q);
			CMLib.quests().save();
			httpReq.addFakeUrlParameter("QUEST",Q.name());
			Log.sysOut("QuestMgr",name+" created quest '"+Q.name()+"'");
			return "Quest '"+Q.name()+"' created.";
		}

		final String last=httpReq.getUrlParameter("QUEST");
		if(last==null)
			return "";
		if(last.length()>0)
		{
			Q=CMLib.quests().fetchQuest(last);
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
			if(parms.containsKey("MODIFY"))
			{
				final String err=populateQuest(httpReq,Q,parms.containsKey("REDIRECT"));
				if(err.length()>0)
					return err;
				httpReq.addFakeUrlParameter("QUEST",Q.name());
				CMLib.quests().save();
				Log.sysOut("QuestMgr",name+" modified quest '"+Q.name()+"'");
			}
			if(parms.containsKey("DELETE"))
			{
				CMLib.quests().delQuest(Q);
				CMLib.quests().save();
				httpReq.addFakeUrlParameter("QUEST","");
				final CMFile F=new CMFile(Resources.makeFileResourceName("quests/"+Q.name()+".quest"),M,CMFile.FLAG_FORCEALLOW);
				if(F.exists())
				{
					Log.sysOut("QuestMgr",name+" deleted quest '"+Q.name()+"'");
					if(F.delete())
						return "Quest script file '"+Resources.makeFileResourceName("quests/"+Q.name()+".quest")+"' deleted.";
				}
				return "Quest '"+Q.name()+"' removed from quests list.";
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
			if(parms.containsKey("ENABLE"))
			{
				if(!Q.suspended())
					return "Quest '"+Q.name()+"' was not disabled.";
				Q.setSuspended(false);
				CMLib.database().DBUpdateQuest(Q);
				return "Quest '"+Q.name()+"' enabled.";
			}
			if(parms.containsKey("DISABLE"))
			{
				if(Q.suspended())
					return "Quest '"+Q.name()+"' was already disabled.";
				Q.setSuspended(true);
				if(Q.running())
					Q.stopQuest();
				CMLib.database().DBUpdateQuest(Q);
				return "Quest '"+Q.name()+"' disabled.";
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

	public String populateQuest(HTTPRequest httpReq, Quest Q, boolean redirect)
	{
		Q.script();
		String script=httpReq.getUrlParameter("RAWTEXT");
		final String unRedirectedScript=script;
		CMFile redirectF=null;
		if(redirect
		&&(Q.script().toUpperCase().trim().startsWith("LOAD=")))
		{
			final String fileName=Q.script().trim().substring(5);
			redirectF=new CMFile(Resources.makeFileResourceName(fileName),null,CMFile.FLAG_LOGERRORS);
			if((!redirectF.exists())||(!redirectF.canRead()))
				return "Unable to load redirected file '"+fileName+"'";
		}
		else
			redirect=false;

		script=CMStrings.replaceAll(script,"&amp;","&");

		String postFix="";
		final int x=script.toUpperCase().indexOf("<?XML");
		if(x>=0)
		{
			postFix=script.substring(x);
			script=script.substring(0,x);
		}
		script=CMStrings.replaceAll(script,"'","`");
		if(redirect)
			script=CMStrings.replaceAll(script,";","\\;");
		else
		{
			script=CMStrings.replaceAll(script,"\n",";");
			script=CMStrings.replaceAll(script,"\r",";");
			script=CMStrings.replaceAll(script,";;",";");
			script=CMStrings.replaceAll(script,";;",";");
		}
		script=script.trim();
		while(script.endsWith(";"))
			script=script.substring(0,script.length()-1);
		script=script.trim();
		if((script==null)||(script.trim().length()==0))
			return "No script was specified.";
		if((redirect)&&(redirectF!=null))
		{
			redirectF.saveText(script+postFix);
			script=unRedirectedScript;
			Q.setScript(Q.script(),true);
		}
		else
			Q.setScript(script+postFix,true);
		if(Q.name().length()==0)
			return "You must specify a VALID quest string.  This one contained no name.";
		else
		if(Q.duration()<0)
			return "You must specify a VALID quest string.  This one contained no duration.";
		else
		for(int q=0;q<CMLib.quests().numQuests();q++)
		{
			final Quest Q1=CMLib.quests().fetchQuest(q);
			if(Q1.name().equalsIgnoreCase(Q.name())&&(Q1!=Q))
				return "A quest with that name already exists.";
		}
		return "";
	}
}
