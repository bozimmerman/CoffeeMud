package com.planet_ink.coffee_mud.Commands;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.Agent;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Award;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;
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
public class Achievements extends StdCommand
{
	private final String[]	access	= I(new String[] { "ACHIEVEMENTS" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public enum NonAgentList
	{
		AWARDS, REMORT, FUTURE, RETIRE
	}
	
	private enum ValidLists
	{
		WON, ALL, NOW
	}
	
	private enum ValidParms
	{
		ANNOUNCE, ALL, WON, NOW, PLAYER, ACCOUNT, CHARACTER
	}
	
	private Tattooable getTattooable(Agent agent, MOB mob)
	{
		switch(agent)
		{
		case PLAYER:
			return mob;
		case ACCOUNT:
			if(mob.playerStats()!=null)
				return mob.playerStats().getAccount();
			return null;
		}
		return null;
	}
	
	private AccountStats getStatter(Agent agent, MOB mob)
	{
		switch(agent)
		{
		case PLAYER:
			return mob.playerStats();
		case ACCOUNT:
			if(mob.playerStats()!=null)
				return mob.playerStats().getAccount();
			return null;
		}
		return null;
	}
	
	private List<Achievement> getLowestNumberedTattoos(final Agent agent, final Set<String> WonList)
	{
		List<Achievement> useList = new LinkedList<Achievement>();
		HashSet<String> ignoredStarters = new HashSet<String>();
		for(Enumeration<Achievement> a=CMLib.achievements().achievements(agent);a.hasMoreElements();)
		{
			final Achievement A=a.nextElement();
			final String tattooName = A.getTattoo();
			if(WonList.contains(A.getTattoo()))
				useList.add(A);
			else
			if((tattooName.length()>1)
			&& Character.isDigit(tattooName.charAt(tattooName.length()-1)))
			{
				int x=tattooName.length()-1;
				while((x>0) && Character.isDigit(tattooName.charAt(x)))
					x--;
				final String starter = tattooName.substring(0,x+1);
				if(!ignoredStarters.contains(starter))
				{
					ignoredStarters.add(starter);
					useList.add(A);
				}
			}
			else
				useList.add(A);
		}
		return useList;
	}
	
	public List<Achievement> getAccountAwards(final PlayerAccount account)
	{
		if(account != null)
		{
			List<Achievement> awards=new Vector<Achievement>(1);
			for(Enumeration<Tattoo> t=account.tattoos();t.hasMoreElements();)
			{
				final Tattoo T = t.nextElement();
				final Achievement A=CMLib.achievements().getAchievement(T.getTattooName());
				if(A != null)
				{
					awards.add(A);
				}
			}
			return awards;
		}
		return new ArrayList<Achievement>(0);
	}
	
	public List<Achievement> getAccountAwards(final MOB mob)
	{
		final PlayerStats pStats = (mob==null) ? null : mob.playerStats();
		final PlayerAccount account = (pStats == null) ? null : pStats.getAccount();
		if(account != null)
			return getAccountAwards(account);
		return new ArrayList<Achievement>(0);
	}
	
	@Override
	public boolean execute(final MOB mob, List<String> commands, int metaFlags) throws java.io.IOException
	{
		String rest = CMParms.combine(commands,1);
		final PlayerStats pStats = mob.playerStats();
		if (pStats == null)
		{
			mob.tell(L("You don't have any achievements."));
			return false;
		}
		
		if(CMLib.achievements().evaluateAchievements(mob))
		{
			CMLib.s_sleep(4000); 
			// yes, I know, but if I call tick down, or some other
			// async method, I'll get a freaking prompt.
		}
		
		MOB whoM=mob;
		final List<String> parms = CMParms.parseSpaces(rest.toUpperCase(), true);
		if(parms.size()>0)
		{
			final String lastParm=CMStrings.capitalizeAndLower(parms.get(parms.size()-1));
			if(CMLib.players().playerExists(lastParm))
			{
				whoM=CMLib.players().getLoadPlayer(lastParm);
				parms.remove(parms.size()-1);
			}
			else
			{
				if((CMath.s_valueOf(ValidParms.class,lastParm.toUpperCase().trim()) == null)
				&&(CMath.s_valueOf(NonAgentList.class, lastParm.toUpperCase().trim())==null))
				{
					mob.tell(L("There is no such player as '@x1'.",lastParm));
					return false;
				}
			}
		}
		
		String cmd = CMParms.combine(parms);
		NonAgentList noAgent = (NonAgentList)CMath.s_valueOf(NonAgentList.class, cmd.toUpperCase().trim());
		if(noAgent != null)
		{
			switch(noAgent)
			{
			case RETIRE:
			case REMORT:
			{
				final Event E=(noAgent == NonAgentList.REMORT) ? Event.REMORT : Event.RETIRE;
				List<Achievement> awards = CMLib.achievements().fakeBumpAchievement(whoM, E, 1);
				int numAwards=0;
				for(Achievement A : awards)
					numAwards+=A.getRewards().length;
				if(numAwards==0)
				{
					mob.tell(whoM,null,null,L("<S-YOUPOSS> next "+noAgent.toString().toLowerCase()+" would grant <S-NAME> no new awards."));
				}
				else
				{
					StringBuilder str=new StringBuilder(L("^H<S-YOUPOSS> next "+noAgent.toString().toLowerCase()+" will get the following awards:^?"));
					int i=1;
					for(Achievement A : awards)
					{
						if(A.getRewards().length>0)
							str.append(L("\n\rFrom the achievement '@x1':",A.getDisplayStr()));
						for(final Award award : A.getRewards())
							str.append("\n\r"+(i++)+") "+award.getDescription());
					}
					str.append("\n\r");
					mob.tell(mob,whoM,null,str.toString());
				}
				if(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)<=1)
				{
					return false;
				}
				List<Achievement> futureAwards = getAccountAwards(whoM);
				if(futureAwards.size()==0)
				{
					return false;
				}
				mob.tell(whoM,null,null,L("^HFrom <S-YOUPOSS> previous achievements. ^?"));
			}
			//$FALL-THROUGH$
			case FUTURE:
			{
				if(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)<=1)
				{
					mob.tell(L("This system does not support new character achievement awards."));
					return false;
				}
				List<Achievement> awards = this.getAccountAwards(whoM);
				int numAwards=0;
				for(Achievement A : awards)
					numAwards+=A.getRewards().length;
				if(numAwards==0)
				{
					mob.tell(whoM,null,null,L("<S-NAME> <S-HAS-HAVE> not won any account awards for new/future characters or remorting."));
					return false;
				}
				StringBuilder str=new StringBuilder(L("<S-YOUPOSS> next character will get the following awards:"));
				int i=1;
				for(Achievement A : awards)
				{
					if(A.getRewards().length>0)
						str.append(L("\n\rFrom the achievement '@x1':",A.getDisplayStr()));
					for(final Award award : A.getRewards())
						str.append("\n\r"+(i++)+") "+award.getDescription());
				}
				str.append("\n\r");
				mob.tell(whoM,null,null,str.toString());
				return true;
			}
			case AWARDS:
			{
				final StringBuilder str=new StringBuilder("");
				int i=1;
				for(final Enumeration<Tattoo> t = whoM.tattoos();t.hasMoreElements();)
				{
					final Achievement A=CMLib.achievements().getAchievement(t.nextElement().getTattooName());
					if(A != null)
					{
						if(A.getRewards().length>0)
							str.append(L("\n\rFrom the achievement '@x1':",A.getDisplayStr()));
						for(final Award award : A.getRewards())
							str.append("\n\r"+(i++)+") "+award.getDescription());
					}
				}
				if(str.length()==0)
				{
					mob.tell(whoM,null,null,L("<S-NAME> <S-HAS-HAVE> not won any achievement awards."));
					return false;
				}
				else
				{
					mob.tell(whoM,null,null,L("<S-NAME> <S-HAS-HAVE> have been granted the following achievement awards:")+str.toString());
					return true;
				}
			}
			}
		}
		
		List<AccountStats.Agent> agents = new LinkedList<AccountStats.Agent>();
		boolean announce=false;
		ValidLists list = ValidLists.WON;
		for(int p=parms.size()-1;p>=0;p--)
		{
			ValidParms V = (ValidParms)CMath.s_valueOf(ValidParms.class,parms.get(p).toUpperCase().trim());
			if(V!=null)
			{
				switch(V)
				{
				case ANNOUNCE:
					announce=true;
					break;
				case NOW:
				case WON:
				case ALL:
					list = ValidLists.valueOf(V.name());
					break;
				case CHARACTER:
				case PLAYER:
					if(!agents.contains(AccountStats.Agent.PLAYER))
						agents.add(AccountStats.Agent.PLAYER);
					break;
				case ACCOUNT: 
					if(!agents.contains(AccountStats.Agent.ACCOUNT))
						agents.add(AccountStats.Agent.ACCOUNT);
					break;
				}
			}
		}
		if(agents.size()==0)
		{
			agents.add(AccountStats.Agent.ACCOUNT);
			agents.add(AccountStats.Agent.PLAYER);
		}
		
		String prefix = "";
		if(whoM != mob)
		{
			prefix=whoM.Name()+L("'s ");
		}

		Set<String> WonList = new HashSet<String>();
		for(Agent agent : agents)
		{
			final Tattooable T = getTattooable(agent, whoM);
			if(T!=null)
			{
				for(Enumeration<Achievement> a=CMLib.achievements().achievements(agent);a.hasMoreElements();)
				{
					final Achievement A=a.nextElement();
					if(T.findTattoo(A.getTattoo())!=null)
						WonList.add(A.getTattoo());
				}
			}
		}
		final String done=L("DONE!");
		
		switch(list)
		{
		case ALL:
			prefix += prefix=L("All ");
			break;
		case NOW:
			prefix += L("Progress in ");
			break;
		case WON:
			break;
		}
		StringBuilder finalResponse = new StringBuilder();
		for(Agent agent : agents)
		{
			AccountStats stat = getStatter(agent,whoM);
			if(stat != null)
			{
				List<String> AchievedList = new Vector<String>();
				switch(list)
				{
				case ALL:
				{
					final List<Achievement> useList = getLowestNumberedTattoos(agent,WonList);
					int padding=done.length()+1;
					for(Iterator<Achievement> a=useList.iterator();a.hasNext();)
					{
						final Achievement A=a.next();
						if(!WonList.contains(A.getTattoo()))
						{
							AchievementLibrary.Tracker T=stat.getAchievementTracker(A, whoM);
							final int score = (T==null) ? 0 : T.getCount(whoM);
							final int targetScore = A.getTargetCount();
							if(targetScore != Integer.MIN_VALUE)
							{
								final int len = (""+score+"/"+targetScore).length(); 
								if(len >= padding)
									padding = len+1;
							}
						}
					}
					for(Iterator<Achievement> a=useList.iterator();a.hasNext();)
					{
						final Achievement A=a.next();
						if(WonList.contains(A.getTattoo()))
							AchievedList.add(CMStrings.padRight("^H"+done+"^?", padding)+": "+A.getDisplayStr());
						else
						{
							AchievementLibrary.Tracker T=stat.getAchievementTracker(A, whoM);
							int score = (T==null) ? 0 : T.getCount(whoM);
							int targetScore = A.getTargetCount();
							if(targetScore == Integer.MIN_VALUE)
								AchievedList.add(CMStrings.padRight("^w", padding)+"^?: "+A.getDisplayStr());
							else
								AchievedList.add(CMStrings.padRight("^w"+score+"/"+targetScore, padding)+"^?: "+A.getDisplayStr());
						}
					}
					break;
				}
				case NOW:
				{
					int padding=done.length()+1;
					final List<Achievement> useList = getLowestNumberedTattoos(agent,WonList);
					for(Iterator<Achievement> a=useList.iterator();a.hasNext();)
					{
						final Achievement A=a.next();
						if(!WonList.contains(A.getTattoo()))
						{
							AchievementLibrary.Tracker T=stat.getAchievementTracker(A, whoM);
							final int score = (T==null) ? 0 : T.getCount(whoM);
							if(score != 0)
							{
								final int targetScore = A.getTargetCount();
								if(targetScore != Integer.MIN_VALUE)
								{
									final int len = (""+score+"/"+targetScore).length(); 
									if(len >= padding)
										padding = len+1;
								}
							}
						}
					}
					for(Iterator<Achievement> a=useList.iterator();a.hasNext();)
					{
						final Achievement A=a.next();
						if(WonList.contains(A.getTattoo()))
							AchievedList.add(CMStrings.padRight("^H"+done+"^?", padding)+": "+A.getDisplayStr());
						else
						{
							AchievementLibrary.Tracker T=stat.getAchievementTracker(A, whoM);
							int score = (T==null) ? 0 : T.getCount(whoM);
							if(score != 0)
							{
								int targetScore = A.getTargetCount();
								if(targetScore != Integer.MIN_VALUE)
									AchievedList.add(CMStrings.padRight("^w"+score+"/"+targetScore, padding)+"^?: "+A.getDisplayStr());
								else
									AchievedList.add(CMStrings.padRight("^w", padding)+"^?: "+A.getDisplayStr());
							}
						}
					}
					break;
				}
				case WON:
				{
					for(Enumeration<Achievement> a=CMLib.achievements().achievements(agent);a.hasMoreElements();)
					{
						final Achievement A=a.nextElement();
						if(WonList.contains(A.getTattoo()))
						{
							AchievedList.add(A.getDisplayStr());
						}
					}
					break;
				}
				}
				if(AchievedList.size()==0)
					finalResponse .append("^H"+prefix+L(CMStrings.capitalizeAndLower(agent.name())+" Achievements: ^NNone!")+"^w\n\r\n\r");
				else
				{
					finalResponse.append("^H"+prefix+L(CMStrings.capitalizeAndLower(agent.name())+" Achievements:")+"^w\n\r");
					finalResponse.append(CMLib.lister().makeColumns(mob, AchievedList, null, 2).toString()+"^w\n\r\n\r");
				}
			}
		}
		if(finalResponse.length()==0)
			finalResponse.append("^H"+prefix+L("Achievements: ^NNone!")+"^w\n\r");
		if(announce)
			CMLib.commands().postSay(mob, finalResponse.toString());
		else
			mob.tell(finalResponse.toString());
		
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
