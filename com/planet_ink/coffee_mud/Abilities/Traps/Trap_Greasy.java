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
   Copyright 2003-2025 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "Trap_Greasy";
	}

	private final static String	localizedName	= CMLib.lang().L("greasy");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	public Trap_Greasy()
	{
		super();
		trapLevel = 10;
	}

	@Override
	public String requiresToSet()
	{
		return "a container of lamp oil";
	}

	int times=20;

	protected Item getPoison(final MOB mob)
	{
		if(mob==null)
			return null;
		if(mob.location()==null)
			return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I=mob.location().getItem(i);
			if((I!=null)
			&&(I instanceof Drink)
			&&(((Drink)I).containsLiquid())
			&&(((Drink)I).liquidType()==RawMaterial.RESOURCE_LAMPOIL))
				return I;
		}
		return null;
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final List<Item> V=new Vector<Item>();
		V.add(CMClass.getBasicItem("OilFlask"));
		return V;
	}

	@Override
	public Trap setTrap(final MOB mob, final Physical P, final int trapBonus, final int qualifyingClassLevel, final boolean perm)
	{
		if(P==null)
			return null;
		final Item I=getPoison(mob);
		if((I!=null)&&(I instanceof Drink))
		{
			((Drink)I).setLiquidHeld(0);
			I.destroy();
		}
		return super.setTrap(mob,P,trapBonus,qualifyingClassLevel,perm);
	}

	@Override
	public boolean canSetTrapOn(final MOB mob, final Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		final Item I=getPoison(mob);
		if((I==null)
		&&(mob!=null))
		{
			mob.tell(L("You'll need to set down a container of lamp oil first."));
			return false;
		}
		return true;
	}

	@Override
	public void spring(final MOB target)
	{
		if((target!=invoker())
		&&(target.location()!=null))
		{
			if((doesSaveVsTraps(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target)))
			{
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						getAvoidMsg(L("<S-NAME> avoid(s) setting off a trap!")));
			}
			else
			{
				if(target.location().show(target,target,this,
						CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,getTrigMsg(L("@x1 is covered in grease!",CMStrings.capitalizeAndLower(affected.name())))))
				{
					super.spring(target);
					target.location().show(target,affected,null,CMMsg.MSG_DROP,
							getDamMsg(L("<S-NAME> drop(s) the greasy <T-NAME>!")));
					if(((--times)<=0)&&(canBeUninvoked())&&(affected instanceof Item))
						disable();
					else
						sprung=false;
				}
			}
		}
	}
}
