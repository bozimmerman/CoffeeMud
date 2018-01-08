package com.planet_ink.coffee_mud.Items.Weapons;
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
   Copyright 2001-2018 Bo Zimmerman

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
public class Sword extends StdWeapon
{
	@Override
	public String ID()
	{
		return "Sword";
	}

	public Sword()
	{
		super();

		setName("a sword");
		setDisplayText("a rather plain looking sword leans against the wall.");
		setDescription("An plain sword.");
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(0);
		basePhyStats.setWeight(10);
		recoverPhyStats();
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(8);
		recoverPhyStats();
		weaponDamageType=TYPE_SLASHING;
		material=RawMaterial.RESOURCE_STEEL;
		weaponClassification=Weapon.CLASS_SWORD;
	}

	@Override
	public CMObject newInstance()
	{
		if(!ID().equals("Sword"))
		{
			try
			{
				return this.getClass().newInstance();
			}
			catch(final Exception e)
			{
			}
			return new Sword();
		}
		final Random randomizer = new Random(System.currentTimeMillis());
		final int swordType = Math.abs(randomizer.nextInt() % 6);
		switch (swordType)
		{
		case 0:
			return new Rapier();
		case 1:
			return new Katana();
		case 2:
			return new Longsword();
		case 3:
			return new Scimitar();
		case 4:
			return new Claymore();
		case 5:
			return new Shortsword();
		default:
			try
			{
				return this.getClass().newInstance();
			}
			catch(final Exception e)
			{
			}
			return new Sword();
		}

	}
}
