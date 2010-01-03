package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Questwins extends StdCommand
{
	public Questwins(){}

	private String[] access={"QUESTS","QUESTWINS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
	    if((commands.size()==1)&&(((String)commands.firstElement()).startsWith("QUESTW")))
	        commands.addElement("WON");
	    if((commands.size()>1)&&(((String)commands.elementAt(1)).equalsIgnoreCase("WON")))
        {
    		Vector qVec=new Vector();
    		for(int q=0;q<CMLib.quests().numQuests();q++)
    		{
    			Quest Q=CMLib.quests().fetchQuest(q);
    			if(Q.wasWinner(mob.Name()))
                {
                    String name=Q.displayName().trim().length()>0?Q.displayName():Q.name();
                    if(!qVec.contains(name))
                        qVec.addElement(name);
                }
    		}
    		Collections.sort(qVec);
    		StringBuffer msg=new StringBuffer("^HQuests you are listed as having won:^?^N\n\r");
    		for(int i=0;i<qVec.size();i++)
                msg.append(((String)qVec.elementAt(i))+"^N\n\r");
    		if(!mob.isMonster())
    			mob.tell(msg.toString());
        }
	    else
        if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("DROP")))
        {
            ScriptingEngine foundS=null;
            for(int s=0;s<mob.numScripts();s++)
            {
                ScriptingEngine S=mob.fetchScript(s);
                if(S==null) continue;
                if((S.defaultQuestName().length()>0)
                &&(CMLib.quests().findQuest(S.defaultQuestName())==null))
                    foundS=S;
            }
            if(foundS!=null)
                mob.delScript(foundS);
            foundS=null;
            
            String rest=CMParms.combine(commands,2);
            Quest Q=CMLib.quests().findQuest(rest);
            if(Q==null)
            {
                mob.tell("There is no such quest as '"+rest+"'.");
                return false;
            }
            for(int s=0;s<mob.numScripts();s++)
            {
                ScriptingEngine S=mob.fetchScript(s);
                if(S==null) continue;
                if((S.defaultQuestName().length()>0)
                &&(S.defaultQuestName().equalsIgnoreCase(Q.name())))
                    foundS=S;
            }
            if(foundS==null)
            {
                mob.tell("You have not accepted a quest called '"+rest+"'.  Enter QUESTS for a list.");
                return false;
            }
            if((!mob.isMonster()&&(mob.session().confirm("Drop the quest '"+Q.name()+"', are you sure (y/N)?","N"))))
            {
            	CMLib.coffeeTables().bump(Q,CoffeeTableRow.STAT_QUESTDROPPED);
                mob.delScript(foundS);
                mob.tell("Quest dropped.");
                return false;
            }
        }
        else
	    if(commands.size()==1)
	    {
            Vector<Quest> qQVec=CMLib.quests().getPlayerPersistantQuests(mob);
            Vector<String> qVec = new Vector<String>();
            for(Quest Q : qQVec)
            {
                String name=Q.displayName().trim().length()>0?Q.displayName():Q.name();
                if(!qVec.contains(name))
                    qVec.addElement(name);
            }
            Collections.sort(qVec);
            StringBuffer msg=new StringBuffer("^HQuests you are listed as having accepted:^?^N\n\r");
            for(int i=0;i<qVec.size();i++)
                msg.append(((String)qVec.elementAt(i))+"^N\n\r");
            if(!mob.isMonster())
                mob.tell(msg.toString()+"\n\r^HEnter QUEST [QUEST NAME] for more information.^N^.");
	        
	    }
	    else
	    {
	        String rest=CMParms.combine(commands,1);
	        Quest Q=CMLib.quests().findQuest(rest);
	        if(Q==null)
	        {
	            mob.tell("There is no such quest as '"+rest+"'.");
	            return false;
	        }
	        ScriptingEngine foundS=null;
            for(int s=0;s<mob.numScripts();s++)
            {
                ScriptingEngine S=mob.fetchScript(s);
                if(S==null) continue;
                if((S.defaultQuestName().length()>0)
                &&(S.defaultQuestName().equalsIgnoreCase(Q.name())))
                    foundS=S;
            }
            if(foundS==null)
            {
                mob.tell("You have not accepted a quest called '"+rest+"'.  Enter QUESTS for a list.");
                return false;
            }
            String name=Q.displayName().trim().length()>0?Q.displayName():Q.name();
            if(!Q.name().equals(name))
                name+=" ("+Q.name()+")";
            mob.tell("^HQuest Information: ^w"+name+"^N");
            String instructions=null;
            if((instructions==null)||(instructions.length()==0))
                instructions=foundS.getVar("*","INSTRUCTIONS");
            if((instructions==null)||(instructions.length()==0))
                instructions=Q.isStat("INSTRUCTIONS")?Q.getStat("INSTRUCTIONS"):null;
            if((instructions==null)||(instructions.length()==0))
                instructions="No further information available.";
            String timeRemaining=foundS.getVar("*","TIME_REMAINING");
            if((timeRemaining!=null)&&(timeRemaining.length()>0))
            {
                String timeRemainingType=foundS.getVar("*","TIME_REMAINING_TYPE");
                if(((timeRemainingType.equalsIgnoreCase("TICKS")||(timeRemainingType.length()==0))
                &&(CMath.isInteger(timeRemaining))))
                {
                    long ticks=CMath.s_int(timeRemaining);
                    ticks*=Tickable.TIME_TICK;
                    if(ticks>60000)
                        timeRemaining=(ticks/60000)+" minutes";
                    else
                        timeRemaining=(ticks/1000)+" seconds";
                }
                else
                if(timeRemainingType.length()>0)
                    timeRemaining+=" "+timeRemainingType;
            }
            String progress=foundS.getVar("*","PROGRESS");
            mob.tell("^w"+instructions+"^N");
            if((timeRemaining!=null)&&(timeRemaining.length()>0))
                mob.tell("\n\r^yTime Remaining: ^w"+timeRemaining+"^N");
            if((progress!=null)&&(progress.length()>0))
                mob.tell("\n\r^yProgress: ^w"+progress+"^N");
	    }
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
