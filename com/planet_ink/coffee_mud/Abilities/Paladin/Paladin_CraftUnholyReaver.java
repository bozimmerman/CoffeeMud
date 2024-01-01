package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.EnhancedCraftingSkill;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftorType;
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
   Copyright 2023-2024 Bo Zimmerman

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
public class Paladin_CraftUnholyReaver extends Paladin_CraftHolyAvenger
{
	@Override
	public String ID()
	{
		return "Paladin_CraftUnholyReaver";
	}

	private final static String localizedName = CMLib.lang().L("Craft Unholy Reaver");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"CRAFTUNHOLY","CRAFTUNHOLYREAVER","CRAFTREAVER"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected String getItemName(final MOB mob, final Item I)
	{
		return L("the Unholy Reaver");
	}

	@Override
	protected void applyItemRestrictions(final Item buildingI)
	{
		final Ability A=CMClass.getAbility("Prop_HaveZapper");
		String mask="ACTUAL -CLASS +Paladin ";
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.GOOD))
			mask +="-ALIGNMENT +Evil ";
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
			mask +="-ALIGNMENT +Chaotic ";
		A.setMiscText(mask);
		buildingI.addNonUninvokableEffect(A);
		A.setMiscText("120%");
		buildingI.addNonUninvokableEffect(A);
		buildingI.basePhyStats().setDisposition(buildingI.basePhyStats().disposition()|PhyStats.IS_EVIL);
		buildingI.phyStats().setDisposition(buildingI.phyStats().disposition()|PhyStats.IS_EVIL);
	}
}
