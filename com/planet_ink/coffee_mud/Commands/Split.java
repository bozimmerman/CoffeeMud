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
public class Split extends StdCommand
{
	public Split(){}

	private String[] access={"SPLIT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Split how much?");
			return false;
		}
		String itemID=CMParms.combine(commands,1);
		long numGold=CMLib.english().numPossibleGold(mob,itemID);
		if(numGold<0)
		{
			mob.tell("Split how much?!?");
			return false;
		}
		String currency=CMLib.english().numPossibleGoldCurrency(mob,itemID);
		double denom=CMLib.english().numPossibleGoldDenomination(mob,currency,itemID);

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

		double totalAbsoluteValue=CMath.mul(numGold,denom);
		totalAbsoluteValue=CMath.div(totalAbsoluteValue,num+1);
		if((totalAbsoluteValue*num)>CMLib.beanCounter().getTotalAbsoluteValue(mob,currency))
		{
			mob.tell("You don't have that much "+CMLib.beanCounter().getDenominationName(currency,denom)+".");
			return false;
		}
		Vector V=CMLib.beanCounter().makeAllCurrency(currency,totalAbsoluteValue);
		CMLib.beanCounter().subtractMoney(mob,totalAbsoluteValue*num);
		for(Iterator e=H.iterator();e.hasNext();)
		{
			MOB recipient=(MOB)e.next();
			for(int v=0;v<V.size();v++)
			{
			    Coins C=(Coins)V.elementAt(v);
			    C=(Coins)C.copyOf();
				mob.addInventory(C);
				CMMsg newMsg=CMClass.getMsg(mob,recipient,C,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
				if(mob.location().okMessage(mob,newMsg))
					mob.location().send(mob,newMsg);
				C.putCoinsBack();
			}
		}
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
