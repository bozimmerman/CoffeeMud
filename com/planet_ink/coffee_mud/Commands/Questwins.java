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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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

public class Questwins extends StdCommand
{
	public Questwins()
	{
	}

	private final String[]	access	= I(new String[] { "QUESTS", "QUESTWINS" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public String getQuestsWonList(MOB mob, String pronoun)
	{
		final ArrayList<Quest> qVec=new ArrayList<Quest>();
		for(int q=0;q<CMLib.quests().numQuests();q++)
		{
			final Quest Q=CMLib.quests().fetchQuest(q);
			if(Q.wasWinner(mob.Name()))
			{
				if(!qVec.contains(Q))
					qVec.add(Q);
			}
		}
		Collections.sort(qVec,new Comparator<Quest>()
		{
			@Override
			public int compare(Quest o1, Quest o2)
			{
				if(o1 == null)
					return (o2==null) ? 0 : -1;
				else
				if(o2 == null)
					return 1;
				else
				{
					final String name1=o1.displayName().trim().length()>0?o1.displayName():o1.name();
					final String name2=o2.displayName().trim().length()>0?o2.displayName():o2.name();
					return name1.compareTo(name2);
				}
			}
			
		});
		final StringBuffer msg=new StringBuffer(L("^HQuests @x1 listed as having won:^?^N\n\r",pronoun));
		for(Quest Q : qVec)
		{
			final String name=Q.displayName().trim().length()>0?Q.displayName():Q.name();
			final String time = CMLib.time().date2String(Q.whenLastWon(mob.Name()).longValue());
			msg.append(CMStrings.padRight(time,20)+name+"^N\n\r");
		}
		return msg.toString();
	}

	public String getQuestsDoingList(MOB mob, String pronoun)
	{
		final List<Quest> qQVec=CMLib.quests().getPlayerPersistentQuests(mob);
		final Vector<String> qVec = new Vector<String>();
		for(final Quest Q : qQVec)
		{
			final String name=Q.displayName().trim().length()>0?Q.displayName():Q.name();
			if(!qVec.contains(name))
				qVec.add(name);
		}
		Collections.sort(qVec);
		final StringBuffer msg=new StringBuffer(L("^HQuests @x1 listed as having accepted:^?^N\n\r",pronoun));
		for(int i=0;i<qVec.size();i++)
			msg.append((qVec.get(i))+"^N\n\r");
		return msg.toString();
	}

	public Quest findScriptedQuest(final MOB mob, String rest)
	{
		final List<Quest> quests = new ArrayList<Quest>(mob.numScripts());
		for(final Enumeration<ScriptingEngine> e=mob.scripts();e.hasMoreElements();)
		{
			final ScriptingEngine SE=e.nextElement();
			if(SE!=null)
			{
				if(SE.defaultQuestName().length()>0)
				{
					Quest Q=CMLib.quests().fetchQuest(SE.defaultQuestName());
					if(Q==null) 
						Q=CMLib.quests().findQuest(SE.defaultQuestName());
					if(Q!=null)
						quests.add(Q);
				}
			}
		}
		return findQuest(quests, rest);
	}
	
	public Quest findQuest(List<Quest> fromList, String rest)
	{
		Quest Q=null;
		String lowerRest = rest.toLowerCase();
		for(final Iterator<Quest> q=fromList.iterator();q.hasNext();)
		{
			final Quest Q2=q.next();
			if((Q2!=null)
			&&(Q2.displayName()!=null)
			&&(Q2.displayName().length()>0)
			&&(Q2.displayName().toLowerCase().startsWith(lowerRest)))
			{
				Q=Q2;
				break;
			}
		}
		if(Q==null)
		{
			for(final Iterator<Quest> q=fromList.iterator();q.hasNext();)
			{
				final Quest Q2=q.next();
				if((Q2!=null)
				&&(Q2.displayName()!=null)
				&&(Q2.displayName().length()>0)
				&&(CMLib.english().containsString(Q2.displayName().toUpperCase(), rest)))
				{
					Q=Q2;
					break;
				}
			}
		}
		if(Q==null)
		{
			for(final Iterator<Quest> q=fromList.iterator();q.hasNext();)
			{
				final Quest Q2=q.next();
				if((Q2!=null)
				&&(Q2.name().toLowerCase().startsWith(lowerRest)))
				{
					Q=Q2;
					break;
				}
			}
		}
		if(Q==null)
		{
			for(final Iterator<Quest> q=fromList.iterator();q.hasNext();)
			{
				final Quest Q2=q.next();
				if((Q2!=null)
				&&(CMLib.english().containsString(Q2.name().toUpperCase(), rest)))
				{
					Q=Q2;
					break;
				}
			}
		}
		if(Q==null)
			Q=CMLib.quests().findQuest(rest);
		return Q;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.get(0).toUpperCase().startsWith("QUESTW"))
			commands.add("WON");

		if((commands.size()>1)&&(commands.get(commands.size()-1).equalsIgnoreCase("WON")))
		{
			final String msg=this.getQuestsWonList(mob, L("you are"));
			if(!mob.isMonster())
				mob.tell(msg);
		}
		else
		if((commands.size()>2)&&(commands.get(1).equalsIgnoreCase("DROP")))
		{
			ScriptingEngine foundS=null;
			for(final Enumeration<ScriptingEngine> e=mob.scripts();e.hasMoreElements();)
			{
				final ScriptingEngine SE=e.nextElement();
				if(SE!=null)
				{
					if((SE.defaultQuestName().length()>0)
					&&(CMLib.quests().findQuest(SE.defaultQuestName())==null))
						foundS=SE;
				}
			}
			if(foundS!=null)
				mob.delScript(foundS);
			foundS=null;

			final String rest=CMParms.combine(commands,2);
			final Quest Q=findScriptedQuest(mob, rest);
			if(Q==null)
			{
				mob.tell(L("There is no such quest as '@x1'.",rest));
				return false;
			}
			for(final Enumeration<ScriptingEngine> e=mob.scripts();e.hasMoreElements();)
			{
				final ScriptingEngine SE=e.nextElement();
				if(SE!=null)
				{
					if((SE.defaultQuestName().length()>0)
					&&(SE.defaultQuestName().equalsIgnoreCase(Q.name())))
						foundS=SE;
				}
			}
			if(foundS==null)
			{
				mob.tell(L("You have not accepted a quest called '@x1'.  Enter QUESTS for a list.",rest));
				return false;
			}
			if((!mob.isMonster()&&(mob.session().confirm(L("Drop the quest '@x1', are you sure (y/N)?",Q.name()),"N"))))
			{
				CMLib.coffeeTables().bump(Q,CoffeeTableRow.STAT_QUESTDROPPED);
				mob.delScript(foundS);
				mob.tell(L("Quest dropped."));
				return false;
			}
		}
		else
		if(commands.size()==1)
		{
			final String msg=getQuestsDoingList(mob, L("you are"));
			if(!mob.isMonster())
				mob.tell(L("@x1\n\r^HEnter QUEST [QUEST NAME] for more information.^N^.",msg.toString()));

		}
		else
		{
			final boolean admin=CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDQUESTS)
								||CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDPLAYERS);
			final String rest=CMParms.combine(commands,1);
			if(admin)
			{
				final MOB M=CMLib.players().getLoadPlayer(rest);
				if(M!=null)
				{
					String msg=getQuestsWonList(M, M.Name()+" is");
					msg += "\n\r";
					msg += getQuestsDoingList(M, M.Name()+" is");
					if(!mob.isMonster())
						mob.tell(msg);
					return false;
				}
			}
			Quest Q=findScriptedQuest(mob, rest);
			if(Q==null)
				Q=findQuest(new XVector<Quest>(CMLib.quests().enumQuests()),rest);
			if(Q==null)
			{
				if(admin)
					mob.tell(L("There is no such quest or player as '@x1'.",rest));
				else
					mob.tell(L("There is no such quest as '@x1'.",rest));
				return false;
			}
			ScriptingEngine foundS=null;
			for(final Enumeration<ScriptingEngine> e=mob.scripts();e.hasMoreElements();)
			{
				final ScriptingEngine SE=e.nextElement();
				if(SE==null)
					continue;
				if((SE.defaultQuestName().length()>0)
				&&(SE.defaultQuestName().equalsIgnoreCase(Q.name())))
					foundS=SE;
			}
			if(foundS==null)
			{
				if(admin)
				{
					Map<String,Long> winners = Q.getWinners();
					final StringBuffer msg=new StringBuffer(L("^HWinners of Quest '@x1':^?^N\n\r",Q.name()));
					for(String name : winners.keySet())
					{
						final String time = CMLib.time().date2String(Q.whenLastWon(name).longValue());
						msg.append(CMStrings.padRight(time,20)+name+"^N\n\r");
					}
					if(mob.session()!=null)
						mob.session().colorOnlyPrintln(msg.toString());
					return true;
				}
				else
				{
					mob.tell(L("You have not accepted a quest called '@x1'.  Enter QUESTS for a list.",rest));
					return false;
				}
			}
			String name=Q.displayName().trim().length()>0?Q.displayName():Q.name();
			if(!Q.name().equals(name))
				name+=" ("+Q.name()+")";
			mob.tell(L("^HQuest Information: ^w@x1^N",name));
			String instructions=foundS.getVar("*","INSTRUCTIONS");
			if((instructions==null)||(instructions.length()==0))
				instructions=Q.isStat("INSTRUCTIONS")?Q.getStat("INSTRUCTIONS"):null;
			if((instructions==null)||(instructions.length()==0))
				instructions="No further information available.";
			String timeRemaining=foundS.getVar("*","TIME_REMAINING");
			if((timeRemaining!=null)&&(timeRemaining.length()>0))
			{
				final String timeRemainingType=foundS.getVar("*","TIME_REMAINING_TYPE");
				if(((timeRemainingType.equalsIgnoreCase("TICKS")||(timeRemainingType.length()==0))
				&&(CMath.isInteger(timeRemaining))))
				{
					long ticks=CMath.s_int(timeRemaining);
					ticks*=CMProps.getTickMillis();
					if(ticks>60000)
						timeRemaining=(ticks/60000)+" minutes";
					else
						timeRemaining=(ticks/1000)+" seconds";
				}
				else
				if(timeRemainingType.length()>0)
					timeRemaining+=" "+timeRemainingType;
			}
			final String progress=foundS.getVar("*","PROGRESS");
			mob.tell("^w"+instructions+"^N");
			if((timeRemaining!=null)&&(timeRemaining.length()>0))
				mob.tell(L("\n\r^yTime Remaining: ^w@x1^N",timeRemaining));
			if((progress!=null)&&(progress.length()>0))
				mob.tell(L("\n\r^yProgress: ^w@x1^N",progress));
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
