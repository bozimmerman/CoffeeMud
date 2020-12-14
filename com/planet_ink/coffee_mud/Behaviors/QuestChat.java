package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2006-2020 Bo Zimmerman

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

public class QuestChat extends MudChat
{
	@Override
	public String ID()
	{
		return "QuestChat";
	}

	private final Map<String, List<String>>	alreadySaid	= new Hashtable<String, List<String>>();
	private String							myQuestName	= null;

	@Override
	public void registerDefaultQuest(final Object questName)
	{
		if(questName instanceof String)
			myQuestName = (String)questName;
		else
		if(questName instanceof CMObject)
			myQuestName = ((CMObject)questName).name();
	}

	@Override
	protected boolean match(final MOB speaker, final ChatMatch match, final String message, final String[] rest)
	{
		final int codeDex=match.str.lastIndexOf("::");
		if((codeDex>0)
		&&(codeDex<match.str.length()-2))
		{
			final String codeStr=match.str.substring(codeDex+2).trim();
			final String newExp=match.str.substring(0,codeDex);
			List<String> V=alreadySaid.get(speaker.Name().toUpperCase());
			if(V==null)
			{
				V=new Vector<String>();
				alreadySaid.put(speaker.Name().toUpperCase(),V);
			}
			else
			if(V.contains(codeStr))
				return false;
			final ChatMatch newMatch=new ChatMatch();
			newMatch.flag=match.flag;
			newMatch.str=newExp;
			if(super.match(speaker,newMatch,message,rest))
			{
				V.add(codeStr);
				if((myQuestName!=null)&&(myQuestName.length()>0))
				{
					final Quest myQuest=CMLib.quests().fetchQuest(myQuestName);
					if(myQuest!=null)
					{
						String stat=myQuest.getStat("CHAT:"+speaker.Name().toUpperCase());
						if(stat.length()>0)
							stat+=" ";
						myQuest.setStat("CHAT:"+speaker.Name().toUpperCase(),stat+codeStr);
					}
				}
				return true;
			}
			return false;
		}
		return super.match(speaker,match,message,rest);
	}
}
