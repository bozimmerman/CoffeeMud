package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_MetalMold extends Chant
{
	public String ID() { return "Chant_MetalMold"; }
	public String name(){return "Metal Mold";}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB mobTarget=getTarget(mob,commands,givenTarget,true,false);
		Item target=null;
		if(mobTarget!=null)
		{
			Vector goodPossibilities=new Vector();
			Vector possibilities=new Vector();
			for(int i=0;i<mobTarget.inventorySize();i++)
			{
				Item item=mobTarget.fetchInventory(i);
				if((item!=null)
				&&(Sense.isMetal(item))
				&&(item.subjectToWearAndTear()))
				{
					if(item.amWearingAt(Item.INVENTORY))
						possibilities.addElement(item);
					else
						goodPossibilities.addElement(item);
				}
				if(goodPossibilities.size()>0)
					target=(Item)goodPossibilities.elementAt(Dice.roll(1,goodPossibilities.size(),-1));
				else
				if(possibilities.size()>0)
					target=(Item)possibilities.elementAt(Dice.roll(1,possibilities.size(),-1));
			}
		}

		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);

		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> grow(s) moldy!":"^S<S-NAME> chant(s), causing <T-NAMESELF> to get eaten by mold.^?");
			FullMsg msg2=new FullMsg(mob,mobTarget,this,affectType(auto),null);
			if((mob.location().okMessage(mob,msg))&&((mobTarget==null)||(mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(mobTarget!=null)
					mob.location().send(mob,msg2);
				if(msg.value()<=0)
				{
					int damage=2;
					for(int i=0;i<(mob.envStats().level()/2);i++)
						damage+=Dice.roll(1,2,2);
					if(Sense.isABonusItems(target))
						damage=(int)Math.round(Util.div(damage,2.0));
					if(target.envStats().ability()>0)
						damage=(int)Math.round(Util.div(damage,1+target.envStats().ability()));
					target.setUsesRemaining(target.usesRemaining()-damage);
					if(target.usesRemaining()>0)
						target.recoverEnvStats();
					else
					{
						target.setUsesRemaining(100);
						if(mobTarget==null)
							mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> is destroyed by mold!");
						else
							mob.location().show(mobTarget,target,CMMsg.MSG_OK_VISUAL,"<T-NAME>, possessed by <S-NAME>, is destroyed by mold!");
						target.unWear();
						target.destroy();
						mob.location().recoverRoomStats();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s) for mold, but nothing happens.");


		// return whether it worked
		return success;
	}
}
