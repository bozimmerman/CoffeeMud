package com.planet_ink.coffee_mud.Abilities.Common;
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
public class GaolFood extends Cooking
{
	@Override
	public String ID()
	{
		return "GaolFood";
	}

	private final static String	localizedName	= CMLib.lang().L("Gaol Food");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "GAOLFOOD", "GFOOD" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String cookWordShort()
	{
		return "make";
	}

	@Override
	public String cookWord()
	{
		return "making";
	}

	@Override
	public boolean honorHerbs()
	{
		return false;
	}

	@Override
	public boolean requireFire()
	{
		return false;
	}

	@Override
	public String parametersFile()
	{
		return "gaolfood.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(parametersFile());
	}

	public GaolFood()
	{
		super();

		defaultFoodSound = "chopchop.wav";
	}

	@Override
	public void stirThePot(final MOB mob)
	{
		if(buildingI!=null)
		{
			if((tickUp % 5)==1)
			{
				final Room R=mob.location();
				if(R==activityRoom)
				{
					R.show(mob,cookingPot,buildingI,CMMsg.MASK_ALWAYS|getActivityMessageType(),
							L("<S-NAME> do(es) gaol food work on <O-NAME>."));
				}
			}
		}
	}
}
