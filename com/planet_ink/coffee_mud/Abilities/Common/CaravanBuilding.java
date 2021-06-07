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
   Copyright 2021-2021 Bo Zimmerman

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
	protected List<Item> getShips()
	{
		final String allItemID = "CARAVANBUILD_PARSED";
		@SuppressWarnings("unchecked")
		List<Item> shipPrototypes = (List<Item>)Resources.getResource(allItemID);
		if(shipPrototypes == null)
		{
			final CMFile F=new CMFile(Resources.makeFileResourceName("skills/caravanbuilding.cmare"),null);
			if(F.exists())
			{
				shipPrototypes=new Vector<Item>();
				CMLib.coffeeMaker().addItemsFromXML(F.textUnformatted().toString(), shipPrototypes, null);
				for(final Item I : shipPrototypes)
					CMLib.threads().deleteAllTicks(I);
				if(shipPrototypes.size()>0)
					Resources.submitResource(allItemID, shipPrototypes);
			}
		}
		return shipPrototypes;
	}

	@Override
	public String parametersFile()
	{
		final CMFile F=new CMFile(Resources.makeFileResourceName("::skills/caravanbuilding.txt"),null);
		if(F.exists())
			return "caravanbuilding.txt";
		final List<Item> ships = getShips();
		if(ships == null)
			return "";
		final StringBuilder recipes = new StringBuilder("");
		int x=0;
		for(final Item I : getShips())
		{
			recipes.append(I.Name()).append("\t")
					.append(""+I.basePhyStats().level()).append("\t")
					.append(""+I.basePhyStats().weight()/10).append("\t")
					.append(""+I.basePhyStats().weight()).append("\t")
					.append(""+I.baseGoldValue()).append("\t")
					.append(""+(x++)).append("\r\n");
		}
		F.saveText(recipes.toString());
		return "caravanbuilding.txt";
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

	protected String getIdentifierCommandWord()
	{
		return "caravanbuild";
	}
}
