package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
public class Trap_SpikePit extends Trap_RoomPit
{
	@Override
	public String ID()
	{
		return "Trap_SpikePit";
	}

	private final static String	localizedName	= CMLib.lang().L("spike pit");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	protected int trapLevel()
	{
		return 8;
	}

	@Override
	public String requiresToSet()
	{
		return "5 dagger-class weapons";
	}

	public List<Integer> daggerDamages=null;

	protected Item getDagger(MOB mob)
	{
		if(mob==null)
			return null;
		if(mob.location()==null)
			return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I=mob.location().getItem(i);
			if((I instanceof Weapon)
			&&(((Weapon)I).weaponClassification()==Weapon.CLASS_DAGGER))
				return I;
		}
		return null;
	}

	@Override
	public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(P==null)
			return null;
		Item I=getDagger(mob);
		int num=0;
		while((I!=null)&&((++num)<6))
		{
			if(daggerDamages==null)
				daggerDamages=new Vector<Integer>();
			daggerDamages.add(Integer.valueOf(I.basePhyStats().damage()));
			I.destroy();
			I=getDagger(mob);
		}
		return super.setTrap(mob,P,trapBonus,qualifyingClassLevel,perm);
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final List<Item> V=new Vector<Item>();
		if((daggerDamages==null)||(daggerDamages.size()==0))
			V.add(CMClass.getWeapon("Dagger"));
		else
		for(int d=0;d<daggerDamages.size();d++)
		{
			final Item I=CMClass.getWeapon("Dagger");
			I.basePhyStats().setDamage(daggerDamages.get(d).intValue());
			I.recoverPhyStats();
			V.add(I);
		}
		return V;
	}

	@Override
	public boolean canSetTrapOn(MOB mob, Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		if(mob!=null)
		{
			if(getDagger(mob)==null)
			{
				mob.tell(L("You'll need to set down some dagger-class weapons first."));
				return false;
			}
		}
		return true;
	}

	@Override
	public void finishSpringing(MOB target)
	{
		if((!invoker().mayIFight(target))||(target.phyStats().weight()<5))
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> float(s) gently into the pit!"));
		else
		{
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> hit(s) the pit floor!"));
			int damage=CMLib.dice().roll(trapLevel()+abilityCode(),6,1);
			if((daggerDamages!=null)&&(daggerDamages.size()>0))
			{
				for(int i=0;i<daggerDamages.size();i++)
					damage+=CMLib.dice().roll(1,daggerDamages.get(i).intValue(),0);
			}
			else
				damage+=CMLib.dice().roll(5,4,0);
			CMLib.combat().postDamage(invoker(),target,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_PIERCING,L("Spikes on the pit floor <DAMAGE> <T-NAME>!"));
		}
		CMLib.commands().postLook(target,true);
	}
}
