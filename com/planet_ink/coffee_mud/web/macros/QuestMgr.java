package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


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
			Q=new Quests();
			String err=populateQuest(httpReq,Q);
			if(err.length()>0) return err;
			Quests.addQuest(Q);
			Quests.save();
			httpReq.addRequestParameters("QUEST",Q.name());
			return "Quest '"+Q.name()+"' created.";
		}
		
		String last=httpReq.getRequestParameter("QUEST");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Q=Quests.fetchQuest(last);
			if(Q==null) return " @break@";
			if(parms.containsKey("MODIFY"))
			{
				String err=populateQuest(httpReq,Q);
				if(err.length()>0) return err;
				httpReq.addRequestParameters("QUEST",Q.name());
				Quests.save();
			}
			if(parms.containsKey("DELETE"))
			{
				String name=Q.name();
				Quests.delQuest(Q);
				Quests.save();
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
		}
		return "";
	}
	
	public String populateQuest(ExternalHTTPRequests httpReq, Quest Q)
	{
		String oldParm=Q.script();
		String script=httpReq.getRequestParameter("SCRIPT");
		script=Util.replaceAll(script,"'","`");
		script=Util.replaceAll(script,"\n",";");
		script=Util.replaceAll(script,"\r",";");
		script=Util.replaceAll(script,";;",";");
		script=Util.replaceAll(script,";;",";");
		if((script==null)||(script.trim().length()==0))
			return "No script was specified.";
		Q.setScript(script.trim());
		if(Q.name().length()==0)
			return "You must specify a VALID quest string.  This one contained no name.";
		else
		if(Q.duration()<0)
			return "You must specify a VALID quest string.  This one contained no duration.";
		else
		for(int q=0;q<Quests.numQuests();q++)
		{
			Quest Q1=Quests.fetchQuest(q);
			if(Q1.name().equalsIgnoreCase(Q.name())&&(Q1!=Q))
				return "A quest with that name already exists.";
		}
		return "";
	}
}
