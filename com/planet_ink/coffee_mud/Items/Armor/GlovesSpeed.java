package com.planet_ink.coffee_mud.Items.Armor;
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
public class GlovesSpeed extends StdArmor
{
	@Override
	public String ID()
	{
		return "GlovesSpeed";
	}

	public GlovesSpeed()
	{
		super();

		setName("a pair of gloves");
		setDisplayText("a pair of finely crafted gloves is found on the ground.");
		setDescription("This is a pair of very nice gloves.");
		secretIdentity="Gloves of the blinding strike (Double attack speed, truly usable only by fighters.)";
		baseGoldValue+=10000;
		properWornBitmap=Wearable.WORN_HANDS;
		wornLogicalAnd=false;
		basePhyStats().setArmor(15);
		basePhyStats().setAbility(0);
		basePhyStats().setWeight(1);
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_BONUS);
		recoverPhyStats();

	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((!this.amWearingAt(Wearable.IN_INVENTORY))&&(!this.amWearingAt(Wearable.WORN_HELD)))
		{
			affectableStats.setSpeed(affectableStats.speed() * 2.0);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() + 10);
		}
	}

}
