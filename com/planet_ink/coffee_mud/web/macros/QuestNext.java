package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class QuestNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("QUEST");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("QUEST");
			return "";
		}
		String lastID="";
		for(int q=0;q<Quests.numQuests();q++)
		{
			Quest Q=Quests.fetchQuest(q);
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!Q.name().equals(lastID))))
			{
				httpReq.addRequestParameters("QUEST",Q.name());
				return "";
			}
			lastID=Q.name();
		}
		httpReq.addRequestParameters("QUEST","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}

}