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
public class Buy extends StdCommand
{
	public Buy(){}

	private String[] access={getScr("Buy","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		MOB shopkeeper=EnglishParser.parseShopkeeper(mob,commands,getScr("Buy","buywhatwhom"));
		if(shopkeeper==null) return false;
		if(commands.size()==0)
		{
			mob.tell(getScr("Buy","buywhat"));
			return false;
		}
		if(CoffeeUtensils.getShopKeeper(shopkeeper)==null)
		{
			mob.tell(getScr("Buy","notaspk",shopkeeper.name()));
			return false;
		}

		int maxToDo=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0))
		{
			maxToDo=Util.s_int((String)commands.firstElement());
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
			mob.tell(shopkeeper,null,null,getScr("Buy","donthaveany",whatName));
		else
		for(int v=0;v<V.size();v++)
		{
			Environmental thisThang=(Environmental)V.elementAt(v);
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,CMMsg.MSG_BUY,getScr("Buy","buysfrom",forName));
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
