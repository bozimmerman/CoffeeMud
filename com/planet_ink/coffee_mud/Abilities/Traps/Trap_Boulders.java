package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Trap_Boulders extends StdTrap
{
	public String ID() { return "Trap_Boulders"; }
	public String name(){ return "boulders";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 20;}
	public String requiresToSet(){return "50 pounds of boulders";}

	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_ROCK);
			if(I!=null)
				super.destroyResources(mob.location(),I.material(),50);
		}
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_ROCK);
			if((I==null)
			||(super.findNumberOfResource(mob.location(),I.material())<50))
			{
				mob.tell("You'll need to set down at least 50 pounds of rock first.");
				return false;
			}
			if(E instanceof Room)
			{
				Room R=(Room)E;
				if((R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
				   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
				   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_ROCKS)
				   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_HILLS))
				{
					mob.tell("You can only set this trap in caves, or by mountains or hills.");
					return false;
				}
			}
		}
		return true;
	}

	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((!invoker().mayIFight(target))||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) setting off a boulder trap!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> trigger(s) a trap!"))
			{
				super.spring(target);
				int damage=Dice.roll(trapLevel(),20,1);
				MUDFight.postDamage(invoker(),target,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MSG_OK_ACTION,Weapon.TYPE_BASHING,"Dozens of boulders <DAMAGE> <T-NAME>!");
			}
		}
	}
}
