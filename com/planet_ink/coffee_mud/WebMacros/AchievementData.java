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
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.AchievementFlag;
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
   Copyright 2015-2025 Bo Zimmerman

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

	public void rebuildTrackers(final String tattoo)
	{
		for(final Enumeration<MOB> m = CMLib.players().players();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if(M.playerStats()!=null)
			{
				M.playerStats().rebuildAchievementTracker(M, M, tattoo);
			}
		}
	}

	public String deleteAchievement(final String tattoo)
	{
		final Achievement A=CMLib.achievements().deleteAchievement(tattoo);
		if(A!=null)
		{
			rebuildTrackers(A.getTattoo());
			CMLib.achievements().resaveAchievements(A.getTattoo());
		}
		return "";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		if(!CMProps.isState(CMProps.HostState.RUNNING))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("ACHIEVEMENT");
		if((last==null)&&(!parms.containsKey("EDIT")))
			return " @break@";

		String agentStr = parms.get("AGENT");
		if(agentStr == null)
			agentStr=httpReq.getUrlParameter("AGENT");
		AccountStats.Agent agent = (AccountStats.Agent)CMath.s_valueOf(AccountStats.Agent.class, agentStr.toUpperCase().trim());
		if(agent == null)
			agent = AccountStats.Agent.PLAYER;
		if((agent == AccountStats.Agent.ACCOUNT)&&(!CMProps.isUsingAccountSystem()))
			agent = AccountStats.Agent.PLAYER;

		if(parms.containsKey("EDIT"))
		{
			final MOB M = Authenticate.getAuthenticatedMob(httpReq, httpResp);
			if(M==null)
				return "[authentication error]";
			if(!CMSecurity.isAllowed(M,M.location(),CMSecurity.SecFlag.ACHIEVEMENTS))
				return "[authentication error]";

			String row = "";

			String newTattoo=httpReq.getUrlParameter("TATTOO");
			if(newTattoo==null)
				return "[missing TATTOO error]";
			row=newTattoo.trim()+"=";
			newTattoo=newTattoo.toUpperCase();
			if((last!=null)&&((last.length()==0)&&(CMLib.achievements().getAchievement(newTattoo)!=null)))
			{
				return "[new achievement tattoo already exists!]";
			}

			final String newEvent=httpReq.getUrlParameter("EVENT");
			if((newEvent==null)||(!CMStrings.contains(Event.getEventChoices(), newEvent)))
				return "[missing EVENT error]";
			final Event E=(Event)CMath.s_valueOf(Event.class, newEvent);
			row+="EVENT=\""+newEvent+"\" ";

			final String newDisplay=httpReq.getUrlParameter("DISPLAY");
			if(newDisplay==null)
				return "[missing DISPLAY error]";
			row+="DISPLAY=\""+CMStrings.escape(newDisplay)+"\" ";

			final String newTitle=httpReq.getUrlParameter("TITLE");
			if((newTitle != null)&&(newTitle.length()>0))
				row+="TITLE=\""+CMStrings.escape(newTitle)+"\" ";

			final String newRewards=httpReq.getUrlParameter("REWARDS");
			if((newRewards != null)&&(newRewards.length()>0))
				row+="REWARDS=\""+CMStrings.escape(newRewards)+"\" ";

			final String newDuration=httpReq.getUrlParameter("DURATION");
			if((newDuration != null)&&(newDuration.length()>0)&&(CMath.s_int(newDuration)>0))
				row+="DURATION="+CMath.s_int(newDuration)+" ";

			final String newVisiMask=httpReq.getUrlParameter("VISIBLEMASK");
			if((newVisiMask != null)&&(newVisiMask.length()>0))
				row+="VISIBLEMASK=\""+CMStrings.escape(newVisiMask)+"\" ";

			final String newPlayMask=httpReq.getUrlParameter("PLAYERMASK");
			if((newPlayMask != null)&&(newPlayMask.length()>0))
				row+="PLAYERMASK=\""+CMStrings.escape(newPlayMask)+"\" ";

			if(httpReq.isUrlParameter("FLAGS"))
			{
				String id="";
				int num=0;
				final List<String> V=new ArrayList<String>();
				for(;httpReq.isUrlParameter("FLAGS"+id);id=""+(++num))
					V.add(httpReq.getUrlParameter("FLAGS"+id));
				if(V.size()>0)
					row += "FLAGS=\""+CMParms.combine(V)+"\" ";
			}

			for(final String s : E.getParameters())
			{
				if(!CMStrings.contains(AchievementLibrary.BASE_ACHIEVEMENT_PARAMETERS, s))
				{
					final String newValue=httpReq.getUrlParameter(s);
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
					if(last.equalsIgnoreCase(newTattoo))
					{
						final Achievement A=CMLib.achievements().deleteAchievement(last);
						if(A!=null)
							rebuildTrackers(A.getTattoo());
						// don't save in this case, just let it ride below.
					}
					else
					{
						final String err=deleteAchievement(last);
						if((err!=null)&&(err.length()>0))
						{
							return err;
						}
					}
				}
			}

			error=CMLib.achievements().evaluateAchievement(agent,row,true);
			if((error!=null)&&(error.length()>0))
				return "[error: "+error+"]";
			if(!parms.containsKey("CHECKONLY"))
			{
				CMLib.achievements().resaveAchievements(newTattoo);
			}
		}
		else
		if(parms.containsKey("DELETE"))
		{
			final MOB M = Authenticate.getAuthenticatedMob(httpReq, httpResp);
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

		final Achievement A=CMLib.achievements().getAchievement(last);
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
			final String field=parms.get("FIELD");
			@SuppressWarnings("unchecked")
			Map<String,Map<String,String>> map=(Map<String,Map<String,String>>)httpReq.getRequestObjects().get("SYSTEM_ACHIEVEMENT_HELP_MAP");
			if(map == null)
			{
				map = CMLib.achievements().getAchievementsHelpMap();
				httpReq.getRequestObjects().put("SYSTEM_ACHIEVEMENT_HELP_MAP",map);
			}
			final String help = CMLib.achievements().getAchievementsHelpFromMap(map, E, field);
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
			final StringBuilder s=new StringBuilder("");
			for(final Event E2 : Event.values())
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
		if(parms.containsKey("VISIBLEMASK"))
		{
			String value=httpReq.getUrlParameter("VISIBLEMASK");
			if((value==null)&&(A!=null))
				value=A.getRawParmVal("VISIBLEMASK");
			if(value!=null)
				str.append(CMStrings.replaceAll(value,"\"","&quot;")+", ");
		}
		if(parms.containsKey("FLAGS"))
		{
			final List<String> list=new ArrayList<String>();
			if(httpReq.isUrlParameter("FLAGS"))
			{
				String id="";
				int num=0;
				for(;httpReq.isUrlParameter("FLAGS"+id);id=""+(++num))
					list.add(httpReq.getUrlParameter("FLAGS"+id).toUpperCase().trim());
			}
			else
			if(A!=null)
			{
				for(final AchievementFlag f : AchievementFlag.values())
				{
					if(A.isFlag(f))
						list.add(f.name());
				}
			}
			for(final AchievementFlag f : AchievementFlag.values())
				str.append("<OPTION VALUE=\""+f.name()+"\""+(list.contains(f.name())?" SELECTED":"")+">"+f.name());
			str.append(", ");
		}
		if(parms.containsKey("PLAYERMASK"))
		{
			String value=httpReq.getUrlParameter("PLAYERMASK");
			if((value==null)&&(A!=null))
				value=A.getRawParmVal("PLAYERMASK");
			if(value!=null)
				str.append(CMStrings.replaceAll(value,"\"","&quot;")+", ");
		}
		if(parms.containsKey("TITLE"))
		{
			String value=httpReq.getUrlParameter("TITLE");
			final boolean friendly=parms.containsKey("FRIENDLY");
			if((value==null)&&(A!=null))
			{
				for(final Award award: A.getRewards())
				{
					if(award.getType()==AwardType.TITLE)
					{
						if(friendly)
							value=CMLib.achievements().fixAwardDescription(A, award, null, null);
						else
						if(award instanceof TitleAward)
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
		if(parms.containsKey("DURATION"))
		{
			String value=httpReq.getUrlParameter("DURATION");
			if((value==null)&&(A!=null))
				value=""+A.getDuration();
			if(value!=null)
				str.append(CMStrings.replaceAll(value,"\"","&quot;")+", ");
		}
		if(parms.containsKey("MISC"))
		{
			String value=httpReq.getUrlParameter("MISC");
			if((value==null)&&(A!=null))
			{
				value="";
				for(final String otherParmName : E.getParameters())
				{
					if(!CMStrings.contains(AchievementLibrary.BASE_ACHIEVEMENT_PARAMETERS, otherParmName))
					{
						value += CMStrings.deEscape(A.getRawParmVal(otherParmName))+" ";
					}
				}
			}
			if(value!=null)
				str.append(CMStrings.replaceAll(CMStrings.addCommaSpacing(value),"\"","&quot;")+", ");
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
			for(final String otherParmName : E.getParameters())
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
					final PlayerStats pStats = M.playerStats();
					if(pStats!=null)
					{
						final AchievementLibrary.Tracker T = pStats.getAchievementTracker(A, M, M);
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
					final PlayerStats pStats = M.playerStats();
					if(pStats!=null)
					{
						final AchievementLibrary.Tracker T = pStats.getAchievementTracker(A, M, M);
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
		for(final String otherParmName : E.getParameters())
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
