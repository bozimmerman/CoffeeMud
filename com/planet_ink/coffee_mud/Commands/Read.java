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
public class Read extends StdCommand
{
	public Read(){}

	private String[] access={"READ"};
	public String[] getAccessWords(){return access;}

	public void read(MOB mob, Environmental thisThang, String theRest)
	{
		if((thisThang==null)||((!(thisThang instanceof Item)&&(!(thisThang instanceof Exit))))||((thisThang!=null)&&(!Sense.canBeSeenBy(thisThang,mob))))
		{
			mob.tell("You don't seem to have that.");
			return;
		}
		if(thisThang instanceof Item)
		{
			Item thisItem=(Item)thisThang;
			if((Sense.isGettable(thisItem))&&(!mob.isMine(thisItem)))
			{
				mob.tell("You don't seem to be carrying that.");
				return;
			}
		}
		String soMsg="<S-NAME> read(s) <T-NAMESELF>.";
		String tMsg=theRest;
		if((tMsg.trim().length()==0)||(thisThang instanceof MOB)) tMsg=soMsg;
		FullMsg newMsg=new FullMsg(mob,thisThang,null,CMMsg.MSG_READSOMETHING,soMsg,CMMsg.MSG_READSOMETHING,tMsg,CMMsg.MSG_READSOMETHING,soMsg);
		if(mob.location().okMessage(mob,newMsg))
			mob.location().send(mob,newMsg);

	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Read what?");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.firstElement() instanceof Environmental)
		{
			read(mob,(Environmental)commands.firstElement(),Util.combine(commands,1));
			return false;
		}

		int dir=Directions.getGoodDirectionCode(Util.combine(commands,0));
		Environmental thisThang=null;
		if(dir>=0)	thisThang=mob.location().getExitInDir(dir);
		thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,(String)commands.elementAt(commands.size()-1),Item.WORN_REQ_ANY);
		String theRest=null;
		if(thisThang==null)
			thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,Util.combine(commands,0),Item.WORN_REQ_ANY);
		else
		{
			commands.removeElementAt(commands.size()-1);
			theRest=Util.combine(commands,0);
		}
		read(mob,thisThang, theRest);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
