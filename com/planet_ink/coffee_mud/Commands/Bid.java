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
public class Bid extends StdCommand
{
	public Bid(){}

	private String[] access={"BID"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
        Environmental shopkeeper=CMLib.english().parseShopkeeper(mob,commands,"Bid how much, on what, with whom?");
		if(shopkeeper==null) return false;
		if(commands.size()<2)
		{
			mob.tell("Bid how much on what?");
			return false;
		}
		if(!(CMLib.coffeeShops().getShopKeeper(shopkeeper) instanceof Auctioneer))
		{
			mob.tell(shopkeeper.name()+" is not an auctioneer!");
			return false;
		}

		String bidStr=(String)commands.firstElement();
		if(CMLib.english().numPossibleGold(mob,bidStr)<=0)
		{
			mob.tell("It does not look like '"+bidStr+"' is enough to offer.");
			return false;
		}
		Object[] bidThang=CMLib.english().parseMoneyStringSDL(mob,bidStr,null);
		bidStr=CMLib.beanCounter().nameCurrencyShort((String)bidThang[0],CMath.mul(((Double)bidThang[1]).doubleValue(),((Long)bidThang[2]).longValue()));
		commands.removeElementAt(0);
		
		int maxToDo=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(CMath.s_int((String)commands.firstElement())>0))
		{
			maxToDo=CMath.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}

		String whatName=CMParms.combine(commands,0);
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(whatName.toUpperCase().startsWith("ALL.")){ allFlag=true; whatName="ALL "+whatName.substring(4);}
		if(whatName.toUpperCase().endsWith(".ALL")){ allFlag=true; whatName="ALL "+whatName.substring(0,whatName.length()-4);}
		int addendum=1;
		boolean doBugFix = true;
		while(doBugFix || ((allFlag)&&(addendum<=maxToDo)))
		{
			doBugFix=false;
            ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(shopkeeper);
			Environmental itemToDo=SK.getShop().getStock(whatName,mob);
			if(itemToDo==null) break;
			if(CMLib.flags().canBeSeenBy(itemToDo,mob))
				V.addElement(itemToDo);
			if(addendum>=CMLib.coffeeShops().getShopKeeper(shopkeeper).getShop().numberInStock(itemToDo))
				break;
			++addendum;
		}
		if(V.size()==0)
            mob.tell(mob,shopkeeper,null,"<T-NAME> do(es)n't appear to have any '"+whatName+"' available for auction.  Try LIST.");
        else
		for(int v=0;v<V.size();v++)
		{
			Environmental thisThang=(Environmental)V.elementAt(v);
			CMMsg newMsg=CMClass.getMsg(mob,shopkeeper,thisThang,
					CMMsg.MSG_BID,"<S-NAME> bid(s) "+bidStr+" on <O-NAME> with <T-NAMESELF>.",
					CMMsg.MSG_BID,"<S-NAME> bid(s) '"+bidStr+"' on <O-NAME> with <T-NAMESELF>.",
					CMMsg.MSG_BID,"<S-NAME> place(s) a bid with <T-NAMESELF>."
					);
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return false;}
}
