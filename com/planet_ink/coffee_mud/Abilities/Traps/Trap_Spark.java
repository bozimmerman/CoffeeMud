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
public class Trap_Spark extends StdTrap
{
	public String ID() { return "Trap_Spark"; }
	public String name(){ return "sparking trap";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 19;}
	public String requiresToSet(){return "10 pounds of metal";}

	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_METAL);
			if(I!=null)
				super.destroyResources(mob.location(),I.material(),10);
		}
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_METAL);
			if((I==null)
			||(super.findNumberOfResource(mob.location(),I.material())<10))
			{
				mob.tell("You'll need to set down at least 10 pounds of metal first.");
				return false;
			}
		}
		return true;
	}

	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((!invoker().mayIFight(target))||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) setting off a sparking trap!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> set(s) off an sparking trap!"))
			{
				super.spring(target);
				MUDFight.postDamage(invoker(),target,null,Dice.roll(trapLevel(),8,1),CMMsg.MASK_GENERAL|CMMsg.TYP_ELECTRIC,Weapon.TYPE_STRIKING,"The sparks <DAMAGE> <T-NAME>!"+CommonStrings.msp("shock.wav",30));
				if((canBeUninvoked())&&(affected instanceof Item))
					disable();
			}
		}
	}
}
