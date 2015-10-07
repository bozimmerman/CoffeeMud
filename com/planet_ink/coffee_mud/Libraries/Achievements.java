package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Tracker;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2015-2015 Bo Zimmerman

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
public class Achievements extends StdLibrary implements AchievementLibrary
{
	@Override public String ID(){return "Achievements";}
	private List<Achievement> achievements=null;

	@Override
	public String evaluateAchievement(String row, boolean addIfPossible)
	{
		if(row.trim().startsWith("#")||row.trim().startsWith(";")||(row.trim().length()==0))
			return null;
		int x=row.indexOf('=');
		while((x>=1)&&(row.charAt(x-1)=='\\'))
			x=row.indexOf('=',x+1);
		if(x<0)
			return "Error: Invalid line! Not comment, whitespace, and does not contain an = sign!";
		final String tattoo=row.substring(0,x).toUpperCase().trim();
		if(tattoo.length()==0)
			return "Error: Blank achievement tattoo: "+tattoo+"!";
		if(Character.isDigit(tattoo.charAt(tattoo.length()-1)))
			return "Error: Invalid achievement tattoo: "+tattoo+"!";
		String parms=row.substring(x+1).trim();
		
		String eventStr=CMParms.getParmStr(parms, "EVENT", "");
		final Event eventType = (Event)CMath.s_valueOf(Event.class, eventStr.toUpperCase().trim());
		if(eventType == null)
			return "Error: Blank or unknown achievement type: "+eventStr+"!";
		final String displayStr=CMStrings.replaceAll(CMStrings.replaceAll(CMParms.getParmStr(parms, "DISPLAY", ""),"\\\"","\""),"\\\\","\\");
		final String titleStr=CMStrings.replaceAll(CMStrings.replaceAll(CMParms.getParmStr(parms, "TITLE", ""),"\\\"","\""),"\\\\","\\");
		String rewardStr=CMStrings.replaceAll(CMStrings.replaceAll(CMParms.getParmStr(parms, "REWARDS", ""),"\\\"","\""),"\\\\","\\");
		final String[] rewardList = rewardStr.split(" ");
		Achievement A;
		switch(eventType)
		{
		case KILLS:
			A=new Achievement()
			{
				private int num = 0;
				private MaskingLibrary.CompiledZapperMask mask = null;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}
				
				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public String getTitleAward()
				{
					return titleStr;
				}
				
				@Override
				public String[] getRewards()
				{
					return rewardList;
				}
				
				@Override
				public Tracker getTracker(final MOB mob, final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						volatile int count = oldCount;

						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved() 
						{
							return (getCount() >= num);
						}

						@Override
						public int getCount()
						{
							return count;
						}

						@Override
						public void testBump(Object... parms)
						{
							if((parms.length>0)
							&&(parms[0] instanceof MOB)
							&&((mask==null)||(CMLib.masking().maskCheck(mask, (MOB)parms[0], true))))
							{
								count++;
							}
						}
						
						@Override
						public boolean saveCount()
						{
							return true;
						}
					};
				}
				
				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					final String zapperMask=CMStrings.replaceAll(CMStrings.replaceAll(CMParms.getParmStr(parms, "ZAPPERMASK", ""),"\\\"","\""),"\\\\","\\");
					if(zapperMask.trim().length()==0)
						return "Error: Missing or invalid ZAPPERMASK parameter: "+zapperMask+"!";
					this.mask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
		case STATVALUE:
			A=new Achievement()
			{
				private String	statName= "";
				private int 	value	= 0;
				private int		abelo	= 0;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}
				
				@Override
				public String getTitleAward()
				{
					return titleStr;
				}
				
				@Override
				public String[] getRewards()
				{
					return rewardList;
				}

				@Override
				public Tracker getTracker(final MOB mob, final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved() 
						{
							return (abelo > 0) ? (getCount() > value) : (getCount() < value);
						}

						@Override
						public int getCount()
						{
							return CMath.s_int(CMLib.coffeeMaker().getAnyGenStat(mob, statName));
						}

						@Override
						public void testBump(Object... parms) 
						{
						}
						
						@Override
						public boolean saveCount()
						{
							return false;
						}
					};
				}
				
				@Override
				public String parseParms(final String parms)
				{
					MOB mob = CMClass.getFactoryMOB();
					final String numStr=CMParms.getParmStr(parms, "VALUE", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid VALUE parameter: "+numStr+"!";
					value=CMath.s_int(numStr);
					final String aboveBelow=CMParms.getParmStr(parms, "ABOVEBELOW", "").toUpperCase().trim();
					if((!aboveBelow.equals("ABOVE")) && (!aboveBelow.equals("BELOW")))
						return "Error: Missing or invalid ABOVEBELOW parameter: "+aboveBelow+"!";
					this.abelo = aboveBelow.equals("ABOVE")? 1 : -1;
					final String statName=CMParms.getParmStr(parms, "STAT", "").toUpperCase().trim();
					if((statName.length()==0)||(!CMLib.coffeeMaker().isAnyGenStat(mob, statName)))
						return "Error: Missing or invalid STAT parameter: "+statName+"!";
					this.statName = statName;
					return "";
				}
			};
			break;
		case FACTION:
			A=new Achievement()
			{
				private String	factionID	= "";
				private int 	value		= 0;
				private int		abelo		= 0;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public String getTitleAward()
				{
					return titleStr;
				}
				
				@Override
				public String[] getRewards()
				{
					return rewardList;
				}

				@Override
				public Tracker getTracker(final MOB mob, final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved() 
						{
							return (abelo > 0) ? (getCount() > value) : (getCount() < value);
						}

						@Override
						public int getCount()
						{
							return mob.fetchFaction(factionID);
						}

						@Override
						public void testBump(Object... parms) 
						{
						}
						
						@Override
						public boolean saveCount()
						{
							return false;
						}
					};
				}
				
				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "VALUE", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid VALUE parameter: "+numStr+"!";
					value=CMath.s_int(numStr);
					final String aboveBelow=CMParms.getParmStr(parms, "ABOVEBELOW", "").toUpperCase().trim();
					if((!aboveBelow.equals("ABOVE")) && (!aboveBelow.equals("BELOW")))
						return "Error: Missing or invalid ABOVEBELOW parameter: "+aboveBelow+"!";
					this.abelo = aboveBelow.equals("ABOVE")? 1 : -1;
					final String factionID=CMParms.getParmStr(parms, "ID", "").toUpperCase().trim();
					if(factionID.length()==0)
						return "Error: Missing ID parameter: "+factionID+"!";
					return "";
				}
			};
			break;
		case EXPLORE:
			A=new Achievement()
			{
				private String	areaID	= "";
				private int	 	pct		= 0;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public String getTitleAward()
				{
					return titleStr;
				}
				
				@Override
				public String[] getRewards()
				{
					return rewardList;
				}

				@Override
				public Tracker getTracker(final MOB mob, final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved() 
						{
							return getCount() >= pct;
						}

						@Override
						public int getCount()
						{
							final PlayerStats pstats=mob.playerStats();
							final Area A=CMLib.map().getArea(areaID);
							if((pstats != null)&&(A!=null))
							{
								return pstats.percentVisited(mob, A);
							}
							return 0;
						}

						@Override
						public void testBump(Object... parms) 
						{
						}
						
						@Override
						public boolean saveCount()
						{
							return false;
						}
					};
				}

				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "PERCENT", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid PERCENT parameter: "+numStr+"!";
					this.pct=CMath.s_int(numStr);
					final String areaID=CMParms.getParmStr(parms, "AREA", "").toUpperCase().trim();
					if(areaID.length()==0)
						return "Error: Missing AREA parameter: "+areaID+"!";
					return "";
				}
			};
			break;
		case CRAFTING:
			A=new Achievement()
			{
				private int 	num 		= 0;
				private String	abilityID 	= "";
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public String getTitleAward()
				{
					return titleStr;
				}
				
				@Override
				public String[] getRewards()
				{
					return rewardList;
				}

				@Override
				public Tracker getTracker(final MOB mob, final int oldCount)
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String parseParms(final String parms)
				{
					// TODO Auto-generated method stub
					return null;
				}
			};
			break;
		case MENDER:
			A=new Achievement()
			{
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public String getTitleAward()
				{
					return titleStr;
				}
				
				@Override
				public String[] getRewards()
				{
					return rewardList;
				}

				@Override
				public Tracker getTracker(final MOB mob, final int oldCount)
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					// TODO Auto-generated method stub
					return null;
				}
			};
			break;
		case QUESTOR:
			A=new Achievement()
			{
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public String getTitleAward()
				{
					return titleStr;
				}
				
				@Override
				public String[] getRewards()
				{
					return rewardList;
				}

				@Override
				public Tracker getTracker(final MOB mob, final int oldCount)
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String parseParms(final String parms)
				{
					// TODO Auto-generated method stub
					return null;
				}
			};
			break;
		case ACHIEVER:
			A=new Achievement()
			{
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public String getTitleAward()
				{
					return titleStr;
				}
				
				@Override
				public String[] getRewards()
				{
					return rewardList;
				}

				@Override
				public Tracker getTracker(final MOB mob, final int oldCount)
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String parseParms(final String parms)
				{
					// TODO Auto-generated method stub
					return null;
				}
			};
			break;
			default:
				A=null;
				break;
		}
		
		if(A==null)
		{
			return "Error: Unimplemented achievement type: "+eventStr+"!";
		}
		
		if(addIfPossible)
		{
			if(achievements==null)
				reloadAchievements();
			for(final Achievement A2 : achievements)
			{
				if(A2.getTattoo().equalsIgnoreCase(tattoo))
					return "Error: Duplicate achievement: "+tattoo+"!";
			}
			achievements.add(A);
		}
		return null;
	}
	
	@Override
	public Enumeration<Achievement> achievements()
	{
		if(achievements==null)
			reloadAchievements();
		return new IteratorEnumeration<Achievement>(achievements.iterator());
	}

	@Override
	public boolean evaluateAchievements(final MOB mob)
	{
		if(mob==null)
			return false;
		final PlayerStats P=mob.playerStats();
		if(P==null)
			return false;
		boolean somethingDone = false;
		if(achievements==null)
		{
			reloadAchievements();
		}
		return somethingDone;
	}

	@Override
	public void reloadAchievements()
	{
		achievements=new LinkedList<Achievement>();
		final List<String> V=Resources.getFileLineVector(Resources.getFileResource("achievements.txt",true));
		String WKID=null;
		for(int v=0;v<V.size();v++)
		{
			final String row=V.get(v);
			WKID=evaluateAchievement(row,true);
			if(WKID==null)
				continue;
			if(WKID.startsWith("Error: "))
				Log.errOut("Achievements",WKID);
		}
		for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
		{
			final MOB M=e.nextElement();
			if(M.playerStats()!=null)
			{
				if((evaluateAchievements(M))&&(!CMLib.flags().isInTheGame(M,true)))
					CMLib.database().DBUpdatePlayerPlayerStats(M);
			}
		}
	}

}
