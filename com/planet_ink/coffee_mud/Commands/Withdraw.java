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
public class Withdraw extends StdCommand
{
	public Withdraw(){}

	private String[] access={"WITHDRAW"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		MOB shopkeeper=EnglishParser.parseShopkeeper(mob,commands,"Withdraw how much from whom?");
		if(shopkeeper==null) return false;
		if(!(shopkeeper instanceof Banker))
		{
			mob.tell("You can not withdraw anything from "+shopkeeper.name()+".");
			return false;
		}
		if(commands.size()==0)
		{
			mob.tell("Withdraw what or how much?");
			return false;
		}
		String str=Util.combine(commands,0);
		if(str.equalsIgnoreCase("all")) str=""+Integer.MAX_VALUE;
	    long numCoins=EnglishParser.numPossibleGold(null,str);
	    String currency=EnglishParser.numPossibleGoldCurrency(shopkeeper,str);
	    double denomination=EnglishParser.numPossibleGoldDenomination(shopkeeper,currency,str);
		Item thisThang=null;
		if(numCoins>0)
		{
		    if(denomination==0.0)
		    {
				mob.tell("Withdraw how much?");
				return false;
		    }
		    else
		    {
				thisThang=((Banker)shopkeeper).findDepositInventory(mob,""+Integer.MAX_VALUE);
				if(thisThang instanceof Coins)
				    thisThang=BeanCounter.makeCurrency(currency,denomination,numCoins);
		    }
		}
		else
			thisThang=((Banker)shopkeeper).findDepositInventory(mob,str);

		if(((thisThang==null)||((thisThang instanceof Coins)&&(((Coins)thisThang).getNumberOfCoins()<=0)))
		&&(((Banker)shopkeeper).whatIsSold()!=ShopKeeper.DEAL_CLANBANKER)
		&&(mob.isMarriedToLiege()))
		{
			MOB mob2=CMMap.getPlayer(mob.getLiegeID());
			if(numCoins>0)
			{
				thisThang=((Banker)shopkeeper).findDepositInventory(mob2,""+Integer.MAX_VALUE);
				if(thisThang instanceof Coins)
				    thisThang=BeanCounter.makeCurrency(currency,denomination,numCoins);
				else
			    {
					mob.tell("Withdraw how much?");
					return false;
			    }
			}
			else
				thisThang=((Banker)shopkeeper).findDepositInventory(mob2,str);
		}

		if((thisThang==null)||(!Sense.canBeSeenBy(thisThang,mob)))
		{
			mob.tell("That doesn't appear to be available.  Try LIST.");
			return false;
		}
		FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,CMMsg.MSG_WITHDRAW,"<S-NAME> withdraw(s) <O-NAME> from <S-HIS-HER> account with "+shopkeeper.name()+".");
		if(!mob.location().okMessage(mob,newMsg))
			return false;
		mob.location().send(mob,newMsg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
