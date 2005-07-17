package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Give extends BaseItemParser
{
	public Give(){}

	private String[] access={"GIVE","GI"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Give what to whom?");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("To whom should I give that?");
			return false;
		}

		MOB recipient=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
		if((recipient==null)||((recipient!=null)&&(!Sense.canBeSeenBy(recipient,mob))))
		{
			mob.tell("I don't see anyone called "+(String)commands.elementAt(commands.size()-1)+" here.");
			return false;
		}
		commands.removeElementAt(commands.size()-1);
		if((commands.size()>0)&&(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("to")))
			commands.removeElementAt(commands.size()-1);

		int maxToGive=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(EnglishParser.numPossibleGold(mob,Util.combine(commands,0))==0)
		&&(Util.s_int((String)commands.firstElement())>0))
		{
			maxToGive=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}

		String thingToGive=Util.combine(commands,0);
		int addendum=1;
		String addendumStr="";
		Vector V=new Vector();
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(thingToGive.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(4);}
		if(thingToGive.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(0,thingToGive.length()-4);}
        boolean onlyGoldFlag=hasOnlyGoldInInventory(mob);
        Item giveThis=EnglishParser.bestPossibleGold(mob,null,thingToGive);
        if(giveThis!=null)
        {
            if(((Coins)giveThis).getNumberOfCoins()<EnglishParser.numPossibleGold(mob,thingToGive))
                return false;
            if(Sense.canBeSeenBy(giveThis,mob))
                V.addElement(giveThis);
        }
        if(V.size()==0)
		do
		{
			giveThis=mob.fetchCarried(null,thingToGive+addendumStr);
			if((giveThis==null)
			&&(V.size()==0)
			&&(addendumStr.length()==0)
			&&(!allFlag))
			{
				giveThis=mob.fetchWornItem(thingToGive);
				if(giveThis!=null)
				{
					if((!(giveThis).amWearingAt(Item.HELD))&&(!(giveThis).amWearingAt(Item.WIELD)))
					{
						mob.tell("You must remove that first.");
						return false;
					}
					else
					{
						FullMsg newMsg=new FullMsg(mob,giveThis,null,CMMsg.MSG_REMOVE,null);
						if(mob.location().okMessage(mob,newMsg))
							mob.location().send(mob,newMsg);
						else
							return false;
					}
				}
			}
            if((allFlag)&&(!onlyGoldFlag)&&(giveThis instanceof Coins)&&(thingToGive.equalsIgnoreCase("all")))
                giveThis=null;
            else
            {
    			if(giveThis==null) break;
    			if(Sense.canBeSeenBy(giveThis,mob))
    				V.addElement(giveThis);
    			addendumStr="."+(++addendum);
            }
		}
		while((allFlag)&&(addendum<=maxToGive));

		if(V.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<V.size();i++)
		{
			giveThis=(Item)V.elementAt(i);
			FullMsg newMsg=new FullMsg(mob,recipient,giveThis,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
			if(giveThis instanceof Coins)
				((Coins)giveThis).putCoinsBack();
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
