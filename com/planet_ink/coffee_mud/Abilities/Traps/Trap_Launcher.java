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
public class Trap_Launcher extends StdTrap
{
	public String ID() { return "Trap_Launcher"; }
	public String name(){ return "launcher trap";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 6;}
	public String requiresToSet(){return "a ranged weapon";}

	protected Item getPoison(MOB mob)
	{
		if(mob==null) return null;
		if(mob.location()==null) return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)
			&&(I instanceof Weapon)
			&&(((Weapon)I).weaponClassification()==Weapon.CLASS_RANGED))
				return I;
		}
		return null;
	}

    public Vector getTrapComponents() {
        Vector V=new Vector();
        Item I=CMClass.getWeapon("StdBow");
        V.addElement(I);
        return V;
    }
	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(E==null) return null;
		Item I=getPoison(mob);
		setMiscText("3/a projectile");
		if(I!=null){
			setMiscText(""+I.baseEnvStats().damage()+"/"+I.name());
			I.destroy();
		}
		return super.setTrap(mob,E,trapBonus,qualifyingClassLevel,perm);
	}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Item I=getPoison(mob);
		if(I==null)
		{
			if(mob!=null)
				mob.tell("You'll need to set down a ranged weapon first.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			int x=text().indexOf("/");
			int dam=3;
			String name="a projectile";
			if(x>=0)
			{
				dam=CMath.s_int(text().substring(0,x));
				name=text().substring(x+1);
			}
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet()).contains(target))
			||(target==invoker())
			||(CMLib.dice().rollPercentage()<=target.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) setting off "+name+" trap!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> <S-IS-ARE> struck by "+name+" trap!"))
			{
				super.spring(target);
				int damage=CMLib.dice().roll(trapLevel()+abilityCode(),dam,1);
				CMLib.combat().postDamage(invoker(),target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,-1,null);
				if((canBeUninvoked())&&(affected instanceof Item))
					disable();
			}
		}
	}
}
