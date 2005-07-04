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
public class Deposit extends BaseItemParser
{
	public Deposit(){}

	private String[] access={"DEPOSIT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		MOB shopkeeper=EnglishParser.parseShopkeeper(mob,commands,"Deposit how much with whom?");
		if(shopkeeper==null) return false;
		if((!(shopkeeper instanceof Banker))&&(!(shopkeeper instanceof PostOffice)))
		{
			mob.tell("You can not deposit anything with "+shopkeeper.name()+".");
			return false;
		}
		if(commands.size()==0)
		{
			mob.tell("Deposit what or how much?");
			return false;
		}
		String thisName=Util.combine(commands,0);
		Item thisThang=EnglishParser.bestPossibleGold(mob,null,thisName);
		if(thisThang==null)
		{
			thisThang=mob.fetchCarried(null,thisName);
			if((thisThang==null)||(!Sense.canBeSeenBy(thisThang,mob)))
			{
				mob.tell("You don't seem to be carrying that.");
				return false;
			}
		}
		else
	    if(((Coins)thisThang).getNumberOfCoins()<EnglishParser.numPossibleGold(mob,thisName))
	        return false;
		FullMsg newMsg=null;
        if(shopkeeper instanceof Banker)
            newMsg=new FullMsg(mob,shopkeeper,thisThang,CMMsg.MSG_DEPOSIT,"<S-NAME> deposit(s) <O-NAME> into <S-HIS-HER> account with <T-NAMESELF>.");
        else
            newMsg=new FullMsg(mob,shopkeeper,thisThang,CMMsg.MSG_DEPOSIT,"<S-NAME> mail(s) <O-NAME>.");
		if(mob.location().okMessage(mob,newMsg))
			mob.location().send(mob,newMsg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
