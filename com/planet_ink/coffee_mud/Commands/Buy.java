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
   Copyright 2000-2006 Bo Zimmerman

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
public class Buy extends StdCommand
{
	public Buy(){}

	private String[] access={getScr("Buy","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
        Environmental shopkeeper=CMLib.english().parseShopkeeper(mob,commands,getScr("Buy","buywhatwhom"));
		if(shopkeeper==null) return false;
		if(commands.size()==0)
		{
			mob.tell(getScr("Buy","buywhat"));
			return false;
		}
		if(CMLib.coffeeShops().getShopKeeper(shopkeeper)==null)
		{
			mob.tell(getScr("Buy","notaspk",shopkeeper.name()));
			return false;
		}

		int maxToDo=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(CMath.s_int((String)commands.firstElement())>0))
		{
			maxToDo=CMath.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}

		MOB mobFor=null;
		if((commands.size()>2)
		&&(((String)commands.elementAt(commands.size()-2)).equalsIgnoreCase("for")))
		{
			MOB M=mob.location().fetchInhabitant((String)commands.lastElement());
			if(M==null)
			{
				mob.tell(getScr("Buy","nonecalled",((String)commands.lastElement())));
				return false;
			}
			commands.removeElementAt(commands.size()-1);
			commands.removeElementAt(commands.size()-1);
			mobFor=M;
		}

		String whatName=CMParms.combine(commands,0);
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(whatName.toUpperCase().startsWith("ALL.")){ allFlag=true; whatName="ALL "+whatName.substring(4);}
		if(whatName.toUpperCase().endsWith(".ALL")){ allFlag=true; whatName="ALL "+whatName.substring(0,whatName.length()-4);}
		int addendum=1;
		do
		{
            ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(shopkeeper);
			Environmental itemToDo=SK.getShop().getStock(whatName,mob,SK.whatIsSold(),CMLib.utensils().roomStart(shopkeeper));
			if(itemToDo==null) break;
			if(CMLib.flags().canBeSeenBy(itemToDo,mob))
				V.addElement(itemToDo);
			if(addendum>=CMLib.coffeeShops().getShopKeeper(shopkeeper).getShop().numberInStock(itemToDo))
				break;
			++addendum;
		}
		while((allFlag)&&(addendum<=maxToDo));
		String forName="";
		if((mobFor!=null)&&(mobFor!=mob))
		{
			if(mobFor.name().indexOf(" ")>=0)
				forName=" "+getScr("Buy","forname1",mobFor.Name());
			else
				forName=" "+getScr("Buy","forname2",mobFor.Name());
		}

		if(V.size()==0)
            mob.tell(mob,shopkeeper,null,getScr("Buy","donthaveany",whatName));
        else
		for(int v=0;v<V.size();v++)
		{
			Environmental thisThang=(Environmental)V.elementAt(v);
			CMMsg newMsg=CMClass.getMsg(mob,shopkeeper,thisThang,CMMsg.MSG_BUY,getScr("Buy","buysfrom",forName));
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
		return false;
	}
    public double combatActionsCost(){return 1.0;}
    public double actionsCost(){return 0.25;}
	public boolean canBeOrdered(){return false;}

	
}
