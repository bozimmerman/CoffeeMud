package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Trap_Boomerang extends StdTrap
{
	public String ID() { return "Trap_Boomerang"; }
	public String name(){ return "boomerang";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 24;}
	public String requiresToSet(){return "";}

	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			boolean ok=((invoker()!=null)&&(invoker().location()!=null));
			if((!ok)||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> foil(s) a trap on "+affected.name()+"!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> set(s) off a trap!"))
			{
				super.spring(target);
				if((canBeUninvoked())&&(affected instanceof Item))
					disable();
				if(affected instanceof Item)
				{
					((Item)affected).unWear();
					((Item)affected).removeFromOwnerContainer();
					invoker().addInventory((Item)affected);
				}
			}
		}
	}
}
