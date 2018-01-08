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
public class Claymore extends Sword
{
	@Override
	public String ID()
	{
		return "Claymore";
	}

	public final static int PLAIN					= 0;
	public final static int QUALITY_WEAPON			= 1;
	public final static int EXCEPTIONAL	  			= 2;

	public Claymore()
	{
		super();

		final Random randomizer = new Random(System.currentTimeMillis());
		final int claymoreType = Math.abs(randomizer.nextInt() % 3);

		this.phyStats.setAbility(claymoreType);
		setItemDescription(this.phyStats.ability());

		basePhyStats().setAbility(0);
		basePhyStats().setLevel(0);
		basePhyStats.setWeight(10);
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(8);
		baseGoldValue=25;
		recoverPhyStats();
		wornLogicalAnd=true;
		properWornBitmap=Wearable.WORN_HELD|Wearable.WORN_WIELD;
		material=RawMaterial.RESOURCE_STEEL;
		weaponDamageType=TYPE_SLASHING;
	}

	public void setItemDescription(int level)
	{
		switch(level)
		{
			case Claymore.PLAIN:
				setName("a simple claymore");
				setDisplayText("a simple claymore is on the ground.");
				setDescription("It\\`s an oversized two-handed sword.  Someone made it just to make it.");
				break;

			case Claymore.QUALITY_WEAPON:
				setName("a very nice claymore");
				setDisplayText("a very nice claymore leans against the wall.");
				setDescription("It\\`s ornate with an etched hilt.  Someone took their time making it.");
				break;

			case Claymore.EXCEPTIONAL:
				setName("an exceptional claymore");
				setDisplayText("an exceptional claymore is found nearby.");
				setDescription("It\\`s a huge two-handed sword, with a etchings in the blade and a tassel hanging from the hilt.");
				break;

			default:
				setName("a simple claymore");
				setDisplayText("a simple claymore is on the ground.");
				setDescription("It\\`s an oversized two-handed sword.  Someone made it just to make it.");
				break;
		}
	}

}
