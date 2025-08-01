package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2004-2025 Bo Zimmerman

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
public class Chant_FindOre extends Chant_FindPlant
{
	@Override
	public String ID()
	{
		return "Chant_FindOre";
	}

	private final static String localizedName = CMLib.lang().L("Find Ore");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_ROCKCONTROL;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRACKING | Ability.FLAG_DIVINING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private final int[] myMats={RawMaterial.MATERIAL_ROCK, // mithril omitted
						  RawMaterial.MATERIAL_METAL};

	@Override
	protected int[] okMaterials()
	{
		return myMats;
	}

	@Override
	protected int[] okResources()
	{
		return null;
	}

	private static String DEFAULT_LOOKING_FOR=CMLib.lang().L("ore");
	private static String DEFAULT_DISPLAYTEXT=CMLib.lang().L("(Finding Ore)");

	public Chant_FindOre()
	{
		super();

		lookingFor = DEFAULT_LOOKING_FOR;
		displayText = DEFAULT_DISPLAYTEXT;
	}
}
