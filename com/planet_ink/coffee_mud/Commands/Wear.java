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
public class Wear extends BaseItemParser
{
	public Wear(){}

	private String[] access={"WEAR"};
	public String[] getAccessWords(){return access;}

	public boolean wear(MOB mob, Item item, boolean quiet)
	{
		String str="<S-NAME> put(s) on <T-NAME>.";
		int msgType=CMMsg.MSG_WEAR;
		if(item.rawProperLocationBitmap()==Item.HELD)
		{
			str="<S-NAME> hold(s) <T-NAME>.";
			msgType=CMMsg.MSG_HOLD;
		}
		else
		if((item.rawProperLocationBitmap()==Item.WIELD)
		||(item.rawProperLocationBitmap()==(Item.HELD|Item.WIELD)))
		{
			str="<S-NAME> wield(s) <T-NAME>.";
			msgType=CMMsg.MSG_WIELD;
		}
		FullMsg newMsg=new FullMsg(mob,item,null,msgType,quiet?null:str);
		if(mob.location().okMessage(mob,newMsg))
		{
			mob.location().send(mob,newMsg);
			return true;
		}
		return false;
	}


	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Wear what?");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.firstElement() instanceof Item)
			return wear(mob,(Item)commands.firstElement(),((commands.size()>1)&&(commands.lastElement() instanceof String)&&(((String)commands.lastElement()).equalsIgnoreCase("QUIETLY"))));

		Vector items=fetchItemList(mob,mob,null,commands,Item.WORN_REQ_UNWORNONLY,true);
		if(items.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<items.size();i++)
			if((items.size()==1)||(((Item)items.elementAt(i)).canWear(mob,0)))
			{
				wear(mob,(Item)items.elementAt(i),false);
			}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
