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
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item thisItem=mob.fetchInventory(i);
			if((thisItem!=null)
			&&(thisItem.container()==null)
			&&(thisItem.amWearingAt(Item.INVENTORY)))
			{
				viewItems.addElement(thisItem);
				if(Sense.canBeSeenBy(thisItem,seer))
					foundAndSeen=true;
			}
		}
		if((viewItems.size()>0)&&(!foundAndSeen))
			viewItems.clear();
		else
		if((mask!=null)&&(mask.trim().length()>0))
		{
			mask=mask.trim().toUpperCase();
			if(!mask.startsWith("all")) mask="all "+mask;
			Vector V=(Vector)viewItems.clone();
			viewItems.clear();
			Item I=(Item)V.firstElement();
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
		if(viewItems.size()>0)
			msg.append(CMLister.niceLister(seer,viewItems,true));
		
		if((viewItems.size()==0)&&((mob.getMoney()==0)||(!Sense.canBeSeenBy(mob.location(),seer))))
		{
			if((mask!=null)&&(mask.trim().length()>0))
				msg.append("(nothing like that you can see right now)");
			else
				msg.append("(nothing you can see right now)");
		}
		else
		if((mob.getMoney()>0)&&(!Sense.canBeSeenBy(mob.location(),seer)))
			msg.append("(some ^ygold^? coins you can't see)");
		else
		if(mob.getMoney()>0)
			msg.append(mob.getMoney()+" ^ygold^? coins.\n\r");
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
			mob.session().unfilteredPrintln("^HYou are carrying:^?\n\r"+msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
