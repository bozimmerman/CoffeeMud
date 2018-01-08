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
public class GlowingMageArmor extends StdArmor
{
	@Override
	public String ID()
	{
		return "GlowingMageArmor";
	}

	public GlowingMageArmor()
	{
		super();

		setName("a mystical glowing breast plate");
		setDisplayText("If this is sitting around somewhere, something is wrong!");
		setDescription("This suit of armor is made from magical energy, but looks sturdy and protective.");
		properWornBitmap=Wearable.WORN_TORSO;
		wornLogicalAnd=false;
		basePhyStats().setArmor(45);
		basePhyStats().setWeight(0);
		basePhyStats().setAbility(0);
		baseGoldValue=40000;
		material=RawMaterial.RESOURCE_NOTHING;
		recoverPhyStats();
	}

	@Override
	public boolean isSavable()
	{
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((amWearingAt(Wearable.IN_INVENTORY)||(owner()==null)||(owner() instanceof Room))
		&&(!amDestroyed()))
			destroy();

		final MOB mob=msg.source();
		if(!msg.amITarget(this))
			return true;
		else
		if((msg.targetMinor()==CMMsg.TYP_GET)
		||(msg.targetMinor()==CMMsg.TYP_PUSH)
		||(msg.targetMinor()==CMMsg.TYP_PULL)
		||(msg.targetMinor()==CMMsg.TYP_REMOVE))
		{
			mob.tell(L("The mage armor cannot be removed from where it is."));
			return false;
		}
		return true;
	}
}
