package com.planet_ink.coffee_mud.Abilities.Traps;
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
public class Trap_Boulders extends StdTrap
{
	public String ID() { return "Trap_Boulders"; }
	public String name(){ return "boulders";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 20;}
	public String requiresToSet(){return "50 pounds of boulders";}

	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(E==null) return null;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_ROCK);
			if(I!=null)
				super.destroyResources(mob.location(),I.material(),50);
		}
		return super.setTrap(mob,E,trapBonus,qualifyingClassLevel,perm);
	}

    public Vector getTrapComponents() {
        Vector V=new Vector();
        for(int i=0;i<50;i++)
            V.addElement(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_STONE));
        return V;
    }
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_ROCK);
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
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet()).contains(target))
			||(target==invoker())
			||(CMLib.dice().rollPercentage()<=target.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) setting off a boulder trap!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> trigger(s) a trap!"))
			{
				super.spring(target);
				int damage=CMLib.dice().roll(trapLevel()+abilityCode(),20,1);
				CMLib.combat().postDamage(invoker(),target,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,"Dozens of boulders <DAMAGE> <T-NAME>!");
			}
		}
	}
}
