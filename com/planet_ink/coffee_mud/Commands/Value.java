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
public class Value extends StdCommand
{
	public Value(){}

	private String[] access={"VALUE","VAL","V"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		MOB shopkeeper=EnglishParser.parseShopkeeper(mob,commands,"Value what with whom?");
		if(shopkeeper==null) return false;
		if(commands.size()==0)
		{
			mob.tell("Value what?");
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
		String addendumStr="";
		do
		{
			Item itemToDo=mob.fetchCarried(null,whatName+addendumStr);
			if(itemToDo==null) break;
			if((Sense.canBeSeenBy(itemToDo,mob))
			&&(!V.contains(itemToDo)))
				V.addElement(itemToDo);
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToDo));

		if(V.size()==0)
			mob.tell("You don't seem to have '"+whatName+"'.");
		else
		for(int v=0;v<V.size();v++)
		{
			Item thisThang=(Item)V.elementAt(v);
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,CMMsg.MSG_VALUE,null);
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
