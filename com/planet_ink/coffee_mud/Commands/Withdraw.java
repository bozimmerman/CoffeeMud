package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
		String str=(String)commands.firstElement();
		if(((String)commands.lastElement()).equalsIgnoreCase("coins"))
		{
			if((!str.equalsIgnoreCase("all"))
			&&(Util.s_int((String)commands.firstElement())<=0))
			{
				mob.tell("Withdraw how much?");
				return false;
			}

			commands.removeElement(commands.lastElement());
		}
		if(((String)commands.lastElement()).equalsIgnoreCase("gold"))
		{
			if((!str.equalsIgnoreCase("all"))
			&&(Util.s_int((String)commands.firstElement())<=0))
			{
				mob.tell("Withdraw how much?");
				return false;
			}
			commands.removeElement(commands.lastElement());
		}

		String thisName=Util.combine(commands,0);
		Item thisThang=null;
		if(thisName.equalsIgnoreCase("all"))
			thisThang=((Banker)shopkeeper).findDepositInventory(mob,""+Integer.MAX_VALUE);
		else
			thisThang=((Banker)shopkeeper).findDepositInventory(mob,thisName);
		if((thisThang==null)
		&&(((Banker)shopkeeper).whatIsSold()!=ShopKeeper.DEAL_CLANBANKER)
		&&(mob.isMarriedToLiege()))
		{
			MOB mob2=CMMap.getPlayer(mob.getLiegeID());
			if(thisName.equalsIgnoreCase("all"))
				thisThang=((Banker)shopkeeper).findDepositInventory(mob2,""+Integer.MAX_VALUE);
			else
				thisThang=((Banker)shopkeeper).findDepositInventory(mob2,thisName);
		}

		if((thisThang==null)||(!Sense.canBeSeenBy(thisThang,mob)))
		{
			mob.tell("That doesn't appear to be available.  Try LIST.");
			return false;
		}
		if(thisThang instanceof Coins)
		{
			Coins oldThang=(Coins)thisThang;
			if(!thisName.equalsIgnoreCase("all"))
			{
				thisThang=(Item)oldThang.copyOf();
				((Coins)thisThang).setNumberOfCoins(Util.s_int(thisName));
				if(((Coins)thisThang).numberOfCoins()<=0)
				{
					mob.tell("Withdraw how much?");
					return false;
				}
			}
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
