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
public class Trap_Greasy extends StdTrap
{
	public String ID() { return "Trap_Greasy"; }
	public String name(){ return "greasy";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 10;}
	public String requiresToSet(){return "a container of lamp oil";}
	int times=20;

	private Item getPoison(MOB mob)
	{
		if(mob==null) return null;
		if(mob.location()==null) return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)
			&&(I instanceof Drink)
			&&(((Drink)I).containsDrink())
			&&(((Drink)I).liquidType()==EnvResource.RESOURCE_LAMPOIL))
				return I;
		}
		return null;
	}

	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Item I=getPoison(mob);
		if((I!=null)&&(I instanceof Drink))
		{
			((Drink)I).setLiquidHeld(0);
			I.destroy();
		}
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Item I=getPoison(mob);
		if((I==null)
		&&(mob!=null))
		{
			mob.tell("You'll need to set down a container of lamp oil first.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) setting off a trap!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,Util.capitalize(affected.name())+" is covered in grease!"))
			{
				super.spring(target);
				target.location().show(target,affected,null,CMMsg.MSG_DROP,"<S-NAME> drop(s) the greasy <T-NAME>!");
				if(((--times)<=0)&&(canBeUninvoked())&&(affected instanceof Item))
					disable();
				else
					sprung=false;
			}
		}
	}
}
