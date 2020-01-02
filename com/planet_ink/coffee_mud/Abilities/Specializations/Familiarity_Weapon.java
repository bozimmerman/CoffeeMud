package com.planet_ink.coffee_mud.Abilities.Specializations;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class Familiarity_Weapon extends StdAbility
{
	@Override
	public String ID()
	{
		return "Familiarity_Weapon";
	}

	private final static String	localizedName	= CMLib.lang().L("Weapon Familiarity");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	protected int		weaponClass			= -1;
	protected int		secondWeaponClass	= -1;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_WEAPON_USE;
	}

	protected boolean isWeaponMatch(final MOB mob, final Weapon W)
	{
		if((W.weaponClassification()==weaponClass)
		||(weaponClass<0)
		||(W.weaponClassification()==secondWeaponClass))
			return (mob != null) && getBrand(W).equalsIgnoreCase(mob.Name());
		return false;
	}

	protected boolean canDamage(final MOB mob, final Weapon W)
	{
		return W.amBeingWornProperly();
	}

	protected String getBrand(final Item buildingI)
	{
		if(buildingI != null)
		{
			final int x=buildingI.secretIdentity().indexOf(ItemCraftor.CRAFTING_BRAND_STR_PREFIX);
			if(x>=0)
			{
				final int y=buildingI.secretIdentity().indexOf('.',x+ItemCraftor.CRAFTING_BRAND_STR_PREFIX.length());
				if(y>=0)
				{
					return buildingI.secretIdentity().substring(x,y);
				}
			}
		}
		return "";
	}

	protected boolean isWearableItem(final MOB mob, final Item I)
	{
		return (I instanceof Weapon) && isWeaponMatch(mob,(Weapon)I);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			final Item myWeapon=((MOB)affected).fetchWieldedItem();
			if((myWeapon instanceof Weapon)
			&&(isWeaponMatch((MOB)affected,(Weapon)myWeapon)))
			{
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()
						+(int)Math.round((adjustedLevel((MOB)affected,0))*(CMath.div(proficiency(),100.0))));
			}
		}
	}
}
