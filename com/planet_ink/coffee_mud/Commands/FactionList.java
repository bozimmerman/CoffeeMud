package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
/**
 * <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Portions Copyright (c) 2004 Bo Zimmerman</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 */

public class FactionList extends StdCommand
{
	public FactionList(){}

	private String[] access={"FACTIONS","FAC"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("\n\r^HFaction Standings:^?^N\n\r");
        boolean none=true;
        for(Enumeration e=mob.fetchFactions();e.hasMoreElements();) {
            String name=(String)e.nextElement();
            Faction F=Factions.getFaction(name);
            if((F!=null)&&(F.showinfactionscommand))
            {
                none=false;
                msg.append(formatFactionLine(name,mob.fetchFaction(name)));
            }
        }
		if(!mob.isMonster())
            if(none)
                mob.session().colorOnlyPrintln("\n\r^HNo factions apply.^?^N");
            else
    			mob.session().colorOnlyPrintln(msg.toString());
		return false;
	}

    public String formatFactionLine(String name,int faction) 
    {
        StringBuffer line=new StringBuffer();
        line.append("  "+Util.padRight(Util.capitalize(Factions.getName(name).toLowerCase()),21)+" ");
        Faction.FactionRange FR=Factions.getRange(name,faction);
        if(FR==null)
	        line.append(Util.padRight(""+faction,17)+" ");
        else
	        line.append(Util.padRight(FR.Name,17)+" ");
        line.append("[");
        line.append(Util.padRight(calcRangeBar(name,faction),25));
        line.append("]\n\r");
        return line.toString();
    }

    public String calcRangeBar(String factionID, int faction) 
    {
		StringBuffer bar=new StringBuffer();
        Double fill=new Double(Util.div(Factions.getRangePercent(factionID,faction),4));
        for(int i=0;i<fill.intValue();i++) 
        {
            bar.append("*");
        }
        return bar.toString();
    }

	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
