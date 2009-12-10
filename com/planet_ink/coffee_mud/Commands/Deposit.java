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
public class Deposit extends StdCommand
{
	public Deposit(){}

	private String[] access={"DEPOSIT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
        Environmental shopkeeper=CMLib.english().parseShopkeeper(mob,commands,"Deposit how much with whom?");
        ShopKeeper SHOP=CMLib.coffeeShops().getShopKeeper(shopkeeper);
		if(shopkeeper==null) return false;
		if((!(SHOP instanceof Banker))&&(!(SHOP instanceof PostOffice)))
		{
			mob.tell("You can not deposit anything with "+shopkeeper.name()+".");
			return false;
		}
		if(commands.size()==0)
		{
			mob.tell("Deposit what or how much?");
			return false;
		}
		String thisName=CMParms.combine(commands,0);
		Item thisThang=CMLib.english().bestPossibleGold(mob,null,thisName);
		if(thisThang==null)
		{
			thisThang=mob.fetchCarried(null,thisName);
			if((thisThang==null)||(!CMLib.flags().canBeSeenBy(thisThang,mob)))
			{
				mob.tell("You don't seem to be carrying that.");
				return false;
			}
		}
		else
	    if(((Coins)thisThang).getNumberOfCoins()<CMLib.english().numPossibleGold(mob,thisName))
	        return false;
		CMMsg newMsg=null;
        if(SHOP instanceof Banker)
            newMsg=CMClass.getMsg(mob,shopkeeper,thisThang,CMMsg.MSG_DEPOSIT,"<S-NAME> deposit(s) <O-NAME> into <S-HIS-HER> account with <T-NAMESELF>.");
        else
            newMsg=CMClass.getMsg(mob,shopkeeper,thisThang,CMMsg.MSG_DEPOSIT,"<S-NAME> mail(s) <O-NAME>.");
		if(mob.location().okMessage(mob,newMsg))
			mob.location().send(mob,newMsg);
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return false;}

	
}
