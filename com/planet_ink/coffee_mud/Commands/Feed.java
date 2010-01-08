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
public class Feed extends StdCommand
{
	public Feed(){}

	private String[] access={"FEED"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell("Feed who what?");
			return false;
		}
		commands.removeElementAt(0);
		String what=(String)commands.lastElement();
		commands.removeElement(what);
		String whom=CMParms.combine(commands,0);
		MOB target=mob.location().fetchInhabitant(whom);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("I don't see "+whom+" here.");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are in combat!");
			return false;
		}
		if(target.willFollowOrdersOf(mob)||(CMLib.flags().isBoundOrHeld(target)))
		{
			Item item=mob.fetchInventory(null,what);
			if((item==null)||(!CMLib.flags().canBeSeenBy(item,mob)))
			{
				mob.tell("I don't see "+what+" here.");
				return false;
			}
			if(!item.amWearingAt(Wearable.IN_INVENTORY))
			{
				mob.tell("You might want to remove that first.");
				return false;
			}
			if((!(item instanceof Food))&&(!(item instanceof Drink)))
			{
				mob.tell("You might want to try feeding them something edibile or drinkable.");
				return false;
			}
			if(target.isInCombat())
			{
				mob.tell("Not while "+target.name()+" is in combat!");
				return false;
			}
			CMMsg msg=CMClass.getMsg(mob,target,item,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> feed(s) "+item.name()+" to <T-NAMESELF>.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((CMLib.commands().postDrop(mob,item,true,false))
				   &&(mob.location().isContent(item)))
				{
					msg=CMClass.getMsg(target,item,CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,null);
					target.location().send(target,msg);
					if(target.isMine(item))
					{
						if(item instanceof Food)
							msg=CMClass.getMsg(target,item,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_EAT,CMMsg.MSG_EAT,CMMsg.MSG_EAT,null);
						else
							msg=CMClass.getMsg(target,item,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_DRINK,CMMsg.MSG_DRINK,CMMsg.MSG_DRINK,null);
						if(target.location().okMessage(target,msg))
							target.location().send(target,msg);
						if(target.isMine(item))
						{
							msg=CMClass.getMsg(target,item,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_DROP,CMMsg.MSG_DROP,CMMsg.MSG_DROP,null);
							if(mob.location().okMessage(mob,msg))
							{
								mob.location().send(mob,msg);
								CMLib.commands().postGet(mob,null,item,true);
							}
						}
					}
				}
			}
		}
		else
			mob.tell(target.name()+" won't let you.");
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
