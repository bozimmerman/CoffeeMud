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
public class View extends StdCommand
{
	public View(){}

	private String[] access={"VIEW"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		MOB shopkeeper=EnglishParser.parseShopkeeper(mob,commands,"View what merchandise from whom?");
		if(shopkeeper==null) return false;
		if(commands.size()==0)
		{
			mob.tell("View what merchandise?");
			return false;
		}

		if(CoffeeUtensils.getShopKeeper(shopkeeper)==null)
		{
			mob.tell(shopkeeper.name()+" is not a shopkeeper!");
			return false;
		}

		int maxToDo=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0))
		{
			maxToDo=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}

		String whatName=Util.combine(commands,0);
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(whatName.toUpperCase().startsWith("ALL.")){ allFlag=true; whatName="ALL "+whatName.substring(4);}
		if(whatName.toUpperCase().endsWith(".ALL")){ allFlag=true; whatName="ALL "+whatName.substring(0,whatName.length()-4);}
		int addendum=1;
		do
		{
			Environmental itemToDo=CoffeeUtensils.getShopKeeper(shopkeeper).getStock(whatName,mob);
			if(itemToDo==null) break;
			if(Sense.canBeSeenBy(itemToDo,mob))
				V.addElement(itemToDo);
			if(addendum>=CoffeeUtensils.getShopKeeper(shopkeeper).numberInStock(itemToDo))
				break;
			addendum++;
		}
		while((allFlag)&&(addendum<=maxToDo));

		if(V.size()==0)
			mob.tell(shopkeeper,null,null,"<S-NAME> doesn't appear to have any '"+whatName+"' for sale.  Try LIST.");
		else
		for(int v=0;v<V.size();v++)
		{
			Environmental thisThang=(Environmental)V.elementAt(v);
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,CMMsg.MSG_VIEW,null);
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
