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
public class Bomb_Water extends StdBomb
{
	public String ID() { return "Bomb_Water"; }
	public String name(){ return "water bomb";}
	protected int trapLevel(){return 1;}
	public String requiresToSet(){return "a water container";}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if((!(E instanceof Drink))
		||(((Drink)E).liquidHeld()!=((Drink)E).liquidRemaining())
		||(((Drink)E).liquidType()!=EnvResource.RESOURCE_FRESHWATER))
		{
			if(mob!=null)
				mob.tell("You need a full water container to make this out of.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if(target.location()!=null)
		{
			if((target==invoker())||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) the water bomb!");
			else
			if(target.location().show(invoker(),target,this,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,affected.name()+" explodes water all over <T-NAME>!"))
			{
				super.spring(target);
				CoffeeUtensils.extinguish(invoker(),target,true);
			}
		}
	}

}
