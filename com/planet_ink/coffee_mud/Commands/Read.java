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
public class Read extends StdCommand
{
	public Read(){}

	private String[] access={"READ"};
	public String[] getAccessWords(){return access;}

	public void read(MOB mob, Environmental thisThang, String theRest)
	{
		if((thisThang==null)||((!(thisThang instanceof Item)&&(!(thisThang instanceof Exit))))||(!CMLib.flags().canBeSeenBy(thisThang,mob)))
		{
			mob.tell("You don't seem to have that.");
			return;
		}
		if(thisThang instanceof Item)
		{
			Item thisItem=(Item)thisThang;
			if((CMLib.flags().isGettable(thisItem))&&(!mob.isMine(thisItem)))
			{
				mob.tell("You don't seem to be carrying that.");
				return;
			}
		}
		String srcMsg="<S-NAME> read(s) <T-NAMESELF>.";
		String soMsg=(mob.isMine(thisThang)?srcMsg:null);
		String tMsg=theRest;
		if((tMsg==null)||(tMsg.trim().length()==0)||(thisThang instanceof MOB)) tMsg=soMsg;
		CMMsg newMsg=CMClass.getMsg(mob,thisThang,null,CMMsg.MSG_READ,srcMsg,CMMsg.MSG_READ,tMsg,CMMsg.MSG_READ,soMsg);
		if(mob.location().okMessage(mob,newMsg))
			mob.location().send(mob,newMsg);

	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
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
			read(mob,(Environmental)commands.firstElement(),CMParms.combine(commands,1));
			return false;
		}

		int dir=Directions.getGoodDirectionCode(CMParms.combine(commands,0));
		Environmental thisThang=null;
		if(dir>=0)	thisThang=mob.location().getExitInDir(dir);
		thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,(String)commands.lastElement(),Wearable.FILTER_ANY);
		String theRest=null;
		if(thisThang==null)
			thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,CMParms.combine(commands,0),Wearable.FILTER_ANY);
		else
		{
			commands.removeElementAt(commands.size()-1);
			theRest=CMParms.combine(commands,0);
		}
		read(mob,thisThang, theRest);
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
