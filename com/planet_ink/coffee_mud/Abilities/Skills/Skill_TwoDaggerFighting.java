package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2014-2020 Bo Zimmerman

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
public class Skill_TwoDaggerFighting extends Skill_TwoWeaponFighting
{
	@Override
	public String ID()
	{
		return "Skill_TwoDaggerFighting";
	}

	private final static String localizedName = CMLib.lang().L("Two Dagger Fighting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private volatile boolean active = false;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(mob.isInCombat())
			{
				final Weapon W1=getFirstWeapon(mob);
				final Weapon W2=getSecondWeapon(mob);
				if((W1!=null)&&(W2!=null)
				&&(W1.weaponClassification()==Weapon.CLASS_DAGGER)
				&&(W2.weaponClassification()==Weapon.CLASS_DAGGER))
				{
					active=true;
					final int xlvl=super.getXLEVELLevel(invoker());
					final boolean adjustOnly = mob.fetchEffect("Skill_TwoWeaponFighting")!=null;
					if(!adjustOnly)
						affectableStats.setSpeed(affectableStats.speed()+(0.1*xlvl));
					else
					{
						affectableStats.setSpeed(affectableStats.speed()+(0.1*xlvl));
						affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(affectableStats.attackAdjustment()/(5+xlvl)));
						affectableStats.setDamage(affectableStats.damage()+(affectableStats.damage()/(20+xlvl)));
					}
				}
				else
					active=false;
			}
			else
				active=false;
		}
	}

	@Override
	public int abilityCode()
	{
		return active?1:0;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)&&(affected instanceof MOB))
		{
			if(((MOB)affected).fetchEffect("Skill_TwoWeaponFighting")!=null)
				return true;
		}
		return super.tick(ticking,tickID);
	}
}
