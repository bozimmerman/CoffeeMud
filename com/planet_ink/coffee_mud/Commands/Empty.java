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
public class Empty extends BaseItemParser
{
	public Empty(){}

	private String[] access={"EMPTY","EMP"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String whatToDrop=null;
		Environmental target=mob;
		Vector V=new Vector();
		if(commands.size()<2)
		{
			mob.tell("Empty what where?");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()>1)
		{
			String s=(String)commands.lastElement();
			if(s.equalsIgnoreCase("here")) target=mob.location();
			else
			if(s.equalsIgnoreCase("me")) target=mob;
			else
			if(s.equalsIgnoreCase("self")) target=mob;
			else
			if("INVENTORY".startsWith(s.toUpperCase())) target=mob;
			else
			if(s.equalsIgnoreCase("floor")) target=mob.location();
			else
			if(s.equalsIgnoreCase("ground")) target=mob.location();
			else
			{
				target=EnglishParser.possibleContainer(mob,commands,false,Item.WORN_REQ_UNWORNONLY);
				if(target==null) 
					target=mob.location().fetchFromRoomFavorItems(null,s,Item.WORN_REQ_UNWORNONLY);
				else
					commands.addElement("delme");
			}
			if(target!=null)
				commands.removeElementAt(commands.size()-1);
		}
		
		if((target==null)||(!Sense.canBeSeenBy(target,mob)))
		{
			mob.tell("Empty it where?");
			return false;
		}

		int maxToDrop=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0)
		&&(EnglishParser.numPossibleGold(mob,Util.combine(commands,0))==0))
		{
			maxToDrop=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}

		whatToDrop=Util.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(whatToDrop.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(4);}
		if(whatToDrop.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(0,whatToDrop.length()-4);}
		int addendum=1;
		String addendumStr="";
		Drink drink=null;
		do
		{
			Item dropThis=mob.fetchCarried(null,whatToDrop+addendumStr);
			if((dropThis==null)
			&&(V.size()==0)
			&&(addendumStr.length()==0)
			&&(!allFlag))
			{
				dropThis=mob.fetchWornItem(whatToDrop);
				if((dropThis!=null)&&(dropThis instanceof Container))
				{
					if((!dropThis.amWearingAt(Item.HELD))&&(!dropThis.amWearingAt(Item.WIELD)))
					{
						mob.tell("You must remove that first.");
						return false;
					}
					FullMsg newMsg=new FullMsg(mob,dropThis,null,CMMsg.MSG_REMOVE,null);
					if(mob.location().okMessage(mob,newMsg))
						mob.location().send(mob,newMsg);
					else
						return false;
				}
			}
			if(dropThis==null) break;
			if(dropThis instanceof Drink)
				drink=(Drink)dropThis;
			if((Sense.canBeSeenBy(dropThis,mob))
			&&(dropThis instanceof Container)
			&&(!V.contains(dropThis)))
				V.addElement(dropThis);
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToDrop));

		String str="<S-NAME> empt(ys) <T-NAME>";
		if(target instanceof Room) str+=" here.";
		else
		if(target instanceof MOB) str+=".";
		else str+=" into "+target.Name()+".";
		
		if((V.size()==0)&&(drink!=null))
		{
			mob.tell(drink.name()+" must be POURed out.");
			return false;
		}
		
		if(V.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		if((V.size()==1)&&(V.firstElement()==target))
			mob.tell("You can't empty something into itself!");
		else
		for(int v=0;v<V.size();v++)
		{
			Container C=(Container)V.elementAt(v);
			if(C==target) continue;
			Vector V2=C.getContents();
            
            boolean skipMessage=false;
            if((C instanceof Drink)&&(((Drink)C).containsDrink()))
            {
                if(target instanceof Drink)
                {
                    Command C2=CMClass.getCommand("Pour");
                    C2.execute(mob,Util.makeVector("POUR",C.Name()+"$",target.Name()+"$"));
                    skipMessage=true;
                }
                else
                {
                    ((Drink)C).setLiquidRemaining(0);
                    if(((Drink)C).disappearsAfterDrinking())
                        C.destroy();
                }
            }
			FullMsg msg=new FullMsg(mob,C,CMMsg.MSG_QUIETMOVEMENT,str);
			if(skipMessage||(mob.location().okMessage(mob,msg)))
			{
                if(!skipMessage) mob.location().send(mob,msg);
				for(int v2=0;v2<V2.size();v2++)
				{
					Item I=(Item)V2.elementAt(v2);
					if(I instanceof Coins) ((Coins)I).setContainer(null);
					if(((I.container()==null)||(Get.get(mob,C,I,true,null,true)))
					&&(I.container()==null))
					{
						if(target instanceof Room)
							Drop.drop(mob,I,true,true);
						else
						if(target instanceof Container)
						{
							FullMsg putMsg=new FullMsg(mob,target,I,CMMsg.MASK_OPTIMIZE|CMMsg.MSG_PUT,null);
							if(mob.location().okMessage(mob,putMsg))
								mob.location().send(mob,putMsg);
						}
						if(I instanceof Coins)
							((Coins)I).putCoinsBack();
					}
				}
			}
		}
		mob.location().recoverRoomStats();
		mob.location().recoverRoomStats();
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
