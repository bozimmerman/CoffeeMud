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
public class Split extends StdCommand
{
	public Split(){}

	private String[] access={"SPLIT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Split how much?");
			return false;
		}
		String itemID=Util.combine(commands,1);
		long numGold=EnglishParser.numPossibleGold(itemID);
		if(numGold<0)
		{
			mob.tell("Split how much?!?");
			return false;
		}
		String currency=EnglishParser.numPossibleGoldCurrency(mob,itemID);
		double denom=EnglishParser.numPossibleGoldDenomination(mob,currency,itemID);

		int num=0;
		HashSet H=mob.getGroupMembers(new HashSet());
		
		for(Iterator e=((HashSet)H.clone()).iterator();e.hasNext();)
		{
			MOB recipient=(MOB)e.next();
			if((!recipient.isMonster())
			&&(recipient!=mob)
			&&(recipient.location()==mob.location())
			&&(mob.location().isInhabitant(recipient)))
				num++;
			else
				H.remove(recipient);
		}
		if(num==0)
		{
			mob.tell("No one appears to be eligible to receive any of your money.");
			return false;
		}

		double totalAbsoluteValue=Util.mul(numGold,denom);
		totalAbsoluteValue=Util.div(totalAbsoluteValue,num+1);
		if((totalAbsoluteValue*num)>BeanCounter.getTotalAbsoluteValue(mob,currency))
		{
			mob.tell("You don't have that much "+BeanCounter.getDenominationName(currency,denom)+".");
			return false;
		}
		Vector V=BeanCounter.makeAllCurrency(currency,totalAbsoluteValue);
		BeanCounter.subtractMoney(mob,totalAbsoluteValue*num);
		for(Iterator e=H.iterator();e.hasNext();)
		{
			MOB recipient=(MOB)e.next();
			for(int v=0;v<V.size();v++)
			{
			    Coins C=(Coins)V.elementAt(v);
			    C=(Coins)C.copyOf();
				mob.addInventory(C);
				FullMsg newMsg=new FullMsg(mob,recipient,C,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
				if(mob.location().okMessage(mob,newMsg))
					mob.location().send(mob,newMsg);
				C.putCoinsBack();
			}
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
