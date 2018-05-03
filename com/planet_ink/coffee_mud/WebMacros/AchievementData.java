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
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Award;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.AwardType;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.TitleAward;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2015-2018 Bo Zimmerman

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
public class AchievementData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AchievementData";
	}

	public void rebuildTrackers(String tattoo)
	{
		for(Enumeration<MOB> m = CMLib.players().players();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if(M.playerStats()!=null)
			{
				M.playerStats().rebuildAchievementTracker(M, tattoo);
			}
		}
	}
	
	public String deleteAchievement(String tattoo)
	{
		Achievement A=CMLib.achievements().deleteAchievement(tattoo);
		if(A!=null)
		{
			rebuildTrackers(A.getTattoo());
			CMLib.achievements().resaveAchievements(A.getTattoo());
		}
		return "";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("ACHIEVEMENT");
		if((last==null)&&(!parms.containsKey("EDIT")))
			return " @break@";

		String agentStr = parms.get("AGENT");
		if(agentStr == null)
			agentStr=httpReq.getUrlParameter("AGENT");
		AccountStats.Agent agent = ((agentStr == null)||(agentStr.length()==0)||(!CMProps.isUsingAccountSystem())) ? 
				AccountStats.Agent.PLAYER : (AccountStats.Agent)CMath.s_valueOf(AccountStats.Agent.class, agentStr.toUpperCase().trim());
		if(agent == null)
		{
			agent = AccountStats.Agent.PLAYER;
		}
		
		if(parms.containsKey("EDIT"))
		{
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return "[authentication error]";
			if(!CMSecurity.isAllowed(M,M.location(),CMSecurity.SecFlag.ACHIEVEMENTS))
				return "[authentication error]";

			String row = "";

			String newTattoo=httpReq.getUrlParameter("TATTOO");
			if(newTattoo==null)
				return "[missing TATTOO error]";
			row=newTattoo.toUpperCase().trim()+"=";
			if((last!=null)&&((last.length()==0)&&(CMLib.achievements().getAchievement(newTattoo)!=null)))
			{
				return "[new achievement tattoo already exists!]";
			}

			String newEvent=httpReq.getUrlParameter("EVENT");
			if((newEvent==null)||(!CMStrings.contains(Event.getEventChoices(), newEvent)))
				return "[missing EVENT error]";
			final Event E=(Event)CMath.s_valueOf(Event.class, newEvent);
			row+="EVENT=\""+newEvent+"\" ";

			String newDisplay=httpReq.getUrlParameter("DISPLAY");
			if(newDisplay==null)
				return "[missing DISPLAY error]";
			row+="DISPLAY=\""+CMStrings.escape(newDisplay)+"\" ";
			
			String newTitle=httpReq.getUrlParameter("TITLE");
			if((newTitle != null)&&(newTitle.length()>0))
				row+="TITLE=\""+CMStrings.escape(newTitle)+"\" ";

			String newRewards=httpReq.getUrlParameter("REWARDS");
			if((newRewards != null)&&(newRewards.length()>0))
				row+="REWARDS=\""+CMStrings.escape(newRewards)+"\" ";

			for(String s : E.getParameters())
			{
				if(!CMStrings.contains(AchievementLibrary.BASE_ACHIEVEMENT_PARAMETERS, s))
				{
					String newValue=httpReq.getUrlParameter(s);
					if((newValue != null)&&(newValue.length()>0))
					{
						row+=s+"=\""+CMStrings.escape(newValue)+"\" ";
					}
				}
			}

			String error=CMLib.achievements().evaluateAchievement(agent,row,false);
			if(error!=null)
				return "[error: "+error+"]";

			if(!parms.containsKey("CHECKONLY"))
			{
				if((last!=null)&&(CMLib.achievements().getAchievement(last)!=null))
				{
					final String err=deleteAchievement(last);
					if((err!=null)&&(err.length()>0))
					{
						return err;
					}
				}
			}

			error=CMLib.achievements().evaluateAchievement(agent,row,true);
			if((error!=null)&&(error.length()>0))
				return "[error: "+error+"]";
			if(!parms.containsKey("CHECKONLY"))
			{
				CMLib.achievements().resaveAchievements(last);
			}
		}
		else
		if(parms.containsKey("DELETE"))
		{
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return "[authentication error]";
			if(!CMSecurity.isAllowed(M,M.location(),CMSecurity.SecFlag.ACHIEVEMENTS))
				return "[authentication error]";
			if(last==null)
				return " @break@";
			if(CMLib.achievements().getAchievement(last)==null)
				return "Unknown title!";
			final String err=deleteAchievement(last);
			if((err==null)||(err.length()==0))
				return "Achievement deleted.";
			return err;
		}
		else
		if(last==null)
			return " @break@";
		final StringBuffer str=new StringBuffer("");
		
		Achievement A=CMLib.achievements().getAchievement(last);
		Event E;
		String eventName=httpReq.getUrlParameter("EVENT");
		if(eventName==null)
			eventName=(A!=null)?A.getEvent().name():"KILLS";
		E=(Event)CMath.s_valueOf(Event.class, eventName);
		if(E==null)
			E=Event.KILLS;

		if(parms.containsKey("TATTOO"))
		{
			String value=httpReq.getUrlParameter("TATTOO");
			if((value==null)&&(A!=null))
				value=A.getTattoo();
			if(value!=null)
				str.append(CMStrings.replaceAll(value,"\"","&quot;")+", ");
		}
		if(parms.containsKey("EVENT"))
		{
			if(eventName!=null)
				str.append(CMStrings.replaceAll(eventName,"\"","&quot;")+", ");
		}
		if(parms.containsKey("HELP") && parms.containsKey("FIELD"))
		{
			String field=parms.get("FIELD");
			@SuppressWarnings("unchecked")
			Map<String,Map<String,String>> map=(Map<String,Map<String,String>>)httpReq.getRequestObjects().get("SYSTEM_ACHIEVEMENT_HELP_MAP");
			if(map == null)
			{
				map = CMLib.achievements().getAchievementsHelpMap();
				httpReq.getRequestObjects().put("SYSTEM_ACHIEVEMENT_HELP_MAP",map);
			}
			String help = CMLib.achievements().getAchievementsHelpFromMap(map, E, field);
			if(help != null)
			{
				str.append("<PRE>");
				str.append(CMStrings.deEscape(help).replaceAll("\n\r","\n"));
				str.append("</PRE>");
				str.append(", ");
			}
		}
		if(parms.containsKey("EVENTOPTIONS"))
		{
			StringBuilder s=new StringBuilder("");
			for(Event E2 : Event.values())
			{
				s.append("<OPTION VALUE="+E2.name()+" ");
				if(E2==E)
					s.append("SELECTED ");
				s.append(">"+E2.name());
			}
			str.append(s.toString()+", ");
		}
		if(parms.containsKey("DISPLAY"))
		{
			String value=httpReq.getUrlParameter("DISPLAY");
			if((value==null)&&(A!=null))
				value=A.getDisplayStr();
			if(value!=null)
				str.append(CMStrings.replaceAll(value,"\"","&quot;")+", ");
		}
		if(parms.containsKey("TITLE"))
		{
			String value=httpReq.getUrlParameter("TITLE");
			if((value==null)&&(A!=null))
			{
				for(Award award: A.getRewards())
				{
					if(award.getType()==AwardType.TITLE)
					{
						value=((TitleAward)award).getTitle();
					}
				}
			}
			if(value!=null)
				str.append(CMStrings.replaceAll(value,"\"","&quot;")+", ");
		}
		if(parms.containsKey("REWARDS"))
		{
			String value=httpReq.getUrlParameter("REWARDS");
			if((value==null)&&(A!=null))
				value=CMLib.achievements().getAwardString(A.getRewards());
			if(value!=null)
				str.append(CMStrings.replaceAll(value,"\"","&quot;")+", ");
		}
		if(parms.containsKey("COUNT"))
		{
			String value=httpReq.getUrlParameter("COUNT");
			if((value==null)&&(A!=null))
				value=""+A.getTargetCount();
			if(value!=null)
				str.append(CMStrings.replaceAll(value,"\"","&quot;")+", ");
		}
		if(parms.containsKey("MISC"))
		{
			String value=httpReq.getUrlParameter("MISC");
			if((value==null)&&(A!=null))
			{
				value="";
				for(String otherParmName : E.getParameters())
				{
					if(!CMStrings.contains(AchievementLibrary.BASE_ACHIEVEMENT_PARAMETERS, otherParmName))
					{
						value += CMStrings.deEscape(A.getRawParmVal(otherParmName))+" ";
					}
				}
			}
			if(value!=null)
				str.append(CMStrings.replaceAll(value,"\"","&quot;")+", ");
		}
		if(parms.containsKey("OTHERPARMNEXT"))
		{
			final String lastOtherParm=httpReq.getUrlParameter("OTHERPARM");
			if(parms.containsKey("RESET"))
			{
				if(lastOtherParm!=null)
					httpReq.removeUrlParameter("OTHERPARM");
				return "";
			}
			String lastOtherParmID="";
			for(String otherParmName : E.getParameters())
			{
				if(!CMStrings.contains(AchievementLibrary.BASE_ACHIEVEMENT_PARAMETERS, otherParmName))
				{
					if((lastOtherParm==null)||((lastOtherParm.length()>0)&&(lastOtherParm.equals(lastOtherParmID))&&(!otherParmName.equals(lastOtherParmID))))
					{
						httpReq.addFakeUrlParameter("OTHERPARM",otherParmName);
						httpReq.addFakeUrlParameter("OTHERPARMFIELD",CMStrings.capitalizeAndLower(otherParmName));
						return "";
					}
					lastOtherParmID=otherParmName;
				}
			}
			httpReq.addFakeUrlParameter("OTHERPARM","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}
		if(parms.containsKey("PLAYERPROGRESS"))
		{
			if(A == null)
				return "[no achievement error]";
			final String playerID=httpReq.getUrlParameter("PLAYER");
			if((playerID!=null)&&(playerID.length()>0))
			{
				final MOB M=CMLib.players().getLoadPlayer(playerID);
				if(M!=null)
				{
					PlayerStats pStats = M.playerStats();
					if(pStats!=null)
					{
						AchievementLibrary.Tracker T = pStats.getAchievementTracker(A, M);
						str.append(""+T.getCount(M)).append(", ");
					}
					else
						return "[bad player error]";
				}
				else
					return "[unknown player error]";
			}
			else
				return "[player reference error]";
			
		}
		
		if(parms.containsKey("ISPLAYERACHIEVED") || parms.containsKey("ISPLAYERPROGRESS"))
		{
			if(A == null)
				return "[no achievement error]";
			final String playerID=httpReq.getUrlParameter("PLAYER");
			if((playerID!=null)&&(playerID.length()>0))
			{
				final MOB M=CMLib.players().getLoadPlayer(playerID);
				if(M!=null)
				{
					PlayerStats pStats = M.playerStats();
					if(pStats!=null)
					{
						AchievementLibrary.Tracker T = pStats.getAchievementTracker(A, M);
						if(parms.containsKey("ISPLAYERACHIEVED"))
						{
							final boolean achieved = M.findTattoo(A.getTattoo()) != null;
							if(parms.containsKey("ISPLAYERPROGRESS"))
								str.append((achieved || (T.getCount(M) != 0)) ? "true" : "false").append(", ");
							else
								str.append(achieved ? "true" : "false").append(", ");
						}
						else
							str.append(T.getCount(M) != 0 ? "true" : "false").append(", ");
					}
					else
						return "[bad player error]";
				}
				else
					return "[unknown player error]";
			}
			else
				return "[player reference error]";
		}
		for(String otherParmName : E.getParameters())
		{
			if(!CMStrings.contains(AchievementLibrary.BASE_ACHIEVEMENT_PARAMETERS, otherParmName))
			{
				if(parms.containsKey(otherParmName))
				{
					String newValue=httpReq.getUrlParameter(otherParmName);
					if((newValue==null)&&(A!=null))
						newValue=CMStrings.deEscape(A.getRawParmVal(otherParmName));
					if(newValue!=null)
						str.append(CMStrings.replaceAll(newValue,"\"","&quot;")+", ");
				}
			}
		}
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return clearWebMacros(strstr);
	}
}
