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
   Copyright 2016-2018 Bo Zimmerman

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
public class Skill_Keenvision extends Skill_Stonecunning
{
	@Override
	public String ID()
	{
		return "Skill_Keenvision";
	}

	private final static String	localizedName	= CMLib.lang().L("Keenvision");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			final Item myWeapon=((MOB)affected).fetchWieldedItem();
			if((myWeapon instanceof Weapon)
			&&(((Weapon)myWeapon).weaponClassification()==Weapon.CLASS_RANGED))
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() + 10 + (super.getXLEVELLevel((MOB)affected)));
		}
	}

	@Override
	protected boolean appliesToRoom(Room R)
	{
		if((R.domainType()&Room.INDOORS)==Room.INDOORS)
			return false;
		switch(R.domainType())
		{
		case Room.DOMAIN_OUTDOORS_CITY:
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
		case Room.DOMAIN_OUTDOORS_AIR:
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
		case Room.DOMAIN_OUTDOORS_SPACEPORT:
		case Room.DOMAIN_OUTDOORS_SEAPORT:
			return false;
		default:
			return true;
		}
	}

}
