package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.BuildingSkill.Building;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2025 Bo Zimmerman

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
public class Excavation extends BuildingSkill
{
	@Override
	public String ID()
	{
		return "Excavation";
	}

	private final static String	localizedName	= CMLib.lang().L("Excavation");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "EXCAVATE", "EXCAVATION" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "ROCK|STONE";
	}

	@Override
	public String getRecipeFilename()
	{
		return "excavation.txt";
	}

	@Override
	protected String getMainResourceName()
	{
		return "Stone";
	}

	@Override
	protected String getSoundName()
	{
		return "stone.wav";
	}

	public Excavation()
	{
		super();
	}

	@Override
	public String establishVerb(final MOB mob, final String[] recipe)
	{
		final Building doingCode = Building.valueOf(recipe[DAT_BUILDCODE]);
		if(doingCode == Building.EXCAVATE)
			return L("excavating the @x1",
					CMStrings.replaceVariables(recipe[DAT_DESC],new String[] {CMLib.directions().getInDirectionName(dir).toLowerCase()}));
		else
			return super.establishVerb(mob, recipe);
	}

	@Override
	protected int[][] getBasicMaterials(final MOB mob, final int woodRequired, String miscType)
	{
		if(miscType.length()==0)
			miscType="rock";
		final int[][] idata=fetchFoundResourceData(mob,
													woodRequired,miscType,null,
													0,null,null,
													false,
													0,null);
		return idata;
	}
}
