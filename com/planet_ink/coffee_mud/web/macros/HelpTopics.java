package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


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
			if((last!=null)&&(last.length()>0))
			{
				StringBuffer s=ExternalPlay.getHelpText(last);
				if(s!=null)
					return helpHelp(s).toString();
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
				fletter=new Character((char)(fletter.charAt(0)+1)).toString();
			httpReq.addRequestParameters("HELPFIRSTLETTER",fletter);
		}
		else
		if(parms.containsKey("NEXT"))
		{
			Vector topics=null;
			if(parms.containsKey("ARCHON"))
				topics=ExternalPlay.getTopics(true,false);
			else
			if(parms.containsKey("BOTH"))
				topics=ExternalPlay.getTopics(true,true);
			else
				topics=ExternalPlay.getTopics(false,true);
		
			boolean noables=parms.containsKey("SHORT");
			String fletter=(String)parms.get("FIRSTLETTER");
			if(fletter==null) fletter=httpReq.getRequestParameter("FIRSTLETTER");
			if(fletter==null) fletter="";
		
			String lastID="";
			for(int h=0;h<topics.size();h++)
			{
				String topic=(String)topics.elementAt(h);
				if(noables&&(topic.startsWith("SPELL_")
							 ||topic.startsWith("SONG_")
							 ||topic.startsWith("DANCE_")
							 ||topic.startsWith("BEHAVIOR_")
							 ||topic.startsWith("CHANT_")
							 ||topic.startsWith("PRAYER_")
							 ||topic.startsWith("PROP_")))
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
			else
				return " @break@";
		}
		else
		if(last!=null)
			return last;
		return "<!--EMPTY-->";
	}

}