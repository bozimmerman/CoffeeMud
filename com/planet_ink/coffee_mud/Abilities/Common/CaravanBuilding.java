package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.BuildingSkill.Flag;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaterialLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2021-2024 Bo Zimmerman

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
public class CaravanBuilding extends Shipwright
{
	@Override
	public String ID()
	{
		return "CaravanBuilding";
	}

	private final static String	localizedName	= CMLib.lang().L("Caravan Building");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "CARAVANBUILD", "CARAVANBUILDING", "CARABUILD" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "WOODEN";
	}

	@Override
	public CraftorType getCraftorType()
	{
		return CraftorType.LargeConstructions;
	}

	@Override
	public String getRecipeFilename()
	{
		return "caravanbuilding.cmare";
	}

	@Override
	public boolean mayICraft(final Item I)  // tots bypass shipwright
	{
		if(I==null)
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		if((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if((I instanceof NavigableItem)
		&&((((NavigableItem)I).navBasis() == Rideable.Basis.LAND_BASED)
			||(((NavigableItem)I).navBasis() == Rideable.Basis.WAGON)))
			return true;
		return false;
	}

	@Override
	protected String getIdentifierCommandWord()
	{
		return "caravanbuild";
	}
}
