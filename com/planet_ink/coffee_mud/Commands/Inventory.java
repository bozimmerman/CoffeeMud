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
public class Inventory extends StdCommand
{
	public Inventory(){}

	private String[] access={"INVENTORY","INV","I"};
	public String[] getAccessWords(){return access;}


	public static StringBuffer getInventory(MOB seer, MOB mob, String mask)
	{
		StringBuffer msg=new StringBuffer("");
		boolean foundAndSeen=false;
		Vector viewItems=new Vector();
		Hashtable moneyItems=new Hashtable();
		Vector V=null;
		int insertAt=-1;
		if(mob.getMoney()>0) BeanCounter.getTotalAbsoluteNativeValue(mob);
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item thisItem=mob.fetchInventory(i);
			if((thisItem!=null)
			&&(thisItem.container()==null)
			&&(thisItem.amWearingAt(Item.INVENTORY)))
			{
				if(Sense.canBeSeenBy(thisItem,seer))
					foundAndSeen=true;
				if((!(thisItem instanceof Coins))||(((Coins)thisItem).getDenomination()==0.0))
					viewItems.addElement(thisItem);
				else
				{
				    V=(Vector)moneyItems.get(((Coins)thisItem).getCurrency());
				    if(V==null)
				    {
				        V=new Vector();
				        moneyItems.put(((Coins)thisItem).getCurrency(),V);
				    }
                    for(insertAt=0;insertAt<V.size();insertAt++)
                        if(((Coins)V.elementAt(insertAt)).getDenomination()>((Coins)thisItem).getDenomination())
                            break;
                    if(insertAt>=V.size())
		                V.addElement(thisItem);
                    else
                        V.insertElementAt(thisItem,insertAt);
				}
			}
		}
		if((viewItems.size()>0)&&(!foundAndSeen))
			viewItems.clear();
		else
		if((mask!=null)&&(mask.trim().length()>0))
		{
			mask=mask.trim().toUpperCase();
			if(!mask.startsWith("all")) mask="all "+mask;
			V=(Vector)viewItems.clone();
			viewItems.clear();
			Item I=(V.size()>0)?(Item)V.firstElement():null;
			while(I!=null)
			{
				I=(Item)EnglishParser.fetchEnvironmental(V,mask,false);
				if(I!=null)
				{
					viewItems.addElement(I);
					V.remove(I);
				}
			}
		}
		if((viewItems.size()==0)&&(moneyItems.size()==0))
		{
			if((mask!=null)&&(mask.trim().length()>0))
				msg.append("(nothing like that you can see right now)");
			else
				msg.append("(nothing you can see right now)");
		}
		else
		{
			if(viewItems.size()>0)
				msg.append(CMLister.niceLister(seer,viewItems,true,"MItem",""));
			if(moneyItems.size()>0)
			{
			    msg.append("\n\r^HMoney:^N\n\r");
			    Item I=null;
				for(Enumeration e=moneyItems.keys();e.hasMoreElements();)
				{
				    String key=(String)e.nextElement();
				    V=(Vector)moneyItems.get(key);
				    for(int v=0;v<V.size();v++)
				    {
				        I=(Item)V.elementAt(v);
				        if(v>0) msg.append(", ");
				        msg.append(I.name());
				    }
				    if(e.hasMoreElements()) msg.append("\n\r");
				}
			}
		}
		return msg;
	}


	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((commands.size()==1)&&(commands.firstElement() instanceof MOB))
		{
			commands.addElement(getInventory((MOB)commands.firstElement(),mob,null));
			return true;
		}
		StringBuffer msg=getInventory(mob,mob,Util.combine(commands,1));
		if(msg.length()==0)
			mob.tell("^HYou are carrying:\n\r^!Nothing!^?\n\r");
		else
		if(!mob.isMonster())
			mob.session().wraplessPrintln("^HYou are carrying:^?\n\r"+msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
