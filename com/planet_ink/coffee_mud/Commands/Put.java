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
public class Put extends BaseItemParser
{
	public Put(){}

	private String[] access={"PUT","PU","P"};
	public String[] getAccessWords(){return access;}

	public static void putout(MOB mob, Vector commands, boolean quiet)
	{
		if(commands.size()<3)
		{
			mob.tell("Put out what?");
			return;
		}
		commands.removeElementAt(1);
		commands.removeElementAt(0);

		Vector items=EnglishParser.fetchItemList(mob,mob,null,commands,Item.WORN_REQ_UNWORNONLY,true);
		if(items.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<items.size();i++)
		{
			Item I=(Item)items.elementAt(i);
			if((items.size()==1)||(I instanceof Light))
			{
				FullMsg msg=new FullMsg(mob,I,null,CMMsg.MSG_EXTINGUISH,quiet?null:"<S-NAME> put(s) out <T-NAME>.");
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
		}
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Put what where?");
			return false;
		}

		if(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("on"))
		{
			commands.removeElementAt(commands.size()-1);
			Command C=CMClass.getCommand("Wear");
			if(C!=null) C.execute(mob,commands);
			return false;
		}

		if(((String)commands.elementAt(1)).equalsIgnoreCase("on"))
		{
			commands.removeElementAt(1);
			Command C=CMClass.getCommand("Wear");
			if(C!=null) C.execute(mob,commands);
			return false;
		}

		if(((String)commands.elementAt(1)).equalsIgnoreCase("out"))
		{
			putout(mob,commands,false);
			return false;
		}

		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("Where should I put the "+(String)commands.elementAt(0));
			return false;
		}

		Environmental container=EnglishParser.possibleContainer(mob,commands,false,Item.WORN_REQ_ANY);
		if((container==null)||((container!=null)&&(!Sense.canBeSeenBy(container,mob))))
		{
			mob.tell("I don't see a "+(String)commands.elementAt(commands.size()-1)+" here.");
			return false;
		}

		int maxToPut=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0)
		&&(EnglishParser.numPossibleGold(mob,Util.combine(commands,0))==0))
		{
			maxToPut=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}

		String thingToPut=Util.combine(commands,0);
		int addendum=1;
		String addendumStr="";
		Vector V=new Vector();
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(thingToPut.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToPut="ALL "+thingToPut.substring(4);}
		if(thingToPut.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToPut="ALL "+thingToPut.substring(0,thingToPut.length()-4);}
		do
		{
			Environmental putThis=EnglishParser.bestPossibleGold(mob,null,thingToPut);
			if(putThis!=null)
			{
			    if(((Coins)putThis).getNumberOfCoins()<EnglishParser.numPossibleGold(mob,thingToPut))
			        return false;
				allFlag=false;
			}
			else
				putThis=mob.fetchCarried(null,thingToPut+addendumStr);
			if(putThis==null) break;
			if((Sense.canBeSeenBy(putThis,mob))
			&&(!V.contains(putThis)))
				V.addElement(putThis);
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToPut));

		if((container!=null)&&(V.contains(container)))
			V.remove(container);

		if(V.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<V.size();i++)
		{
			Environmental putThis=(Environmental)V.elementAt(i);
			String putWord=(putThis instanceof Rideable)?((Rideable)putThis).putString(mob):"in";
			FullMsg putMsg=new FullMsg(mob,container,putThis,CMMsg.MASK_OPTIMIZE|CMMsg.MSG_PUT,"<S-NAME> put(s) <O-NAME> "+putWord+" <T-NAME>.");
			if(mob.location().okMessage(mob,putMsg))
				mob.location().send(mob,putMsg);
			if(putThis instanceof Coins)
				((Coins)putThis).putCoinsBack();
		}
		mob.location().recoverRoomStats();
		mob.location().recoverRoomStats();
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
