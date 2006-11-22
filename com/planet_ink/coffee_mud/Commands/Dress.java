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
   Copyright 2000-2006 Bo Zimmerman

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
public class Dress extends StdCommand
{
	public Dress(){}

	private String[] access={getScr("Dress","cmd1")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("Dress","whom"));
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell(getScr("Dress","nocombat"));
			return false;
		}
		commands.removeElementAt(0);
		String what=(String)commands.lastElement();
		commands.removeElement(what);
		String whom=CMParms.combine(commands,0);
		MOB target=mob.location().fetchInhabitant(whom);
		if((target==null)||((target!=null)&&(!CMLib.flags().canBeSeenBy(target,mob))))
		{
			mob.tell(getScr("Dress","nothere",whom));
			return false;
		}
		if((!target.isMonster())&&(!CMSecurity.isAllowedEverywhere(mob,"ORDER")))
		{
			mob.tell(target.Name()+getScr("Dress","isplayer"));
			return false;
		}
		if((target.willFollowOrdersOf(mob))||(CMLib.flags().isBoundOrHeld(target)))
		{
			Item item=mob.fetchInventory(null,what);
			if((item==null)||(!CMLib.flags().canBeSeenBy(item,mob)))
			{
				mob.tell(getScr("Dress","nothere",what));
				return false;
			}
			if(CMSecurity.isAllowed(mob,mob.location(),"ORDER")
			||(CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS")&&(target.isMonster()))
			||(CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")&&(target.isMonster())))
			{
				mob.location().show(mob,target,item,CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,getScr("Dress","arcputson"));
				item.unWear();
				target.giveItem(item);
				item.wearIfPossible(target);
				if((item.rawProperLocationBitmap()!=0)&&(item.amWearingAt(Item.IN_INVENTORY))&&(target.isMonster()))
				{
					if(item.rawLogicalAnd())
						item.wearAt(item.rawProperLocationBitmap());
					else
					{
						for(int i=0;i<Item.WORN_CODES.length;i++)
						{
							long wornCode=1<<i;
							if(item.fitsOn(wornCode)&&(wornCode!=Item.WORN_HELD))
							{ item.wearAt(wornCode); break;}
						}
						if(item.amWearingAt(Item.IN_INVENTORY))
							item.wearAt(Item.WORN_HELD);
					}
				}
				target.location().recoverRoomStats();
			}
			else
			{
				if(!item.amWearingAt(Item.IN_INVENTORY))
				{
					mob.tell(getScr("Dress","removefirst"));
					return false;
				}
				if(item instanceof Coins)
				{
				    mob.tell(getScr("Dress","nodress")+item.name()+".");
				    return false;
				}
				if(target.isInCombat())
				{
					mob.tell(getScr("Dress","incombat",target.name()));
					return false;
				}
				CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_QUIETMOVEMENT,null);
				if(mob.location().okMessage(mob,msg))
				{
					if(CMLib.commands().postDrop(mob,item,true,false))
					{
						msg=CMClass.getMsg(target,item,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,CMMsg.MSG_GET,CMMsg.MSG_GET,null);
						if(mob.location().okMessage(mob,msg))
						{
							mob.location().send(mob,msg);
							msg=CMClass.getMsg(target,item,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_WEAR,CMMsg.MSG_WEAR,CMMsg.MSG_WEAR,null);
							if(mob.location().okMessage(mob,msg))
							{
								mob.location().send(mob,msg);
								mob.location().show(mob,target,item,CMMsg.MSG_QUIETMOVEMENT,getScr("Dress","putson"));
							}
							else
								mob.tell(getScr("Dress","noput",item.name())+target.name()+".");
						}
						else
							mob.tell(getScr("Dress","nodo",item.name())+target.name()+".");
					}
				}
			}
		}
		else
			mob.tell(target.name()+getScr("Dress","nolet"));
		return false;
	}
    public double combatActionsCost(){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
