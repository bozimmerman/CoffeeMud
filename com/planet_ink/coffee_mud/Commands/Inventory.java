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
public class Inventory extends StdCommand
{
	public Inventory(){}

	private String[] access={"INVENTORY","INV","I"};
	public String[] getAccessWords(){return access;}
	
	public static class InventoryList
	{
		public boolean foundAndSeen=false;
		public boolean foundButUnseen=false;
		public Vector<Item> viewItems=new Vector<Item>();
		public Hashtable<String,Vector<Coins>> moneyItems=new Hashtable<String,Vector<Coins>>();
	}
	
	public static InventoryList fetchInventory(MOB seer, MOB mob)
	{
		InventoryList lst = new InventoryList();
		Vector<Coins> coinsV=null;
		int insertAt=-1;
		CMLib.beanCounter().getTotalAbsoluteNativeValue(mob);
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item thisItem=mob.fetchInventory(i);
			if((thisItem!=null)
			&&(thisItem.container()==null)
			&&(thisItem.amWearingAt(Wearable.IN_INVENTORY)))
			{
				if(CMLib.flags().canBeSeenBy(thisItem,seer))
					lst.foundAndSeen=true;
                else
                	lst.foundButUnseen=true;
				if((!(thisItem instanceof Coins))||(((Coins)thisItem).getDenomination()==0.0))
					lst.viewItems.addElement(thisItem);
				else
				{
					coinsV=lst.moneyItems.get(((Coins)thisItem).getCurrency());
				    if(coinsV==null)
				    {
				    	coinsV=new Vector<Coins>();
				        lst.moneyItems.put(((Coins)thisItem).getCurrency(),coinsV);
				    }
                    for(insertAt=0;insertAt<coinsV.size();insertAt++)
                        if(((Coins)coinsV.elementAt(insertAt)).getDenomination()>((Coins)thisItem).getDenomination())
                            break;
                    if(insertAt>=coinsV.size())
                    	coinsV.addElement((Coins)thisItem);
                    else
                    	coinsV.insertElementAt((Coins)thisItem,insertAt);
				}
			}
		}
		return lst;
	}

	protected static String getShowableMoney(InventoryList list)
	{
		StringBuilder msg=new StringBuilder("");
		if(list.moneyItems.size()>0)
		{
		    msg.append("\n\r^HMoney:^N\n\r");
		    Item I=null;
			for(Enumeration e=list.moneyItems.keys();e.hasMoreElements();)
			{
			    String key=(String)e.nextElement();
			    Vector<Coins> V=list.moneyItems.get(key);
                double totalValue=0.0;
			    for(int v=0;v<V.size();v++)
			    {
			        I=(Item)V.elementAt(v);
			        if(v>0) msg.append(", ");
                    if(I instanceof Coins)
                        totalValue+=((Coins)I).getTotalValue();
			        msg.append(I.name());
			    }
                msg.append(" ^N("+CMLib.beanCounter().abbreviatedPrice(key,totalValue)+")");
			    if(e.hasMoreElements()) msg.append("\n\r");
			}
		}
		return msg.toString();
	}
	
	public static StringBuilder getInventory(MOB seer, MOB mob, String mask)
	{
		StringBuilder msg=new StringBuilder("");
		InventoryList list = fetchInventory(seer,mob);
		if(((list.viewItems.size()>0)||(list.moneyItems.size()>0))
        &&(!list.foundAndSeen))
        {
			list.viewItems.clear();
			list.moneyItems.clear();
			list.foundButUnseen=true;
        }
		else
		if((mask!=null)&&(mask.trim().length()>0))
		{
			mask=mask.trim().toUpperCase();
			if(!mask.startsWith("all")) mask="all "+mask;
			Vector<Item> V=(Vector<Item>)list.viewItems.clone();
			list.viewItems.clear();
			Item I=(V.size()>0)?(Item)V.firstElement():null;
			while(I!=null)
			{
				I=(Item)CMLib.english().fetchEnvironmental(V,mask,false);
				if(I!=null)
				{
					list.viewItems.addElement(I);
					V.remove(I);
				}
			}
		}
		if((list.viewItems.size()==0)&&(list.moneyItems.size()==0))
		{
			if((mask!=null)&&(mask.trim().length()>0))
				msg.append("(nothing like that you can see right now)");
			else
				msg.append("(nothing you can see right now)");
		}
		else
		{
			if(list.viewItems.size()>0)
				msg.append(CMLib.lister().lister(seer,list.viewItems,true,"MItem","",false,CMath.bset(seer.getBitmap(),MOB.ATT_COMPRESS)));
            if(list.foundButUnseen)
                msg.append("(stuff you can't see right now)");
                
            msg.append(getShowableMoney(list));
		}
		return msg;
	}


	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()==1)&&(commands.firstElement() instanceof MOB))
		{
			commands.addElement(getInventory((MOB)commands.firstElement(),mob,null));
			return true;
		}
		StringBuilder msg=getInventory(mob,mob,CMParms.combine(commands,1));
		if(msg.length()==0)
			mob.tell("^HYou are carrying:\n\r^!Nothing!^?\n\r");
		else
		if(!mob.isMonster())
			mob.session().wraplessPrintln("^HYou are carrying:^?\n\r"+msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
