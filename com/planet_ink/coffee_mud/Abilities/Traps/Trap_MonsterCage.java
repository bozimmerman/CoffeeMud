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
public class Trap_MonsterCage extends StdTrap
{
	@Override
	public String ID()
	{
		return "Trap_MonsterCage";
	}

	private final static String	localizedName	= CMLib.lang().L("monster cage");

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
		return 10;
	}

	@Override
	public String requiresToSet()
	{
		return "a caged monster";
	}

	protected MOB monster=null;

	protected Item getCagedAnimal(MOB mob)
	{
		if(mob==null)
			return null;
		if(mob.location()==null)
			return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I=mob.location().getItem(i);
			if(I instanceof CagedAnimal)
			{
				final MOB M=((CagedAnimal)I).unCageMe();
				if(M!=null)
					return I;
			}
		}
		return null;
	}

	@Override
	public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(P==null)
			return null;
		final Item I=getCagedAnimal(mob);
		if(I!=null)
		{
			setMiscText(((CagedAnimal)I).cageText());
			I.destroy();
		}
		return super.setTrap(mob,P,trapBonus,qualifyingClassLevel,perm);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_TRAP_RESET)&&(getReset()>0))
		{
			// recage the motherfather
			if((tickDown<=1)
			&&(monster!=null)
			&&(monster.amDead()||(!monster.isInCombat())))
				monster.destroy();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if((monster!=null)&&(canBeUninvoked()))
			monster.destroy();
		super.unInvoke();
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final List<Item> V=new Vector<Item>();
		final Item I=CMClass.getItem("GenCaged");
		((CagedAnimal)I).setCageText(text());
		I.recoverPhyStats();
		I.text();
		V.add(I);
		return V;
	}

	@Override
	public boolean canSetTrapOn(MOB mob, Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		if(getCagedAnimal(mob)==null)
		{
			if(mob!=null)
				mob.tell(L("You'll need to set down a caged animal of some sort first."));
			return false;
		}
		return true;
	}

	@Override
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null)&&(text().length()>0))
		{
			if((doesSaveVsTraps(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,L("<S-NAME> avoid(s) opening a monster cage!"));
			else
			if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,L("<S-NAME> trip(s) open a caged monster!")))
			{
				super.spring(target);
				final Item I=CMClass.getItem("GenCaged");
				((CagedAnimal)I).setCageText(text());
				monster=((CagedAnimal)I).unCageMe();
				if(monster!=null)
				{
					monster.basePhyStats().setRejuv(PhyStats.NO_REJUV);
					monster.bringToLife(target.location(),true);
					monster.setVictim(target);
					if(target.getVictim()==null)
						target.setVictim(monster);
				}
			}
		}
	}
}
